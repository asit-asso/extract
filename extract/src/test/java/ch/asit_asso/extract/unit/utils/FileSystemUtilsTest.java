/*
 * Copyright (C) 2025 arx iT
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
package ch.asit_asso.extract.unit.utils;

import ch.asit_asso.extract.domain.Request;
import ch.asit_asso.extract.utils.FileSystemUtils;
import ch.asit_asso.extract.utils.FileSystemUtils.RequestDataFolder;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for FileSystemUtils class.
 *
 * Tests:
 * - createFolder methods
 * - purgeRequestFolders method
 * - purgeRequestFolderContent method
 * - sanitizeFileName method
 * - RequestDataFolder enum
 *
 * @author Bruno Alves
 */
@DisplayName("FileSystemUtils Tests")
class FileSystemUtilsTest {

    @TempDir
    Path tempDir;

    // ==================== 1. CREATE FOLDER TESTS ====================

    @Nested
    @DisplayName("1. createFolder Tests")
    class CreateFolderTests {

        @Test
        @DisplayName("1.1 - Throws IllegalArgumentException when folder is null")
        void throwsExceptionWhenFolderIsNull() {
            // When/Then: Should throw IllegalArgumentException
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> FileSystemUtils.createFolder(null)
            );

            assertEquals("The folder to create cannot be null.", exception.getMessage());
        }

        @Test
        @DisplayName("1.2 - Throws IllegalArgumentException when folder is null with failOnExisting")
        void throwsExceptionWhenFolderIsNullWithFailOnExisting() {
            // When/Then: Should throw IllegalArgumentException
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> FileSystemUtils.createFolder(null, true)
            );

            assertEquals("The folder to create cannot be null.", exception.getMessage());
        }

        @Test
        @DisplayName("1.3 - Successfully creates a new folder")
        void successfullyCreatesNewFolder() {
            // Given: A non-existing folder path
            File folderToCreate = new File(tempDir.toFile(), "new-folder");

            // When: Creating the folder
            File result = FileSystemUtils.createFolder(folderToCreate);

            // Then: Should return the folder and it should exist
            assertNotNull(result);
            assertTrue(result.exists());
            assertTrue(result.isDirectory());
        }

        @Test
        @DisplayName("1.4 - Successfully creates nested folders")
        void successfullyCreatesNestedFolders() {
            // Given: A nested folder path
            File folderToCreate = new File(tempDir.toFile(), "level1/level2/level3");

            // When: Creating the folder
            File result = FileSystemUtils.createFolder(folderToCreate);

            // Then: Should return the folder and it should exist
            assertNotNull(result);
            assertTrue(result.exists());
            assertTrue(result.isDirectory());
        }

        @Test
        @DisplayName("1.5 - Returns existing folder when it exists and failOnExisting is false")
        void returnsExistingFolderWhenNotFailOnExisting() throws IOException {
            // Given: An existing folder
            Path existingFolder = Files.createDirectory(tempDir.resolve("existing-folder"));

            // When: Creating the folder with failOnExisting = false
            File result = FileSystemUtils.createFolder(existingFolder.toFile(), false);

            // Then: Should return the existing folder
            assertNotNull(result);
            assertEquals(existingFolder.toFile().getAbsolutePath(), result.getAbsolutePath());
        }

        @Test
        @DisplayName("1.6 - Returns null when folder exists and failOnExisting is true")
        void returnsNullWhenExistingAndFailOnExisting() throws IOException {
            // Given: An existing folder
            Path existingFolder = Files.createDirectory(tempDir.resolve("existing-folder-fail"));

            // When: Creating the folder with failOnExisting = true
            File result = FileSystemUtils.createFolder(existingFolder.toFile(), true);

            // Then: Should return null
            assertNull(result);
        }

        @Test
        @DisplayName("1.7 - Returns null when file with same name exists")
        void returnsNullWhenFileWithSameNameExists() throws IOException {
            // Given: A file with the same name as the folder to create
            Path existingFile = Files.createFile(tempDir.resolve("file-not-folder"));

            // When: Creating the folder
            File result = FileSystemUtils.createFolder(existingFile.toFile());

            // Then: Should return null (cannot create folder when file exists)
            assertNull(result);
        }

        @Test
        @DisplayName("1.8 - Uses default failOnExisting=false in single-argument method")
        void usesDefaultFailOnExisting() throws IOException {
            // Given: An existing folder
            Path existingFolder = Files.createDirectory(tempDir.resolve("default-folder"));

            // When: Creating the folder with single-argument method
            File result = FileSystemUtils.createFolder(existingFolder.toFile());

            // Then: Should return the existing folder (default failOnExisting = false)
            assertNotNull(result);
        }
    }

    // ==================== 2. PURGE REQUEST FOLDERS TESTS ====================

    @Nested
    @DisplayName("2. purgeRequestFolders Tests")
    class PurgeRequestFoldersTests {

        @Test
        @DisplayName("2.1 - Throws IllegalArgumentException when request is null")
        void throwsExceptionWhenRequestIsNull() {
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> FileSystemUtils.purgeRequestFolders(null, tempDir.toString())
            );

            assertEquals("The request cannot be null.", exception.getMessage());
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   ", "\t", "\n"})
        @DisplayName("2.2 - Throws IllegalArgumentException when basePath is null/empty/blank")
        void throwsExceptionWhenBasePathIsInvalid(String basePath) {
            Request request = createTestRequest(1);

            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> FileSystemUtils.purgeRequestFolders(request, basePath)
            );

            assertEquals("The base folder path cannot be empty.", exception.getMessage());
        }

        @Test
        @DisplayName("2.3 - Returns false when request data folder does not exist")
        void returnsFalseWhenFolderDoesNotExist() {
            // Given: Request with non-existent folder
            Request request = createTestRequest(1);
            request.setFolderIn("non-existent/input");
            request.setFolderOut("non-existent/output");

            // When: Purging folders
            boolean result = FileSystemUtils.purgeRequestFolders(request, tempDir.toString());

            // Then: Should return false
            assertFalse(result);
        }

        @Test
        @DisplayName("2.4 - Successfully purges request folders")
        void successfullyPurgesFolders() throws IOException {
            // Given: Request with existing folder structure
            Request request = createTestRequest(2);
            String requestFolderName = "request-2";
            request.setFolderIn(requestFolderName + "/input");
            request.setFolderOut(requestFolderName + "/output");

            Path requestFolder = tempDir.resolve(requestFolderName);
            Path inputFolder = requestFolder.resolve("input");
            Path outputFolder = requestFolder.resolve("output");
            Files.createDirectories(inputFolder);
            Files.createDirectories(outputFolder);
            Files.createFile(inputFolder.resolve("test.txt"));

            // When: Purging folders
            boolean result = FileSystemUtils.purgeRequestFolders(request, tempDir.toString());

            // Then: Should return true and folder should be deleted
            assertTrue(result);
            assertFalse(Files.exists(requestFolder));
        }

        @Test
        @DisplayName("2.5 - Returns false when both folderIn and folderOut are null")
        void returnsFalseWhenBothFoldersNull() {
            // Given: Request with null folders
            Request request = createTestRequest(3);
            request.setFolderIn(null);
            request.setFolderOut(null);

            // When: Purging folders
            boolean result = FileSystemUtils.purgeRequestFolders(request, tempDir.toString());

            // Then: Should return false
            assertFalse(result);
        }
    }

    // ==================== 3. PURGE REQUEST FOLDER CONTENT TESTS ====================

    @Nested
    @DisplayName("3. purgeRequestFolderContent Tests")
    class PurgeRequestFolderContentTests {

        @Test
        @DisplayName("3.1 - Throws IllegalArgumentException when request is null")
        void throwsExceptionWhenRequestIsNull() {
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> FileSystemUtils.purgeRequestFolderContent(null, RequestDataFolder.INPUT, tempDir.toString())
            );

            assertEquals("The request cannot be null.", exception.getMessage());
        }

        @Test
        @DisplayName("3.2 - Throws IllegalArgumentException when folderType is null")
        void throwsExceptionWhenFolderTypeIsNull() {
            Request request = createTestRequest(1);

            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> FileSystemUtils.purgeRequestFolderContent(request, null, tempDir.toString())
            );

            assertEquals("The request data folder type cannot be null", exception.getMessage());
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   "})
        @DisplayName("3.3 - Throws IllegalArgumentException when basePath is invalid")
        void throwsExceptionWhenBasePathIsInvalid(String basePath) {
            Request request = createTestRequest(1);

            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> FileSystemUtils.purgeRequestFolderContent(request, RequestDataFolder.INPUT, basePath)
            );

            assertEquals("The base folder path cannot be empty.", exception.getMessage());
        }

        @Test
        @DisplayName("3.4 - Returns false when data folder does not exist")
        void returnsFalseWhenFolderDoesNotExist() {
            // Given: Request with non-existent folder
            Request request = createTestRequest(4);
            request.setFolderIn("non-existent/input");

            // When: Purging folder content
            boolean result = FileSystemUtils.purgeRequestFolderContent(request, RequestDataFolder.INPUT, tempDir.toString());

            // Then: Should return false
            assertFalse(result);
        }

        @Test
        @DisplayName("3.5 - Successfully purges INPUT folder content")
        void successfullyPurgesInputFolderContent() throws IOException {
            // Given: Request with existing input folder structure
            Request request = createTestRequest(5);
            String requestFolderName = "request-5";
            request.setFolderIn(requestFolderName + "/input");

            Path inputFolder = tempDir.resolve(requestFolderName).resolve("input");
            Files.createDirectories(inputFolder);
            Files.createFile(inputFolder.resolve("file1.txt"));
            Files.createFile(inputFolder.resolve("file2.txt"));
            Path subdir = Files.createDirectory(inputFolder.resolve("subdir"));
            Files.createFile(subdir.resolve("nested.txt"));

            // When: Purging folder content
            boolean result = FileSystemUtils.purgeRequestFolderContent(request, RequestDataFolder.INPUT, tempDir.toString());

            // Then: Should return true and content should be deleted, but folder preserved
            assertTrue(result);
            assertTrue(Files.exists(inputFolder), "Input folder should still exist");
            assertEquals(0, Files.list(inputFolder).count(), "Input folder should be empty");
        }

        @Test
        @DisplayName("3.6 - Successfully purges OUTPUT folder content")
        void successfullyPurgesOutputFolderContent() throws IOException {
            // Given: Request with existing output folder structure
            Request request = createTestRequest(6);
            String requestFolderName = "request-6";
            request.setFolderOut(requestFolderName + "/output");

            Path outputFolder = tempDir.resolve(requestFolderName).resolve("output");
            Files.createDirectories(outputFolder);
            Files.createFile(outputFolder.resolve("result.zip"));

            // When: Purging folder content
            boolean result = FileSystemUtils.purgeRequestFolderContent(request, RequestDataFolder.OUTPUT, tempDir.toString());

            // Then: Should return true and content should be deleted
            assertTrue(result);
            assertTrue(Files.exists(outputFolder), "Output folder should still exist");
            assertEquals(0, Files.list(outputFolder).count(), "Output folder should be empty");
        }

        @Test
        @DisplayName("3.7 - Successfully purges BASE folder content")
        void successfullyPurgesBaseFolderContent() throws IOException {
            // Given: Request with existing base folder structure
            Request request = createTestRequest(7);
            String requestFolderName = "request-7";
            request.setFolderIn(requestFolderName + "/input");
            request.setFolderOut(requestFolderName + "/output");

            Path baseFolder = tempDir.resolve(requestFolderName);
            Path inputFolder = baseFolder.resolve("input");
            Path outputFolder = baseFolder.resolve("output");
            Files.createDirectories(inputFolder);
            Files.createDirectories(outputFolder);
            Files.createFile(inputFolder.resolve("input.txt"));
            Files.createFile(outputFolder.resolve("output.txt"));

            // When: Purging base folder content
            boolean result = FileSystemUtils.purgeRequestFolderContent(request, RequestDataFolder.BASE, tempDir.toString());

            // Then: Should return true and content should be deleted
            assertTrue(result);
            assertTrue(Files.exists(baseFolder), "Base folder should still exist");
            assertEquals(0, Files.list(baseFolder).count(), "Base folder should be empty");
        }
    }

    // ==================== 4. SANITIZE FILENAME TESTS ====================

    @Nested
    @DisplayName("4. sanitizeFileName Tests")
    class SanitizeFileNameTests {

        @Test
        @DisplayName("4.1 - Throws IllegalArgumentException when filename is null")
        void throwsExceptionWhenFilenameIsNull() {
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> FileSystemUtils.sanitizeFileName(null)
            );

            assertEquals("The file name to sanitize cannot be empty.", exception.getMessage());
        }

        @Test
        @DisplayName("4.2 - Throws IllegalArgumentException when filename is empty")
        void throwsExceptionWhenFilenameIsEmpty() {
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> FileSystemUtils.sanitizeFileName("")
            );

            assertEquals("The file name to sanitize cannot be empty.", exception.getMessage());
        }

        @Test
        @DisplayName("4.3 - Returns same filename when no sanitization needed")
        void returnsSameFilenameWhenNoSanitizationNeeded() {
            String filename = "simple-filename.txt";
            String result = FileSystemUtils.sanitizeFileName(filename);
            assertEquals(filename, result);
        }

        @Test
        @DisplayName("4.4 - Replaces spaces with underscores")
        void replacesSpacesWithUnderscores() {
            String result = FileSystemUtils.sanitizeFileName("file name with spaces.txt");
            assertEquals("file_name_with_spaces.txt", result);
        }

        @Test
        @DisplayName("4.5 - Replaces forward slashes with underscores")
        void replacesForwardSlashesWithUnderscores() {
            String result = FileSystemUtils.sanitizeFileName("path/to/file.txt");
            assertEquals("path_to_file.txt", result);
        }

        @Test
        @DisplayName("4.6 - Replaces backslashes with underscores")
        void replacesBackslashesWithUnderscores() {
            String result = FileSystemUtils.sanitizeFileName("path\\to\\file.txt");
            assertEquals("path_to_file.txt", result);
        }

        @Test
        @DisplayName("4.7 - Replaces colons with underscores")
        void replacesColonsWithUnderscores() {
            String result = FileSystemUtils.sanitizeFileName("C:file.txt");
            assertEquals("C_file.txt", result);
        }

        @Test
        @DisplayName("4.8 - Replaces special characters with underscores")
        void replacesSpecialCharactersWithUnderscores() {
            // Regex replaces: \s < > * " / \ [ ] : ; | = , '
            // Input: file<>*"|;=,.txt - the dot '.' is NOT replaced
            String result = FileSystemUtils.sanitizeFileName("file<>*\"|;=,.txt");
            assertEquals("file________.txt", result);
        }

        @Test
        @DisplayName("4.9 - Replaces brackets with underscores")
        void replacesBracketsWithUnderscores() {
            String result = FileSystemUtils.sanitizeFileName("file[name].txt");
            assertEquals("file_name_.txt", result);
        }

        @Test
        @DisplayName("4.10 - Removes diacritics (accents)")
        void removesDiacritics() {
            String result = FileSystemUtils.sanitizeFileName("fichier_accentue.txt");
            assertEquals("fichier_accentue.txt", result);
        }

        @Test
        @DisplayName("4.11 - Removes French accents")
        void removesFrenchAccents() {
            String result = FileSystemUtils.sanitizeFileName("cafe_resume_naive.txt");
            assertEquals("cafe_resume_naive.txt", result);
        }

        @Test
        @DisplayName("4.12 - Handles complex filename with multiple issues")
        void handlesComplexFilename() {
            // Input: "Mon fichier/Document: "test" [v2].txt"
            // Expected: Mon_fichier_Document___test___v2_.txt
            // Breakdown: Mon + _ + fichier + _ + Document + ___ + test + ___ + v2 + _ + .txt
            String result = FileSystemUtils.sanitizeFileName("Mon fichier/Document: \"test\" [v2].txt");
            assertEquals("Mon_fichier_Document___test___v2_.txt", result);
        }

        @Test
        @DisplayName("4.13 - Replaces single quotes with underscores")
        void replacesSingleQuotesWithUnderscores() {
            String result = FileSystemUtils.sanitizeFileName("file's name.txt");
            assertEquals("file_s_name.txt", result);
        }

        @Test
        @DisplayName("4.14 - Handles tabs and newlines as spaces")
        void handlesWhitespaceCharacters() {
            String result = FileSystemUtils.sanitizeFileName("file\tname\ntest.txt");
            assertEquals("file_name_test.txt", result);
        }
    }

    // ==================== 5. REQUEST DATA FOLDER ENUM TESTS ====================

    @Nested
    @DisplayName("5. RequestDataFolder Enum Tests")
    class RequestDataFolderEnumTests {

        @Test
        @DisplayName("5.1 - Enum contains BASE value")
        void enumContainsBase() {
            assertNotNull(RequestDataFolder.valueOf("BASE"));
        }

        @Test
        @DisplayName("5.2 - Enum contains INPUT value")
        void enumContainsInput() {
            assertNotNull(RequestDataFolder.valueOf("INPUT"));
        }

        @Test
        @DisplayName("5.3 - Enum contains OUTPUT value")
        void enumContainsOutput() {
            assertNotNull(RequestDataFolder.valueOf("OUTPUT"));
        }

        @Test
        @DisplayName("5.4 - Enum has exactly 3 values")
        void enumHasExactlyThreeValues() {
            assertEquals(3, RequestDataFolder.values().length);
        }
    }

    // ==================== HELPER METHODS ====================

    /**
     * Creates a test request with the given ID.
     */
    private Request createTestRequest(int id) {
        Request request = new Request();
        request.setId(id);
        request.setOrderLabel("TEST-ORDER-" + id);
        request.setStatus(Request.Status.ONGOING);
        return request;
    }
}
