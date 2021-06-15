/*
 * Copyright (C) 2017 arx iT
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
package org.easysdi.extract.requestmatching;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import org.apache.commons.lang3.ArrayUtils;
import org.easysdi.extract.domain.Request;
import org.easysdi.extract.domain.Rule;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thymeleaf.util.StringUtils;



/**
 * A matcher that associates a request with a process through the rules of the connector that imported it.
 *
 * @author Florent Krin
 */
public class RequestMatcher {

    /**
     * The string that indicates a line return.
     */
    private static final String STRING_NEWLINE = "\r\n";

    /**
     * The string that indicates an AND operator.
     */
    private static final String OPERATOR_AND = "AND";

    /**
     * The string that indicates an OR operator.
     */
    private static final String OPERATOR_OR = "OR";

    /**
     * The string representation of the operator that determines if two geometries overlap.
     */
    private static final String GEOM_OPERATOR_INTERSECT = "INTERSECTS";

    /**
     * The string representation of the operator that determines if one geometry is fully inside another.
     */
    private static final String GEOM_OPERATOR_CONTAINS = "CONTAINS";

    /**
     * The string representation of the operator that determines if two geometries are fully separated.
     */
    private static final String GEOM_OPERATOR_DISJOINT = "DISJOINT";

    /**
     * The string representation of the operator that determines if two geometries are the same.
     */
    private static final String GEOM_OPERATOR_EQUALS = "EQUALS";

    /**
     * The string representation of the operator that determines if one geometry is inside another.
     */
    private static final String GEOM_OPERATOR_WITHIN = "WITHIN";

    /**
     * An array that contains all the operators that can be used to compare geometries.
     */
    private static final String[] GEOMETRIC_OPERATORS = new String[]{
        GEOM_OPERATOR_CONTAINS, GEOM_OPERATOR_DISJOINT, GEOM_OPERATOR_EQUALS, GEOM_OPERATOR_INTERSECT,
        GEOM_OPERATOR_WITHIN
    };

    /**
     * An array that contains all the operators that can be used to bind logical expressions.
     */
    private static final String[] LOGICAL_OPERATORS = new String[]{OPERATOR_AND, OPERATOR_OR};

    /**
     * The engine that evaluates the rules.
     */
    private ScriptEngine engine;

    /**
     * The writer to the application logs.
     */
    private final Logger logger = LoggerFactory.getLogger(RequestMatcher.class);

    /**
     * The data item request to match with a process.
     */
    private final Request request;



    /**
     * Creates a new request matcher instance.
     *
     * @param importedRequest the request to associate to a process
     */
    public RequestMatcher(final Request importedRequest) {
        this.request = importedRequest;
    }



    /**
     * Create a JavaScript engine instance with the parameters of the associated request.
     */
    private void initAssignements() {

        ScriptEngineManager factory = new ScriptEngineManager();
        this.engine = factory.getEngineByName("JavaScript");

        Class<Request> requestClass = Request.class;
        try {
            for (Field requestField : requestClass.getDeclaredFields()) {
                this.logger.debug("Processing field {}", requestField.getName());
                if (!java.lang.reflect.Modifier.isStatic(requestField.getModifiers())) {
                    requestField.setAccessible(true);
                    Object fieldValue = requestField.get(this.request);
                    requestField.setAccessible(false);

                    String assignement;

                    if (fieldValue instanceof String) {
                        String stringValue = String.valueOf(fieldValue).trim();

                        if (this.isJSONValid(stringValue)) {
                            //la valeur du champ est un json : il faut donc faire les assignations pour tous les
                            // paramètres du json
                            Gson gson = new Gson();
                            engine.eval(requestField.getName().toUpperCase() + " = {}");
                            JsonObject jsonObject = gson.fromJson(stringValue, JsonObject.class);
                            //set assignements for json keys
                            for (Map.Entry<String, JsonElement> jsonItem : jsonObject.entrySet()) {
                                assignement = String.format("%s.%s = %s", requestField.getName(), jsonItem.getKey(),
                                        jsonItem.getValue());
                                engine.eval(assignement.toUpperCase());
                                this.logger.debug("set assignement : " + assignement.toUpperCase());
                            }

                        } else {
                            assignement = String.format("%s = \"%s\"", requestField.getName(),
                                    stringValue.replaceAll(STRING_NEWLINE, " "));
                            engine.eval(assignement.toUpperCase());
                            this.logger.debug("set assignement : " + assignement.toUpperCase());
                        }

                    } else if (fieldValue instanceof Integer || fieldValue instanceof Double) {
                        assignement = String.format("%s = %s", requestField.getName(), String.valueOf(fieldValue));
                        engine.eval(assignement.toUpperCase());
                        this.logger.debug("set assignement : " + assignement.toUpperCase());

                    } else if (fieldValue instanceof Boolean) {
                        assignement = String.format("%s = %s", requestField.getName().toUpperCase(),
                                String.valueOf(fieldValue));
                        engine.eval(assignement);
                        this.logger.debug("set assignement : " + assignement.toUpperCase());
                    }

                }

            }
        } catch (IllegalAccessException exc) {
            this.logger.error("Could not be access to a field in a request object.", exc);

        } catch (ScriptException exc) {
            this.logger.error("Could not evaluate an assignement for the request.", exc);
        }

    }



    /**
     * Checks if input string is JSON.
     *
     * @param jsonInString the string to check
     * @return <code>true</code> if the input is a valid JSON string
     */
    private boolean isJSONValid(final String jsonInString) {
        Gson gson = new Gson();

        if (jsonInString == null || jsonInString.equals("")) {
            return false;
        }

        try {
            gson.fromJson(jsonInString, JsonObject.class);
            return true;

        } catch (com.google.gson.JsonSyntaxException exception) {
            return false;
        }
    }



    /**
     * Reformats the rule so it can be parsed by the JavaScript engine.
     *
     * @param rule the rule to format
     * @return the formatted rule
     */
    private String reformatRule(final String rule) {

        return rule.replaceAll(String.format("(?i)\\s+%s\\s+", OPERATOR_AND), " && ")
                .replaceAll(String.format("(?i)\\s+%s\\s+", OPERATOR_OR), " || ");
    }



    /**
     * Evaluate the attributes and geographic criteria of a rule.
     *
     * @param rule the rule to evaluate
     * @return <code>true</code> if the request matches the rule
     */
    private boolean evaluateRule(final String rule) {

        Pattern patternBoolOperator = Pattern.compile(String.format("(?i)\\s+(?:%s)\\s+",
                StringUtils.join(GEOMETRIC_OPERATORS, "|")));
        //split rule by logical operator
        String[] splittedRule = rule.split(String.format("(?i)\\s+(?:%s)\\s+",
                StringUtils.join(LOGICAL_OPERATORS, "|")));

        try {
            String finalRuleToEvaluate = rule;
            //Loop sub rule
            for (String subrule : splittedRule) {

                if (patternBoolOperator.matcher(subrule.toUpperCase()).find()) { //is an geographic filter
                    Boolean geomRuleMatched = this.evaluateGeographicCondition(subrule.trim().toUpperCase());
                    finalRuleToEvaluate = finalRuleToEvaluate.replace(subrule.trim(), geomRuleMatched.toString());

                } else {
                    Boolean attrRuleMatched = this.evaluateLogicalCondition(subrule.trim().toUpperCase());
                    finalRuleToEvaluate = finalRuleToEvaluate.replace(subrule.trim(), attrRuleMatched.toString());
                }
            }

            Boolean matched = (Boolean) this.engine.eval(this.reformatRule(finalRuleToEvaluate));
            this.logger.info(rule + " => " + matched);

            return matched;

        } catch (ScriptException exc) {
            this.logger.error("Could not match request with rule " + rule, exc);

        } catch (Exception exc) {
            this.logger.error("Could not match request with rule " + rule, exc);
        }

        return false;

    }



    /**
     * Evaluate a logical criterion.
     *
     * @param condition the logical expression to evaluate
     * @return <code>true</code> if the request matches the logical criterion
     */
    private Boolean evaluateLogicalCondition(final String condition) {

        try {
            Matcher inMatcher = Pattern.compile("^(\\S+)\\s+((?:NOT\\s+)?IN)\\s+\\(([^)]+)\\)$",
                    Pattern.CASE_INSENSITIVE | Pattern.CANON_EQ | Pattern.DOTALL).matcher(condition);

            if (inMatcher.find()) {
                this.logger.debug("The condition \"{}\" matches the array operators", condition);
                return this.evaluateArrayCondition((String) this.engine.eval(inMatcher.group(1)),
                        inMatcher.group(3).split(","), inMatcher.group(2).toUpperCase());
            }

            this.logger.debug("The condition \"{}\" does NOT match the array operators", condition);

            Boolean matched = (Boolean) this.engine.eval(condition.toUpperCase());
            this.logger.info(condition + " => " + matched);

            return matched;

        } catch (ScriptException exc) {
            this.logger.error("Could not match request with rule " + condition, exc);

        } catch (Exception exc) {
            this.logger.error("Could not match request with rule " + condition, exc);
        }

        return false;
    }



    /**
     * Checks if a given value is (or is not) in an array.
     *
     * @param needle   the value to search in the array
     * @param haystack the array where the value must be searched
     * @param operator <code>IN</code> to check if the value in the array, or <code>NOT IN</code> to check if the
     *                 value is not in the array
     * @return <code>true</code> if the check was positive
     */
    private boolean evaluateArrayCondition(final String needle, final String[] haystack, final String operator) {
        assert needle != null : "The value to search cannot be null";
        assert haystack != null : "The array cannot be null";
        assert operator != null && (operator.equals("IN") || operator.equals("NOT IN")) :
                "The operator must be IN or NOT IN";

        String[] noQuotesHaystack = new String[haystack.length];

        for (int hayIndex = 0; hayIndex < haystack.length; hayIndex++) {
            String hayItem = haystack[hayIndex].trim();

            if (hayItem.matches("\".*\"")) {
                noQuotesHaystack[hayIndex] = hayItem.substring(1, hayItem.length() - 1);
            } else {
                noQuotesHaystack[hayIndex] = hayItem;
            }
        }

        this.logger.debug("Trying to find whether {} is {} {}", needle, operator, noQuotesHaystack);

        int needleIndex = ArrayUtils.indexOf(noQuotesHaystack, needle);
        this.logger.debug("The index is {}", needleIndex);

        if (operator.equals("NOT IN")) {
            return (needleIndex == ArrayUtils.INDEX_NOT_FOUND);
        }

        return (needleIndex != ArrayUtils.INDEX_NOT_FOUND);
    }



    /**
     * Evaluate a geographic criterion.
     *
     * @param condition the geographic expression to evaluate
     * @return <code>true</code> if the request matches the geographic criterion
     */
    private Boolean evaluateGeographicCondition(final String condition) {

        Boolean matched = false;

        try {
            final GeometryFactory fact = new GeometryFactory();
            final WKTReader wktReader = new WKTReader(fact);
            final String[] splittedRule = condition.split(String.format("(?i)\\s+(?:%s)\\s+",
                    StringUtils.join(GEOMETRIC_OPERATORS, "|")));

            if (splittedRule.length == 2) {
                this.logger.info("Check matching with rule {}.", condition);
                final String fieldRule = splittedRule[0].trim();
                final String geomFilter = splittedRule[1].trim();
                final String fieldRuleValue = (String) this.engine.eval(fieldRule);
                final Geometry requestGeometry = wktReader.read(fieldRuleValue);
                final Geometry conditionGeometry = wktReader.read(geomFilter);

                if (condition.contains(GEOM_OPERATOR_CONTAINS)) {
                    matched = requestGeometry.contains(conditionGeometry);
                } else if (condition.contains(GEOM_OPERATOR_DISJOINT)) {
                    matched = requestGeometry.disjoint(conditionGeometry);
                } else if (condition.contains(GEOM_OPERATOR_EQUALS)) {
                    matched = requestGeometry.equals(conditionGeometry);
                } else if (condition.contains(GEOM_OPERATOR_INTERSECT)) {
                    matched = requestGeometry.intersects(conditionGeometry);
                } else if (condition.contains(GEOM_OPERATOR_WITHIN)) {
                    matched = requestGeometry.within(conditionGeometry);
                }

                if (matched) {
                    this.logger.info(condition + " => " + matched);
                }

            } else {
                this.logger.error("The syntaxe of the fowllowing rule is incorrect : " + condition);
            }

        } catch (ScriptException | ParseException exception) {
            this.logger.error("Could not match request with rule " + condition, exception);
        }

        return matched;
    }



    /**
     * Check if request match with at least one rule.
     *
     * @param rules a list of rules to match against the associated request
     * @return the first rule that matches the request, or <code>null</code> if no rule matches
     */
    public final Rule matchRequestWithRules(final List<Rule> rules) {

        this.logger.info("check request matching with rules");

        this.initAssignements();

        for (Rule rule : rules) {

            if (StringUtils.isEmpty(rule.getRule())) {
                this.logger.debug("Rule at position {} is empty.", rule.getPosition());
                continue;
            }

            if (!rule.isActive()) {
                this.logger.debug("Rule at position {} is inactive.", rule.getPosition());
                continue;
            }

            //String condition = this.reformatRule(rule.getRule());
            this.logger.info("Check matching with rule at position {}.", rule.getPosition());

            if (this.evaluateRule(rule.getRule())) {
                this.logger.info("Request match with rule {}.", rule.getRule());
                return rule;
            }
        }

        return null;
    }

}
