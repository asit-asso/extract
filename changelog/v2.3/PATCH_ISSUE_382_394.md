# PATCH_ISSUE_382_394 - Atomic Transactions and Concurrency Protection

## Status: DONE

### Issue Description
- **#382**: Requests impossible to cancel — race condition between the Orchestrator (background thread) and user actions (validate/cancel) causes inconsistent state in `requests`, `request_history`, and `tasks` tables.
- **#394**: Duplicate FME task execution — the scheduler dispatches the same request twice due to missing DB locking, resulting in duplicate `request_history` entries.

### Root Cause
Non-atomic SQL transactions and missing locking mechanisms (optimistic/pessimistic), exacerbated by network latency when PostgreSQL runs on a separate server. ~15 stuck requests over 6 months at Romande Energie.

### Changes Applied

1. **Optimistic Locking (`@Version`)** on `Request`, `RequestHistoryRecord`, `Task`
   - `BIGINT NOT NULL DEFAULT 0` column with concurrent modification detection
   - `ObjectOptimisticLockingFailureException` handled gracefully in `RequestTaskRunner`

2. **Transactional Service (`RequestTaskService`)** — new `@Service`
   - All DB writes from `RequestTaskRunner` routed through transactional methods
   - Atomic history record creation, result updates, export marking

3. **Pessimistic Locking** on `findByStatus(ONGOING)`
   - `SELECT ... FOR UPDATE` via `findByStatusWithLock()` in `RequestsRepository`

4. **Atomic Step Calculation** — `MAX(step)+1` via JPQL
   - Replaced vulnerable `history.size() + 1` pattern in 3 files
   - Pre-computed step counter in `addSkippedTasksRecords` loop

5. **DB Unique Constraint** `UNIQUE(id_request, step)` on `request_history`

6. **Bug Fixes**
   - Fixed copy-paste assert: `ERROR || ERROR` → `ERROR || STANDBY` in `validateCurrentTask()`
   - Added empty history guard before `.get(0)` in `validateCurrentTask()`
   - Removed dead `@Transactional` annotations on non-Spring-bean `RequestTaskRunner`

### Code Locations
- extract/src/main/java/ch/asit_asso/extract/domain/Request.java
- extract/src/main/java/ch/asit_asso/extract/domain/RequestHistoryRecord.java
- extract/src/main/java/ch/asit_asso/extract/domain/Task.java
- extract/src/main/java/ch/asit_asso/extract/persistence/RequestsRepository.java
- extract/src/main/java/ch/asit_asso/extract/persistence/RequestHistoryRepository.java
- extract/src/main/java/ch/asit_asso/extract/orchestrator/runners/RequestTaskService.java (new)
- extract/src/main/java/ch/asit_asso/extract/orchestrator/runners/RequestTaskRunner.java
- extract/src/main/java/ch/asit_asso/extract/orchestrator/schedulers/RequestsProcessingScheduler.java
- extract/src/main/java/ch/asit_asso/extract/orchestrator/Orchestrator.java
- extract/src/main/java/ch/asit_asso/extract/configuration/OrchestratorConfiguration.java
- extract/src/main/java/ch/asit_asso/extract/web/controllers/RequestsController.java
- extract/src/main/java/ch/asit_asso/extract/batch/processor/ExportRequestProcessor.java
- extract/src/main/java/module-info.java
- sql/update_db.sql

### DB Migration
```sql
ALTER TABLE requests ADD COLUMN IF NOT EXISTS version BIGINT NOT NULL DEFAULT 0;
ALTER TABLE request_history ADD COLUMN IF NOT EXISTS version BIGINT NOT NULL DEFAULT 0;
ALTER TABLE tasks ADD COLUMN IF NOT EXISTS version BIGINT NOT NULL DEFAULT 0;
CREATE UNIQUE INDEX IF NOT EXISTS uq_request_history_request_step
    ON request_history (id_request, step);
```

### Testing
- 497 integration tests: 0 failures, 0 errors related to changes
- 6 dedicated concurrency tests (`RequestConcurrencyIntegrationTest`)
- Playwright visual tests: validation, cancellation, error handling all OK

### Conclusion
Defense-in-depth approach with 4 layers: optimistic locking, pessimistic locking, DB unique constraint, and in-memory tracking. All identified race conditions are covered.
