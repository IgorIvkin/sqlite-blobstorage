package com.igorivkin.blobstorage.fileutils;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.Set;

/**
 * Contains a set of file system functions that are commonly applicable.
 */
public class FileUtils {

    private static final Set<String> SPECIAL_FILE_PATHS = Set.of(
            "", ".", ".."
    );

    /**
     * Checks whether the target file exists or not.
     * @param fileName path to file in filesystem
     * @return true if such file exists
     */
    public static boolean checkFileExists(String fileName) {
        Path path = Paths.get(fileName);
        return Files.exists(path);
    }

    /**
     * Returns the file size of a target file in bytes.
     * @param fileName file path to file in filesystem
     * @return file size in bytes
     * @throws IOException attempts to open FileChannel
     */
    public static long getFileSize(String fileName) throws IOException {
        if(!FileUtils.checkFileExists(fileName)) {
            throw new IllegalArgumentException(MessageFormat.format("Cannot get file size, file does not exist: {0}", fileName));
        } else {
            Path targetPath = Paths.get(fileName);
            FileChannel targetFileChannel = FileChannel.open(targetPath);
            return targetFileChannel.size();
        }
    }

    /**
     * Returns a string representing a current working directory of the application.
     * @return file path to current working directory in filesystem
     */
    public static String getCurrentWorkingDirectory() {
        return Paths.get(".").toAbsolutePath().normalize().toString();
    }

    /**
     * Deletes file by its filename. Does nothing if file does not exist.
     * @param fileName file name of the file in filesystem
     * @throws IOException attempts to delete file physically
     */
    public static void deleteFile(String fileName) throws IOException {
        FileUtils.deleteFile(fileName, false);
    }

    /**
     * Deletes file by its filename.
     * @param fileName file name of the file in filesystem
     * @param failIfNotExists function will fail in case it's true and file does not exist
     * @throws IOException attempts to delete file physically
     */
    public static void deleteFile(String fileName, boolean failIfNotExists) throws IOException {
        if(FileUtils.SPECIAL_FILE_PATHS.contains(fileName)) {
            throw new IllegalArgumentException(MessageFormat.format("Cannot delete file, filepath looks like special file path: {0}", fileName));
        } else {
            if (!FileUtils.checkFileExists(fileName) && failIfNotExists) {
                throw new IllegalArgumentException(MessageFormat.format("Cannot get file size, file does not exist: {0}", fileName));
            } else {
                Path targetPath = Paths.get(fileName);
                Files.delete(targetPath);
            }
        }
    }

}
