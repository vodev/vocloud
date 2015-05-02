package cz.rk.vocloud.downloader;

import cz.rk.vocloud.entity.DownloadJob;
import cz.rk.vocloud.ssap.model.IndexedSSAPVotable;
import cz.rk.vocloud.ssap.model.Param;
import cz.rk.vocloud.ssap.model.Record;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

/**
 *
 * @author radio.koza
 */
@Startup
@Singleton
public class DownloadManager {

    private static final Logger LOG = Logger.getLogger(DownloadManager.class.getName());

    @EJB
    private DownloadJobFacade djb;

    @EJB
    private DownloadProcessor proc;

    @PostConstruct
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public void init() {
        //initialization during startup
        List<DownloadJob> jobs = djb.findUnfinishedJobs();
        proc.processDownloadJobs(jobs);//all remaining jobs will be downloaded sequentially in one ejb async thread
    }

    private static final String[] supportedDownloadProtocols = {"HTTP", "HTTPS", "FTP"};

    /**
     *
     * @param downloadURL
     * @param targetFolder
     * @return True if success, false otherwise
     */
    public boolean enqueueNewURLDownload(String downloadURL, String targetFolder) {
        try {
            URL url = new URL(downloadURL);
            //check protocol validity
            //supported HTTP, HTTPS, FTP
            if (!Arrays.asList(supportedDownloadProtocols).contains(url.getProtocol().toUpperCase())) {
                throw new MalformedURLException("Unsupported protocol");
            }
        } catch (MalformedURLException ex) {
            return false;
        }
        //clear downloadURL of doubleslashes
        DownloadJob job = djb.createNewDownloadJob(downloadURL, targetFolder);
        proc.processDownloadJob(job);//must get proxy business object to properly call async method
        return true;
    }

    public boolean enqueueVotableAccrefDownload(IndexedSSAPVotable votable, String targetFolder) {
        List<String> accRefs = new ArrayList<>();
        for (Record r : votable.getRows()) {
            accRefs.add(votable.getAccrefColumn(r));
        }
        List<DownloadJob> jobs = djb.createNewDownloadJobs(accRefs, targetFolder);
        proc.processDownloadJobs(jobs);
        return true;
    }

    public boolean enqueueVotableDatalinkDownload(IndexedSSAPVotable votable, Map<String, String> paramMap, String targetFolder) {
        //it is necessary to construct url by using input params
        String baseUrl = votable.getDatalinkResourceUrl();
        StringBuilder postfix = new StringBuilder();
        String idParamName = null;
        try {
            for (Param p : votable.getDatalinkInputParams()) {
                if (p.isIdParam()) {
                    idParamName = p.getName();
                    continue;
                }
                String paramValue = paramMap.get(p.getName());
                if (paramValue == null) {
                    continue;
                }
                postfix.append('&').append(p.getName()).append('=').append(URLEncoder.encode(paramValue, "ASCII"));
            }
            if (idParamName == null) {
                return false;//no id param - should not happen thanks to detection during parsing
            }
            //construct url without id param
            if (!baseUrl.contains("?")) {
                baseUrl += '?';
            }
            postfix.append('&').append(idParamName).append('=');
            baseUrl += postfix;
            List<UrlWithName> urlsWithNames = new ArrayList<>();
            for (Record r : votable.getRows()) {
                String url = baseUrl + URLEncoder.encode(votable.getPubDIDColumn(r), "ASCII");
                String name = votable.getAccrefColumn(r).replaceAll("^.*?([^/]+)$", "$1").replaceAll("^(.*?)\\.?[^\\.]*$", "$1");;
                urlsWithNames.add(new UrlWithName(url, name));
            }
            //create download jobs
            List<DownloadJob> jobs = djb.createNewDownloadJobsWithNames(urlsWithNames, targetFolder);
            
            proc.processDownloadJobs(jobs);
            return true;
        } catch (UnsupportedEncodingException ex) {//should not be thrown
            Logger.getLogger(DownloadManager.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }

}
