--******************************************************************--
--* Updates the structure of the database with the actions that    *--
--* are not necessarily automatically executed by the ORM engine.  *--
--*                                                                *--
--* Author: Yves Grasset                                           *--
--******************************************************************--

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
      
-- USERS Table

UPDATE users
 SET mailactive = CASE WHEN login = 'system' THEN false ELSE true END;

