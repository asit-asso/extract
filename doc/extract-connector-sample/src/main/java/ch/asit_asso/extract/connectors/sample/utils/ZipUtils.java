/*
 * Copyright (C) 2019 arx iT
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
package ch.asit_asso.extract.connectors.sample.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 *
 * @author Yves Grasset
 */
public abstract class ZipUtils {

    /**
     * The size in bytes of the buffer used to read files.
     */
    private static final int FILE_READ_BUFFER_SIZE = 1024;

    /**
     * The writer to the application logs.
     */
    public static final Logger LOGGER = LoggerFactory.getLogger(ZipUtils.class);



    /**
     * Creates an archive that contains the content of a directory.
     *
     * @param folder the directory that contains the data to compress
     * @return the created archive file
     * @throws IOException if a file system error prevented the creation of the archive file
     */
    public static final byte[] zipFolderContentToByteArray(final File folder) throws IOException {

        if (folder == null || !folder.exists() || !folder.isDirectory()) {
            throw new IllegalArgumentException("The folder to zip must exist and be accessible.");
        }

        byte[] zipBytes;

        try (ByteArrayOutputStream byteArrayWriter = new ByteArrayOutputStream()) {
            ZipUtils.zipFolderContentToStream(folder, byteArrayWriter, null);
            zipBytes = byteArrayWriter.toByteArray();
        }

        ZipUtils.LOGGER.debug("all files are zipped in a byte array of length {}", zipBytes.length);

        return zipBytes;
    }



    /**
     * Creates an archive that contains the content of a directory.
     *
     * @param folder      the directory that contains the data to compress
     * @param zipFileName the name to to give to the archive file
     * @return the created archive file
     * @throws IOException if a file system error prevented the creation of the archive file
     */
    public static final File zipFolderContentToFile(final File folder, final String zipFileName) throws IOException {
        assert StringUtils.isNotBlank(zipFileName) && zipFileName.endsWith(".zip") : "The ZIP file name is invalid.";

        if (folder == null || !folder.exists() || !folder.isDirectory()) {
            throw new IllegalArgumentException("The folder to zip must exist and be accessible.");
        }

        ZipUtils.LOGGER.debug("Creating archive file \"{}\"", zipFileName);
        File zipFile = new File(folder, zipFileName);

        try (FileOutputStream fileWriter = new FileOutputStream(zipFile)) {
            ZipUtils.zipFolderContentToStream(folder, fileWriter, zipFileName);
        }

        ZipUtils.LOGGER.debug("all files are zipped in : {}", zipFile.getCanonicalPath());

        return zipFile;
    }



    /**
     * Creates an archive that contains the content of a directory.
     *
     * @param folder            the directory that contains the data to compress
     * @param stream            the stream to output the archive content to
     * @param fileNameToExclude the name of a file that must not be included in the archive, or <code>null</code> to
     *                          include all the files. <i><b>Note:</b> Wildcard characters are not supported.</i>
     * @throws IOException if a file system error prevented the creation of the archive file
     */
    public static final void zipFolderContentToStream(final File folder, final OutputStream stream,
            final String fileNameToExclude) throws IOException {

        if (folder == null || !folder.exists() || !folder.isDirectory()) {
            throw new IllegalArgumentException("The folder to zip must exist and be accessible.");
        }

        if (stream == null) {
            throw new IllegalArgumentException("The stream to write the ZIP to cannot be null.");
        }

        final String folderPath = folder.getCanonicalPath();
        ZipUtils.LOGGER.debug("Zipping all files in folder out : {}", folderPath);

        try (ZipOutputStream zip = new ZipOutputStream(stream)) {

            for (String fileName : folder.list()) {

                if (fileNameToExclude == null || !fileName.equals(fileNameToExclude)) {
                    File sourceFile = new File(folderPath, fileName);
                    ZipUtils.addFileToZip("", sourceFile.getPath(), zip);
                }
            }

            zip.flush();
        }

        ZipUtils.LOGGER.debug("all files are zipped in the stream.");
    }



    /**
     * Adds a file to an archive.
     *
     * @param path       the path of the file to add in the archive
     * @param sourceFile the path of the file to add in the file system
     * @param zip        the archive to add the file to
     * @throws IOException the file to add could not be read
     */
    public static final void addFileToZip(final String path, final String sourceFile, final ZipOutputStream zip)
            throws IOException {

        final File folder = new File(sourceFile);

        if (folder.isDirectory()) {
            addFolderToZip(path, sourceFile, zip);

        } else {
            final byte[] buf = new byte[ZipUtils.FILE_READ_BUFFER_SIZE];
            int len;

            try (FileInputStream in = new FileInputStream(sourceFile)) {

                if (path.equals("")) {
                    zip.putNextEntry(new ZipEntry(folder.getName()));

                } else {
                    zip.putNextEntry(new ZipEntry(path + "/" + folder.getName()));
                }

                while ((len = in.read(buf)) > 0) {
                    zip.write(buf, 0, len);
                }

                zip.closeEntry();
                zip.flush();
            }
        }
    }



    /**
     * Adds a directory and its content to an archive.
     *
     * @param path         the path of the folder to add in the archive
     * @param sourceFolder the path of the folder to add in the file system
     * @param zip          the archive to add the folder to
     * @throws IOException the folder to add could not be read
     */
    public static final void addFolderToZip(final String path, final String sourceFolder, final ZipOutputStream zip)
            throws IOException {

        final File folder = new File(sourceFolder);

        for (String fileName : folder.list()) {
            File sourceFile = new File(sourceFolder, fileName);

            if ("".equals(path)) {
                ZipUtils.addFileToZip(folder.getName(), sourceFile.getPath(), zip);

            } else {
                ZipUtils.addFileToZip(path + "/" + folder.getName(), sourceFile.getPath(), zip);
            }
        }
    }

}
