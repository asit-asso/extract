package ch.asit_asso.extract.unit.domain;

import ch.asit_asso.extract.domain.Remark;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Remark Entity Tests")
class RemarkTest {

    private Remark remark;

    @BeforeEach
    void setUp() {
        remark = new Remark();
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Default constructor creates instance with null id")
        void defaultConstructor_createsInstanceWithNullId() {
            Remark newRemark = new Remark();
            assertNull(newRemark.getId());
        }

        @Test
        @DisplayName("Constructor with id sets the id correctly")
        void constructorWithId_setsIdCorrectly() {
            Integer expectedId = 42;
            Remark newRemark = new Remark(expectedId);
            assertEquals(expectedId, newRemark.getId());
        }
    }

    @Nested
    @DisplayName("Getter and Setter Tests")
    class GetterSetterTests {

        @Test
        @DisplayName("setId and getId work correctly")
        void setAndGetId() {
            Integer expectedId = 100;
            remark.setId(expectedId);
            assertEquals(expectedId, remark.getId());
        }

        @Test
        @DisplayName("setTitle and getTitle work correctly")
        void setAndGetTitle() {
            String expectedTitle = "Validation Required";
            remark.setTitle(expectedTitle);
            assertEquals(expectedTitle, remark.getTitle());
        }

        @Test
        @DisplayName("setContent and getContent work correctly")
        void setAndGetContent() {
            String expectedContent = "This request requires additional validation before processing.";
            remark.setContent(expectedContent);
            assertEquals(expectedContent, remark.getContent());
        }
    }

    @Nested
    @DisplayName("Title Tests")
    class TitleTests {

        @Test
        @DisplayName("title can be set to null")
        void title_canBeSetToNull() {
            remark.setTitle(null);
            assertNull(remark.getTitle());
        }

        @Test
        @DisplayName("title can be set to empty string")
        void title_canBeSetToEmptyString() {
            remark.setTitle("");
            assertEquals("", remark.getTitle());
        }

        @Test
        @DisplayName("title can be set to long string")
        void title_canBeSetToLongString() {
            String longTitle = "A".repeat(255);
            remark.setTitle(longTitle);
            assertEquals(longTitle, remark.getTitle());
        }

        @Test
        @DisplayName("title can contain special characters")
        void title_canContainSpecialCharacters() {
            String specialTitle = "Remarque: Validation n\u00e9cessaire!";
            remark.setTitle(specialTitle);
            assertEquals(specialTitle, remark.getTitle());
        }
    }

    @Nested
    @DisplayName("Content Tests")
    class ContentTests {

        @Test
        @DisplayName("content can be set to null")
        void content_canBeSetToNull() {
            remark.setContent(null);
            assertNull(remark.getContent());
        }

        @Test
        @DisplayName("content can be set to empty string")
        void content_canBeSetToEmptyString() {
            remark.setContent("");
            assertEquals("", remark.getContent());
        }

        @Test
        @DisplayName("content can be set to long text")
        void content_canBeSetToLongText() {
            String longContent = "This is a very long remark content. ".repeat(100);
            remark.setContent(longContent);
            assertEquals(longContent, remark.getContent());
        }

        @Test
        @DisplayName("content can contain multiline text")
        void content_canContainMultilineText() {
            String multilineContent = "Line 1\nLine 2\nLine 3";
            remark.setContent(multilineContent);
            assertEquals(multilineContent, remark.getContent());
        }

        @Test
        @DisplayName("content can contain HTML")
        void content_canContainHtml() {
            String htmlContent = "<p>This is a <strong>remark</strong> with <em>HTML</em> content.</p>";
            remark.setContent(htmlContent);
            assertEquals(htmlContent, remark.getContent());
        }

        @Test
        @DisplayName("content can contain special characters")
        void content_canContainSpecialCharacters() {
            String specialContent = "Caract\u00e8res sp\u00e9ciaux: \u00e9\u00e8\u00e0\u00f9\u00e7\u00f6\u00fc\u00e4";
            remark.setContent(specialContent);
            assertEquals(specialContent, remark.getContent());
        }
    }

    @Nested
    @DisplayName("Complete Remark Configuration Tests")
    class CompleteConfigurationTests {

        @Test
        @DisplayName("fully configured remark has all attributes")
        void fullyConfiguredRemark_hasAllAttributes() {
            Integer id = 1;
            String title = "Validation Pending";
            String content = "The request is pending validation by an operator.";

            remark.setId(id);
            remark.setTitle(title);
            remark.setContent(content);

            assertEquals(id, remark.getId());
            assertEquals(title, remark.getTitle());
            assertEquals(content, remark.getContent());
        }

        @Test
        @DisplayName("remark can be updated")
        void remark_canBeUpdated() {
            remark.setId(1);
            remark.setTitle("Original Title");
            remark.setContent("Original Content");

            remark.setTitle("Updated Title");
            remark.setContent("Updated Content");

            assertEquals("Updated Title", remark.getTitle());
            assertEquals("Updated Content", remark.getContent());
        }
    }

    @Nested
    @DisplayName("Common Remark Scenarios Tests")
    class CommonScenariosTests {

        @Test
        @DisplayName("validation remark can be created")
        void validationRemark_canBeCreated() {
            remark.setId(1);
            remark.setTitle("Validation Required");
            remark.setContent("Please review the request parameters and validate.");

            assertNotNull(remark.getId());
            assertNotNull(remark.getTitle());
            assertNotNull(remark.getContent());
        }

        @Test
        @DisplayName("rejection remark can be created")
        void rejectionRemark_canBeCreated() {
            remark.setId(2);
            remark.setTitle("Request Rejected");
            remark.setContent("The request has been rejected due to invalid perimeter.");

            assertEquals("Request Rejected", remark.getTitle());
            assertTrue(remark.getContent().contains("rejected"));
        }

        @Test
        @DisplayName("information remark can be created")
        void informationRemark_canBeCreated() {
            remark.setId(3);
            remark.setTitle("Additional Information");
            remark.setContent("Please note that processing may take up to 5 business days.");

            assertEquals("Additional Information", remark.getTitle());
        }
    }

    @Nested
    @DisplayName("Id Tests")
    class IdTests {

        @Test
        @DisplayName("id can be set to zero")
        void id_canBeSetToZero() {
            remark.setId(0);
            assertEquals(0, remark.getId());
        }

        @Test
        @DisplayName("id can be set to negative value")
        void id_canBeSetToNegativeValue() {
            remark.setId(-1);
            assertEquals(-1, remark.getId());
        }

        @Test
        @DisplayName("id can be set to large value")
        void id_canBeSetToLargeValue() {
            remark.setId(Integer.MAX_VALUE);
            assertEquals(Integer.MAX_VALUE, remark.getId());
        }

        @Test
        @DisplayName("id can be replaced")
        void id_canBeReplaced() {
            remark.setId(1);
            assertEquals(1, remark.getId());

            remark.setId(2);
            assertEquals(2, remark.getId());
        }
    }
}
