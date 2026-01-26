package ch.asit_asso.extract.unit.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import ch.asit_asso.extract.utils.ZipUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ZipUtilsTest {

    @TempDir
    Path tempDir;


    @Test
    void zipFolderContentToByteArrayWithValidFolder() throws IOException {
        Path subDir = tempDir.resolve("testFolder");
        Files.createDirectory(subDir);
        Files.writeString(subDir.resolve("file1.txt"), "Hello World");
        Files.writeString(subDir.resolve("file2.txt"), "Test Content");

        byte[] zipBytes = ZipUtils.zipFolderContentToByteArray(subDir.toFile());

        assertNotNull(zipBytes);
        assertTrue(zipBytes.length > 0);

        Set<String> entryNames = extractZipEntryNames(zipBytes);
        assertTrue(entryNames.contains("file1.txt"));
        assertTrue(entryNames.contains("file2.txt"));
    }


    @Test
    void zipFolderContentToByteArrayWithNullFolder() {
        assertThrows(IllegalArgumentException.class, () -> ZipUtils.zipFolderContentToByteArray(null));
    }


    @Test
    void zipFolderContentToByteArrayWithNonExistentFolder() {
        File nonExistent = new File(tempDir.toFile(), "nonExistent");

        assertThrows(IllegalArgumentException.class, () -> ZipUtils.zipFolderContentToByteArray(nonExistent));
    }


    @Test
    void zipFolderContentToByteArrayWithFile() throws IOException {
        Path file = tempDir.resolve("notAFolder.txt");
        Files.writeString(file, "This is a file, not a folder");

        assertThrows(IllegalArgumentException.class, () -> ZipUtils.zipFolderContentToByteArray(file.toFile()));
    }


    @Test
    void zipFolderContentToByteArrayWithEmptyFolder() throws IOException {
        Path emptyDir = tempDir.resolve("emptyFolder");
        Files.createDirectory(emptyDir);

        byte[] zipBytes = ZipUtils.zipFolderContentToByteArray(emptyDir.toFile());

        assertNotNull(zipBytes);
        Set<String> entryNames = extractZipEntryNames(zipBytes);
        assertTrue(entryNames.isEmpty());
    }


    @Test
    void zipFolderContentToByteArrayWithNestedFolders() throws IOException {
        Path mainDir = tempDir.resolve("mainFolder");
        Files.createDirectory(mainDir);
        Path nestedDir = mainDir.resolve("nested");
        Files.createDirectory(nestedDir);
        Files.writeString(mainDir.resolve("root.txt"), "Root file");
        Files.writeString(nestedDir.resolve("nested.txt"), "Nested file");

        byte[] zipBytes = ZipUtils.zipFolderContentToByteArray(mainDir.toFile());

        assertNotNull(zipBytes);
        Set<String> entryNames = extractZipEntryNames(zipBytes);
        assertTrue(entryNames.contains("root.txt"));
        assertTrue(entryNames.contains("nested/nested.txt"));
    }


    @Test
    void zipFolderContentToFileWithValidFolder() throws IOException {
        Path subDir = tempDir.resolve("testFolder");
        Files.createDirectory(subDir);
        Files.writeString(subDir.resolve("file1.txt"), "Hello World");
        String zipFileName = "output.zip";

        File zipFile = ZipUtils.zipFolderContentToFile(subDir.toFile(), zipFileName);

        assertNotNull(zipFile);
        assertTrue(zipFile.exists());
        assertEquals(zipFileName, zipFile.getName());
        assertTrue(zipFile.length() > 0);
    }


    @Test
    void zipFolderContentToFileWithNullFolder() {
        assertThrows(IllegalArgumentException.class, () -> ZipUtils.zipFolderContentToFile(null, "output.zip"));
    }


    @Test
    void zipFolderContentToFileWithNonExistentFolder() {
        File nonExistent = new File(tempDir.toFile(), "nonExistent");

        assertThrows(IllegalArgumentException.class, () -> ZipUtils.zipFolderContentToFile(nonExistent, "output.zip"));
    }


    @Test
    void zipFolderContentToFileExcludesZipFile() throws IOException {
        Path subDir = tempDir.resolve("testFolder");
        Files.createDirectory(subDir);
        Files.writeString(subDir.resolve("file1.txt"), "Hello World");
        String zipFileName = "output.zip";

        File zipFile = ZipUtils.zipFolderContentToFile(subDir.toFile(), zipFileName);
        Set<String> entryNames = extractZipEntryNamesFromFile(zipFile);

        assertTrue(entryNames.contains("file1.txt"));
        assertFalse(entryNames.contains(zipFileName));
    }


    @Test
    void zipFolderContentToStreamWithValidFolder() throws IOException {
        Path subDir = tempDir.resolve("testFolder");
        Files.createDirectory(subDir);
        Files.writeString(subDir.resolve("file1.txt"), "Test Content");
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        ZipUtils.zipFolderContentToStream(subDir.toFile(), outputStream, null);

        byte[] zipBytes = outputStream.toByteArray();
        assertNotNull(zipBytes);
        assertTrue(zipBytes.length > 0);

        Set<String> entryNames = extractZipEntryNames(zipBytes);
        assertTrue(entryNames.contains("file1.txt"));
    }


    @Test
    void zipFolderContentToStreamWithNullFolder() {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        assertThrows(IllegalArgumentException.class,
                () -> ZipUtils.zipFolderContentToStream(null, outputStream, null));
    }


    @Test
    void zipFolderContentToStreamWithNullStream() throws IOException {
        Path subDir = tempDir.resolve("testFolder");
        Files.createDirectory(subDir);

        assertThrows(IllegalArgumentException.class,
                () -> ZipUtils.zipFolderContentToStream(subDir.toFile(), null, null));
    }


    @Test
    void zipFolderContentToStreamExcludesFile() throws IOException {
        Path subDir = tempDir.resolve("testFolder");
        Files.createDirectory(subDir);
        Files.writeString(subDir.resolve("file1.txt"), "Include me");
        Files.writeString(subDir.resolve("exclude.txt"), "Exclude me");
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        ZipUtils.zipFolderContentToStream(subDir.toFile(), outputStream, "exclude.txt");

        Set<String> entryNames = extractZipEntryNames(outputStream.toByteArray());
        assertTrue(entryNames.contains("file1.txt"));
        assertFalse(entryNames.contains("exclude.txt"));
    }


    @Test
    void addFileToZipWithFile() throws IOException {
        Path subDir = tempDir.resolve("testFolder");
        Files.createDirectory(subDir);
        Path file = subDir.resolve("test.txt");
        Files.writeString(file, "Test content");

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try (java.util.zip.ZipOutputStream zipOut = new java.util.zip.ZipOutputStream(byteArrayOutputStream)) {
            ZipUtils.addFileToZip("", file.toString(), zipOut);
        }

        Set<String> entryNames = extractZipEntryNames(byteArrayOutputStream.toByteArray());
        assertTrue(entryNames.contains("test.txt"));
    }


    @Test
    void addFileToZipWithPath() throws IOException {
        Path subDir = tempDir.resolve("testFolder");
        Files.createDirectory(subDir);
        Path file = subDir.resolve("test.txt");
        Files.writeString(file, "Test content");

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try (java.util.zip.ZipOutputStream zipOut = new java.util.zip.ZipOutputStream(byteArrayOutputStream)) {
            ZipUtils.addFileToZip("custom/path", file.toString(), zipOut);
        }

        Set<String> entryNames = extractZipEntryNames(byteArrayOutputStream.toByteArray());
        assertTrue(entryNames.contains("custom/path/test.txt"));
    }


    @Test
    void addFileToZipWithDirectory() throws IOException {
        Path subDir = tempDir.resolve("testFolder");
        Files.createDirectory(subDir);
        Path nestedDir = subDir.resolve("nested");
        Files.createDirectory(nestedDir);
        Files.writeString(nestedDir.resolve("nested.txt"), "Nested content");

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try (java.util.zip.ZipOutputStream zipOut = new java.util.zip.ZipOutputStream(byteArrayOutputStream)) {
            ZipUtils.addFileToZip("", nestedDir.toString(), zipOut);
        }

        Set<String> entryNames = extractZipEntryNames(byteArrayOutputStream.toByteArray());
        assertTrue(entryNames.contains("nested/nested.txt"));
    }


    @Test
    void addFolderToZipWithEmptyPath() throws IOException {
        Path subDir = tempDir.resolve("testFolder");
        Files.createDirectory(subDir);
        Files.writeString(subDir.resolve("file.txt"), "Content");

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try (java.util.zip.ZipOutputStream zipOut = new java.util.zip.ZipOutputStream(byteArrayOutputStream)) {
            ZipUtils.addFolderToZip("", subDir.toString(), zipOut);
        }

        Set<String> entryNames = extractZipEntryNames(byteArrayOutputStream.toByteArray());
        assertTrue(entryNames.contains("testFolder/file.txt"));
    }


    @Test
    void addFolderToZipWithPath() throws IOException {
        Path subDir = tempDir.resolve("testFolder");
        Files.createDirectory(subDir);
        Files.writeString(subDir.resolve("file.txt"), "Content");

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try (java.util.zip.ZipOutputStream zipOut = new java.util.zip.ZipOutputStream(byteArrayOutputStream)) {
            ZipUtils.addFolderToZip("base", subDir.toString(), zipOut);
        }

        Set<String> entryNames = extractZipEntryNames(byteArrayOutputStream.toByteArray());
        assertTrue(entryNames.contains("base/testFolder/file.txt"));
    }


    @Test
    void zipPreservesFileContent() throws IOException {
        Path subDir = tempDir.resolve("testFolder");
        Files.createDirectory(subDir);
        String expectedContent = "This is the test content that should be preserved!";
        Files.writeString(subDir.resolve("content.txt"), expectedContent);

        byte[] zipBytes = ZipUtils.zipFolderContentToByteArray(subDir.toFile());
        String actualContent = extractFileContentFromZip(zipBytes, "content.txt");

        assertEquals(expectedContent, actualContent);
    }


    @Test
    void zipWithLargeFile() throws IOException {
        Path subDir = tempDir.resolve("testFolder");
        Files.createDirectory(subDir);

        // Create a file larger than the buffer size (1024 bytes)
        byte[] largeContent = new byte[5000];
        for (int i = 0; i < largeContent.length; i++) {
            largeContent[i] = (byte) (i % 256);
        }
        Files.write(subDir.resolve("large.bin"), largeContent);

        byte[] zipBytes = ZipUtils.zipFolderContentToByteArray(subDir.toFile());
        byte[] extractedContent = extractFileBytesFromZip(zipBytes, "large.bin");

        assertArrayEquals(largeContent, extractedContent);
    }


    @Test
    void zipWithMultipleNestedLevels() throws IOException {
        Path subDir = tempDir.resolve("testFolder");
        Files.createDirectory(subDir);
        Path level1 = subDir.resolve("level1");
        Files.createDirectory(level1);
        Path level2 = level1.resolve("level2");
        Files.createDirectory(level2);
        Path level3 = level2.resolve("level3");
        Files.createDirectory(level3);
        Files.writeString(level3.resolve("deep.txt"), "Deep content");

        byte[] zipBytes = ZipUtils.zipFolderContentToByteArray(subDir.toFile());

        Set<String> entryNames = extractZipEntryNames(zipBytes);
        assertTrue(entryNames.contains("level1/level2/level3/deep.txt"));
    }


    // Helper methods

    private Set<String> extractZipEntryNames(byte[] zipBytes) throws IOException {
        Set<String> names = new HashSet<>();
        try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(zipBytes))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                names.add(entry.getName());
            }
        }
        return names;
    }


    private Set<String> extractZipEntryNamesFromFile(File zipFile) throws IOException {
        return extractZipEntryNames(Files.readAllBytes(zipFile.toPath()));
    }


    private String extractFileContentFromZip(byte[] zipBytes, String fileName) throws IOException {
        try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(zipBytes))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (entry.getName().equals(fileName)) {
                    ByteArrayOutputStream content = new ByteArrayOutputStream();
                    byte[] buffer = new byte[1024];
                    int len;
                    while ((len = zis.read(buffer)) > 0) {
                        content.write(buffer, 0, len);
                    }
                    return content.toString();
                }
            }
        }
        return null;
    }


    private byte[] extractFileBytesFromZip(byte[] zipBytes, String fileName) throws IOException {
        try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(zipBytes))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (entry.getName().equals(fileName)) {
                    ByteArrayOutputStream content = new ByteArrayOutputStream();
                    byte[] buffer = new byte[1024];
                    int len;
                    while ((len = zis.read(buffer)) > 0) {
                        content.write(buffer, 0, len);
                    }
                    return content.toByteArray();
                }
            }
        }
        return null;
    }
}
