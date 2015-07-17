package cz.ivoa.vocloud.view;

import cz.ivoa.vocloud.downloader.DownloadJobFacade;
import cz.ivoa.vocloud.entity.DownloadJob;
import cz.ivoa.vocloud.entity.SSAPDownloadJobItem;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortOrder;

/**
 *
 * @author radio.koza
 */
@Named
@ViewScoped
public class DownloadQueueBean implements Serializable {

    private LazyDataModel<DownloadJob> model;
    private Map<DownloadJob, LazyDataModel<SSAPDownloadJobItem>> ssapModels;

    @EJB
    private DownloadJobFacade djf;

    private String shownMessageLog;

    @PostConstruct
    private void init() {
        ssapModels = new HashMap<>();
        model = new LazyDataModel<DownloadJob>() {
            private static final long serialVersionUID = 1L;

            @Override
            public List<DownloadJob> load(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String, Object> filters) {
                List<DownloadJob> jobs = djf.findAllJobsPaginated(first, pageSize);//sorting and filtering will not be used
                model.setRowCount(djf.count());
                return jobs;
            }

        };
    }

    public LazyDataModel<DownloadJob> getModel() {
        return model;
    }

    public void setModel(LazyDataModel<DownloadJob> model) {
        this.model = model;
    }

    public String getShownMessageLog() {
        return shownMessageLog;
    }

    public void showMessageLog(DownloadJob job) {
        this.shownMessageLog = job.getMessageLog();
    }

    public LazyDataModel<SSAPDownloadJobItem> fetchLazySSAPModel(final DownloadJob job) {
        if (job == null) {
            throw new IllegalArgumentException("passed job argument is null");
        }
        LazyDataModel<SSAPDownloadJobItem> tmpModel = ssapModels.get(job);
        if (tmpModel == null) {
            //create new one
            tmpModel = new LazyDataModel<SSAPDownloadJobItem>() {
                private static final long serialVersionUID = 1L;

                @Override
                public List<SSAPDownloadJobItem> load(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String, Object> filters) {
                    List<SSAPDownloadJobItem> jobs = djf.findAllSSAPItemsPaginated(job, first, pageSize);
                    this.setRowCount((int) djf.countSSAPItems(job));
                    return jobs;
                }
            };
            ssapModels.put(job, tmpModel);
        }
        return tmpModel;
    }

}
