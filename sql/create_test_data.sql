INSERT INTO connectors(id_connector, active, connector_code, connector_label, connector_params, import_freq, 
                       last_import_date, last_import_msg, name, error_count, max_retries)
VALUES(1, false, 'easysdiv4', 'EasySdi V4', 
       '{"uploadSize":"","detailsUrl":"","pass":"tititoto","login":"dev_test","url":"http://ags104-demo.arxit.com/Projets/6084_ASITVD/asitvd.xml"}', 
       240, '2022-10-11 15:42:21.259', 'Une erreur est survenue lors de la tentative d import des commandes : ags104-demo.arxit.com: Name or service not known', 
       'Bouchon', 0, 0);

INSERT INTO processes(id_process, name)
VALUES(1, 'Traitement test');

INSERT INTO tasks(id_task, task_code, task_label, task_params, position, id_process)
VALUES(1, 'VALIDATION', 'Validation opérateur', '{"reject_msgs":"","valid_msgs":""}', 1, 1);

INSERT INTO requests(id_request, p_client, p_clientdetails, end_date, folder_in, folder_out, p_orderguid, p_orderlabel, 
                     p_organism, p_parameters, p_perimeter, p_productguid, p_productlabel, rejected, remark, start_date, 
                     status, tasknum, p_tiers, p_tiersdetails, id_connector, id_process, p_surface, p_clientguid, 
                     p_organismguid, p_external_url, p_tiersguid)
VALUES (1, 'Yves Grasset', 'Boulevard de Grancy 56
1006 Lausanne
+41 22 344 45 10
ygr@arxit.com', NULL, 'abc2953b-2ad6-4caf-a9c9-75968f3320fa/input', 'abc2953b-2ad6-4caf-a9c9-75968f3320fa/output',
        '4e8827c6-1a93-415a-8fe6-17ba7f475c26', '435747', 'ASIT', '{"FORMAT":"SHP","PROJECTION":"SWITZERLAND95","REMARK":""}',
        'POLYGON((6.841911979416146 46.46278854702276,6.849168963266539 46.45985790656295,6.850009172658301 46.46040202842485,6.852291010951498 46.46005402896992,6.85553840034233 46.46070050402028,6.855921638324666 46.46137716627096,6.856633703301899 46.461740661947715,6.854340647210315 46.46312321582543,6.85485503433851 46.463710599388584,6.861223092099635 46.46473278491887,6.865894751611408 46.4661508605426,6.865942220948613 46.46781533395983,6.863390680632963 46.46901684637229,6.862834010912125 46.47238746493873,6.86035113385743 46.47322942488355,6.856875161940195 46.471261748323144,6.854310014216785 46.47198398586242,6.852671846488619 46.47296506999416,6.852029149951359 46.47219709539574,6.851778518959785 46.47129621067176,6.85016675440234 46.4698485110051,6.847706894515294 46.468576284589616,6.841911979416146 46.46278854702276))',
        'a8405d50-f712-4e3e-96b2-a5452cf4e03e', 'Plan réseaux de démonstration', False, NULL, '2022-11-21 16:36:02.099',
        'STANDBY', 1, '', '', 1, 1, '1485134', '41320d0f-3130-4c4c-974a-0c4d3bde1749',
        'a35f0327-bceb-43a1-b366-96c3a94bc47b', 'https://int.viageo.ch/commandes/435747', '');

INSERT INTO requests(id_request, p_client, p_clientdetails, end_date, folder_in, folder_out, p_orderguid, p_orderlabel, 
                     p_organism, p_parameters, p_perimeter, p_productguid, p_productlabel, rejected, remark, start_date, 
                     status, tasknum, p_tiers, p_tiersdetails, id_connector, id_process, p_surface, p_clientguid, 
                     p_organismguid, p_external_url, p_tiersguid)
VALUES (2, 'Yves Grasset', 'Boulevard de Grancy 56
1006 Lausanne
+41 22 344 45 10
ygr@arxit.com', NULL, '1ddb76a6-2610-47c3-bde2-7a72f7425855/input', '1ddb76a6-2610-47c3-bde2-7a72f7425855/output',
        '92169728-d300-4e3e-bc14-138e610de9cc', '443530', 'ASIT',
        '{"FORMAT":"DXF","PROJECTION":"SWITZERLAND95","RAISON":"LOCALISATION","RAISON_LABEL":"Localisation en vue de projets","REMARK":"Ceci est un test\nAvec retour à la ligne"}',
        'POLYGON((7.008802763251656 46.245519329293245,7.008977478638646 46.24596978223839,7.010099318044382 46.24634512591109,7.011161356635566 46.24649533820254,7.011851394695592 46.24654742881326,7.012123110524144 46.24662042289713,7.012329750692657 46.246724655380014,7.012417623228246 46.24668000889588,7.012559036117633 46.24642191589558,7.012535717792058 46.246088985456616,7.012514122624683 46.245949469899564,7.012472496413521 46.245884093468234,7.012185407319924 46.24570534214322,7.01217302489515 46.24563108046702,7.011217983680352 46.24547903436611,7.009977076726536 46.244995300279086,7.009187734983265 46.24479663917551,7.008860662659381 46.24516646719812,7.008784739864421 46.24533934577381,7.008802763251656 46.245519329293245))',
        'a8405d50-f712-4e3e-96b2-a5452cf4e03e','Plan réseaux de démonstration', False, NULL, '2023-02-27 16:14:36.961',
        'ERROR', 1, '', '', 1, 1, '34792', '4b01553d-9766-4014-9166-3f00f58adfc7', 'a35f0327-bceb-43a1-b366-96c3a94bc47b',
        'https://int.viageo.ch/commandes/443530', '');

INSERT INTO requests(id_request, p_client, p_clientdetails, end_date, folder_in, folder_out, p_orderguid, p_orderlabel, 
                     p_organism, p_parameters, p_perimeter, p_productguid, p_productlabel, rejected, remark, start_date, 
                     status, tasknum, p_tiers, p_tiersdetails, id_connector, id_process, p_surface, p_clientguid, 
                     p_organismguid, p_external_url, p_tiersguid)
VALUES (3, 'DavidTEST BeniTEST', 'Avenue de la Praille 45
1227 Carouge', '2017-08-25 17:13:56.371', 'c05b9afc-5d06-4377-b22c-cccd0dea9aff/input', 
        'c05b9afc-5d06-4377-b22c-cccd0dea9aff/output', '29740951-6bc9-b164-bd27-4c84079b49c6', '227976','arx iT DEV-TESTS',
        '{"FORMAT" : "SHP", "PROJECTION" : "SWITZERLAND"}',
        'POLYGON((6.305801504197224 46.34034193773002, 6.300574962196896 46.58949427029274, 6.976470602791147 46.59425513709236, 6.978595958249548 46.34508114541769, 6.305801504197224 46.34034193773002))',
        '6f5afa6b-7f0a-ec54-a9a7-44f004d6edda', 'Plan réseaux de démonstration', FALSE, '', '2017-08-25 17:06:12.412',
        'FINISHED', 2, '', '', 1, 1, '1434255488', NULL, NULL, NULL, NULL);

INSERT INTO requests(
    id_request, p_client, p_clientdetails, end_date, folder_in, folder_out, p_orderguid, p_orderlabel, p_organism, p_parameters, p_perimeter, p_productguid, p_productlabel, rejected, remark, start_date, status, tasknum, p_tiers, p_tiersdetails, id_connector, id_process, p_surface, p_clientguid, p_organismguid, p_external_url, p_tiersguid)
VALUES (4, 'DavidTEST BeniTEST', 'Avenue de la Praille 45
1227 Carouge', '2017-08-25 17:13:56.371', 'c05b9afc-5d06-4377-b22c-cccd0dea9aff/input',
        'c05b9afc-5d06-4377-b22c-cccd0dea9aff/output', '29740951-6bc9-b164-bd27-4c84079b49c6', '227976',
        'arx iT DEV-TESTS','{"FORMAT" : "SHP","PROJECTION" : "SWITZERLAND"}',
        'POLYGON((6.305801504197224 46.34034193773002,6.300574962196896 46.58949427029274,6.976470602791147 46.59425513709236,6.978595958249548 46.34508114541769,6.305801504197224 46.34034193773002))',
        '6f5afa6b-7f0a-ec54-a9a7-44f004d6edda', 'Plan réseaux de démonstration', TRUE,
        'Pas de données disponibles dans cette zone', '2017-08-25 17:06:12.412', 'FINISHED', 2, '', '', 1, 1,
        '1434255488', NULL, NULL, NULL, NULL);

INSERT INTO request_history(id_record, end_date, last_msg, process_step, start_date, status, step, task_label,
                            id_request, id_user)
VALUES(1, '2022-11-21 16:36:02.126', 'OK', 0, '2022-11-21 16:36:02.099', 'FINISHED', 1, 'Import', 1, 1);
INSERT INTO request_history(id_record, end_date, last_msg, process_step, start_date, status, step, task_label,
                            id_request, id_user)
VALUES(2, '2022-11-21 16:47:02.126', 'Le traitement est en attente de validation par l opérateur.', 1,
       '2022-11-21 16:46:02.126', 'STANDBY', 2, 'Validation opérateur', 1, 1);

INSERT INTO request_history(id_record, end_date, last_msg, process_step, start_date, status, step, task_label,
                            id_request, id_user)
VALUES(3, '2022-11-21 16:36:02.126', 'OK', 0, '2022-11-21 16:36:02.099', 'FINISHED', 1, 'Import', 2, 1);
INSERT INTO request_history(id_record, end_date, last_msg, process_step, start_date, status, step, task_label,
                            id_request, id_user)
VALUES(4, '2022-11-21 16:47:02.126', 'L''exécution de la tâche FME a échoué - Erreur test', 1, '2022-11-21 16:46:02.126',
       'ERROR', 2, 'Validation opérateur', 2, 1);

INSERT INTO request_history(id_record, end_date, last_msg, process_step, start_date, status, step, task_label,
                            id_request, id_user)
VALUES(5, '2022-11-21 16:36:02.126', 'OK', 0, '2022-11-21 16:36:02.099', 'FINISHED', 1, 'Import', 3, 1);
INSERT INTO request_history(id_record, end_date, last_msg, process_step, start_date, status, step, task_label,
                            id_request, id_user)
VALUES(6, '2022-11-21 16:47:02.126', 'OK', 1, '2022-11-21 16:46:02.126', 'FINISHED', 2, 'Validation opérateur', 3, 2);
INSERT INTO request_history(id_record, end_date, last_msg, process_step, start_date, status, step, task_label,
                            id_request, id_user)
VALUES(7, '2022-11-21 16:57:02.126', '', 2, '2022-11-21 16:56:02.126', 'FINISHED', 3, 'Exportation', 3, 1);

INSERT INTO request_history(id_record, end_date, last_msg, process_step, start_date, status, step, task_label,
                            id_request, id_user)
VALUES(8, '2022-11-21 16:36:02.126', 'OK', 0, '2022-11-21 16:36:02.099', 'FINISHED', 1, 'Import', 4, 1);
INSERT INTO request_history(id_record, end_date, last_msg, process_step, start_date, status, step, task_label,
                            id_request, id_user)
VALUES(9, '2022-11-21 16:47:02.126', 'OK', 1, '2022-11-21 16:46:02.126', 'FINISHED', 2, 'Validation opérateur', 4, 2);
INSERT INTO request_history(id_record, end_date, last_msg, process_step, start_date, status, step, task_label,
                            id_request, id_user)
VALUES(10, '2022-11-21 16:57:02.126', '', 2, '2022-11-21 16:56:02.126', 'FINISHED', 3, 'Exportation', 4, 1);

UPDATE system SET value = 20 WHERE key = 'dashboard_interval';
