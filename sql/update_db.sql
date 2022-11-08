--******************************************************************--
--* Updates the structure of the database with the actions that    *--
--* are not necessarily automatically executed by the ORM engine.  *--
--*                                                                *--
--* Author: Yves Grasset                                           *--
--******************************************************************--

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

-- REMARKS Table

ALTER TABLE remarks ALTER COLUMN content TYPE TEXT;

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

-- USERS_USERGROUPS Table

ALTER TABLE users_usergroups
DROP CONSTRAINT fk_users_usergroups_user;

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
