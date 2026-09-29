/*
 * Copyright (C) 2026 asit-asso
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package ch.asit_asso.extract.integration.connectors;

import java.util.ArrayList;
import java.util.Arrays;
import ch.asit_asso.extract.connectors.common.IConnector;
import ch.asit_asso.extract.connectors.implementation.ConnectorDiscovererWrapper;
import ch.asit_asso.extract.persistence.ConnectorsRepository;
import ch.asit_asso.extract.persistence.RequestsRepository;
import ch.asit_asso.extract.services.SecretParameters;
import ch.asit_asso.extract.web.model.ConnectorModel;
import ch.asit_asso.extract.web.model.PluginItemModelParameter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ch.asit_asso.extract.integration.WithMockApplicationUser;

/**
 * Integration coverage for connector secret persistence (issue #362).
 *
 * @author Bruno Alves
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Tag("integration")
@DisplayName("Connector Secret Integration Tests (issue #362)")
class ConnectorSecretIntegrationTest {

    private static final String CONNECTOR_CODE = "easysdiv4";
    private static final String MARKER = "ZZ362-secret-connector";
    private static final String PASSWORD = "ZZ362-clear-password";
    private static final String CONNECTOR_PARAMETERS = "["
            + "{\"code\":\"serviceUrl\",\"label\":\"Service URL\",\"type\":\"text\",\"req\":true,\"maxlength\":255},"
            + "{\"code\":\"login\",\"label\":\"Login\",\"type\":\"text\",\"req\":true,\"maxlength\":50},"
            + "{\"code\":\"pass\",\"label\":\"Password\",\"type\":\"pass\",\"req\":true,\"maxlength\":50},"
            + "{\"code\":\"uploadSize\",\"label\":\"Upload size\",\"type\":\"numeric\",\"req\":false,\"min\":1,\"step\":1},"
            + "{\"code\":\"detailsUrl\",\"label\":\"Details URL\",\"type\":\"text\",\"req\":false,\"maxlength\":255}"
            + "]";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ConnectorsRepository connectorsRepository;

    @Autowired
    private RequestsRepository requestsRepository;

    @Autowired
    private SecretParameters secretParameters;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @MockBean
    private ConnectorDiscovererWrapper connectorDiscoverer;

    @Test
    @WithMockApplicationUser(username = "admin", userId = 2, role = "ADMIN")
    @Transactional
    @DisplayName("Connector form encrypts the password and model reload decrypts it")
    void connectorFormPersistsAndReloadsSecret() throws Exception {
        IConnector plugin = mock(IConnector.class);
        when(plugin.getCode()).thenReturn(CONNECTOR_CODE);
        when(plugin.getLabel()).thenReturn("easySDI v4");
        when(plugin.getParams()).thenReturn(CONNECTOR_PARAMETERS);
        when(this.connectorDiscoverer.getConnectorForLanguage(CONNECTOR_CODE, "fr")).thenReturn(plugin);

        MockHttpServletRequestBuilder request = post("/connectors/add").with(csrf())
                .param("id", "0")
                .param("name", MARKER)
                .param("typeCode", plugin.getCode())
                .param("typeLabel", plugin.getLabel())
                .param("importFrequency", "240")
                .param("maximumRetries", "0")
                .param("active", "false");

        ObjectMapper objectMapper = new ObjectMapper();
        int index = 0;
        for (JsonNode parameter : objectMapper.readTree(plugin.getParams())) {
            String code = parameter.get("code").asText();
            String type = parameter.get("type").asText();
            String parameterIndex = String.valueOf(index++);
            String submittedType = "pass".equals(code) ? "text" : type;
            request.param("parameters[" + parameterIndex + "].name", code)
                    .param("parameters[" + parameterIndex + "].type", submittedType)
                    .param("parameters[" + parameterIndex + "].label", parameter.get("label").asText())
                    .param("parameters[" + parameterIndex + "].required",
                            String.valueOf(parameter.path("req").asBoolean(false)))
                    .param("parameters[" + parameterIndex + "].maxLength",
                            String.valueOf(parameter.path("maxlength").asInt(0)))
                    .param("parameters[" + parameterIndex + "].value", valueFor(code));
        }

        this.mockMvc.perform(request).andExpect(status().is3xxRedirection());

        ch.asit_asso.extract.domain.Connector savedConnector = null;
        for (ch.asit_asso.extract.domain.Connector connector : this.connectorsRepository.findAll()) {
            if (MARKER.equals(connector.getName())) {
                savedConnector = connector;
                break;
            }
        }
        assertNotNull(savedConnector);
        savedConnector.setRulesCollection(new ArrayList<>());
        final String rawParameters = this.jdbcTemplate.queryForObject(
                "SELECT connector_params FROM connectors WHERE id_connector = ?", String.class, savedConnector.getId());
        MockHttpServletRequestBuilder updateRequest = post("/connectors/" + savedConnector.getId()).with(csrf())
                .param("id", String.valueOf(savedConnector.getId()))
                .param("name", MARKER)
                .param("typeCode", plugin.getCode())
                .param("typeLabel", plugin.getLabel())
                .param("importFrequency", "240")
                .param("maximumRetries", "0")
                .param("active", "false");
        index = 0;
        for (JsonNode parameter : objectMapper.readTree(plugin.getParams())) {
            String code = parameter.get("code").asText();
            String type = parameter.get("type").asText();
            String parameterIndex = String.valueOf(index++);
            String submittedType = "pass".equals(code) ? "text" : type;
            updateRequest.param("parameters[" + parameterIndex + "].name", code)
                    .param("parameters[" + parameterIndex + "].type", submittedType)
                    .param("parameters[" + parameterIndex + "].label", parameter.get("label").asText())
                    .param("parameters[" + parameterIndex + "].required",
                            String.valueOf(parameter.path("req").asBoolean(false)))
                    .param("parameters[" + parameterIndex + "].maxLength",
                            String.valueOf(parameter.path("maxlength").asInt(0)))
                    .param("parameters[" + parameterIndex + "].value",
                            "pass".equals(code) ? "*****" : valueFor(code));
        }
        this.mockMvc.perform(updateRequest).andExpect(status().is3xxRedirection());
        String updatedRawParameters = this.jdbcTemplate.queryForObject(
                "SELECT connector_params FROM connectors WHERE id_connector = ?", String.class, savedConnector.getId());
        assertEquals(rawParameters, updatedRawParameters, "Submitting the mask must preserve the ciphertext.");

        assertTrue(rawParameters.contains("enc:v1:"), "The database column must contain ciphertext.");
        assertFalse(rawParameters.contains(PASSWORD), "The clear password must not be stored in the database.");
        assertTrue(savedConnector.getConnectorParametersValues().get("pass").startsWith("enc:v1:"));

        ConnectorModel reloadedModel = new ConnectorModel(plugin, savedConnector, this.requestsRepository,
                this.secretParameters);
        PluginItemModelParameter passwordParameter = Arrays.stream(reloadedModel.getParameters())
                .filter(parameter -> "pass".equals(parameter.getName()))
                .findFirst()
                .orElseThrow();

        assertEquals(PASSWORD, passwordParameter.getValue());
    }

    private static String valueFor(final String code) {
        return switch (code) {
            case "serviceUrl" -> "https://easy-sdi.example.test";
            case "login" -> "ZZ362-login";
            case "pass" -> PASSWORD;
            case "uploadSize" -> "10";
            default -> "";
        };
    }
}
