package cz.ivoa.vocloud.ejb;

import cz.ivoa.vocloud.entity.Job;
import cz.ivoa.vocloud.entity.Phase;
import cz.ivoa.vocloud.uwsparser.model.UWSJob;
import java.text.SimpleDateFormat;
import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Singleton;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.ejb.Asynchronous;
import javax.ejb.Schedule;
import javax.ejb.Startup;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

/**
 *
 * @author voadmin
 */
@Singleton
@LocalBean
@Startup
public class SchedulerBean {

    @EJB
    private JobFacade jf;

    private final List<Job> watchedJobs = new CopyOnWriteArrayList<>();

    private Date lastUpdate;

    private static final Logger logger = Logger.getLogger(SchedulerBean.class.toString());

    @PostConstruct
    public void init() {
        logger.log(Level.INFO, "scheduler initialized");

        // find executing jobs
        watchedJobs.addAll(jf.findByPhase(Phase.EXECUTING));
        updateExecutingJobs();
    }

    /**
     * periodically query uws for job's progress
     *
     */
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    @Schedule(second = "*/10", minute = "*", hour = "*", persistent = false)
    public void updateExecutingJobs() {
        int prevsize = watchedJobs.size();
        for (Job job : watchedJobs) {
            UWSJob uwsJob = jf.refreshJob(job);
            if (job.getPhase() == Phase.COMPLETED || job.getPhase() == Phase.ERROR || job.getPhase() == Phase.ABORTED) {
//                jf.exportUWSJob(job);
                watchedJobs.remove(job);
                if (uwsJob != null){
                    jf.downloadResults(job, uwsJob);
                }
                jf.destroyRemoteJob(job);
                if (job.getResultsEmail()){
                    jf.sendResults(job);
                }
                if (job.getTargetDir() != null){
                    jf.copyResultsToFilesystem(job);
                }
            }
        }
        lastUpdate = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat();
        if (prevsize != watchedJobs.size()) {
            logger.log(Level.INFO, "update happened at {0}", sdf.format(lastUpdate));
            logger.log(Level.INFO, "watched jobs: {0}", watchedJobs.size());
        }
    }

    public void addWatchedJob(Job job) {
        if (!watchedJobs.contains(job)) {
            watchedJobs.add(job);
        }
    }

    public void removeWatchedJob(Job job) {
        if (watchedJobs.contains(job)) {
            watchedJobs.remove(job);
        }
    }

    public Date getLastUpdate() {
        return lastUpdate;
    }
    
    @Asynchronous
    public void asyncPushJobToWorker(Job job, boolean runImmediately){
        jf.createJob(job, job.getConfigurationJson(), runImmediately);
        if (runImmediately){
            addWatchedJob(job);
        }
    }
}
