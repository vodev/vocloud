package cz.ivoa.vocloud.downloader;

import cz.ivoa.vocloud.ejb.AbstractFacade;
import cz.ivoa.vocloud.entity.DownloadJob;
import cz.ivoa.vocloud.entity.DownloadState;
import cz.ivoa.vocloud.entity.SSAPDownloadJob;
import cz.ivoa.vocloud.entity.SSAPDownloadJobItem;
import cz.ivoa.vocloud.entity.UrlDownloadJob;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
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
    
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public UrlDownloadJob createNewUrlDownloadJob(String downloadUrl, String folderPath){
        UrlDownloadJob job = new UrlDownloadJob();
        job.setCreateTime(new Date());
        job.setDownloadUrl(downloadUrl);
        job.setSaveDir(folderPath);
        job.setState(DownloadState.CREATED);
        this.create(job);
        em.flush();
        return job;
    }
    
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public SSAPDownloadJob createNewSSAPDownloadJobs(String votableUrl, List<String> downloadUrls, String folderPath){
        if (downloadUrls == null || downloadUrls.isEmpty()){
            throw new IllegalArgumentException("downloadUrls must contain at least 1 element");
        }
        SSAPDownloadJob parent = new SSAPDownloadJob();
        parent.setCreateTime(new Date());
        parent.setSsapUrl(votableUrl);
        parent.setSaveDir(folderPath);
        parent.setState(DownloadState.CREATED);
        em.persist(parent);
        List<SSAPDownloadJobItem> jobs = new ArrayList<>();
        for (String url: downloadUrls){
            SSAPDownloadJobItem job = new SSAPDownloadJobItem();
            job.setDownloadUrl(url);
            job.setDownloadState(DownloadState.CREATED);
            job.setParent(parent);
            jobs.add(job);
            em.persist(job);
        }
        //flush all to database to obtain primary key values
        em.flush();
        parent.setItems(jobs);
        return parent;
    }
    
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public SSAPDownloadJob createNewSSAPDownloadJobsWithNames(String votableUrl, List<UrlWithName> downloadUrls, String folderPath){
        if (downloadUrls == null || downloadUrls.isEmpty()){
            throw new IllegalArgumentException("downloadUrls must contain at least 1 element");
        }
        SSAPDownloadJob parent = new SSAPDownloadJob();
        parent.setCreateTime(new Date());
        parent.setSsapUrl(votableUrl);
        parent.setSaveDir(folderPath);
        parent.setState(DownloadState.CREATED);
        em.persist(parent);
        List<SSAPDownloadJobItem> jobs = new ArrayList<>();
        for (UrlWithName i: downloadUrls){
            SSAPDownloadJobItem job = new SSAPDownloadJobItem();
            job.setDownloadUrl(i.getUrl());
            job.setFileName(i.getName());
            job.setDownloadState(DownloadState.CREATED);
            job.setParent(parent);
            jobs.add(job);
            em.persist(job);
        }
        //flush all to database to obtain primary key values
        em.flush();
        parent.setItems(jobs);
        return parent;
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
    
     public void edit(SSAPDownloadJobItem entity) {
        getEntityManager().merge(entity);
        em.flush();
    }
     
     public long countSSAPItems(DownloadJob job){
         if (job == null || ! (job instanceof SSAPDownloadJob)){
             throw new IllegalArgumentException("Passed job argument must not be null and it must be of type SSAPDownloadJob");
         }
         TypedQuery<Long> q = em.createNamedQuery("SSAPDownloadJobItem.countParentItems", Long.class);
         q.setParameter("parentJobId", job.getId());
         return q.getSingleResult();
     }
     
     public List<SSAPDownloadJobItem> findAllSSAPItemsPaginated(DownloadJob job, int offset, int count){
         if (job == null || ! (job instanceof SSAPDownloadJob)){
             throw new IllegalArgumentException("Passed job argument must not be null and it must be of type SSAPDownloadJob");
         }
         TypedQuery<SSAPDownloadJobItem> q = em.createNamedQuery("SSAPDownloadJobItem.findAllByIdOrdered", SSAPDownloadJobItem.class);
         q.setParameter("parentJobId", job.getId());
         q.setFirstResult(offset);
         q.setMaxResults(count);
         return q.getResultList();
     }
     
     public void deleteDownloadJobs(List<DownloadJob> downloadJobs){
         for (DownloadJob job: downloadJobs){
             this.remove(job);
         }
     }
     
     public void deleteAllDownloadJobs(){
         Query q1 = em.createQuery("DELETE FROM SSAPDownloadJobItem");
         Query q2 = em.createQuery("DELETE FROM DownloadJob");
         q1.executeUpdate();
         q2.executeUpdate();
     }
}
