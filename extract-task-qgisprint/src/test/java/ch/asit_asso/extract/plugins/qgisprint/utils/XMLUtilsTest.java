package ch.asit_asso.extract.plugins.qgisprint.utils;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXParseException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

class XMLUtilsTest {
    @Test
    @DisplayName("Shows that XXE can be triggered with the default config")
    public void testXXEIsPossible() throws Exception
    {
        String data = """
            <!DOCTYPE root [
              <!ELEMENT root ANY >
              <!ENTITY xxe SYSTEM "file:///etc/passwd" >]>
            <root>&xxe;</root>
        """;
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(new InputSource(new StringReader(data)));

        String content = document.getDocumentElement().getTextContent();
        assertNotNull(content);
        assertTrue(StringUtils.containsIgnoreCase(content, "root"));
    }

    @Test()
    @DisplayName("Shows that XXE is mitigated")
    public void testXXEIsMitigated()
    {
        String data = """
            <!DOCTYPE root [
              <!ELEMENT root ANY >
              <!ENTITY xxe SYSTEM "file:///etc/passwd" >]>
            <root>&xxe;</root>
        """;
        Exception exception = assertThrows(SAXParseException.class, () -> {
            XMLUtils.parseSecure(data);
        });
        assertTrue(exception.getMessage().contains("DOCTYPE is disallowed when the feature"));
    }

    @Test
    public void testXMLBombIsPossible() throws Exception {
        String data = """
            <!DOCTYPE root [
              <!ENTITY a "a" >
              <!ENTITY b "&a;&a;&a;&a;&a;&a;&a;&a;&a;&a;" >
              <!ENTITY c "&b;&b;&b;&b;&b;&b;&b;&b;&b;&b;" >
              <!ENTITY d "&c;&c;&c;&c;&c;&c;&c;&c;&c;&c;" >
              <!ENTITY e "&d;&d;&d;&d;&d;&d;&d;&d;&d;&d;" >
            ]>
            <root>&e;</root>
        """;
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(new InputSource(new StringReader(data)));

        String content = document.getDocumentElement().getTextContent();
        assertNotNull(content);
        assertEquals(10000, content.length());
    }

    @Test
    public void testXMLBombIsMitigated() throws Exception {
        String data = """
            <!DOCTYPE root [
              <!ENTITY a "a" >
              <!ENTITY b "&a;&a;&a;&a;&a;&a;&a;&a;&a;&a;" >
              <!ENTITY c "&b;&b;&b;&b;&b;&b;&b;&b;&b;&b;" >
              <!ENTITY d "&c;&c;&c;&c;&c;&c;&c;&c;&c;&c;" >
              <!ENTITY e "&d;&d;&d;&d;&d;&d;&d;&d;&d;&d;" >
            ]>
            <root>&e;</root>
        """;
        Exception exception = assertThrows(SAXParseException.class, () -> {
            XMLUtils.parseSecure(data);
        });
        assertTrue(exception.getMessage().contains("DOCTYPE is disallowed when the feature"));
    }

    @Test
    @DisplayName("This test shows that DOCTYPE is not allowed")
    public void testDocTypeIsNotAllowed() throws Exception {
        String data = """
            <!DOCTYPE root>
            <root>
              <element>Test</element>
            </root>
        """;
        Exception exception = assertThrows(SAXParseException.class, () -> {
            XMLUtils.parseSecure(data);
        });
        assertTrue(exception.getMessage().contains("DOCTYPE is disallowed when the feature"));
    }

    @Test
    @DisplayName("This test should not throw any error")
    public void testXmlParsesCorrectly() throws Exception {
        String data = """
            <root>
              <element>Test</element>
            </root>
        """;
        assertDoesNotThrow(() -> {
            XMLUtils.parseSecure(data);
        });
    }

    @Test
    @DisplayName("Parse XML with attributes")
    public void testXmlWithAttributes() throws Exception {
        String data = """
            <root id="123" name="test">
              <element attr="value">Content</element>
            </root>
        """;
        Document doc = XMLUtils.parseSecure(data);
        assertNotNull(doc);
        assertEquals("root", doc.getDocumentElement().getTagName());
        assertEquals("123", doc.getDocumentElement().getAttribute("id"));
    }

    @Test
    @DisplayName("Parse XML with nested elements")
    public void testXmlWithNestedElements() throws Exception {
        String data = """
            <root>
              <level1>
                <level2>
                  <level3>Deep content</level3>
                </level2>
              </level1>
            </root>
        """;
        Document doc = XMLUtils.parseSecure(data);
        assertNotNull(doc);
        assertNotNull(doc.getDocumentElement());
    }

    @Test
    @DisplayName("Parse XML with multiple sibling elements")
    public void testXmlWithMultipleSiblings() throws Exception {
        String data = """
            <root>
              <item>First</item>
              <item>Second</item>
              <item>Third</item>
            </root>
        """;
        Document doc = XMLUtils.parseSecure(data);
        assertNotNull(doc);
        assertEquals(3, doc.getElementsByTagName("item").getLength());
    }

    @Test
    @DisplayName("Parse XML with CDATA section")
    public void testXmlWithCDATA() throws Exception {
        String data = """
            <root>
              <content><![CDATA[Some <special> & characters]]></content>
            </root>
        """;
        Document doc = XMLUtils.parseSecure(data);
        assertNotNull(doc);
    }

    @Test
    @DisplayName("Parse XML with comments")
    public void testXmlWithComments() throws Exception {
        String data = """
            <root>
              <!-- This is a comment -->
              <element>Content</element>
            </root>
        """;
        Document doc = XMLUtils.parseSecure(data);
        assertNotNull(doc);
    }

    @Test
    @DisplayName("Parse XML with special characters in text content")
    public void testXmlWithSpecialCharacters() throws Exception {
        String data = """
            <root>
              <element>Text with &lt; and &gt; and &amp;</element>
            </root>
        """;
        Document doc = XMLUtils.parseSecure(data);
        assertNotNull(doc);
        String content = doc.getElementsByTagName("element").item(0).getTextContent();
        assertTrue(content.contains("<"));
        assertTrue(content.contains(">"));
        assertTrue(content.contains("&"));
    }

    @Test
    @DisplayName("Parse XML with unicode characters")
    public void testXmlWithUnicodeCharacters() throws Exception {
        String data = """
            <root>
              <element>Texte en francais avec des accents</element>
              <element>Umlaute: ae oe ue</element>
            </root>
        """;
        Document doc = XMLUtils.parseSecure(data);
        assertNotNull(doc);
    }

    @Test
    @DisplayName("Parse empty root element")
    public void testEmptyRootElement() throws Exception {
        String data = "<root/>";
        Document doc = XMLUtils.parseSecure(data);
        assertNotNull(doc);
        assertEquals("root", doc.getDocumentElement().getTagName());
    }

    @Test
    @DisplayName("Parse XML with empty text content")
    public void testXmlWithEmptyTextContent() throws Exception {
        String data = """
            <root>
              <element></element>
            </root>
        """;
        Document doc = XMLUtils.parseSecure(data);
        assertNotNull(doc);
        String content = doc.getElementsByTagName("element").item(0).getTextContent();
        assertEquals("", content);
    }

    @Test
    @DisplayName("Parse XML with whitespace preservation")
    public void testXmlWithWhitespace() throws Exception {
        String data = """
            <root>
              <element>   spaced content   </element>
            </root>
        """;
        Document doc = XMLUtils.parseSecure(data);
        assertNotNull(doc);
    }

    @Test
    @DisplayName("Parse minimal XML document")
    public void testMinimalXmlDocument() throws Exception {
        String data = "<r/>";
        Document doc = XMLUtils.parseSecure(data);
        assertNotNull(doc);
        assertEquals("r", doc.getDocumentElement().getTagName());
    }

    @Test
    @DisplayName("Parse XML with numeric element names suffix")
    public void testXmlWithNumericSuffix() throws Exception {
        String data = """
            <root>
              <element1>One</element1>
              <element2>Two</element2>
            </root>
        """;
        Document doc = XMLUtils.parseSecure(data);
        assertNotNull(doc);
    }

    @Test
    @DisplayName("Invalid XML throws exception")
    public void testInvalidXmlThrowsException() {
        String data = "<root><unclosed>";
        assertThrows(Exception.class, () -> XMLUtils.parseSecure(data));
    }

    @Test
    @DisplayName("Malformed XML without closing tag throws exception")
    public void testMalformedXmlWithoutClosingTag() {
        String data = "<root>";
        assertThrows(Exception.class, () -> XMLUtils.parseSecure(data));
    }

    @Test
    @DisplayName("XML with mismatched tags throws exception")
    public void testXmlWithMismatchedTags() {
        String data = "<root><element></wrong></root>";
        assertThrows(Exception.class, () -> XMLUtils.parseSecure(data));
    }

    @Test
    @DisplayName("Empty string throws exception")
    public void testEmptyStringThrowsException() {
        assertThrows(Exception.class, () -> XMLUtils.parseSecure(""));
    }

    @Test
    @DisplayName("Whitespace only string throws exception")
    public void testWhitespaceOnlyThrowsException() {
        assertThrows(Exception.class, () -> XMLUtils.parseSecure("   "));
    }

    @Test
    @DisplayName("Parse WFS GetFeature response structure")
    public void testWfsGetFeatureResponseStructure() throws Exception {
        String data = """
            <wfs:FeatureCollection xmlns:wfs="http://www.opengis.net/wfs">
              <gml:featureMember xmlns:gml="http://www.opengis.net/gml">
                <feature gml:id="feature.1">
                  <name>Test Feature</name>
                </feature>
              </gml:featureMember>
            </wfs:FeatureCollection>
        """;
        Document doc = XMLUtils.parseSecure(data);
        assertNotNull(doc);
    }

    @Test
    @DisplayName("Parse WMS GetCapabilities response structure")
    public void testWmsCapabilitiesResponseStructure() throws Exception {
        String data = """
            <WMS_Capabilities version="1.3.0">
              <Service>
                <Name>WMS</Name>
                <Title>Test WMS</Title>
              </Service>
              <Capability>
                <Layer>
                  <Name>testlayer</Name>
                </Layer>
              </Capability>
            </WMS_Capabilities>
        """;
        Document doc = XMLUtils.parseSecure(data);
        assertNotNull(doc);
    }

    @Test
    @DisplayName("Parse exception report XML")
    public void testExceptionReportXml() throws Exception {
        String data = """
            <ServiceExceptionReport version="1.3.0">
              <ServiceException code="InvalidRequest">
                An error occurred
              </ServiceException>
            </ServiceExceptionReport>
        """;
        Document doc = XMLUtils.parseSecure(data);
        assertNotNull(doc);
    }
}