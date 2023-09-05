package ch.asit_asso.extract.requestmatching;

import ch.asit_asso.extract.domain.Request;
import ch.asit_asso.extract.domain.Rule;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Tag;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

@Tag("unit")
public class RequestMatcherTest {

    RequestMatcher testMatcher;

    RequestMatcher testMatcherWithNoThirdParty;

    final List<Rule> testRules = new ArrayList<>();


    @Before
    public void setUp() {
        final Request testRequest = new Request();
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
        testRequest.setTiers("Bex");
        testRequest.setTiersGuid("0b2496c1-68a4-4bb6-a6c0-bb9b6771ff84");
        //testRequest.setClientDetails("av. de la Praille 45\n1227 Carouge\n+41 22 344 45 10\nygr@arxit.com");
        testRequest.setClientDetails("ygr@arxit.com");
        this.testMatcher = new RequestMatcher(testRequest);

        final Request testRequestWithNoThirdParty = new Request();
        testRequestWithNoThirdParty.setClient("Yves Grasset");
        testRequestWithNoThirdParty.setClientGuid("17915324-ea6c-434f-bc1b-1e44cc3dee96");
        testRequestWithNoThirdParty.setOrderGuid("dc8d3730-1e95-4294-b21c-823925710fb1");
        testRequestWithNoThirdParty.setOrderLabel("426524");
        testRequestWithNoThirdParty.setOrganism("ASIT - Association pour le système d'information du territoire");
        testRequestWithNoThirdParty.setOrganismGuid("a35f0327-bceb-43a1-b366-96c3a94bc47b");
        testRequestWithNoThirdParty.setParameters("{\"FORMAT\":\"DXF\",\"PROJECTION\":\"SWITZERLAND95\",\"REMARK\":\"\"}");
        testRequestWithNoThirdParty.setPerimeter("POLYGON((6.556259943632146 46.541251354110784,6.550785473603749 46.51736879969809,6.585272600054474 46.520332304121965,6.575204996013936 46.53869822097311,6.556259943632146 46.541251354110784))");
        testRequestWithNoThirdParty.setProductGuid("71bf9e39-646d-4da6-a832-c4b3eafd4150");
        testRequestWithNoThirdParty.setProductLabel("Plan réseaux de démonstration 2");
        testRequestWithNoThirdParty.setSurface(4817500d);
        //testRequest.setClientDetails("av. de la Praille 45\n1227 Carouge\n+41 22 344 45 10\nygr@arxit.com");
        testRequest.setClientDetails("ygr@arxit.com");
        this.testMatcherWithNoThirdParty = new RequestMatcher(testRequestWithNoThirdParty);
        
        this.testRules.clear();
    }

    @Test
    public void matchRequestWithEmptyRule() {
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

        assertNull(this.testMatcher.matchRequestWithRules(this.testRules));
    }

    @Test
    public void matchRequestWithInactiveRule() {
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

        assertSame(activeRuleThatMatches, this.testMatcher.matchRequestWithRules(this.testRules));
    }

    @Test
    public void matchRequestWithAllInactiveRules() {
        final Rule inactiveRule1 = new Rule();
        inactiveRule1.setRule("orderLabel == \"426524\"");
        inactiveRule1.setActive(false);
        inactiveRule1.setPosition(1);
        this.testRules.add(inactiveRule1);

        final Rule inactiveRule2 = new Rule();
        inactiveRule2.setRule("tiers == \"Bex\"");
        inactiveRule2.setActive(false);
        inactiveRule2.setPosition(2);
        this.testRules.add(inactiveRule2);

        assertNull(this.testMatcher.matchRequestWithRules(this.testRules));
    }

    @Test
    public void matchRequestWithCustomerName() {
        final Rule customerEqualsRule = new Rule();
        customerEqualsRule.setRule("client == \"Yves Grasset\"");
        customerEqualsRule.setActive(true);
        customerEqualsRule.setPosition(1);
        assertTrue(this.testMatcher.isRuleMatching(customerEqualsRule));

        final Rule customerDifferentRule = new Rule();
        customerDifferentRule.setRule("client != \"Yves Grasset\"");
        customerDifferentRule.setActive(true);
        customerDifferentRule.setPosition(2);
        assertFalse(this.testMatcher.isRuleMatching(customerDifferentRule));

        final Rule customerInListRule = new Rule();
        customerInListRule.setRule("client IN (\"Julien Longchamp\", \"Yves Grasset\", \"Florent Krin\")");
        customerInListRule.setActive(true);
        customerInListRule.setPosition(3);
        assertTrue(this.testMatcher.isRuleMatching(customerInListRule));

        final Rule customerNotInListRule = new Rule();
        customerNotInListRule.setRule("client NOT IN (\"Yves Blatti\", \"Hans Mustermann\", \"Maria Bernasconi\")");
        customerNotInListRule.setActive(true);
        customerNotInListRule.setPosition(3);
        assertTrue(this.testMatcher.isRuleMatching(customerNotInListRule));
    }

    @Test
    public void matchRequestWithCustomerGuid() {
        final Rule customerGuidEqualsRule = new Rule();
        customerGuidEqualsRule.setRule("clientguid == \"17915324-ea6c-434f-bc1b-1e44cc3dee96\"");
        customerGuidEqualsRule.setActive(true);
        customerGuidEqualsRule.setPosition(1);
        assertTrue(this.testMatcher.isRuleMatching(customerGuidEqualsRule));

        final Rule customerGuidDifferentRule = new Rule();
        customerGuidDifferentRule.setRule("clientguid != \"17915324-ea6c-434f-bc1b-1e44cc3dee96\"");
        customerGuidDifferentRule.setActive(true);
        customerGuidDifferentRule.setPosition(2);
        assertFalse(this.testMatcher.isRuleMatching(customerGuidDifferentRule));
    }

    @Test
    public void matchRequestWithOrderGuid() {
        final Rule orderGuidEqualsRule = new Rule();
        orderGuidEqualsRule.setRule("orderguid == \"dc8d3730-1e95-4294-b21c-823925710fb1\"");
        orderGuidEqualsRule.setActive(true);
        orderGuidEqualsRule.setPosition(1);
        assertTrue(this.testMatcher.isRuleMatching(orderGuidEqualsRule));

        final Rule orderGuidDifferentRule = new Rule();
        orderGuidDifferentRule.setRule("orderguid != \"dc8d3730-1e95-4294-b21c-823925710fb1\"");
        orderGuidDifferentRule.setActive(true);
        orderGuidDifferentRule.setPosition(2);
        assertFalse(this.testMatcher.isRuleMatching(orderGuidDifferentRule));
    }

    @Test
    public void matchRequestWithOrderLabel() {
        final Rule orderLabelEqualsRule = new Rule();
        orderLabelEqualsRule.setRule("orderlabel == \"426524\"");
        orderLabelEqualsRule.setActive(true);
        orderLabelEqualsRule.setPosition(1);
        assertTrue(this.testMatcher.isRuleMatching(orderLabelEqualsRule));

        final Rule orderLabelDifferentRule = new Rule();
        orderLabelDifferentRule.setRule("orderlabel != \"426524\"");
        orderLabelDifferentRule.setActive(true);
        orderLabelDifferentRule.setPosition(2);
        assertFalse(this.testMatcher.isRuleMatching(orderLabelDifferentRule));
    }

    @Test
    public void matchRequestWithOrganismName() {
        final Rule organismNameEqualsRule = new Rule();
        organismNameEqualsRule.setRule("organism == \"ASIT - Association pour le système d'information du territoire\"");
        organismNameEqualsRule.setActive(true);
        organismNameEqualsRule.setPosition(1);
        assertTrue(this.testMatcher.isRuleMatching(organismNameEqualsRule));

        final Rule organismNameDifferentRule = new Rule();
        organismNameDifferentRule.setRule("organism != \"ASIT - Association pour le système d'information du territoire\"");
        organismNameDifferentRule.setActive(true);
        organismNameDifferentRule.setPosition(2);
        assertFalse(this.testMatcher.isRuleMatching(organismNameDifferentRule));
    }

    @Test
    public void matchRequestWithOrganismGuid() {
        final Rule organismGuidEqualsRule = new Rule();
        organismGuidEqualsRule.setRule("organismguid == \"a35f0327-bceb-43a1-b366-96c3a94bc47b\"");
        organismGuidEqualsRule.setActive(true);
        organismGuidEqualsRule.setPosition(1);
        assertTrue(this.testMatcher.isRuleMatching(organismGuidEqualsRule));

        final Rule organismGuidDifferentRule = new Rule();
        organismGuidDifferentRule.setRule("organismguid != \"a35f0327-bceb-43a1-b366-96c3a94bc47b\"");
        organismGuidDifferentRule.setActive(true);
        organismGuidDifferentRule.setPosition(2);
        assertFalse(this.testMatcher.isRuleMatching(organismGuidDifferentRule));
    }

    @Test
    public void matchRequestWithParameters() {
        final Rule parameterFormatEqualsRule = new Rule();
        parameterFormatEqualsRule.setRule("parameters.FORMAT == \"DXF\"");
        parameterFormatEqualsRule.setActive(true);
        parameterFormatEqualsRule.setPosition(1);
        assertTrue(this.testMatcher.isRuleMatching(parameterFormatEqualsRule));

        final Rule parameterFormatDifferentRule = new Rule();
        parameterFormatDifferentRule.setRule("parameters.FORMAT == \"SHP\"");
        parameterFormatDifferentRule.setActive(true);
        parameterFormatDifferentRule.setPosition(2);
        assertFalse(this.testMatcher.isRuleMatching(parameterFormatDifferentRule));

        final Rule absentParameterEqualsRule = new Rule();
        absentParameterEqualsRule.setRule("parameters.DUMMY_PARAM == \"Whatever\"");
        absentParameterEqualsRule.setActive(true);
        absentParameterEqualsRule.setPosition(3);
        assertFalse(this.testMatcher.isRuleMatching(absentParameterEqualsRule));

        final Rule absentParameterDifferentRule = new Rule();
        absentParameterDifferentRule.setRule("parameters.DUMMY_PARAM != \"Whatever\"");
        absentParameterDifferentRule.setActive(true);
        absentParameterDifferentRule.setPosition(4);
        assertTrue(this.testMatcher.isRuleMatching(absentParameterDifferentRule));
    }

    @Test
    public void matchRequestWithPerimeter() {
        final Rule perimeterIntersectsRule = new Rule();
        perimeterIntersectsRule.setRule("perimeter intersects POLYGON((6.556259943632146 46.541251354110784,6.563488415498275 46.51748692571157,6.598833848833761 46.52021418429415,6.5999798563838175 46.535774963981254,6.598722604656531 46.550268804975566,6.5660446421184915 46.547154743818105,6.556259943632146 46.541251354110784))");
        perimeterIntersectsRule.setActive(true);
        perimeterIntersectsRule.setPosition(1);
        assertTrue(this.testMatcher.isRuleMatching(perimeterIntersectsRule));

        final Rule perimeterDisjointRule = new Rule();
        perimeterDisjointRule.setRule("perimeter disjoint POLYGON((6.556259943632146 46.541251354110784,6.563488415498275 46.51748692571157,6.598833848833761 46.52021418429415,6.5999798563838175 46.535774963981254,6.598722604656531 46.550268804975566,6.5660446421184915 46.547154743818105,6.556259943632146 46.541251354110784))");
        perimeterDisjointRule.setActive(true);
        perimeterDisjointRule.setPosition(2);
        assertFalse(this.testMatcher.isRuleMatching(perimeterDisjointRule));

        final Rule perimeterContainsRule = new Rule();
        perimeterContainsRule.setRule("perimeter contains POLYGON((6.555916620878257 46.531568405285945,6.556106976289291 46.527881009306,6.569651414751729 46.52800954211501,6.567136911297156 46.53196725716802,6.563126398710288 46.5312141183206,6.555916620878257 46.531568405285945))");
        perimeterContainsRule.setActive(true);
        perimeterContainsRule.setPosition(3);
        assertTrue(this.testMatcher.isRuleMatching(perimeterContainsRule));

        final Rule perimeterEqualsRule = new Rule();
        perimeterEqualsRule.setRule("perimeter equals POLYGON((6.556259943632146 46.541251354110784,6.550785473603749 46.51736879969809,6.585272600054474 46.520332304121965,6.575204996013936 46.53869822097311,6.556259943632146 46.541251354110784))");
        perimeterEqualsRule.setActive(true);
        perimeterEqualsRule.setPosition(4);
        assertTrue(this.testMatcher.isRuleMatching(perimeterEqualsRule));

        final Rule perimeterWithinRule = new Rule();
        perimeterWithinRule.setRule("perimeter within POLYGON((6.555916620878257 46.531568405285945,6.556106976289291 46.527881009306,6.569651414751729 46.52800954211501,6.567136911297156 46.53196725716802,6.563126398710288 46.5312141183206,6.555916620878257 46.531568405285945))");
        perimeterWithinRule.setActive(true);
        perimeterWithinRule.setPosition(5);
        assertFalse(this.testMatcher.isRuleMatching(perimeterWithinRule));
    }

    @Test
    public void matchRequestWithProductGuid() {
        final Rule productGuidEqualsRule = new Rule();
        productGuidEqualsRule.setRule("productguid == \"71bf9e39-646d-4da6-a832-c4b3eafd4150\"");
        productGuidEqualsRule.setActive(true);
        productGuidEqualsRule.setPosition(1);
        assertTrue(this.testMatcher.isRuleMatching(productGuidEqualsRule));

        final Rule productGuidDifferentRule = new Rule();
        productGuidDifferentRule.setRule("productguid != \"71bf9e39-646d-4da6-a832-c4b3eafd4150\"");
        productGuidDifferentRule.setActive(true);
        productGuidDifferentRule.setPosition(2);
        assertFalse(this.testMatcher.isRuleMatching(productGuidDifferentRule));
    }

    @Test
    public void matchRequestWithProductLabel() {
        final Rule productLabelEqualsRule = new Rule();
        productLabelEqualsRule.setRule("productlabel == \"Plan réseaux de démonstration 2\"");
        productLabelEqualsRule.setActive(true);
        productLabelEqualsRule.setPosition(1);
        assertTrue(this.testMatcher.isRuleMatching(productLabelEqualsRule));

        final Rule productLabelDifferentRule = new Rule();
        productLabelDifferentRule.setRule("productlabel != \"Plan réseaux de démonstration 2\"");
        productLabelDifferentRule.setActive(true);
        productLabelDifferentRule.setPosition(2);
        assertFalse(this.testMatcher.isRuleMatching(productLabelDifferentRule));
    }

    @Test
    public void matchRequestWithSurface() {
        final Rule surfaceEqualsRule = new Rule();
        surfaceEqualsRule.setRule("surface == 4817500");
        surfaceEqualsRule.setActive(true);
        surfaceEqualsRule.setPosition(1);
        assertTrue(this.testMatcher.isRuleMatching(surfaceEqualsRule));

        final Rule surfaceDifferentRule = new Rule();
        surfaceDifferentRule.setRule("surface != 4817500");
        surfaceDifferentRule.setActive(true);
        surfaceDifferentRule.setPosition(2);
        assertFalse(this.testMatcher.isRuleMatching(surfaceDifferentRule));

        final Rule surfaceGreaterRule = new Rule();
        surfaceGreaterRule.setRule("surface > 4800000");
        surfaceGreaterRule.setActive(true);
        surfaceGreaterRule.setPosition(3);
        assertTrue(this.testMatcher.isRuleMatching(surfaceGreaterRule));

        final Rule surfaceLessRule = new Rule();
        surfaceLessRule.setRule("surface < 4800000");
        surfaceLessRule.setActive(true);
        surfaceLessRule.setPosition(4);
        assertFalse(this.testMatcher.isRuleMatching(surfaceLessRule));

        final Rule surfaceGreaterEqualRule = new Rule();
        surfaceGreaterEqualRule.setRule("surface >= 4900000");
        surfaceGreaterEqualRule.setActive(true);
        surfaceGreaterEqualRule.setPosition(5);
        assertFalse(this.testMatcher.isRuleMatching(surfaceGreaterEqualRule));

        final Rule surfaceGreaterEqual2Rule = new Rule();
        surfaceGreaterEqual2Rule.setRule("surface >= 4817500");
        surfaceGreaterEqual2Rule.setActive(true);
        surfaceGreaterEqual2Rule.setPosition(6);
        assertTrue(this.testMatcher.isRuleMatching(surfaceGreaterEqual2Rule));

        final Rule surfaceLessEqualRule = new Rule();
        surfaceLessEqualRule.setRule("surface <= 4900000");
        surfaceLessEqualRule.setActive(true);
        surfaceLessEqualRule.setPosition(7);
        assertTrue(this.testMatcher.isRuleMatching(surfaceLessEqualRule));

        final Rule surfaceLessEqual2Rule = new Rule();
        surfaceLessEqual2Rule.setRule("surface <= 4817500");
        surfaceLessEqual2Rule.setActive(true);
        surfaceLessEqual2Rule.setPosition(8);
        assertTrue(this.testMatcher.isRuleMatching(surfaceLessEqual2Rule));
    }

    @Test
    public void matchRequestWithThirdPartyName() {
        final Rule thirdPartyNameEqualsRule = new Rule();
        thirdPartyNameEqualsRule.setRule("tiers == \"Bex\"");
        thirdPartyNameEqualsRule.setActive(true);
        thirdPartyNameEqualsRule.setPosition(1);
        assertTrue(this.testMatcher.isRuleMatching(thirdPartyNameEqualsRule));
        assertFalse(this.testMatcherWithNoThirdParty.isRuleMatching(thirdPartyNameEqualsRule));

        final Rule thirdPartyNameDifferentRule = new Rule();
        thirdPartyNameDifferentRule.setRule("tiers != \"Bex\"");
        thirdPartyNameDifferentRule.setActive(true);
        thirdPartyNameDifferentRule.setPosition(2);
        assertFalse(this.testMatcher.isRuleMatching(thirdPartyNameDifferentRule));
        //TODO Determine whether the following test should return false (current result) or true
        //assertTrue(this.testMatcherWithNoThirdParty.isRuleMatching(thirdPartyNameDifferentRule));
    }

    @Test
    public void matchRequestWithThirdPartyGuid() {
        final Rule thirdPartyGuidEqualsRule = new Rule();
        thirdPartyGuidEqualsRule.setRule("tiersguid == \"0b2496c1-68a4-4bb6-a6c0-bb9b6771ff84\"");
        thirdPartyGuidEqualsRule.setActive(true);
        thirdPartyGuidEqualsRule.setPosition(1);
        assertTrue(this.testMatcher.isRuleMatching(thirdPartyGuidEqualsRule));
        assertFalse(this.testMatcherWithNoThirdParty.isRuleMatching(thirdPartyGuidEqualsRule));

        final Rule thirdPartyGuidDifferentRule = new Rule();
        thirdPartyGuidDifferentRule.setRule("tiersguid != \"0b2496c1-68a4-4bb6-a6c0-bb9b6771ff84\"");
        thirdPartyGuidDifferentRule.setActive(true);
        thirdPartyGuidDifferentRule.setPosition(2);
        assertFalse(this.testMatcher.isRuleMatching(thirdPartyGuidDifferentRule));
        //TODO Determine whether the following test should return false (current result) or true
        //assertTrue(this.testMatcherWithNoThirdParty.isRuleMatching(thirdPartyGuidDifferentRule));
    }

    @Test
    public void matchRequestWithForbiddenProperty() {
        final Rule forbiddenPropertyEqualsRule = new Rule();
        forbiddenPropertyEqualsRule.setRule("clientdetails != \"0b2496c1-68a4-4bb6-a6c0-bb9b6771ff84\"");
        forbiddenPropertyEqualsRule.setActive(true);
        forbiddenPropertyEqualsRule.setPosition(1);
        assertFalse(this.testMatcher.isRuleMatching(forbiddenPropertyEqualsRule));
    }

    @Test
    public void matchRequestWithAndRule() {
        final Rule andMatchRule = new Rule();
        andMatchRule.setRule("productlabel == \"Plan réseaux de démonstration 2\" AND surface > 4800000");
        andMatchRule.setActive(true);
        andMatchRule.setPosition(1);
        assertTrue(this.testMatcher.isRuleMatching(andMatchRule));

        final Rule andNotMatchRule = new Rule();
        andNotMatchRule.setRule("productlabel != \"Plan réseaux de démonstration 2\" AND surface < 4800000");
        andNotMatchRule.setActive(true);
        andNotMatchRule.setPosition(2);
        assertFalse(this.testMatcher.isRuleMatching(andNotMatchRule));

        final Rule andGeographicMatchRule = new Rule();
        andGeographicMatchRule.setRule("productlabel == \"Plan réseaux de démonstration 2\" AND perimeter intersects POLYGON((6.556259943632146 46.541251354110784,6.563488415498275 46.51748692571157,6.598833848833761 46.52021418429415,6.5999798563838175 46.535774963981254,6.598722604656531 46.550268804975566,6.5660446421184915 46.547154743818105,6.556259943632146 46.541251354110784))");
        andGeographicMatchRule.setActive(true);
        andGeographicMatchRule.setPosition(3);
        assertTrue(this.testMatcher.isRuleMatching(andGeographicMatchRule));
    }

    @Test
    public void matchRequestWithOrRule() {
        final Rule orMatchAllRule = new Rule();
        orMatchAllRule.setRule("productlabel == \"Plan réseaux de démonstration 2\" OR surface > 4800000");
        orMatchAllRule.setActive(true);
        orMatchAllRule.setPosition(1);
        assertTrue(this.testMatcher.isRuleMatching(orMatchAllRule));

        final Rule orMatchOneRule = new Rule();
        orMatchOneRule.setRule("productlabel == \"Plan réseaux de démonstration 3\" OR surface > 4800000");
        orMatchOneRule.setActive(true);
        orMatchOneRule.setPosition(2);
        assertTrue(this.testMatcher.isRuleMatching(orMatchOneRule));

        final Rule orNotMatchRule = new Rule();
        orNotMatchRule.setRule("productlabel == \"Plan réseaux de démonstration 3\" OR surface < 4800000");
        orNotMatchRule.setActive(true);
        orNotMatchRule.setPosition(3);
        assertFalse(this.testMatcher.isRuleMatching(orNotMatchRule));

        final Rule orGeographicMatchRule = new Rule();
        orGeographicMatchRule.setRule("productlabel == \"Plan réseaux de démonstration 3\" OR perimeter intersects POLYGON((6.556259943632146 46.541251354110784,6.563488415498275 46.51748692571157,6.598833848833761 46.52021418429415,6.5999798563838175 46.535774963981254,6.598722604656531 46.550268804975566,6.5660446421184915 46.547154743818105,6.556259943632146 46.541251354110784))");
        orGeographicMatchRule.setActive(true);
        orGeographicMatchRule.setPosition(4);
        assertTrue(this.testMatcher.isRuleMatching(orGeographicMatchRule));
    }

    @Test
    public void matchRequestWithRuleWithCarriageReturns() {
        final Rule carriageReturnsInListRule = new Rule();
        carriageReturnsInListRule.setRule("productguid IN (\r\n" +
                "\"522917a2-526a-4857-bcf7-2a7ef07a3097\",\r\n" +
                "\"71bf9e39-646d-4da6-a832-c4b3eafd4150\",\r\n" +
                "\"5b5d2798-6154-42ea-8b25-4a8c0e9fe6e9\"\r\n" +
                ")");
        carriageReturnsInListRule.setActive(true);
        carriageReturnsInListRule.setPosition(1);
        assertTrue(this.testMatcher.isRuleMatching(carriageReturnsInListRule));

        final Rule carriageReturnsNotInListRule = new Rule();
        carriageReturnsNotInListRule.setRule("productguid NOT IN (\r\n" +
                "\"522917a2-526a-4857-bcf7-2a7ef07a3097\",\r\n" +
                "\"71bf9e39-646d-4da6-a832-c4b3eafd4150\",\r\n" +
                "\"5b5d2798-6154-42ea-8b25-4a8c0e9fe6e9\"\r\n" +
                ")");
        carriageReturnsNotInListRule.setActive(true);
        carriageReturnsNotInListRule.setPosition(2);
        assertFalse(this.testMatcher.isRuleMatching(carriageReturnsNotInListRule));
    }
}
