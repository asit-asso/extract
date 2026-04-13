# Fix: Atomic Transactions and Concurrency Protection

## Issues Fixed

- **#382**: Requests impossible to cancel (race condition between Orchestrator and user action)
- **#394**: Duplicate FME task execution (double dispatch by the scheduler)

## Root Cause

Both issues share the same root cause: **non-atomic SQL transactions** and **missing locking mechanisms** (optimistic or pessimistic), exacerbated by network latency when PostgreSQL runs on a separate server.

### Issue #382 — Stuck Requests

1. User loads the page, `activeStep = N` is captured client-side
2. The Orchestrator advances the request, `tasknum` becomes `N+1`
3. User clicks "Cancel" — `checkActiveStep()` detects a mismatch
4. Some tables are updated before the error is raised — inconsistent state

### Issue #394 — Duplicate Tasks

1. The scheduler fetches `ONGOING` requests via `findByStatus()`
2. No DB locking — a second scheduler cycle dispatches the same request
3. Two `RequestTaskRunner` instances submitted for the same request
4. `history.size() + 1` is non-atomic — both threads compute the same step number

## Applied Fixes

### 1. Optimistic Locking (`@Version`) — P0

**Files**: `Request.java`, `RequestHistoryRecord.java`, `Task.java`

Added a `version BIGINT NOT NULL DEFAULT 0` column to all three critical entities. JPA automatically increments the version on each `save()`. If two threads attempt to modify the same entity concurrently, the second one receives an `ObjectOptimisticLockingFailureException`.

```java
@Version
@Column(name = "version", nullable = false, columnDefinition = "BIGINT DEFAULT 0")
private Long version = 0L;
```

Key migration considerations:
- Java initialization to `0L` prevents NPEs on newly created entities
- `columnDefinition` forces Hibernate to create the column with `DEFAULT 0`
- The SQL migration script updates existing rows (`UPDATE ... SET version = 0 WHERE version IS NULL`) and adds the `NOT NULL` constraint

### 2. Transactional Service (`RequestTaskService`) — P0

**File**: `RequestTaskService.java` (new)

`RequestTaskRunner` is not a Spring bean (instantiated via `new`), so `@Transactional` does not work on it. A dedicated Spring service encapsulates the critical DB operations:

| Method | Purpose |
|--------|---------|
| `createHistoryRecord()` | Atomic history record creation using `MAX(step)+1` |
| `updateTaskResult()` | Atomic update of both request and history record |
| `markRequestForExport()` | Atomic export status update |
| `deleteHistoryRecord()` | Transactional deletion (NOT_RUN case) |
| `getOngoingRequestsWithLock()` | Read with `SELECT ... FOR UPDATE` |

The service is injected through the chain: `OrchestratorConfiguration` → `Orchestrator` → `RequestsProcessingScheduler` → `RequestTaskRunner`.

### 3. Pessimistic Locking on `findByStatus(ONGOING)` — P1

**File**: `RequestsRepository.java`

Added a query with `@Lock(LockModeType.PESSIMISTIC_WRITE)`:

```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("SELECT r FROM Request r WHERE r.status = :status")
List<Request> findByStatusWithLock(@Param("status") Status status);
```

**Known limitation**: The lock is released at the end of `getOngoingRequestsWithLock()` (when its `@Transactional` boundary ends), before the runners are submitted to the thread pool. The lock prevents two concurrent `findByStatus(ONGOING)` calls from overlapping, but does not cover runner submission. This is compensated by:
- The `requestsWithRunningTask` in-memory `synchronized` Set
- `@Version` optimistic locking on entities
- The DB unique constraint

Extending the transaction to cover the full runner submission loop would risk deadlocks/timeouts with many concurrent requests.

### 4. Atomic Step Calculation (`MAX(step)+1`) — P2

**File**: `RequestHistoryRepository.java`

Replaced the vulnerable `history.size() + 1` pattern with an atomic JPQL query:

```java
@Query("SELECT COALESCE(MAX(h.step), 0) + 1 FROM RequestHistoryRecord h WHERE h.request = :request")
int findNextStepByRequest(@Param("request") Request request);
```

Fixed in 3 files:
- `RequestTaskRunner.createNewHistoryRecord()` (via `RequestTaskService`)
- `RequestsController.createSkippedTaskHistoryRecord()` — with step pre-computation before the loop to prevent intra-transaction collisions
- `ExportRequestProcessor.createHistoryRecord()`

### 5. DB Unique Constraint — P2

**Files**: `RequestHistoryRecord.java`, `sql/update_db.sql`

Safety net: `UNIQUE(id_request, step)` constraint on `request_history` to prevent any duplicate step insertion.

```sql
CREATE UNIQUE INDEX IF NOT EXISTS uq_request_history_request_step
    ON request_history (id_request, step);
```

### 6. `OptimisticLockingFailureException` Handling — P0

**File**: `RequestTaskRunner.java`

When a concurrent modification is detected, the runner logs a WARN and lets the scheduler retry on the next cycle:

```java
} catch (ObjectOptimisticLockingFailureException lockException) {
    this.logger.warn("Concurrent modification detected for request {}...", requestId);
}
```

### 7. Assert Bug Fix in `validateCurrentTask()` — P1

**File**: `RequestsController.java`

Pre-existing copy-paste bug: the assert condition checked `ERROR || ERROR` instead of `ERROR || STANDBY`. Directly related to issue #382 since `validateCurrentTask()` is called during every STANDBY request validation and cancellation.

```java
// Before (bug)
assert currentTaskRecord.getStatus() == RequestHistoryRecord.Status.ERROR
    || currentTaskRecord.getStatus() == RequestHistoryRecord.Status.ERROR

// After (fixed)
assert currentTaskRecord.getStatus() == RequestHistoryRecord.Status.ERROR
    || currentTaskRecord.getStatus() == RequestHistoryRecord.Status.STANDBY
```

### 8. Empty History Guard in `validateCurrentTask()` — P1

**File**: `RequestsController.java`

Added a guard against `IndexOutOfBoundsException` when fetching the latest history record. If no history exists (e.g., data corruption), the method returns `false` instead of crashing.

### 9. Removed Dead `@Transactional` Annotations — Cleanup

**File**: `RequestTaskRunner.java`

Removed `@Transactional(readOnly = true)` from `getProcessOperatorsAddresses()` and `getProcessOperators()`. Since `RequestTaskRunner` is not a Spring bean, Spring's transaction proxy never wraps it — these annotations were silently ignored.

## Defense-in-Depth Architecture

```
Layer 1: @Version (optimistic locking)
    → Detects concurrent modifications at save() time
    → Covers: Orchestrator vs user action, double dispatch

Layer 2: SELECT ... FOR UPDATE (pessimistic locking)
    → Serializes ONGOING request reads by the scheduler
    → Covers: overlapping scheduler cycles

Layer 3: UNIQUE constraint (id_request, step)
    → DB-level safety net against duplicate steps
    → Covers: any unanticipated scenario

Layer 4: requestsWithRunningTask (synchronized in-memory Set)
    → Existing guard against intra-JVM double dispatch
    → Covers: multiple runner submissions for the same request
```

## DB Migration

The `sql/update_db.sql` script adds:

```sql
-- Version columns for optimistic locking
ALTER TABLE requests ADD COLUMN IF NOT EXISTS version BIGINT NOT NULL DEFAULT 0;
ALTER TABLE request_history ADD COLUMN IF NOT EXISTS version BIGINT NOT NULL DEFAULT 0;
ALTER TABLE tasks ADD COLUMN IF NOT EXISTS version BIGINT NOT NULL DEFAULT 0;

-- Backfill existing rows
UPDATE requests SET version = 0 WHERE version IS NULL;
UPDATE request_history SET version = 0 WHERE version IS NULL;
UPDATE tasks SET version = 0 WHERE version IS NULL;

-- Enforce NOT NULL + DEFAULT for databases where Hibernate created the column first
ALTER TABLE requests ALTER COLUMN version SET NOT NULL;
ALTER TABLE requests ALTER COLUMN version SET DEFAULT 0;
ALTER TABLE request_history ALTER COLUMN version SET NOT NULL;
ALTER TABLE request_history ALTER COLUMN version SET DEFAULT 0;
ALTER TABLE tasks ALTER COLUMN version SET NOT NULL;
ALTER TABLE tasks ALTER COLUMN version SET DEFAULT 0;

-- Unique constraint
CREATE UNIQUE INDEX IF NOT EXISTS uq_request_history_request_step
    ON request_history (id_request, step);
```

> **Note**: With `spring.jpa.hibernate.ddl-auto=update`, Hibernate automatically creates the `version` columns. The `columnDefinition = "BIGINT DEFAULT 0"` in the JPA annotation ensures Hibernate creates the column with the correct DEFAULT. The SQL script is a safety net for manual deployments and covers the case where Hibernate creates the column before the script runs.

## Modified Files

| File | Change |
|------|--------|
| `Request.java` | +`@Version`, `nullable=false`, `columnDefinition`, init `0L` |
| `RequestHistoryRecord.java` | +`@Version`, +`@UniqueConstraint`, same attributes |
| `Task.java` | +`@Version`, same attributes |
| `RequestsRepository.java` | +`findByStatusWithLock()` with `@Lock(PESSIMISTIC_WRITE)` |
| `RequestHistoryRepository.java` | +`findNextStepByRequest()` with `MAX(step)+1` |
| `RequestTaskService.java` | **New**: Spring transactional service |
| `RequestTaskRunner.java` | All DB writes via `RequestTaskService`, +`OptimisticLockingFailureException` handling, removed dead `@Transactional` |
| `RequestsProcessingScheduler.java` | Uses `getOngoingRequestsWithLock()`, passes `taskService` to runner |
| `Orchestrator.java` | +`taskService` in initialization chain |
| `OrchestratorConfiguration.java` | Injects `RequestTaskService` |
| `RequestsController.java` | `size()+1` → `findNextStepByRequest()`, step pre-computation in loop, assert fix `ERROR\|\|STANDBY`, empty history guard |
| `ExportRequestProcessor.java` | `size()+1` → `findNextStepByRequest()` |
| `module-info.java` | +`requires spring.orm` |
| `sql/update_db.sql` | +version columns `NOT NULL DEFAULT 0`, +`UPDATE` for existing rows, +unique index |


## Tests

### Integration Tests (497 tests)
- **0 failures, 0 errors related to our changes**
- 2 pre-existing errors (`RequestsOwnershipIntegrationTest` — NPE on `this.id` in `equals()`, unrelated to `@Version`)

### `RequestConcurrencyIntegrationTest` (6 tests)
- `@Version` on Request and RequestHistoryRecord
- `findNextStepByRequest` returns 1 when no history exists
- `findNextStepByRequest` returns `MAX(step)+1` with existing history
- `findByStatusWithLock` returns ONGOING requests with lock
- `RequestTaskService.createHistoryRecord` atomic step calculation

### Unit Tests (1388 tests)
- 0 regressions (21 pre-existing errors on locale/connector tests)

### Playwright Visual Tests (not committed)
- Admin login OK
- Dashboard with request list OK
- STANDBY request detail (#5): **Validation OK**
- STANDBY request detail (#1): **Cancellation with remark OK**
- ERROR request detail (#2): Retry/Relaunch/Continue/Cancel buttons OK
- Admin pages (Processes, Connectors, Users, Parameters): OK
