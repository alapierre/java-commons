package io.alapierre.io;

import java.awt.*;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Set;

/**
 * @author Adrian Lapierre {@literal <alapierre@soft-project.pl>}
 */
public class FileUtil {

    public static String toFileSystemSafeName(String inputName) {
        return inputName.replaceAll("[^a-zA-Z0-9-_\\.]", "_");
    }

    public static void runDefaultApp(File file) throws Exception {

        Desktop desktop = null;
        if (Desktop.isDesktopSupported()) {
            desktop = Desktop.getDesktop();
            desktop.open(file);
        } else {
            System.out.println("Desktop not supported");
        }
    }

    public static void silentDelete(Path path) {
        try {
            Files.delete(path);
        } catch (IOException x) {
            // ignore
        }
    }

    public static String getFileExtension(String fileName) {
        if (!fileName.contains(".") || fileName.endsWith(".")) return null;
        return fileName.substring(fileName.lastIndexOf(".") + 1);
    }

    public static String getBaseFileName(String fileName) {
        if (!fileName.contains(".") || fileName.endsWith(".")) return null;
        return fileName.substring(0,fileName.lastIndexOf("."));
    }

    public static File addPostfix(File file, String postfix) {
        String originalFileName = file.getName();

        String res = (file.getParent() != null ? file.getParent() + '/' : "")
                + getBaseFileName(originalFileName)
                + postfix
                + getFileExtension(originalFileName);

        return new File(res);
    }

    public static Collection<File> procesFileOrPath(Path inputPath, String fileExtension) throws IOException {

        if(Files.exists(inputPath)) {
            if(Files.isRegularFile(inputPath)) {
                return Collections.singletonList(inputPath.toFile());
            } else {
                DirectoryStream<Path> re = Files.newDirectoryStream(inputPath, path -> path.toString().endsWith(fileExtension));
                LinkedList<File> ret = new LinkedList<>();
                re.forEach(path -> {
                    ret.add(path.toFile());
                });
                return ret;
            }
        } else throw new IOException("File or path not exist");
    }
}
