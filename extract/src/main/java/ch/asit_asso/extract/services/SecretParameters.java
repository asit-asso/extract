package ch.asit_asso.extract.services;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import ch.asit_asso.extract.utils.Secrets;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Encrypts and decrypts password-like plugin parameters at the persistence boundary.
 */
@Service
public class SecretParameters {

    private static final Set<String> PASSWORD_TYPES = Set.of("pass", "password");

    private final Logger logger = LoggerFactory.getLogger(SecretParameters.class);

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final Secrets secrets;

    public SecretParameters(final Secrets secrets) {
        if (secrets == null) {
            throw new IllegalArgumentException("The secrets utility cannot be null.");
        }
        this.secrets = secrets;
    }

    public static boolean isSecretType(final String type) {
        return type != null && PASSWORD_TYPES.contains(type.toLowerCase(Locale.ROOT));
    }

    public HashMap<String, String> encrypt(final Map<String, String> values,
                                           final Collection<String> secretNames) {
        return this.transform(values, secretNames, true);
    }

    public HashMap<String, String> decrypt(final Map<String, String> values,
                                           final Collection<String> secretNames) {
        return this.transform(values, secretNames, false);
    }

    public HashMap<String, String> encrypt(final Map<String, String> values, final String parametersDefinition) {
        return this.encrypt(values, this.getSecretNames(parametersDefinition));
    }

    public HashMap<String, String> decrypt(final Map<String, String> values, final String parametersDefinition) {
        return this.decrypt(values, this.getSecretNames(parametersDefinition));
    }

    public Set<String> getSecretNames(final String parametersDefinition) {
        Set<String> names = new HashSet<>();

        if (parametersDefinition == null || parametersDefinition.isBlank()) {
            return names;
        }

        try {
            JsonNode parameters = this.objectMapper.readTree(parametersDefinition);
            if (parameters.isArray()) {
                for (JsonNode parameter : parameters) {
                    if (isSecretType(parameter.path("type").asText())) {
                        names.add(parameter.path("code").asText());
                    }
                }
            }
        } catch (Exception exception) {
            this.logger.warn("Could not read plugin parameter definitions while locating secrets.", exception);
        }

        names.remove("");
        return names;
    }

    private HashMap<String, String> transform(final Map<String, String> values,
                                              final Collection<String> secretNames,
                                              final boolean encrypt) {
        HashMap<String, String> transformed = new HashMap<>();
        if (values == null) {
            return transformed;
        }

        Set<String> names = (secretNames == null) ? Set.of() : new HashSet<>(secretNames);
        for (Map.Entry<String, String> entry : values.entrySet()) {
            String value = entry.getValue();
            if (names.contains(entry.getKey()) && value != null && !Secrets.isGenericPasswordString(value)) {
                value = encrypt ? this.secrets.encryptIfNeeded(value) : this.secrets.decryptLegacy(value);
            }
            transformed.put(entry.getKey(), value);
        }
        return transformed;
    }
}