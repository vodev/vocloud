package cz.mrq.vocloud.servlet;

import cz.rk.vocloud.filesystem.FilesystemManipulator;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
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
    
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        //download file with specified address if exists
        String requestPath = req.getPathInfo();
        //check that url has additional path info
        if (requestPath == null){
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        if (!fsm.fileExists(requestPath)){
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        //otherwise
        resp.reset();
        resp.setBufferSize(BUFFER_SIZE);
        resp.setContentType("application/octet-stream");
        try (InputStream is = fsm.getDownloadStream(requestPath)){
            resp.setContentLengthLong(fsm.getFileSize(requestPath));
            byte[] buffer = new byte[BUFFER_SIZE];
            int loaded;
            while ((loaded = is.read(buffer)) > 0){
                resp.getOutputStream().write(buffer, 0, loaded);
            }
        } catch (FileNotFoundException ex){
            //should not happen
            LOG.log(Level.SEVERE, "Unexpected state! FileNotFound was thrown despite found by fileExists function!", ex);
            resp.reset();
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }
    
    
}
