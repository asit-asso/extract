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
package ch.asit_asso.extract.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import org.apache.commons.lang3.StringUtils;
import ch.asit_asso.extract.domain.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 * Helper methods to make operations against the file system.
 *
 * @author Yves Grasset
 */
public abstract class FileSystemUtils {

    /**
     * The writer to the application logs.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(FileSystemUtils.class);



    /**
     * Creates a directory.
     *
     * @param folderToCreate the directory that should be created.
     * @return the created (or already existing) folder, or <code>null</code> if the creation failed.
     */
    public static final File createFolder(final File folderToCreate) {
        return FileSystemUtils.createFolder(folderToCreate, false);
    }



    /**
     * Creates a directory.
     *
     * @param folderToCreate the directory that should be created.
     * @param failOnExisting <code>true</code> to consider it an error if the folder already exists. <b>Note:</b> The
     *                       creation will fail if a file that is <i>not</i> a directory already exists with the same
     *                       name and path even if this parameter is set to <code>false</code>.
     * @return the created (or already existing) folder, or <code>null</code> if the creation failed.
     */
    public static final File createFolder(final File folderToCreate, final boolean failOnExisting) {

        if (folderToCreate == null) {
            throw new IllegalArgumentException("The folder to create cannot be null.");
        }

        boolean couldCreateFolder;
        String folderPath = null;

        try {
            folderPath = folderToCreate.getAbsolutePath();

            if (folderToCreate.exists()) {

                if (folderToCreate.isDirectory()) {

                    if (failOnExisting) {
                        FileSystemUtils.LOGGER.error("The folder {} already exists.", folderPath);
                        return null;
                    }

                    FileSystemUtils.LOGGER.warn("The folder {} already exists. Please ensure that it is OK.",
                            folderPath);
                    return folderToCreate;
                }

                FileSystemUtils.LOGGER.error("A file with the same name than the folder to create ({}) already exists.",
                        folderPath);
                return null;
            }

            couldCreateFolder = folderToCreate.mkdirs();

            FileSystemUtils.LOGGER.debug("Created the folder {}.", folderPath);

        } catch (SecurityException exception) {
            FileSystemUtils.LOGGER.error("A security error occurred when the folder {} was created.",
                    (folderPath != null) ? folderPath : "<unaccessible>", exception);
            couldCreateFolder = false;
        }

        return (couldCreateFolder) ? folderToCreate : null;

    }



    /**
     * Deletes the directory that contains the data related to the processing of an order and its content.
     *
     * @param request        the order whose data folder must be deleted.
     * @param baseFolderPath the absolute path of the folder that contains the data related to the processing of each
     *                       order
     * @return <code>true</code> if the folder and all its content have been deleted
     */
    public static boolean purgeRequestFolders(final Request request, final String baseFolderPath) {

        if (request == null) {
            throw new IllegalArgumentException("The request cannot be null.");
        }

        if (StringUtils.isBlank(baseFolderPath)) {
            throw new IllegalArgumentException("The base folder path cannot be empty.");
        }

        final int requestId = request.getId();
        FileSystemUtils.LOGGER.debug("Purging the data folders for request {}.", requestId);
        final File requestBaseDataFolder
                = FileSystemUtils.getRequestBaseDataFolder(request, baseFolderPath);

        if (requestBaseDataFolder == null) {
            FileSystemUtils.LOGGER.warn("Could not get data folders to purge for request {}.", requestId);
            return false;
        }

        final String requestBaseDataFolderPath = requestBaseDataFolder.getAbsolutePath();

        if (!org.springframework.util.FileSystemUtils.deleteRecursively(requestBaseDataFolder)) {
            FileSystemUtils.LOGGER.error("Could not purge the data folder \"{}\" for request {}.",
                    requestBaseDataFolderPath, requestId);
            return false;

        }

        FileSystemUtils.LOGGER.info("The data folder \"{}\" for request {} was successfully purged and deleted.",
                requestBaseDataFolderPath, requestId);
        return true;
    }



    /**
     * Deletes the content of the directory that contains the data related to the processing of an order
     * but not the directory itself.
     *
     * @param request        the order whose data folder must be deleted.
     * @param folderType     the type of request data folder to purge
     * @param baseFolderPath the absolute path of the folder that contains the data related to the processing of each
     *                       order
     * @return <code>true</code> if the folder and all its content have been deleted
     */
    public static boolean purgeRequestFolderContent(final Request request, final RequestDataFolder folderType,
            final String baseFolderPath) {

        if (request == null) {
            throw new IllegalArgumentException("The request cannot be null.");
        }

        if (folderType == null) {
            throw new IllegalArgumentException("The request data folder type cannot be null");
        }

        if (StringUtils.isBlank(baseFolderPath)) {
            throw new IllegalArgumentException("The base folder path cannot be empty.");
        }

        final int requestId = request.getId();
        FileSystemUtils.LOGGER.debug("Purging the data folders for request {}.", requestId);
        final File requestDataFolder = FileSystemUtils.getRequestDataFolder(request, folderType, baseFolderPath);

        if (requestDataFolder == null) {
            FileSystemUtils.LOGGER.warn("Could not get data folders to purge for request {}.", requestId);
            return false;
        }

        final String requestDataFolderPath = requestDataFolder.getAbsolutePath();
        boolean isDeletionComplete = true;

        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(requestDataFolder.toPath())) {

            for (Path itemPath : directoryStream) {
                isDeletionComplete &= org.springframework.util.FileSystemUtils.deleteRecursively(itemPath.toFile());
            }

        } catch (IOException exception) {
            FileSystemUtils.LOGGER.error("Could not parse the content of folder {}.", requestDataFolderPath, exception);
            return false;
        }

        if (!isDeletionComplete) {
            FileSystemUtils.LOGGER.error("Some items in folder {} could not be deleted.", requestDataFolderPath);
            return false;
        }

        FileSystemUtils.LOGGER.info("The data folder \"{}\" for request {} was successfully purged and deleted.",
                requestDataFolderPath, requestId);
        return true;
    }



    /**
     * Transforms a file name to avoid problems with most operating systems. The diacritics will be removed and
     * the spaces and protected characters (such as :, \ or /) will be replaced by underscores.
     *
     * @param originalFilename the file name to sanitize
     * @return the sanitized file name
     */
    public static String sanitizeFileName(final String originalFilename) {

        if (StringUtils.isEmpty(originalFilename)) {
            throw new IllegalArgumentException("The file name to sanitize cannot be empty.");
        }

        return StringUtils.stripAccents(originalFilename.replaceAll("[\\s<>*\"/\\\\\\[\\]:;|=,']", "_"));
    }



    /**
     * The different data folder types for a request.
     */
    public enum RequestDataFolder {
        /**
         * The folder that contains the all the data (input and output) for the request.
         */
        BASE,
        /**
         * The folder that contains the data required to process the request.
         */
        INPUT,
        /**
         * The folder that contains the data produced by the process of the request.
         */
        OUTPUT
    }



    /**
     * Obtains a folder that contains data related to the processing of an order.
     *
     * @param request        the order
     * @param folderType     the type of the desired data folder
     * @param baseFolderPath the absolute path of the folder that contains the data for all the orders
     * @return the file object for the folder, or <code>null</code> if it does not exist
     */
    private static File getRequestDataFolder(final Request request, final RequestDataFolder folderType,
            final String baseFolderPath) {
        assert request != null : "The request cannot be null.";
        assert folderType != null : "The folder type cannot be null.";
        assert StringUtils.isNotBlank(baseFolderPath) : "The base folder path cannot be null.";

        switch (folderType) {

            case BASE:
                return FileSystemUtils.getRequestBaseDataFolder(request, baseFolderPath);

            case INPUT:
                return FileSystemUtils.getRequestInputDataFolder(request, baseFolderPath);

            case OUTPUT:
                return FileSystemUtils.getRequestOutputDataFolder(request, baseFolderPath);

            default:
                FileSystemUtils.LOGGER.error("The request data folder type {} is not supported.", folderType.name());
                throw new IllegalStateException("Unsupported request data folder type.");
        }

    }



    /**
     * Obtains the folder that contains the data related to the processing of an order.
     *
     * @param request        the order
     * @param baseFolderPath the absolute path of the folder that contains the data for all the orders
     * @return the file object for the folder, or <code>null</code> if it does not exist
     */
    private static File getRequestBaseDataFolder(final Request request, final String baseFolderPath) {
        assert request != null : "The request cannot be null.";
        assert StringUtils.isNotBlank(baseFolderPath) : "The base path cannot be empty.";

        for (String requestSubFolderPath : new String[]{request.getFolderIn(), request.getFolderOut()}) {

            if (requestSubFolderPath == null) {
                continue;
            }

            final File requestSubFolder = new File(baseFolderPath, requestSubFolderPath);

            if (!requestSubFolder.exists() || !requestSubFolder.isDirectory()) {
                continue;
            }

            return requestSubFolder.getParentFile();
        }

        return null;
    }



    /**
     * Obtains the folder that contains the data to use to process an order.
     *
     * @param request        the order
     * @param baseFolderPath the absolute path of the folder that contains the data for all the orders
     * @return the file object for the folder, or <code>null</code> if it does not exist
     */
    private static File getRequestInputDataFolder(final Request request, final String baseFolderPath) {
        assert request != null : "The request cannot be null.";
        assert StringUtils.isNotBlank(baseFolderPath) : "The base path cannot be empty.";

        final String inputFolderRelativePath = request.getFolderIn();

        if (inputFolderRelativePath == null) {
            return null;
        }

        final File inputFolder = new File(baseFolderPath, inputFolderRelativePath);

        if (!inputFolder.exists() || !inputFolder.isDirectory()) {
            return null;
        }

        return inputFolder;
    }



    /**
     * Obtains the folder that contains the data produced by the processing of an order.
     *
     * @param request        the order
     * @param baseFolderPath the absolute path of the folder that contains the data for all the orders
     * @return the file object for the folder, or <code>null</code> if it does not exist
     */
    private static File getRequestOutputDataFolder(final Request request, final String baseFolderPath) {
        assert request != null : "The request cannot be null.";
        assert StringUtils.isNotBlank(baseFolderPath) : "The base path cannot be empty.";

        final String outputFolderRelativePath = request.getFolderOut();

        if (outputFolderRelativePath == null) {
            return null;
        }

        final File inputFolder = new File(baseFolderPath, outputFolderRelativePath);

        if (!inputFolder.exists() || !inputFolder.isDirectory()) {
            return null;
        }

        return inputFolder;
    }

}
