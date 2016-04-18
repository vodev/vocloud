package cz.ivoa.vocloud.view;

import cz.ivoa.vocloud.downloader.DownloadJobFacade;
import cz.ivoa.vocloud.entity.DownloadJob;
import cz.ivoa.vocloud.entity.SSAPDownloadJob;
import cz.ivoa.vocloud.entity.SSAPDownloadJobItem;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
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
    private Map<DownloadJob, Statistics> ssapStatistics;
    private List<DownloadJob> selectedJobs;

    @EJB
    private DownloadJobFacade djf;

    private String shownMessageLog;

    @PostConstruct
    private void init() {
        ssapModels = new HashMap<>();
        ssapStatistics = new HashMap<>();
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
        if (!(job instanceof SSAPDownloadJob)){
            return null;//no lazy data model
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
            int finished = (int) djf.countFinishedSSAPItems(job);
            int failed = (int) djf.countFailedSSAPItems(job);
            ssapStatistics.put(job, new Statistics(finished, failed));
        }
        return tmpModel;
    }

    public List<DownloadJob> getSelectedJobs() {
        return selectedJobs;
    }

    public void setSelectedJobs(List<DownloadJob> selectedJobs) {
        this.selectedJobs = selectedJobs;
    }

    public void refresh(){
        this.selectedJobs = null;
    }
    //TODO delete only finished or failed jobs
    public void deleteSelected(){
        if (selectedJobs == null){
            return;//do nothing if nothing is selected
        }
        djf.deleteDownloadJobs(selectedJobs);
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "Jobs were successfully deleted"));
    }
    
    public void deleteAll(){
        djf.deleteAllDownloadJobs();
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "Jobs were successfully deleted"));
    }

    public int countFinished(DownloadJob job){
        Statistics stat = ssapStatistics.get(job);
        if (stat == null){
            return 0;
        }
        return stat.getFinishedCount();
    }

    public int countFailed(DownloadJob job){
        Statistics stat = ssapStatistics.get(job);
        if (stat == null){
            return 0;
        }
        return stat.getFailedCount();
    }

    private static class Statistics {

        private final int finishedCount;
        private final int failedCount;

        public Statistics(int finishedCount, int failedCount){
            this.finishedCount = finishedCount;
            this.failedCount = failedCount;
        }

        public int getFailedCount() {
            return failedCount;
        }

        public int getFinishedCount() {
            return finishedCount;
        }
    }

}
