package ch.asit_asso.extract.unit.requestmatching;

import java.util.ArrayList;
import java.util.List;
import ch.asit_asso.extract.domain.Request;
import ch.asit_asso.extract.domain.Rule;
import ch.asit_asso.extract.requestmatching.RequestMatcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("unit")
public class RequestMatcherTest {

    RequestMatcher testMatcher;

    RequestMatcher testMatcherWithNoThirdParty;

    final List<Rule> testRules = new ArrayList<>();


    @BeforeEach
    public void setUp() {
        this.testMatcher = new RequestMatcher(this.createRequest(true));
        this.testMatcherWithNoThirdParty = new RequestMatcher(this.createRequest(false));
        this.testRules.clear();
    }



    @Test
    @DisplayName("Match with empty rules")
    public void matchRequestWithEmptyRules() {
        final Rule emptyRule = new Rule();
        emptyRule.setRule("");
        emptyRule.setActive(true);
        emptyRule.setPosition(1);
        this.testRules.add(emptyRule);
        final Rule nullRule = new Rule();
        nullRule.setActive(true);
        nullRule.setPosition(2);
        this.testRules.add(nullRule);
        final Rule spacesRule = new Rule();
        spacesRule.setRule("  \n\t");
        spacesRule.setActive(true);
        spacesRule.setPosition(3);
        this.testRules.add(spacesRule);

        final Rule matchingRule = this.testMatcher.matchRequestWithRules(this.testRules);

        assertNull(matchingRule);
    }

    @Test
    @DisplayName("Match with active and inactive rules")
    public void matchRequestWithMixedActivityRule() {
        final Rule inactiveRuleThatWouldMatch = new Rule();
        inactiveRuleThatWouldMatch.setRule("orderLabel == \"426524\"");
        inactiveRuleThatWouldMatch.setActive(false);
        inactiveRuleThatWouldMatch.setPosition(1);
        this.testRules.add(inactiveRuleThatWouldMatch);
        final Rule activeRuleThatMatches = new Rule();
        activeRuleThatMatches.setRule("tiers == \"Bex\"");
        activeRuleThatMatches.setActive(true);
        activeRuleThatMatches.setPosition(2);
        this.testRules.add(activeRuleThatMatches);

        final Rule matchingRule = this.testMatcher.matchRequestWithRules(this.testRules);

        assertSame(activeRuleThatMatches, matchingRule);
    }



    @Test
    @DisplayName("Match with only inactive rules")
    public void matchRequestWithAllInactiveRules() {
        final Rule inactiveRule1 = new Rule();
        inactiveRule1.setRule("orderLabel == \"426524\"");
        inactiveRule1.setActive(false);
        inactiveRule1.setPosition(1);
        this.testRules.add(inactiveRule1);
        final Rule inactiveRule2 = new Rule();
        inactiveRule2.setRule("tiers == \"Bex\"");
        inactiveRule2.setActive(false);
        inactiveRule2.setPosition(1);
        this.testRules.add(inactiveRule2);

        final Rule matchingRule = this.testMatcher.matchRequestWithRules(this.testRules);

        assertNull(matchingRule);
    }



    @Test
    @DisplayName("Match customer name")
    public void matchRequestWithCustomerName() {
        final Rule customerEqualsRule = new Rule();
        customerEqualsRule.setRule("client == \"Yves Grasset\"");
        customerEqualsRule.setActive(true);
        customerEqualsRule.setPosition(1);

        boolean isMatch = this.testMatcher.isRuleMatching(customerEqualsRule);

        assertTrue(isMatch);
    }



    @Test
    @DisplayName("Match not equals customer name")
    public void matchRequestWithCustomerNameNotEquals() {
        final Rule customerDifferentRule = new Rule();
        customerDifferentRule.setRule("client != \"Yves Grasset\"");
        customerDifferentRule.setActive(true);
        customerDifferentRule.setPosition(1);

        boolean isMatch = this.testMatcher.isRuleMatching(customerDifferentRule);

        assertFalse(isMatch);
    }



    @Test
    @DisplayName("Match customer name in list")
    public void matchRequestWithCustomerNameInList() {
        final Rule customerInListRule = new Rule();
        customerInListRule.setRule("client IN (\"Julien Longchamp\", \"Yves Grasset\", \"Florent Krin\")");
        customerInListRule.setActive(true);
        customerInListRule.setPosition(1);

        boolean isMatch = this.testMatcher.isRuleMatching(customerInListRule);

        assertTrue(isMatch);
    }



    @Test
    @DisplayName("Match customer name not in list")
    public void matchRequestWithCustomerNameNotIn() {
        final Rule customerNotInListRule = new Rule();
        customerNotInListRule.setRule("client NOT IN (\"Yves Blatti\", \"Hans Mustermann\", \"Maria Bernasconi\")");
        customerNotInListRule.setActive(true);
        customerNotInListRule.setPosition(1);

        boolean isMatch = this.testMatcher.isRuleMatching(customerNotInListRule);

        assertTrue(isMatch);
    }



    @Test
    @DisplayName("Match customer GUID")
    public void matchRequestWithCustomerGuid() {
        final Rule customerGuidEqualsRule = new Rule();
        customerGuidEqualsRule.setRule("clientguid == \"17915324-ea6c-434f-bc1b-1e44cc3dee96\"");
        customerGuidEqualsRule.setActive(true);
        customerGuidEqualsRule.setPosition(1);

        boolean isMatch = this.testMatcher.isRuleMatching(customerGuidEqualsRule);

        assertTrue(isMatch);
    }



    @Test
    @DisplayName("Match not equals customer GUID")
    public void matchRequestWithCustomerGuidNotEquals() {
        final Rule customerGuidDifferentRule = new Rule();
        customerGuidDifferentRule.setRule("clientguid != \"17915324-ea6c-434f-bc1b-1e44cc3dee96\"");
        customerGuidDifferentRule.setActive(true);
        customerGuidDifferentRule.setPosition(1);

        boolean isMatch = this.testMatcher.isRuleMatching(customerGuidDifferentRule);

        assertFalse(isMatch);
    }



    @Test
    @DisplayName("Match order GUID")
    public void matchRequestWithOrderGuid() {
        final Rule orderGuidEqualsRule = new Rule();
        orderGuidEqualsRule.setRule("orderguid == \"dc8d3730-1e95-4294-b21c-823925710fb1\"");
        orderGuidEqualsRule.setActive(true);
        orderGuidEqualsRule.setPosition(1);

        boolean isMatch = this.testMatcher.isRuleMatching(orderGuidEqualsRule);

        assertTrue(isMatch);
    }



    @Test
    @DisplayName("Match not equals order GUID")
    public void matchRequestWithOrderGuidNotEquals() {
        final Rule orderGuidDifferentRule = new Rule();
        orderGuidDifferentRule.setRule("orderguid != \"dc8d3730-1e95-4294-b21c-823925710fb1\"");
        orderGuidDifferentRule.setActive(true);
        orderGuidDifferentRule.setPosition(1);

        boolean isMatch = this.testMatcher.isRuleMatching(orderGuidDifferentRule);

        assertFalse(isMatch);
    }



    @Test
    @DisplayName("Match order label")
    public void matchRequestWithOrderLabel() {
        final Rule orderLabelEqualsRule = new Rule();
        orderLabelEqualsRule.setRule("orderlabel == \"426524\"");
        orderLabelEqualsRule.setActive(true);
        orderLabelEqualsRule.setPosition(1);

        boolean isMatch = this.testMatcher.isRuleMatching(orderLabelEqualsRule);

        assertTrue(isMatch);
    }



    @Test
    @DisplayName("Match not equals order label")
    public void matchRequestWithOrderLabelNotEquals() {
        final Rule orderLabelDifferentRule = new Rule();
        orderLabelDifferentRule.setRule("orderlabel != \"426524\"");
        orderLabelDifferentRule.setActive(true);
        orderLabelDifferentRule.setPosition(1);

        boolean isMatch = this.testMatcher.isRuleMatching(orderLabelDifferentRule);

        assertFalse(isMatch);
    }



    @Test
    @DisplayName("Match organism name")
    public void matchRequestWithOrganismName() {
        final Rule organismNameEqualsRule = new Rule();
        organismNameEqualsRule.setRule("organism == \"ASIT - Association pour le système d'information du territoire\"");
        organismNameEqualsRule.setActive(true);
        organismNameEqualsRule.setPosition(1);

        boolean isMatch = this.testMatcher.isRuleMatching(organismNameEqualsRule);

        assertTrue(isMatch);
    }



    @Test
    @DisplayName("Match not equals organism name")
    public void matchRequestWithOrganismNameNotEquals() {
        final Rule organismNameDifferentRule = new Rule();
        organismNameDifferentRule.setRule("organism != \"ASIT - Association pour le système d'information du territoire\"");
        organismNameDifferentRule.setActive(true);
        organismNameDifferentRule.setPosition(1);

        boolean isMatch = this.testMatcher.isRuleMatching(organismNameDifferentRule);

        assertFalse(isMatch);
    }



    @Test
    @DisplayName("Match organism GUID")
    public void matchRequestWithOrganismGuid() {
        final Rule organismGuidEqualsRule = new Rule();
        organismGuidEqualsRule.setRule("organismguid == \"a35f0327-bceb-43a1-b366-96c3a94bc47b\"");
        organismGuidEqualsRule.setActive(true);
        organismGuidEqualsRule.setPosition(1);

        boolean isMatch = this.testMatcher.isRuleMatching(organismGuidEqualsRule);

        assertTrue(isMatch);
    }



    @Test
    @DisplayName("Match not equals organism GUID")
    public void matchRequestWithOrganismGuidNotEquals() {
        final Rule organismGuidDifferentRule = new Rule();
        organismGuidDifferentRule.setRule("organismguid != \"a35f0327-bceb-43a1-b366-96c3a94bc47b\"");
        organismGuidDifferentRule.setActive(true);
        organismGuidDifferentRule.setPosition(1);

        boolean isMatch = this.testMatcher.isRuleMatching(organismGuidDifferentRule);

        assertFalse(isMatch);
    }



    @Test
    @DisplayName("Match parameters values")
    public void matchRequestWithParameter() {
        final Rule parameterFormatEqualsRule = new Rule();
        parameterFormatEqualsRule.setRule("parameters.FORMAT == \"DXF\"");
        parameterFormatEqualsRule.setActive(true);
        parameterFormatEqualsRule.setPosition(1);

        boolean isMatch = this.testMatcher.isRuleMatching(parameterFormatEqualsRule);

        assertTrue(isMatch);
    }



    @Test
    @DisplayName("Match parameters values not matching")
    public void matchRequestWithParameterNotMatching() {
        final Rule parameterFormatDifferentRule = new Rule();
        parameterFormatDifferentRule.setRule("parameters.FORMAT == \"SHP\"");
        parameterFormatDifferentRule.setActive(true);
        parameterFormatDifferentRule.setPosition(1);

        boolean isMatch = this.testMatcher.isRuleMatching(parameterFormatDifferentRule);

        assertFalse(isMatch);
    }



    @Test
    @DisplayName("Match parameters values with non-existing parameter")
    public void matchRequestWithNonExistingParameter() {
        final Rule absentParameterEqualsRule = new Rule();
        absentParameterEqualsRule.setRule("parameters.DUMMY_PARAM == \"Whatever\"");
        absentParameterEqualsRule.setActive(true);
        absentParameterEqualsRule.setPosition(1);

        boolean isMatch = this.testMatcher.isRuleMatching(absentParameterEqualsRule);

        assertFalse(isMatch);
    }



    @Test
    @DisplayName("Match parameters values not equals non-existing parameter")
    public void matchRequestWithNonExistingParameterNotEquals() {
        final Rule absentParameterDifferentRule = new Rule();
        absentParameterDifferentRule.setRule("parameters.DUMMY_PARAM != \"Whatever\"");
        absentParameterDifferentRule.setActive(true);
        absentParameterDifferentRule.setPosition(1);

        boolean isMatch = this.testMatcher.isRuleMatching(absentParameterDifferentRule);

        assertTrue(isMatch);
    }



    @Test
    @DisplayName("Match perimeter")
    public void matchRequestWithPerimeter() {
        final Rule perimeterEqualsRule = new Rule();
        perimeterEqualsRule.setRule("perimeter equals POLYGON((6.556259943632146 46.541251354110784,6.550785473603749 46.51736879969809,6.585272600054474 46.520332304121965,6.575204996013936 46.53869822097311,6.556259943632146 46.541251354110784))");
        perimeterEqualsRule.setActive(true);
        perimeterEqualsRule.setPosition(1);

        boolean isMatch = this.testMatcher.isRuleMatching(perimeterEqualsRule);

        assertTrue(isMatch);
    }



    @Test
    @DisplayName("Match perimeter intersects")
    public void matchRequestWithPerimeterIntersects() {
        final Rule perimeterIntersectsRule = new Rule();
        perimeterIntersectsRule.setRule("perimeter intersects POLYGON((6.556259943632146 46.541251354110784,6.563488415498275 46.51748692571157,6.598833848833761 46.52021418429415,6.5999798563838175 46.535774963981254,6.598722604656531 46.550268804975566,6.5660446421184915 46.547154743818105,6.556259943632146 46.541251354110784))");
        perimeterIntersectsRule.setActive(true);
        perimeterIntersectsRule.setPosition(1);

        boolean isMatch = this.testMatcher.isRuleMatching(perimeterIntersectsRule);

        assertTrue(isMatch);
    }



    @Test
    @DisplayName("Match perimeter disjoint")
    public void matchRequestWithPerimeterDisjoint() {
        final Rule perimeterDisjointRule = new Rule();
        perimeterDisjointRule.setRule("perimeter disjoint POLYGON((6.556259943632146 46.541251354110784,6.563488415498275 46.51748692571157,6.598833848833761 46.52021418429415,6.5999798563838175 46.535774963981254,6.598722604656531 46.550268804975566,6.5660446421184915 46.547154743818105,6.556259943632146 46.541251354110784))");
        perimeterDisjointRule.setActive(true);
        perimeterDisjointRule.setPosition(1);

        boolean isMatch = this.testMatcher.isRuleMatching(perimeterDisjointRule);

        assertFalse(isMatch);
    }



    @Test
    @DisplayName("Match perimeter contains")
    public void matchRequestWithPerimeterContains() {
        final Rule perimeterContainsRule = new Rule();
        perimeterContainsRule.setRule("perimeter contains POLYGON((6.555916620878257 46.531568405285945,6.556106976289291 46.527881009306,6.569651414751729 46.52800954211501,6.567136911297156 46.53196725716802,6.563126398710288 46.5312141183206,6.555916620878257 46.531568405285945))");
        perimeterContainsRule.setActive(true);
        perimeterContainsRule.setPosition(1);

        boolean isMatch = this.testMatcher.isRuleMatching(perimeterContainsRule);

        assertTrue(isMatch);
    }



    @Test
    @DisplayName("Match perimeter within")
    public void matchRequestWithPerimeterWithin() {
        final Rule perimeterWithinRule = new Rule();
        perimeterWithinRule.setRule("perimeter within POLYGON((6.555916620878257 46.531568405285945,6.556106976289291 46.527881009306,6.569651414751729 46.52800954211501,6.567136911297156 46.53196725716802,6.563126398710288 46.5312141183206,6.555916620878257 46.531568405285945))");
        perimeterWithinRule.setActive(true);
        perimeterWithinRule.setPosition(1);

        boolean isMatch = this.testMatcher.isRuleMatching(perimeterWithinRule);

        assertFalse(isMatch);
    }



    @Test
    @DisplayName("Match product GUID")
    public void matchRequestWithProductGuid() {
        final Rule productGuidEqualsRule = new Rule();
        productGuidEqualsRule.setRule("productguid == \"71bf9e39-646d-4da6-a832-c4b3eafd4150\"");
        productGuidEqualsRule.setActive(true);
        productGuidEqualsRule.setPosition(1);

        boolean isMatch = this.testMatcher.isRuleMatching(productGuidEqualsRule);

        assertTrue(isMatch);
    }



    @Test
    @DisplayName("Match not equals product GUID")
    public void matchRequestWithProductGuidNotEquals() {
        final Rule productGuidDifferentRule = new Rule();
        productGuidDifferentRule.setRule("productguid != \"71bf9e39-646d-4da6-a832-c4b3eafd4150\"");
        productGuidDifferentRule.setActive(true);
        productGuidDifferentRule.setPosition(1);

        boolean isMatch = this.testMatcher.isRuleMatching(productGuidDifferentRule);

        assertFalse(isMatch);
    }



    @Test
    @DisplayName("Match product label")
    public void matchRequestWithProductLabel() {
        final Rule productLabelEqualsRule = new Rule();
        productLabelEqualsRule.setRule("productlabel == \"Plan réseaux de démonstration 2\"");
        productLabelEqualsRule.setActive(true);
        productLabelEqualsRule.setPosition(1);

        boolean isMatch = this.testMatcher.isRuleMatching(productLabelEqualsRule);

        assertTrue(isMatch);
    }



    @Test
    @DisplayName("Match not equals product label")
    public void matchRequestWithProductLabelNotEquals() {
        final Rule productLabelDifferentRule = new Rule();
        productLabelDifferentRule.setRule("productlabel != \"Plan réseaux de démonstration 2\"");
        productLabelDifferentRule.setActive(true);
        productLabelDifferentRule.setPosition(1);

        boolean isMatch = this.testMatcher.isRuleMatching(productLabelDifferentRule);

        assertFalse(isMatch);
    }



    @Test
    @DisplayName("Match surface")
    public void matchRequestWithSurface() {
        final Rule surfaceEqualsRule = new Rule();
        surfaceEqualsRule.setRule("surface == 4817500");
        surfaceEqualsRule.setActive(true);
        surfaceEqualsRule.setPosition(1);

        boolean isMatch = this.testMatcher.isRuleMatching(surfaceEqualsRule);

        assertTrue(isMatch);
    }



    @Test
    @DisplayName("Match not equals surface")
    public void matchRequestWithNotEqualsSurface() {
        final Rule surfaceDifferentRule = new Rule();
        surfaceDifferentRule.setRule("surface != 4817500");
        surfaceDifferentRule.setActive(true);
        surfaceDifferentRule.setPosition(1);

        boolean isMatch = this.testMatcher.isRuleMatching(surfaceDifferentRule);

        assertFalse(isMatch);
    }



    @Test
    @DisplayName("Match surface greater than")
    public void matchRequestWithSurfaceGreaterThan() {
        final Rule surfaceGreaterRule = new Rule();
        surfaceGreaterRule.setRule("surface > 4800000");
        surfaceGreaterRule.setActive(true);
        surfaceGreaterRule.setPosition(1);

        boolean isMatch = this.testMatcher.isRuleMatching(surfaceGreaterRule);

        assertTrue(isMatch);
    }



    @Test
    @DisplayName("Match surface smaller than")
    public void matchRequestWithSurfaceSmallerThan() {
        final Rule surfaceLessRule = new Rule();
        surfaceLessRule.setRule("surface < 4800000");
        surfaceLessRule.setActive(true);
        surfaceLessRule.setPosition(1);

        boolean isMatch = this.testMatcher.isRuleMatching(surfaceLessRule);

        assertFalse(isMatch);
    }



    @Test
    @DisplayName("Match surface greater than or equal to smaller surface")
    public void matchGreaterSurfaceRequestWithSurfaceGreaterOrEquals() {
        final Rule surfaceGreaterEqualRule = new Rule();
        surfaceGreaterEqualRule.setRule("surface >= 4800000");
        surfaceGreaterEqualRule.setActive(true);
        surfaceGreaterEqualRule.setPosition(1);

        boolean isMatch = this.testMatcher.isRuleMatching(surfaceGreaterEqualRule);

        assertTrue(isMatch);
    }



    @Test
    @DisplayName("Match surface greater than or equal to equal surface")
    public void matchEqualSurfaceRequestWithSurfaceGreaterOrEquals() {
        final Rule surfaceGreaterEqual2Rule = new Rule();
        surfaceGreaterEqual2Rule.setRule("surface >= 4817500");
        surfaceGreaterEqual2Rule.setActive(true);
        surfaceGreaterEqual2Rule.setPosition(1);

        boolean isMatch = this.testMatcher.isRuleMatching(surfaceGreaterEqual2Rule);

        assertTrue(isMatch);
    }



    @Test
    @DisplayName("Match surface greater than or equal to greater surface")
    public void matchSmallerSurfaceRequestWithSurfaceGreaterOrEquals() {
        final Rule surfaceGreaterEqualRule = new Rule();
        surfaceGreaterEqualRule.setRule("surface >= 4900000");
        surfaceGreaterEqualRule.setActive(true);
        surfaceGreaterEqualRule.setPosition(1);

        boolean isMatch = this.testMatcher.isRuleMatching(surfaceGreaterEqualRule);

        assertFalse(isMatch);
    }



    @Test
    @DisplayName("Match surface smaller than or equal to greater surface")
    public void matchSmallerSurfaceRequestWithSurfaceSmallerOrEquals() {
        final Rule surfaceLessEqualRule = new Rule();
        surfaceLessEqualRule.setRule("surface <= 4900000");
        surfaceLessEqualRule.setActive(true);
        surfaceLessEqualRule.setPosition(1);

        boolean isMatch = this.testMatcher.isRuleMatching(surfaceLessEqualRule);

        assertTrue(isMatch);
    }



    @Test
    @DisplayName("Match surface smaller than or equal to equal surface")
    public void matchEqualSurfaceRequestWithSurfaceSmallerOrEqual() {
        final Rule surfaceLessEqual2Rule = new Rule();
        surfaceLessEqual2Rule.setRule("surface <= 4817500");
        surfaceLessEqual2Rule.setActive(true);
        surfaceLessEqual2Rule.setPosition(1);

        boolean isMatch = this.testMatcher.isRuleMatching(surfaceLessEqual2Rule);

        assertTrue(isMatch);
    }



    @Test
    @DisplayName("Match surface smaller than or equal to smaller surface")
    public void matchGreaterSurfaceRequestWithSurfaceSmallerOrEqual() {
        final Rule surfaceLessEqual2Rule = new Rule();
        surfaceLessEqual2Rule.setRule("surface <= 4800000");
        surfaceLessEqual2Rule.setActive(true);
        surfaceLessEqual2Rule.setPosition(1);

        boolean isMatch = this.testMatcher.isRuleMatching(surfaceLessEqual2Rule);

        assertFalse(isMatch);
    }



    @Test
    @DisplayName("Match third-party name")
    public void matchRequestWithThirdPartyName() {
        final Rule thirdPartyNameEqualsRule = new Rule();
        thirdPartyNameEqualsRule.setRule("tiers == \"Bex\"");
        thirdPartyNameEqualsRule.setActive(true);
        thirdPartyNameEqualsRule.setPosition(1);

        boolean isMatch = this.testMatcher.isRuleMatching(thirdPartyNameEqualsRule);

        assertTrue(isMatch);
    }



    @Test
    @DisplayName("Match not equals third-party name")
    public void matchRequestWithNotEqualsThirdPartyName() {
        final Rule thirdPartyNameDifferentRule = new Rule();
        thirdPartyNameDifferentRule.setRule("tiers != \"Bex\"");
        thirdPartyNameDifferentRule.setActive(true);
        thirdPartyNameDifferentRule.setPosition(1);

        boolean isMatch = this.testMatcher.isRuleMatching(thirdPartyNameDifferentRule);

        assertFalse(isMatch);
    }



    @Test
    @DisplayName("Match third-party name with request without a third party")
    public void matchNoThirdPartyRequestWithThirdPartyName() {
        final Rule thirdPartyNameEqualsRule = new Rule();
        thirdPartyNameEqualsRule.setRule("tiers == \"Bex\"");
        thirdPartyNameEqualsRule.setActive(true);
        thirdPartyNameEqualsRule.setPosition(1);

        boolean isMatch = this.testMatcher.isRuleMatching(thirdPartyNameEqualsRule);

        assertTrue(isMatch);
    }



//    @Test
//    @DisplayName("Match not equals third-party name with request without a third-party")
//    public void matchNoThirdPartyRequestWithNotEqualsThirdPartyName() {
//        final Rule thirdPartyNameDifferentRule = new Rule();
//        thirdPartyNameDifferentRule.setRule("tiers != \"Bex\"");
//        thirdPartyNameDifferentRule.setActive(true);
//        thirdPartyNameDifferentRule.setPosition(2);
//
//        boolean isMatch = this.testMatcherWithNoThirdParty.isRuleMatching(thirdPartyNameDifferentRule);
//
//        //TODO Determine whether the following test should return false (current result) or true
//        assertTrue(isMatch);
//    }



    @Test
    @DisplayName("Match third-party GUID")
    public void matchRequestWithThirdPartyGuid() {
        final Rule thirdPartyGuidEqualsRule = new Rule();
        thirdPartyGuidEqualsRule.setRule("tiersguid == \"0b2496c1-68a4-4bb6-a6c0-bb9b6771ff84\"");
        thirdPartyGuidEqualsRule.setActive(true);
        thirdPartyGuidEqualsRule.setPosition(1);

        boolean isMatch = this.testMatcher.isRuleMatching(thirdPartyGuidEqualsRule);

        assertTrue(isMatch);
    }



    @Test
    @DisplayName("Match not equals third-party GUID")
    public void matchRequestWithNotEqualsThirdPartyGuid() {
        final Rule thirdPartyGuidDifferentRule = new Rule();
        thirdPartyGuidDifferentRule.setRule("tiersguid != \"0b2496c1-68a4-4bb6-a6c0-bb9b6771ff84\"");
        thirdPartyGuidDifferentRule.setActive(true);
        thirdPartyGuidDifferentRule.setPosition(1);

        boolean isMatch = this.testMatcher.isRuleMatching(thirdPartyGuidDifferentRule);

        assertFalse(isMatch);
    }



    @Test
    @DisplayName("Match third-party GUID for request without a third party")
    public void matchNoThirdPartyRequestWithThirdPartyGuid() {
        final Rule thirdPartyGuidEqualsRule = new Rule();
        thirdPartyGuidEqualsRule.setRule("tiersguid == \"0b2496c1-68a4-4bb6-a6c0-bb9b6771ff84\"");
        thirdPartyGuidEqualsRule.setActive(true);
        thirdPartyGuidEqualsRule.setPosition(1);

        boolean isMatch = this.testMatcherWithNoThirdParty.isRuleMatching(thirdPartyGuidEqualsRule);

        assertFalse(isMatch);
    }



//    @Test
//    @DisplayName("Match not equals third-party GUID for a request without a third-party")
//    public void matchNoThirdPartyRequestWithNotEqualsThirdPartyGuid() {
//        final Rule thirdPartyGuidDifferentRule = new Rule();
//        thirdPartyGuidDifferentRule.setRule("tiersguid != \"0b2496c1-68a4-4bb6-a6c0-bb9b6771ff84\"");
//        thirdPartyGuidDifferentRule.setActive(true);
//        thirdPartyGuidDifferentRule.setPosition(1);
//
//        boolean isMatch = this.testMatcherWithNoThirdParty.isRuleMatching(thirdPartyGuidDifferentRule);
//
//        //TODO Determine whether the following test should return false (current result) or true
//        assertTrue(isMatch);
//    }



    @Test
    @DisplayName("Match forbidden request property")
    public void matchRequestWithForbiddenProperty() {
        final Rule forbiddenPropertyEqualsRule = new Rule();
        forbiddenPropertyEqualsRule.setRule("clientdetails != \"0b2496c1-68a4-4bb6-a6c0-bb9b6771ff84\"");
        forbiddenPropertyEqualsRule.setActive(true);
        forbiddenPropertyEqualsRule.setPosition(1);

        boolean isMatch = this.testMatcher.isRuleMatching(forbiddenPropertyEqualsRule);

        assertFalse(isMatch);
    }



    @Test
    @DisplayName("Match with AND operator and matching criteria")
    public void matchRequestWithAndRule() {
        final Rule andMatchRule = new Rule();
        andMatchRule.setRule("productlabel == \"Plan réseaux de démonstration 2\" AND surface > 4800000");
        andMatchRule.setActive(true);
        andMatchRule.setPosition(1);

        boolean isMatch = this.testMatcher.isRuleMatching(andMatchRule);

        assertTrue(isMatch);
    }



    @Test
    @DisplayName("Match with AND operator and one unmatching criterion")
    public void matchRequestWithAndRuleWithUnmatchingCriterion() {
        final Rule andNotMatchRule = new Rule();
        andNotMatchRule.setRule("productlabel != \"Plan réseaux de démonstration 2\" AND surface < 4800000");
        andNotMatchRule.setActive(true);
        andNotMatchRule.setPosition(1);

        boolean isMatch = this.testMatcher.isRuleMatching(andNotMatchRule);

        assertFalse(isMatch);
    }



    @Test
    @DisplayName("Match with AND operator and a geographic criterion")
    public void matchRequestWithAndRuleWithGeographicCriterion() {
        final Rule andGeographicMatchRule = new Rule();
        andGeographicMatchRule.setRule("productlabel == \"Plan réseaux de démonstration 2\" AND perimeter intersects POLYGON((6.556259943632146 46.541251354110784,6.563488415498275 46.51748692571157,6.598833848833761 46.52021418429415,6.5999798563838175 46.535774963981254,6.598722604656531 46.550268804975566,6.5660446421184915 46.547154743818105,6.556259943632146 46.541251354110784))");
        andGeographicMatchRule.setActive(true);
        andGeographicMatchRule.setPosition(1);

        boolean isMatch = this.testMatcher.isRuleMatching(andGeographicMatchRule);

        assertTrue(isMatch);
    }



    @Test
    @DisplayName("Match with OR operator and two matching criteria")
    public void matchRequestWithOrRule() {
        final Rule orMatchAllRule = new Rule();
        orMatchAllRule.setRule("productlabel == \"Plan réseaux de démonstration 2\" OR surface > 4800000");
        orMatchAllRule.setActive(true);
        orMatchAllRule.setPosition(1);

        boolean isMatch = this.testMatcher.isRuleMatching(orMatchAllRule);

        assertTrue(isMatch);
    }



    @Test
    @DisplayName("Match with OR operator and one matching criterion")
    public void matchRequestWithOrRuleAndOneMatchingCriterion() {
        final Rule orMatchOneRule = new Rule();
        orMatchOneRule.setRule("productlabel == \"Plan réseaux de démonstration 3\" OR surface > 4800000");
        orMatchOneRule.setActive(true);
        orMatchOneRule.setPosition(1);

        boolean isMatch = this.testMatcher.isRuleMatching(orMatchOneRule);

        assertTrue(isMatch);
    }



    @Test
    @DisplayName("Match with OR operator and no matching criterion")
    public void matchRequestWithOrRuleAndNotMatchingCriterion() {
        final Rule orNotMatchRule = new Rule();
        orNotMatchRule.setRule("productlabel == \"Plan réseaux de démonstration 3\" OR surface < 4800000");
        orNotMatchRule.setActive(true);
        orNotMatchRule.setPosition(1);

        boolean isMatch = this.testMatcher.isRuleMatching(orNotMatchRule);

        assertFalse(isMatch);
    }



    @Test
    @DisplayName("Match with OR operator and geographic criterion")
    public void matchRequestWithOrRuleAndGeographicCriterion() {
        final Rule orGeographicMatchRule = new Rule();
        orGeographicMatchRule.setRule("productlabel == \"Plan réseaux de démonstration 3\" OR perimeter intersects POLYGON((6.556259943632146 46.541251354110784,6.563488415498275 46.51748692571157,6.598833848833761 46.52021418429415,6.5999798563838175 46.535774963981254,6.598722604656531 46.550268804975566,6.5660446421184915 46.547154743818105,6.556259943632146 46.541251354110784))");
        orGeographicMatchRule.setActive(true);
        orGeographicMatchRule.setPosition(1);

        boolean isMatch = this.testMatcher.isRuleMatching(orGeographicMatchRule);

        assertTrue(isMatch);
    }



    @Test
    @DisplayName("Match IN with carriage returns")
    public void matchRequestWithRuleInWithCarriageReturns() {

        final Rule carriageReturnsInListRule = new Rule();
        carriageReturnsInListRule.setRule("""
                                          productguid IN (\r
                                          "522917a2-526a-4857-bcf7-2a7ef07a3097",\r
                                          "71bf9e39-646d-4da6-a832-c4b3eafd4150",\r
                                          "5b5d2798-6154-42ea-8b25-4a8c0e9fe6e9"\r
                                          )"""
        );
        carriageReturnsInListRule.setActive(true);
        carriageReturnsInListRule.setPosition(1);

        boolean isMatch = this.testMatcher.isRuleMatching(carriageReturnsInListRule);

        assertTrue(isMatch);
    }



    @Test
    @DisplayName("Match NOT IN with carriage returns")
    public void matchRequestWithRuleNotInWithCarriageReturns() {

        final Rule carriageReturnsNotInListRule = new Rule();
        carriageReturnsNotInListRule.setRule("""
                                                     productguid NOT IN (\r
                                                     "522917a2-526a-4857-bcf7-2a7ef07a3097",\r
                                                     "71bf9e39-646d-4da6-a832-c4b3eafd4150",\r
                                                     "5b5d2798-6154-42ea-8b25-4a8c0e9fe6e9"\r
                                                     )""");
        carriageReturnsNotInListRule.setActive(true);
        carriageReturnsNotInListRule.setPosition(1);

        boolean isMatch = this.testMatcher.isRuleMatching(carriageReturnsNotInListRule);

        assertFalse(isMatch);
    }



    private Request createRequest(boolean includeThirdParty) {
        Request testRequest = new Request();
        testRequest.setClient("Yves Grasset");
        testRequest.setClientGuid("17915324-ea6c-434f-bc1b-1e44cc3dee96");
        testRequest.setOrderGuid("dc8d3730-1e95-4294-b21c-823925710fb1");
        testRequest.setOrderLabel("426524");
        testRequest.setOrganism("ASIT - Association pour le système d'information du territoire");
        testRequest.setOrganismGuid("a35f0327-bceb-43a1-b366-96c3a94bc47b");
        testRequest.setParameters("{\"FORMAT\":\"DXF\",\"PROJECTION\":\"SWITZERLAND95\",\"REMARK\":\"\"}");
        testRequest.setPerimeter("POLYGON((6.556259943632146 46.541251354110784,6.550785473603749 46.51736879969809,6.585272600054474 46.520332304121965,6.575204996013936 46.53869822097311,6.556259943632146 46.541251354110784))");
        testRequest.setProductGuid("71bf9e39-646d-4da6-a832-c4b3eafd4150");
        testRequest.setProductLabel("Plan réseaux de démonstration 2");
        testRequest.setSurface(4817500d);

        if (includeThirdParty) {
            testRequest.setTiers("Bex");
            testRequest.setTiersGuid("0b2496c1-68a4-4bb6-a6c0-bb9b6771ff84");
        }

        //testRequest.setClientDetails("av. de la Praille 45\n1227 Carouge\n+41 22 344 45 10\nygr@arxit.com");
        testRequest.setClientDetails("ygr@arxit.com");

        return testRequest;
    }
}
