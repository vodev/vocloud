package cz.rk.vocloud.view;

import cz.rk.vocloud.downloader.DownloadJobFacade;
import cz.rk.vocloud.entity.DownloadJob;
import java.io.Serializable;
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
public class DownloadQueueBean implements Serializable{
    
    private LazyDataModel<DownloadJob> model;

    @EJB
    private DownloadJobFacade djf;
    
    private String shownMessageLog;
    
    @PostConstruct
    private void init(){
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
    
    public void showMessageLog(DownloadJob job){
        this.shownMessageLog = job.getMessageLog();
    }
    
}
