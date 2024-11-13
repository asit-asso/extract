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
}