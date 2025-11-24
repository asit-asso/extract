-- Cleanup test data created by test-data.sql
DELETE FROM requests WHERE id_request IN (9998, 9997);
DELETE FROM connectors WHERE id_connector = 9999;