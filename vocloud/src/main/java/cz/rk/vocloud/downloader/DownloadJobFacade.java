package cz.rk.vocloud.downloader;

import cz.mrq.vocloud.ejb.AbstractFacade;
import cz.rk.vocloud.entity.DownloadJob;
import cz.rk.vocloud.entity.DownloadState;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

/**
 *
 * @author radio.koza
 */
@Stateless
public class DownloadJobFacade extends AbstractFacade<DownloadJob> {

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
        em.flush();
        return job;
    }
    
    public List<DownloadJob> createNewDownloadJobs(List<String> downloadUrls, String folderPath){
        List<DownloadJob> jobs = new ArrayList<>();
        Date now = new Date();
        for (String url: downloadUrls){
            DownloadJob job = new DownloadJob();
            job.setCreateTime(now);
            job.setDownloadUrl(url);
            job.setSaveDir(folderPath);
            job.setState(DownloadState.CREATED);
            jobs.add(job);
            this.create(job);
        }
        //flush all to database to obtain primary key values
        em.flush();
        return jobs;
    }
    
    public List<DownloadJob> createNewDownloadJobsWithNames(List<UrlWithName> downloadUrls, String folderPath){
        List<DownloadJob> jobs = new ArrayList<>();
        Date now = new Date();
        for (UrlWithName i: downloadUrls){
            DownloadJob job = new DownloadJob();
            job.setCreateTime(now);
            job.setDownloadUrl(i.getUrl());
            job.setFileName(i.getName());
            job.setSaveDir(folderPath);
            job.setState(DownloadState.CREATED);
            jobs.add(job);
            this.create(job);
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
