package ch.asit_asso.extract.unit.domain;

import ch.asit_asso.extract.domain.Connector;
import ch.asit_asso.extract.domain.Process;
import ch.asit_asso.extract.domain.Rule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Rule Entity Tests")
class RuleTest {

    private Rule rule;

    @BeforeEach
    void setUp() {
        rule = new Rule();
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Default constructor creates instance with null id")
        void defaultConstructor_createsInstanceWithNullId() {
            Rule newRule = new Rule();
            assertNull(newRule.getId());
        }

        @Test
        @DisplayName("Constructor with id sets the id correctly")
        void constructorWithId_setsIdCorrectly() {
            Integer expectedId = 42;
            Rule newRule = new Rule(expectedId);
            assertEquals(expectedId, newRule.getId());
        }
    }

    @Nested
    @DisplayName("Getter and Setter Tests")
    class GetterSetterTests {

        @Test
        @DisplayName("setId and getId work correctly")
        void setAndGetId() {
            Integer expectedId = 100;
            rule.setId(expectedId);
            assertEquals(expectedId, rule.getId());
        }

        @Test
        @DisplayName("setRule and getRule work correctly")
        void setAndGetRule() {
            String expectedRule = "product.label CONTAINS 'cadastre' AND organism.name EQUALS 'Canton'";
            rule.setRule(expectedRule);
            assertEquals(expectedRule, rule.getRule());
        }

        @Test
        @DisplayName("setActive and isActive work correctly")
        void setAndIsActive() {
            rule.setActive(true);
            assertTrue(rule.isActive());

            rule.setActive(false);
            assertFalse(rule.isActive());
        }

        @Test
        @DisplayName("isActive returns null when not set")
        void isActive_returnsNullWhenNotSet() {
            assertNull(rule.isActive());
        }

        @Test
        @DisplayName("setPosition and getPosition work correctly")
        void setAndGetPosition() {
            Integer expectedPosition = 5;
            rule.setPosition(expectedPosition);
            assertEquals(expectedPosition, rule.getPosition());
        }

        @Test
        @DisplayName("setProcess and getProcess work correctly")
        void setAndGetProcess() {
            Process expectedProcess = new Process(1);
            rule.setProcess(expectedProcess);
            assertEquals(expectedProcess, rule.getProcess());
        }

        @Test
        @DisplayName("setConnector and getConnector work correctly")
        void setAndGetConnector() {
            Connector expectedConnector = new Connector(1);
            rule.setConnector(expectedConnector);
            assertEquals(expectedConnector, rule.getConnector());
        }
    }

    @Nested
    @DisplayName("Rule Expression Tests")
    class RuleExpressionTests {

        @Test
        @DisplayName("setRule accepts complex expressions")
        void setRule_acceptsComplexExpressions() {
            String complexRule = "((product.label CONTAINS 'map') OR (product.label CONTAINS 'plan')) " +
                    "AND (surface < 10000) AND (organism.type IN ('public', 'admin'))";
            rule.setRule(complexRule);
            assertEquals(complexRule, rule.getRule());
        }

        @Test
        @DisplayName("setRule accepts null")
        void setRule_acceptsNull() {
            rule.setRule(null);
            assertNull(rule.getRule());
        }

        @Test
        @DisplayName("setRule accepts empty string")
        void setRule_acceptsEmptyString() {
            rule.setRule("");
            assertEquals("", rule.getRule());
        }

        @Test
        @DisplayName("setRule accepts very long expressions")
        void setRule_acceptsVeryLongExpressions() {
            StringBuilder longRule = new StringBuilder();
            for (int i = 0; i < 100; i++) {
                if (i > 0) {
                    longRule.append(" AND ");
                }
                longRule.append("field").append(i).append(" = 'value").append(i).append("'");
            }
            rule.setRule(longRule.toString());
            assertEquals(longRule.toString(), rule.getRule());
        }
    }

    @Nested
    @DisplayName("Position Tests")
    class PositionTests {

        @Test
        @DisplayName("position can be zero")
        void position_canBeZero() {
            rule.setPosition(0);
            assertEquals(0, rule.getPosition());
        }

        @Test
        @DisplayName("position can be negative")
        void position_canBeNegative() {
            rule.setPosition(-1);
            assertEquals(-1, rule.getPosition());
        }

        @Test
        @DisplayName("position can be large number")
        void position_canBeLargeNumber() {
            rule.setPosition(Integer.MAX_VALUE);
            assertEquals(Integer.MAX_VALUE, rule.getPosition());
        }
    }

    @Nested
    @DisplayName("Relationships Tests")
    class RelationshipsTests {

        @Test
        @DisplayName("rule can have process without connector")
        void rule_canHaveProcessWithoutConnector() {
            Process process = new Process(1);
            rule.setProcess(process);

            assertEquals(process, rule.getProcess());
            assertNull(rule.getConnector());
        }

        @Test
        @DisplayName("rule can have connector without process")
        void rule_canHaveConnectorWithoutProcess() {
            Connector connector = new Connector(1);
            rule.setConnector(connector);

            assertEquals(connector, rule.getConnector());
            assertNull(rule.getProcess());
        }

        @Test
        @DisplayName("rule can have both process and connector")
        void rule_canHaveBothProcessAndConnector() {
            Process process = new Process(1);
            Connector connector = new Connector(1);

            rule.setProcess(process);
            rule.setConnector(connector);

            assertEquals(process, rule.getProcess());
            assertEquals(connector, rule.getConnector());
        }

        @Test
        @DisplayName("process can be replaced")
        void process_canBeReplaced() {
            Process process1 = new Process(1);
            Process process2 = new Process(2);

            rule.setProcess(process1);
            assertEquals(process1, rule.getProcess());

            rule.setProcess(process2);
            assertEquals(process2, rule.getProcess());
        }

        @Test
        @DisplayName("connector can be replaced")
        void connector_canBeReplaced() {
            Connector connector1 = new Connector(1);
            Connector connector2 = new Connector(2);

            rule.setConnector(connector1);
            assertEquals(connector1, rule.getConnector());

            rule.setConnector(connector2);
            assertEquals(connector2, rule.getConnector());
        }
    }

    @Nested
    @DisplayName("Equals, HashCode, and ToString Tests")
    class EqualsHashCodeToStringTests {

        @Test
        @DisplayName("equals returns true for same id")
        void equals_returnsTrueForSameId() {
            Rule rule1 = new Rule(1);
            Rule rule2 = new Rule(1);
            assertEquals(rule1, rule2);
        }

        @Test
        @DisplayName("equals returns false for different id")
        void equals_returnsFalseForDifferentId() {
            Rule rule1 = new Rule(1);
            Rule rule2 = new Rule(2);
            assertNotEquals(rule1, rule2);
        }

        @Test
        @DisplayName("equals returns false for null")
        void equals_returnsFalseForNull() {
            Rule rule1 = new Rule(1);
            assertFalse(rule1.equals(null));
        }

        @Test
        @DisplayName("equals returns false for different type")
        void equals_returnsFalseForDifferentType() {
            Rule rule1 = new Rule(1);
            assertNotEquals("not a rule", rule1);
        }

        @Test
        @DisplayName("hashCode is consistent for same id")
        void hashCode_isConsistentForSameId() {
            Rule rule1 = new Rule(1);
            Rule rule2 = new Rule(1);
            assertEquals(rule1.hashCode(), rule2.hashCode());
        }

        @Test
        @DisplayName("hashCode differs for different id")
        void hashCode_differsForDifferentId() {
            Rule rule1 = new Rule(1);
            Rule rule2 = new Rule(2);
            assertNotEquals(rule1.hashCode(), rule2.hashCode());
        }

        @Test
        @DisplayName("toString contains id")
        void toString_containsId() {
            Rule rule1 = new Rule(42);
            String result = rule1.toString();
            assertTrue(result.contains("42"));
            assertTrue(result.contains("idRule"));
        }
    }

    @Nested
    @DisplayName("Complete Rule Configuration Tests")
    class CompleteRuleConfigurationTests {

        @Test
        @DisplayName("fully configured rule has all attributes")
        void fullyConfiguredRule_hasAllAttributes() {
            Integer id = 1;
            String ruleExpression = "product.label = 'test'";
            Boolean active = true;
            Integer position = 5;
            Process process = new Process(10);
            Connector connector = new Connector(20);

            rule.setId(id);
            rule.setRule(ruleExpression);
            rule.setActive(active);
            rule.setPosition(position);
            rule.setProcess(process);
            rule.setConnector(connector);

            assertEquals(id, rule.getId());
            assertEquals(ruleExpression, rule.getRule());
            assertEquals(active, rule.isActive());
            assertEquals(position, rule.getPosition());
            assertEquals(process, rule.getProcess());
            assertEquals(connector, rule.getConnector());
        }
    }
}
