package cz.ivoa.vocloud.servlet;

import cz.ivoa.vocloud.filesystem.FilesystemManipulator;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author radio.koza
 */
@WebServlet(name = "FilesystemDownloadServlet", urlPatterns = {"/files/*"})
public class FilesystemDownloadServlet extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(FilesystemDownloadServlet.class.getName());

    @EJB
    private FilesystemManipulator fsm;

    private static final int BUFFER_SIZE = 16384; //1 << 14

    protected void sendFile(HttpServletResponse resp, String requestPath) throws ServletException, IOException {
        resp.reset();
        resp.setBufferSize(BUFFER_SIZE);
        resp.setContentType("application/octet-stream");
        try (InputStream is = fsm.getDownloadStream(requestPath)) {
            resp.setContentLengthLong(fsm.getFileSize(requestPath));
            byte[] buffer = new byte[BUFFER_SIZE];
            int loaded;
            while ((loaded = is.read(buffer)) > 0) {
                resp.getOutputStream().write(buffer, 0, loaded);
            }
        } catch (FileNotFoundException ex) {
            //should not happen
            LOG.log(Level.SEVERE, "Unexpected state! FileNotFound was thrown despite found by fileExists function!", ex);
            resp.reset();
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    protected void sendDir(HttpServletResponse resp, String requestPath) throws ServletException, IOException {
        resp.reset();
        resp.setContentType("application/json");
        List<String> folders = fsm.getFolderNamesInFolder(requestPath);
        List<String> files = fsm.getFileNamesInFolder(requestPath);
        try (PrintStream ps = new PrintStream(resp.getOutputStream())) {
            //print json start
            ps.print("{\"folders\":[");
            //print folders
            boolean isFirst = true;
            for (String folder : folders) {
                if (!isFirst) {
                    ps.print(',');
                }
                isFirst = false;
                ps.print('"');
                ps.write(folder.getBytes(Charset.forName("UTF-8")));
                ps.print('"');
            }
            ps.print("],\"files\":[");
            //print files
            isFirst = true;
            for (String file : files) {
                if (!isFirst) {
                    ps.print(',');
                }
                isFirst = false;
                ps.print('"');
                ps.write(file.getBytes(Charset.forName("UTF-8")));
                ps.print('"');
            }
            //print json end
            ps.print("]}");
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        //download file with specified address if exists
        String requestPath = req.getPathInfo();
        //check that url has additional path info
        if (requestPath == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        try {
            if (fsm.fileExists(requestPath)) {
                sendFile(resp, requestPath);
            } else if (fsm.directoryExists(requestPath)) {
                sendDir(resp, requestPath);
            } else {
                resp.reset();
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (IllegalArgumentException ex) {
            resp.reset();
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
        }
    }

}
