package cz.ivoa.vocloud.servlet;

import cz.ivoa.vocloud.ejb.JobFacade;
import cz.ivoa.vocloud.ejb.UserAccountFacade;
import cz.ivoa.vocloud.entity.Job;
import cz.ivoa.vocloud.entity.UserAccount;
import cz.ivoa.vocloud.entity.UserGroupName;
import cz.ivoa.vocloud.tools.Config;

import javax.ejb.EJB;
import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLDecoder;
import java.util.zip.GZIPOutputStream;

/**
 * FileServlet
 *
 * @author Lumir Mrkva (lumir.mrkva@topmonks.com)
 */
@WebServlet(name = "FilesServlet", urlPatterns = {"/jobs/preview/*"})
public class FileServlet extends HttpServlet {

    @EJB
    JobFacade jf;

    @Inject
    @Config
    private String jobsDir;

    @EJB
    private UserAccountFacade uaf;
    
    private static final int DEFAULT_BUFFER_SIZE = 10240;

    @Override
    public void init() throws ServletException {
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String path = request.getPathInfo();

        if (path == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND); // 404.
            return;
        }

        String[] split = path.split("/");
        String jobStrId = split[1];
        String jobId = jobStrId.split("-")[1];
        Job job = jf.find(Long.parseLong(jobId));
        String user = request.getRemoteUser();
        UserAccount userAcc = uaf.findByUsername(user);
        if (userAcc == null || (!userAcc.getGroupName().equals(UserGroupName.ADMIN)) && !job.getOwner().equals(userAcc)) {
            response.sendError(403);
            return;
        }

        File file = new File(jobsDir, URLDecoder.decode(path, "UTF-8"));

        if (!file.exists()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, path); // 404.
            return;
        }

        String contentType = getServletContext().getMimeType(file.getName());

        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        // Init servlet response.
        response.reset();
        response.setBufferSize(DEFAULT_BUFFER_SIZE);
        response.setContentType(contentType);
        response.setHeader("Content-Length", String.valueOf(file.length()));

        // Prepare streams.
        BufferedInputStream input = null;
        BufferedOutputStream output = null;

        try {
            // Open streams.
            input = new BufferedInputStream(new FileInputStream(file), DEFAULT_BUFFER_SIZE);
            //check compression availability
            String accHeader = request.getHeader("Accept-Encoding");
            boolean gzipSupported = false;
            if (accHeader != null){
                //find out if gzip compression is supported
                String[] accepted = accHeader.split(",");
                for (String i: accepted){
                    if (i.trim().equals("gzip")){
                        gzipSupported = true;
                        break;
                    }
                }

            }
            if (gzipSupported){
                response.setHeader("Content-Encoding", "gzip");
                output = new BufferedOutputStream(new GZIPOutputStream(response.getOutputStream()), DEFAULT_BUFFER_SIZE);
            } else {
                //send data without compression
                output = new BufferedOutputStream(response.getOutputStream(), DEFAULT_BUFFER_SIZE);
            }


            // Write file contents to response.
            byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
            int length;
            while ((length = input.read(buffer)) > 0) {
                output.write(buffer, 0, length);
            }
        } finally {
            // Gently close streams.
            close(output);
            close(input);
        }
    }

    private static void close(Closeable resource) {
        if (resource != null) {
            try {
                resource.close();
            } catch (IOException e) {
                // Do your thing with the exception. Print it, log it or mail it.
                e.printStackTrace();
            }
        }
    }

}
