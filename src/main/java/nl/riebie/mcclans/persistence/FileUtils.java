/*
 * Copyright (c) 2016 riebie, Kippers <https://bitbucket.org/Kippers/mcclans-core-sponge>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package nl.riebie.mcclans.persistence;

import java.io.*;
import java.util.zip.Adler32;
import java.util.zip.CheckedOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Created by Kippers on 19-1-2016.
 */
public class FileUtils {

    static final int BUFFER = 2048;

    public static void toZip(File file) {
        try {
            File zipFile = new File(file.getParentFile(), stripExtension(file.getName()) + ".zip");

            FileOutputStream dest = new FileOutputStream(zipFile);
            CheckedOutputStream checksum = new CheckedOutputStream(dest, new Adler32());
            ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(checksum));

            byte data[] = new byte[BUFFER];
            FileInputStream fi = new FileInputStream(file);
            BufferedInputStream origin = new BufferedInputStream(fi, BUFFER);
            ZipEntry entry = new ZipEntry(file.getName());
            out.putNextEntry(entry);
            int count;
            while ((count = origin.read(data, 0, BUFFER)) != -1) {
                out.write(data, 0, count);
            }
            out.closeEntry();
            origin.close();

            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void copyFile(File source, File dest) throws IOException {
        if (!source.exists()) {
            source.createNewFile();
        }
        if (!dest.exists()) {
            dest.createNewFile();
        }

        try (InputStream is = new FileInputStream(source); OutputStream os = new FileOutputStream(dest)) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
        }
    }

    public static void moveFile(File source, File dest) throws IOException {
        copyFile(source, dest);
        source.delete();
    }

    public static void removeFolder(File folder) {
        if (folder.isDirectory()) {
            if (folder.listFiles().length == 0) {
                folder.delete();
            } else {
                for (File file : folder.listFiles()) {
                    if (file.isDirectory()) {
                        removeFolder(file);
                    } else {
                        file.delete();
                    }
                }
                folder.delete();
            }
        }
    }

    public static File getLastModifiedFileInFolder(File folder) {
        File[] files = new File[0];
        files = folder.listFiles();

        File lastModifiedFile = null;

        if (files != null)
            for (File file : files) {
                if (lastModifiedFile == null || file.lastModified() > lastModifiedFile.lastModified()) {
                    lastModifiedFile = file;
                }
            }

        return lastModifiedFile;
    }

    public static String getLineSeparator() {
        return System.getProperty("line.separator");
    }

    // from http://stackoverflow.com/a/924506
    public static String stripExtension(String str) {
        // Handle null case specially.

        if (str == null) return null;

        // Get position of last '.'.

        int pos = str.lastIndexOf(".");

        // If there wasn't any '.' just return the string as is.

        if (pos == -1) return str;

        // Otherwise return the string, up to the dot.

        return str.substring(0, pos);
    }
}