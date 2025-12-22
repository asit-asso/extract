/*
 * Copyright (C) 2025 SecureMind Sàrl
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
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for FileSystemUtils.purgeRequestFolders() method.
 *
 * Tests:
 * - Input validation (null request, blank path)
 * - Folder deletion behavior
 * - Return values
 *
 * @author Bruno Alves
 */
@DisplayName("FileSystemUtils.purgeRequestFolders Unit Tests")
class FileSystemUtilsPurgeTest {

    @TempDir
    Path tempDir;

    // ==================== 1. INPUT VALIDATION ====================

    @Nested
    @DisplayName("1. Input Validation")
    class InputValidationTests {

        @Test
        @DisplayName("1.1 - Throws IllegalArgumentException when request is null")
        void throwsExceptionWhenRequestIsNull() {
            // Given: Null request
            Request request = null;
            String basePath = "/tmp/extract";

            // When/Then: Should throw IllegalArgumentException
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> FileSystemUtils.purgeRequestFolders(request, basePath)
            );

            assertEquals("The request cannot be null.", exception.getMessage());
        }

        @Test
        @DisplayName("1.2 - Throws IllegalArgumentException when basePath is null")
        void throwsExceptionWhenBasePathIsNull() {
            // Given: Valid request, null basePath
            Request request = createTestRequest(1);
            String basePath = null;

            // When/Then: Should throw IllegalArgumentException
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> FileSystemUtils.purgeRequestFolders(request, basePath)
            );

            assertEquals("The base folder path cannot be empty.", exception.getMessage());
        }

        @Test
        @DisplayName("1.3 - Throws IllegalArgumentException when basePath is empty")
        void throwsExceptionWhenBasePathIsEmpty() {
            // Given: Valid request, empty basePath
            Request request = createTestRequest(1);
            String basePath = "";

            // When/Then: Should throw IllegalArgumentException
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> FileSystemUtils.purgeRequestFolders(request, basePath)
            );

            assertEquals("The base folder path cannot be empty.", exception.getMessage());
        }

        @Test
        @DisplayName("1.4 - Throws IllegalArgumentException when basePath is blank")
        void throwsExceptionWhenBasePathIsBlank() {
            // Given: Valid request, blank basePath
            Request request = createTestRequest(1);
            String basePath = "   ";

            // When/Then: Should throw IllegalArgumentException
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> FileSystemUtils.purgeRequestFolders(request, basePath)
            );

            assertEquals("The base folder path cannot be empty.", exception.getMessage());
        }
    }

    // ==================== 2. FOLDER DELETION ====================

    @Nested
    @DisplayName("2. Folder Deletion Behavior")
    class FolderDeletionTests {

        @Test
        @DisplayName("2.1 - Returns false when request folder does not exist")
        void returnsFalseWhenFolderDoesNotExist() {
            // Given: Request with non-existent folder
            Request request = createTestRequest(999);
            request.setFolderIn("non-existent-folder/input");
            String basePath = tempDir.toString();

            // When: Purging folders
            boolean result = FileSystemUtils.purgeRequestFolders(request, basePath);

            // Then: Should return false (folder doesn't exist)
            assertFalse(result, "Should return false when folder doesn't exist");
        }

        @Test
        @DisplayName("2.2 - Successfully deletes existing request folder")
        void successfullyDeletesExistingFolder() throws IOException {
            // Given: Request with existing folder structure
            Request request = createTestRequest(1);
            String folderPath = "test-request-1";
            request.setFolderIn(folderPath + "/input");
            request.setFolderOut(folderPath + "/output");

            // Create the folder structure
            Path requestFolder = tempDir.resolve(folderPath);
            Path inputFolder = requestFolder.resolve("input");
            Path outputFolder = requestFolder.resolve("output");
            Files.createDirectories(inputFolder);
            Files.createDirectories(outputFolder);

            // Create some test files
            Files.createFile(inputFolder.resolve("test-input.txt"));
            Files.createFile(outputFolder.resolve("test-output.txt"));

            // Verify folders exist before purge
            assertTrue(Files.exists(requestFolder), "Request folder should exist before purge");
            assertTrue(Files.exists(inputFolder), "Input folder should exist before purge");
            assertTrue(Files.exists(outputFolder), "Output folder should exist before purge");

            // When: Purging folders
            boolean result = FileSystemUtils.purgeRequestFolders(request, tempDir.toString());

            // Then: Should return true and folders should be deleted
            assertTrue(result, "Should return true on successful deletion");
            assertFalse(Files.exists(requestFolder), "Request folder should be deleted");
        }

        @Test
        @DisplayName("2.3 - Deletes folder with nested subdirectories")
        void deletesNestedSubdirectories() throws IOException {
            // Given: Request with nested folder structure
            Request request = createTestRequest(2);
            String folderPath = "test-request-2";
            request.setFolderIn(folderPath + "/input");
            request.setFolderOut(folderPath + "/output");

            // Create nested folder structure
            Path requestFolder = tempDir.resolve(folderPath);
            Path nestedPath = requestFolder.resolve("input/level1/level2/level3");
            Files.createDirectories(nestedPath);
            Files.createFile(nestedPath.resolve("deep-file.txt"));

            // When: Purging folders
            boolean result = FileSystemUtils.purgeRequestFolders(request, tempDir.toString());

            // Then: All nested folders should be deleted
            assertTrue(result, "Should return true on successful deletion");
            assertFalse(Files.exists(requestFolder), "Request folder and all contents should be deleted");
        }
    }

    // ==================== 3. EDGE CASES ====================

    @Nested
    @DisplayName("3. Edge Cases")
    class EdgeCasesTests {

        @Test
        @DisplayName("3.1 - Handles request with null folderIn")
        void handlesNullFolderIn() {
            // Given: Request with null folderIn
            Request request = createTestRequest(3);
            request.setFolderIn(null);
            request.setFolderOut("some-folder/output");

            // When: Purging folders
            boolean result = FileSystemUtils.purgeRequestFolders(request, tempDir.toString());

            // Then: Should return false (cannot determine folder)
            assertFalse(result, "Should return false when folderIn is null");
        }

        @Test
        @DisplayName("3.2 - Handles request with empty folderIn")
        void handlesEmptyFolderIn() {
            // Given: Request with empty folderIn
            Request request = createTestRequest(4);
            request.setFolderIn("");
            request.setFolderOut("some-folder/output");

            // When: Purging folders
            boolean result = FileSystemUtils.purgeRequestFolders(request, tempDir.toString());

            // Then: Should return false (cannot determine folder)
            assertFalse(result, "Should return false when folderIn is empty");
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
        request.setStatus(Request.Status.FINISHED);
        return request;
    }
}
