--******************************************************************--
--* Updates the structure of the database with the actions that    *--
--* are not necessarily automatically executed by the ORM engine.  *--
--*                                                                *--
--* Author: Yves Grasset                                           *--
--******************************************************************--

-- PROCESSES Table

ALTER TABLE processes ADD COLUMN IF NOT EXISTS description VARCHAR(4000);

-- PROCESSES_USERGROUPS Table

ALTER TABLE processes_usergroups
DROP CONSTRAINT fk_processes_usergroups_process;

ALTER TABLE processes_usergroups
    ADD CONSTRAINT fk_processes_usergroups_process FOREIGN KEY (id_process)
        REFERENCES processes (id_process) MATCH SIMPLE
        ON UPDATE NO ACTION ON DELETE CASCADE;

DROP INDEX IF EXISTS idx_processes_usergroups_process;

CREATE INDEX idx_processes_usergroups_process
    ON processes_usergroups (id_process);

ALTER TABLE processes_usergroups
	DROP CONSTRAINT fk_processes_usergroups_usergroup;

ALTER TABLE processes_usergroups
    ADD CONSTRAINT fk_processes_usergroups_usergroup FOREIGN KEY (id_usergroup)
        REFERENCES usergroups (id_usergroup) MATCH SIMPLE
        ON UPDATE NO ACTION ON DELETE CASCADE;

DROP INDEX IF EXISTS idx_processes_usergroups_usergroup;

CREATE INDEX idx_processes_usergroups_usergroup
    ON processes_usergroups (id_usergroup);

-- PROCESSES_USERS Table

ALTER TABLE processes_users
  DROP CONSTRAINT fk_processes_users_process;

ALTER TABLE processes_users
  ADD CONSTRAINT fk_processes_users_process FOREIGN KEY (id_process)
      REFERENCES processes (id_process) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE CASCADE;

DROP INDEX IF EXISTS idx_processes_users_process;

CREATE INDEX idx_processes_users_process
  ON processes_users (id_process);

ALTER TABLE processes_users
  DROP CONSTRAINT fk_processes_users_user;

ALTER TABLE processes_users
  ADD CONSTRAINT fk_processes_users_user FOREIGN KEY (id_user)
      REFERENCES users (id_user) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE CASCADE;

DROP INDEX IF EXISTS idx_processes_users_user;

CREATE INDEX idx_processes_users_user
  ON processes_users (id_user);

-- RECOVERY_CODES Table

ALTER TABLE recovery_codes
DROP CONSTRAINT IF EXISTS fk_recovery_codes_user;

ALTER TABLE recovery_codes
    ADD CONSTRAINT fk_recovery_codes_user FOREIGN KEY (id_user)
        REFERENCES users (id_user) MATCH SIMPLE
        ON UPDATE NO ACTION ON DELETE CASCADE;

-- REMARKS Table

ALTER TABLE remarks ALTER COLUMN content TYPE TEXT;

-- REMEMBERME_TOKENS Table

-- Correction mauvais nommage v2.1 Beta
ALTER TABLE remember_me_tokens
DROP CONSTRAINT IF EXISTS fk_recovery_codes_user;

ALTER TABLE remember_me_tokens
DROP CONSTRAINT IF EXISTS fk_rememberme_user;

ALTER TABLE remember_me_tokens
    ADD CONSTRAINT fk_rememberme_user FOREIGN KEY (id_user)
        REFERENCES users (id_user) MATCH SIMPLE
        ON UPDATE NO ACTION ON DELETE CASCADE;

-- REQUESTS Table

ALTER TABLE requests
  DROP CONSTRAINT fk_request_connector;

ALTER TABLE requests
  ADD CONSTRAINT fk_request_connector FOREIGN KEY (id_connector)
      REFERENCES connectors (id_connector) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE SET NULL;

ALTER TABLE requests
  DROP CONSTRAINT fk_request_process;

ALTER TABLE requests
  ADD CONSTRAINT fk_request_process FOREIGN KEY (id_process)
      REFERENCES processes (id_process) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE SET NULL;

ALTER TABLE requests ALTER COLUMN p_parameters TYPE TEXT;
ALTER TABLE requests ALTER COLUMN p_perimeter TYPE TEXT;
ALTER TABLE requests ALTER COLUMN p_tiersguid TYPE VARCHAR (255) COLLATE pg_catalog."default";
ALTER TABLE requests ALTER COLUMN p_tiersdetails TYPE VARCHAR (4000) COLLATE pg_catalog."default";

UPDATE requests r SET last_reminder = (
    SELECT MAX(rh.end_date)
    FROM request_history rh
    WHERE rh.id_request = r.id_request
) WHERE r.status = 'STANDBY' AND r.last_reminder IS NULL;

-- REQUEST_HISTORY Table

ALTER TABLE request_history
  DROP CONSTRAINT fk_request_history_request;

ALTER TABLE request_history
  ADD CONSTRAINT fk_request_history_request FOREIGN KEY (id_request)
      REFERENCES requests (id_request) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE CASCADE;

ALTER TABLE request_history
  DROP CONSTRAINT fk_request_history_user;

ALTER TABLE request_history
  ADD CONSTRAINT fk_request_history_user FOREIGN KEY (id_user)
      REFERENCES users (id_user) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE SET NULL;

-- RULES Table

ALTER TABLE rules
  DROP CONSTRAINT fk_rule_connector;

ALTER TABLE rules
  ADD CONSTRAINT fk_rule_connector FOREIGN KEY (id_connector)
      REFERENCES connectors (id_connector) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE CASCADE;

ALTER TABLE rules
  DROP CONSTRAINT fk_rule_process;

ALTER TABLE rules
  ADD CONSTRAINT fk_rule_process FOREIGN KEY (id_process)
      REFERENCES processes (id_process) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE CASCADE;

ALTER TABLE rules ALTER COLUMN rule TYPE TEXT;

-- SYSTEM Table

ALTER TABLE system ALTER COLUMN value TYPE VARCHAR(65000);

-- TASKS Table

ALTER TABLE tasks
  DROP CONSTRAINT fk_task_process;

ALTER TABLE tasks
  ADD CONSTRAINT fk_task_process FOREIGN KEY (id_process)
      REFERENCES processes (id_process) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE CASCADE;

UPDATE tasks
  SET task_params = '{"reject_msgs":"","valid_msgs":""}'
  WHERE task_code = 'VALIDATION' AND (task_params IS NULL OR task_params = '' OR LOWER(task_params) = 'null');

UPDATE tasks
  SET task_params = REPLACE(task_params, '}', ',"instances":"1"}')
  WHERE task_code = 'FME2017' AND task_params NOT LIKE '{%"instances"%}';

-- USERS Table

UPDATE users SET mailactive = FALSE WHERE login = 'system';
UPDATE users SET mailactive = true WHERE mailactive IS NULL;
UPDATE users SET two_factor_forced = false WHERE two_factor_forced IS NULL;
UPDATE users SET two_factor_status = 'INACTIVE' WHERE two_factor_status IS NULL;
UPDATE users SET user_type = 'LOCAL' WHERE user_type IS NULL;
-- The hidden system user must never get a profile: it is not an application account. Giving it the
-- administrator profile makes Extract consider itself already configured, which prevents the creation
-- of the first administrator on a fresh installation (issue #432).
UPDATE users SET profile = 'ADMIN' WHERE profile IS NULL AND login <> 'system';
UPDATE users SET profile = NULL WHERE login = 'system';

-- USERS_USERGROUPS Table

ALTER TABLE users_usergroups
DROP CONSTRAINT IF EXISTS fk_users_usergroups_user;

ALTER TABLE users_usergroups
    ADD CONSTRAINT fk_users_usergroups_user FOREIGN KEY (id_user)
        REFERENCES users (id_user) MATCH SIMPLE
        ON UPDATE NO ACTION ON DELETE CASCADE;

DROP INDEX IF EXISTS idx_users_usergroups_user;

CREATE INDEX idx_users_usergroups_user
    ON users_usergroups (id_user);

ALTER TABLE users_usergroups
DROP CONSTRAINT fk_users_usergroups_usergroup;

ALTER TABLE users_usergroups
    ADD CONSTRAINT fk_users_usergroups_usergroup FOREIGN KEY (id_usergroup)
        REFERENCES usergroups (id_usergroup) MATCH SIMPLE
        ON UPDATE NO ACTION ON DELETE CASCADE;

DROP INDEX IF EXISTS idx_users_usergroups_usergroup;

CREATE INDEX idx_users_usergroups_usergroup
    ON users_usergroups (id_usergroup);

-- REQUESTS_USERS Table

ALTER TABLE requests_users
    DROP CONSTRAINT IF EXISTS fk_processes_users_requests;

ALTER TABLE requests_users
    DROP CONSTRAINT IF EXISTS fk_processes_users_user;

ALTER TABLE ONLY requests_users
    ADD CONSTRAINT fk_processes_users_requests FOREIGN KEY (id_request)
    REFERENCES public.requests(id_request);

ALTER TABLE ONLY requests_users
    ADD CONSTRAINT fk_processes_users_user FOREIGN KEY (id_user)
    REFERENCES public.users(id_user);

-- REQUESTS_USERS Table

ALTER TABLE requests_users
    DROP CONSTRAINT IF EXISTS fk_processes_usergroups_requests;

ALTER TABLE requests_users
    DROP CONSTRAINT IF EXISTS fk_processes_usergroups_usergroup;

ALTER TABLE ONLY requests_usergroups
    ADD CONSTRAINT fk_processes_usergroups_requests FOREIGN KEY (id_request)
    REFERENCES public.requests(id_request);

ALTER TABLE ONLY requests_usergroups
    ADD CONSTRAINT fk_processes_usergroups_usergroup FOREIGN KEY (id_usergroup)
    REFERENCES public.usergroups(id_usergroup);

-- OPTIMISTIC LOCKING COLUMNS (Issues #382 & #394)

ALTER TABLE requests ADD COLUMN IF NOT EXISTS version BIGINT NOT NULL DEFAULT 0;
ALTER TABLE request_history ADD COLUMN IF NOT EXISTS version BIGINT NOT NULL DEFAULT 0;
ALTER TABLE tasks ADD COLUMN IF NOT EXISTS version BIGINT NOT NULL DEFAULT 0;

UPDATE requests SET version = 0 WHERE version IS NULL;
UPDATE request_history SET version = 0 WHERE version IS NULL;
UPDATE tasks SET version = 0 WHERE version IS NULL;

ALTER TABLE requests ALTER COLUMN version SET NOT NULL;
ALTER TABLE requests ALTER COLUMN version SET DEFAULT 0;
ALTER TABLE request_history ALTER COLUMN version SET NOT NULL;
ALTER TABLE request_history ALTER COLUMN version SET DEFAULT 0;
ALTER TABLE tasks ALTER COLUMN version SET NOT NULL;
ALTER TABLE tasks ALTER COLUMN version SET DEFAULT 0;

-- UNIQUE CONSTRAINT on request_history to prevent duplicate steps per request
CREATE UNIQUE INDEX IF NOT EXISTS uq_request_history_request_step
    ON request_history (id_request, step);

-- Issue #359 : observateurs des traitements

CREATE TABLE IF NOT EXISTS processes_watchers (
    id_process INTEGER NOT NULL,
    id_user INTEGER NOT NULL,
    CONSTRAINT pk_processes_watchers PRIMARY KEY (id_process, id_user),
    CONSTRAINT fk_processes_watchers_process FOREIGN KEY (id_process)
        REFERENCES processes (id_process) MATCH SIMPLE
        ON UPDATE NO ACTION ON DELETE CASCADE,
    CONSTRAINT fk_processes_watchers_user FOREIGN KEY (id_user)
        REFERENCES users (id_user) MATCH SIMPLE
        ON UPDATE NO ACTION ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_processes_watchers_process
    ON processes_watchers (id_process);

CREATE INDEX IF NOT EXISTS idx_processes_watchers_user
    ON processes_watchers (id_user);

CREATE TABLE IF NOT EXISTS processes_watchergroups (
    id_process INTEGER NOT NULL,
    id_usergroup INTEGER NOT NULL,
    CONSTRAINT pk_processes_watchergroups PRIMARY KEY (id_process, id_usergroup),
    CONSTRAINT fk_processes_watchergroups_process FOREIGN KEY (id_process)
        REFERENCES processes (id_process) MATCH SIMPLE
        ON UPDATE NO ACTION ON DELETE CASCADE,
    CONSTRAINT fk_processes_watchergroups_usergroup FOREIGN KEY (id_usergroup)
        REFERENCES usergroups (id_usergroup) MATCH SIMPLE
        ON UPDATE NO ACTION ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_processes_watchergroups_process
    ON processes_watchergroups (id_process);

CREATE INDEX IF NOT EXISTS idx_processes_watchergroups_usergroup
    ON processes_watchergroups (id_usergroup);

-- STRUCTURED CLIENT CONTACT COLUMNS (Issues #366 and #367)
-- p_clientemail feeds the {clientEmail} placeholder of the e-mail notification plugin (#366); the three
-- columns together feed the structured client contact parameters of the extraction plugins (#367).

ALTER TABLE requests ADD COLUMN IF NOT EXISTS p_clientaddress VARCHAR(4000);
ALTER TABLE requests ADD COLUMN IF NOT EXISTS p_clientemail VARCHAR(255);
ALTER TABLE requests ADD COLUMN IF NOT EXISTS p_clientphone VARCHAR(255);

-- Backfill the rows imported before those columns existed. Until now the connectors stored every piece
-- of contact information in the single p_clientdetails blob, one item per line, in this order:
-- street, additional street line (optional), ZIP and locality, telephone number (optional),
-- e-mail address (optional). Each line is therefore classified back into the column it belongs to:
-- a line holding an e-mail token is the e-mail address, a line made only of digits and punctuation
-- (possibly behind a "Tel:" label) is the telephone number, and every remaining line is an address line.
-- The guard on NULL values keeps this statement idempotent and prevents it from overwriting any value
-- that a connector has already provided.

WITH detail_lines AS (
    SELECT r.id_request,
           t.ord,
           btrim(t.line) AS line
    FROM requests r
             CROSS JOIN LATERAL unnest(
            string_to_array(replace(r.p_clientdetails, chr(13) || chr(10), chr(10)), chr(10))
                                 ) WITH ORDINALITY AS t(line, ord)
    WHERE r.p_clientdetails IS NOT NULL
      AND btrim(r.p_clientdetails) <> ''
      AND r.p_clientaddress IS NULL
      AND r.p_clientemail IS NULL
      AND r.p_clientphone IS NULL
),
     classified_lines AS (
         SELECT id_request,
                ord,
                line,
                regexp_replace(line, '^[[:space:]]*(tel|tél|telephone|téléphone|phone)[[:space:]]*[.:]?[[:space:]]*',
                               '', 'i') AS unlabelled_line,
                CASE
                    WHEN line ~ '[[:alnum:]._%+-]+@[[:alnum:].-]+[.][[:alpha:]]{2,}' THEN 'email'
                    -- An explicit "Tel:" label followed by a digit is evidence enough, whatever the
                    -- number of digits. Requiring that digit also rules out streets such as "Telstrasse 5".
                    WHEN line ~* '^[[:space:]]*(tel|tél|telephone|téléphone|phone)[[:space:]]*[.:]?[[:space:]]*[0-9]'
                        THEN 'phone'
                    -- Without a label, only a line free of letters and holding enough digits can be a
                    -- telephone number: this keeps a ZIP code left alone by an empty locality in the address.
                    WHEN line ~ '^[^[:alpha:]]*[0-9][^[:alpha:]]*$'
                        AND length(regexp_replace(line, '[^0-9]', '', 'g')) >= 6 THEN 'phone'
                    ELSE 'address'
                    END AS line_kind
         FROM detail_lines
         WHERE line <> ''
     ),
     client_contact AS (
         SELECT id_request,
                string_agg(line, chr(10) ORDER BY ord)
                FILTER (WHERE line_kind = 'address') AS address,
                (array_agg(substring(line from '[[:alnum:]._%+-]+@[[:alnum:].-]+[.][[:alpha:]]{2,}') ORDER BY ord)
                 FILTER (WHERE line_kind = 'email'))[1] AS email,
                (array_agg(btrim(unlabelled_line) ORDER BY ord)
                 FILTER (WHERE line_kind = 'phone'))[1] AS phone
         FROM classified_lines
         GROUP BY id_request
     )
UPDATE requests r
SET p_clientaddress = c.address,
    p_clientemail   = c.email,
    p_clientphone   = c.phone
FROM client_contact c
WHERE r.id_request = c.id_request;
