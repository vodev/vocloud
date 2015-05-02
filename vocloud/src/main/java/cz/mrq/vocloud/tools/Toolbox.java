package cz.mrq.vocloud.tools;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.Charset;
import java.util.Enumeration;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/*
 * To change this template, choose Tools | Templates and open the template in
 * the editor.
 */
/**
 *
 * @author voadmin
 */
public class Toolbox {

    public static String httpGet(String urlStr) throws IOException {
        StringBuilder sb = new StringBuilder();
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.connect();

        if (conn.getResponseCode() != 200) {
            throw new IOException(conn.getResponseMessage());
        }

        try (BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            String line;
            while ((line = rd.readLine()) != null) {
                sb.append(line);
                sb.append("\n");
            }
        }

        conn.disconnect();

        return sb.toString();
    }

    public static String httpPost(String urlStr) throws IOException {
        StringBuilder sb = new StringBuilder();
        URL url = new URL(urlStr);
        HttpURLConnection conn
                = (HttpURLConnection) url.openConnection();
        // request json instead of xml
        //conn.setRequestProperty("Accept", "application/json");
        conn.setRequestMethod("POST");
        conn.connect();

        if (conn.getResponseCode() != 200) {
            throw new IOException(urlStr + " " + conn.getResponseMessage());
        }

        // Buffer the result into a string
        BufferedReader rd = new BufferedReader(
                new InputStreamReader(conn.getInputStream()));

        String line;
        while ((line = rd.readLine()) != null) {
            sb.append(line);
            sb.append("\n");
        }
        rd.close();

        conn.disconnect();

        return sb.toString();
    }

    public static String httpPostWithBody(String urlStr, Map<String, String> bodyParams) throws IOException {
        StringBuilder sb = new StringBuilder();
        URL url = new URL(urlStr);
        HttpURLConnection conn;
        try {
            conn = (HttpURLConnection) url.openConnection();
        } catch (ClassCastException ex) {
            throw new IOException(ex);
        }
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        conn.setUseCaches(false);
        conn.setDoInput(true);
        conn.setDoOutput(true);
        try (DataOutputStream dos = new DataOutputStream(conn.getOutputStream())) {
            for (Map.Entry<String, String> entry : bodyParams.entrySet()) {
                dos.write((entry.getKey() + "=").getBytes(Charset.forName("UTF-8")));
                dos.write((URLEncoder.encode(entry.getValue(), "UTF-8") + "&").getBytes(Charset.forName("UTF-8")));
            }
            dos.flush();
        }
        try (BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            String line;
            while ((line = rd.readLine()) != null) {
                sb.append(line).append('\n');
            }
        }
        conn.disconnect();
        return sb.toString();
    }

    public static int getResponseCode(String urlStr) throws IOException {
        URL url = new URL(urlStr);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.connect();
        int responseCode = connection.getResponseCode();
        connection.disconnect();
        return responseCode;
    }

    public static boolean downloadFile(String address, File out) throws MalformedURLException {
        Logger.getLogger(Toolbox.class.getName()).log(Level.INFO, "Downloading from {0}", address);
        URL url = new URL(address);
        try (ReadableByteChannel rbc = Channels.newChannel(url.openStream());
                FileOutputStream fos = new FileOutputStream(out)) {
            long stepToDownload = 1 << 24;
            long offset = 0;
            long downloaded;
            while ((downloaded = fos.getChannel().transferFrom(rbc, offset, stepToDownload)) != 0) {
                offset += downloaded;
            }
        } catch (IOException ex) {
            Logger.getLogger(Toolbox.class.getName()).log(Level.SEVERE, address, ex);
            return false;
        }
        return true;
    }

    /**
     * Recursively deletes file (directory)
     *
     * @param file
     */
    public static void delete(File file) {
        if (!file.isDirectory()) {
            file.delete();
        } else {
            String files[] = file.list();
            if (files.length == 0) {
                file.delete();
            } else {
                for (String fileName : files) {
                    delete(new File(file, fileName));
                }
            }
            file.delete();
        }

    }

    public static void decompress(File file, File outputDirectory) {
        try {
            int buffer = 2048;
            BufferedOutputStream dest;
            BufferedInputStream is;
            ZipEntry entry;
            ZipFile zipfile = new ZipFile(file);
            Enumeration e = zipfile.entries();
            while (e.hasMoreElements()) {
                entry = (ZipEntry) e.nextElement();
                //Logger.getLogger(JobKorel.class.getName()).log(Level.INFO, "Extracting: {0}", entry);
                is = new BufferedInputStream(zipfile.getInputStream(entry));
                int count;
                byte data[] = new byte[buffer];
                File out = new File(outputDirectory, entry.getName());
                FileOutputStream fos = new FileOutputStream(out);
                dest = new BufferedOutputStream(fos, buffer);
                while ((count = is.read(data, 0, buffer))
                        != -1) {
                    dest.write(data, 0, count);
                }
                dest.flush();
                dest.close();
                is.close();
            }
        } catch (Exception e) {
            Logger.getLogger(Toolbox.class.getName()).log(Level.SEVERE, "Cannot compress files {0}", e.toString());
        }

    }

    public static String ziplist(File file) {
        StringBuilder sb = new StringBuilder();
        try {

            BufferedInputStream is;
            ZipEntry entry;
            ZipFile zipfile = new ZipFile(file);
            Enumeration e = zipfile.entries();
            while (e.hasMoreElements()) {
                entry = (ZipEntry) e.nextElement();
                sb.append(entry.toString());
                sb.append("\n");
                is = new BufferedInputStream(zipfile.getInputStream(entry));
                is.close();
            }
        } catch (Exception e) {
            Logger.getLogger(Toolbox.class.getName()).log(Level.SEVERE, "Cannot compress files {0}", e.toString());
        }
        return sb.toString();
    }

    /**
     * Compress files in directory using zip
     *
     * @param directory
     */
    public static void compressFiles(File directory, File archive) {
        int buffer = 2048;
        try {
            BufferedInputStream origin;
            ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(archive)));
            byte data[] = new byte[buffer];
            // get a list of files from current directory

            File[] files = directory.listFiles();
            if (files != null) {
                for (File f : files) {
                    if (archive.getName().equals(f.getName())) {
                        continue;
                    }
                    origin = new BufferedInputStream(new FileInputStream(f), buffer);
                    ZipEntry entry = new ZipEntry(f.getName());
                    out.putNextEntry(entry);
                    int count;
                    while ((count = origin.read(data, 0,
                            buffer)) != -1) {
                        out.write(data, 0, count);
                    }
                    origin.close();
                }
            }
            out.close();
        } catch (Exception e) {
            Logger.getLogger(Toolbox.class.getName()).log(Level.SEVERE, "Cannot compress files {0}", e.toString());
        }

    }

    /**
     * converts size in bytes to human readable form
     *
     * works like in du command
     *
     * FROM: http://stackoverflow.com/questions/3758606
     *
     * @param bytes
     * @param si
     * @return
     */
    public static String humanReadableByteCount(long bytes, boolean si) {
        int unit = si ? 1000 : 1024;
        if (bytes < unit) {
            return bytes + " B";
        }
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "i");
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }
}
