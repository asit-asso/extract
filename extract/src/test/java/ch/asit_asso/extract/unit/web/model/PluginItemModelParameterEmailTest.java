/*
 * Copyright (C) 2026 asit-asso
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
package ch.asit_asso.extract.unit.web.model;

import java.util.stream.Stream;
import ch.asit_asso.extract.web.model.PluginItemModelParameter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for the validation of task parameters of type <code>email</code>, which must accept request
 * variable placeholders such as <code>{clientEmail}</code> next to literal addresses (issue #366).
 */
@DisplayName("E-mail task parameter validation (issue #366)")
class PluginItemModelParameterEmailTest {

    /**
     * Checks which recipient lists are accepted when a task parameter of type <code>email</code> is saved.
     *
     * @param recipients the value entered by the operator in the recipients parameter
     * @param isValid    whether the value is expected to pass validation
     */
    @ParameterizedTest(name = "{index}: \"{0}\" valid = {1}")
    @MethodSource("recipientLists")
    @DisplayName("Placeholders are accepted as recipients, malformed addresses are still rejected")
    void placeholdersAreAcceptedAsRecipients(final String recipients, final boolean isValid) {
        PluginItemModelParameter parameter = new PluginItemModelParameter("to", "Recipients", "email", true, 5000);

        assertEquals(isValid, parameter.validateUpdatedValue(recipients));
    }



    private static Stream<Arguments> recipientLists() {
        return Stream.of(
                Arguments.of("operator@example.com", true),
                Arguments.of("operator@example.com; admin@example.org", true),
                Arguments.of("{clientEmail}", true),
                Arguments.of("{CLIENTEMAIL}", true),
                Arguments.of("{parameters.contact_email}", true),
                Arguments.of("{clientEmail};operator@example.com", true),
                Arguments.of(" {clientEmail} , operator@example.com ", true),
                Arguments.of("not-an-address", false),
                Arguments.of("{client email}", false),
                Arguments.of("{clientEmail", false),
                Arguments.of("clientEmail}", false),
                Arguments.of("{}", false),
                Arguments.of("{clientEmail}; not-an-address", false)
        );
    }
}
