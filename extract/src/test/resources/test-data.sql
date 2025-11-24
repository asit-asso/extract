-- Test data for CancelledRequestWithoutRulesIntegrationTest
-- Insert test connector
INSERT INTO connectors (id_connector, name, active) VALUES
    (9999, 'Test Connector for #337', TRUE);

-- Insert test requests
INSERT INTO requests (id_request, p_productlabel, p_orderlabel, p_client, status, folder_out, start_date, remark, id_connector) VALUES
    (9998, 'Cancelled Request Without Rules', 'ORDER-337-CANCELLED', 'Test Client #337', 'UNMATCHED', NULL, NOW(), 'Cancelled: No matching rules found', 9999),
    (9997, 'Normal Request', 'ORDER-337-NORMAL', 'Test Client', 'ONGOING', 'request337/output', NOW(), NULL, 9999);