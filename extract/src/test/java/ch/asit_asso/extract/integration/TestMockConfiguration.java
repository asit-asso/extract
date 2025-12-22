/*
 * Copyright (C) 2025 SecureMind Sàrl
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ch.asit_asso.extract.integration;

import ch.asit_asso.extract.persistence.SystemParametersRepository;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import static org.mockito.Mockito.when;

/**
 * Common test configuration for integration tests.
 * Provides mock beans that are needed during Spring context initialization.
 *
 * @author Bruno Alves
 */
@TestConfiguration
public class TestMockConfiguration {

    @Bean
    @Primary
    public SystemParametersRepository testSystemParametersRepository() {
        SystemParametersRepository mock = Mockito.mock(SystemParametersRepository.class);
        // Provide default values needed for OrchestratorConfiguration during Spring context initialization
        when(mock.getSchedulerFrequency()).thenReturn("20");
        when(mock.getSchedulerMode()).thenReturn("ON");
        when(mock.getSchedulerRanges()).thenReturn("[]");
        return mock;
    }
}
