package cz.ivoa.vocloud.downloader;

import cz.ivoa.vocloud.ejb.AbstractFacade;
import cz.ivoa.vocloud.entity.DownloadJob;
import cz.ivoa.vocloud.entity.DownloadState;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

/**
 *
 * @author radio.koza
 */
@Stateless
public class DownloadJobFacade extends AbstractFacade<DownloadJob> {
    private static final Logger LOG = Logger.getLogger(DownloadJobFacade.class.getName());

    
    
    @PersistenceContext(unitName = "vokorelPU")
    private EntityManager em;

    public DownloadJobFacade() {
        super(DownloadJob.class);
    }
    
    @Override
    protected EntityManager getEntityManager() {
        return em;
    }
    
    public DownloadJob createNewDownloadJob(String downloadUrl, String folderPath){
        DownloadJob job = new DownloadJob();
        job.setCreateTime(new Date());
        job.setDownloadUrl(downloadUrl);
        job.setSaveDir(folderPath);
        job.setState(DownloadState.CREATED);
        this.create(job);
        return job;
    }
    
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public List<DownloadJob> createNewDownloadJobs(List<String> downloadUrls, String folderPath){
        List<DownloadJob> jobs = new ArrayList<>();
        for (String url: downloadUrls){
            DownloadJob job = new DownloadJob();
            job.setCreateTime(new Date());
            job.setDownloadUrl(url);
            job.setSaveDir(folderPath);
            job.setState(DownloadState.CREATED);
            jobs.add(job);
            em.persist(job);
        }
        //flush all to database to obtain primary key values
        em.flush();
        return jobs;
    }
    
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public List<DownloadJob> createNewDownloadJobsWithNames(List<UrlWithName> downloadUrls, String folderPath){
        List<DownloadJob> jobs = new ArrayList<>();
        for (UrlWithName i: downloadUrls){
            DownloadJob job = new DownloadJob();
            job.setCreateTime(new Date());
            job.setDownloadUrl(i.getUrl());
            job.setFileName(i.getName());
            job.setSaveDir(folderPath);
            job.setState(DownloadState.CREATED);
            jobs.add(job);
            em.persist(job);
        }
        //flush all to database to obtain primary key values
        em.flush();
        return jobs;
    }

    public List<DownloadJob> findUnfinishedJobs(){
        List<DownloadState> allowedStates = Arrays.asList(new DownloadState[]{DownloadState.CREATED, DownloadState.RUNNING});
        TypedQuery<DownloadJob> query = getEntityManager().createNamedQuery("DownloadJob.findJobsInStates", DownloadJob.class);
        query.setParameter("states", allowedStates);
        return query.getResultList();
    }
    
    public List<DownloadJob> findAllJobsPaginated(int offset, int count){
        TypedQuery<DownloadJob> q = em.createNamedQuery("DownloadJob.findCreateDescOrdered", DownloadJob.class);
        q.setFirstResult(offset);
        q.setMaxResults(count);
        return q.getResultList();
    }
    
}
