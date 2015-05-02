package cz.mrq.vocloud.ejb;

import cz.mrq.vocloud.entity.UWS;
import cz.mrq.vocloud.entity.UWSType;
import cz.mrq.vocloud.entity.Worker;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.logging.Logger;
import javax.ejb.EJBException;
import javax.persistence.EntityNotFoundException;

/**
 *
 * @author voadmin
 */
@Stateless
public class UWSFacade extends AbstractFacade<UWS> {

    private static final Logger logger = Logger.getLogger(UWSFacade.class.getName());

    @PersistenceContext(unitName = "vokorelPU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public UWSFacade() {
        super(UWS.class);
    }

    public void createWithAssignedRelations(UWS entity, Integer uwsTypeId, Integer workerId) {
        try {
            UWSType uwsType = em.getReference(UWSType.class, uwsTypeId);
            Worker worker = em.getReference(Worker.class, workerId);
            entity.setUwsType(uwsType);
            entity.setWorker(worker);
            this.create(entity);
        } catch (EntityNotFoundException ex) {
            throw new EJBException(ex);//rethrow
        }
    }

    public void editWithAssignedRelations(UWS entity, Integer uwsTypeId, Integer workerId){
        try {
            UWSType uwsType = em.getReference(UWSType.class, uwsTypeId);
            Worker worker = em.getReference(Worker.class, workerId);
            entity.setUwsType(uwsType);
            entity.setWorker(worker);
            this.edit(entity);
        } catch (EntityNotFoundException ex){
            throw new EJBException(ex);//rethrow
        }
    }
//    public UWS findOneByType(String type) {
//        UWS uws = null;
//        Query q = getEntityManager().createNamedQuery("UWS.findByType");
//        q.setParameter("jobType", type);
//        try {
//            uws = (UWS) q.getSingleResult();
//        } catch (PersistenceException pe) {
//            logger.log(Level.WARNING, "query failed: {0}", pe.toString());
//        }
//        return uws;
//    }
//
//    /**
//     * chooses best uws for the job
//     *
//     */
//    public UWS assign(String type) {
//        List<UWS> allUWS = this.findAll();
//        UWS selected = null;
//        float lowestLoad = 100;
//        for (UWS uws : allUWS) {
//            if (uws.getEnabled() && uws.getType().equals(type)) {
//                float load = getLoad(uws);
//                logger.log(Level.INFO, "queering uws: {0} ({1}), load: {2}", new Object[]{uws.getLabel(), uws.getId(), load});
//
//                if (load == -1) {
//                    continue;
//                }
//
//                if (load < lowestLoad) {
//                    selected = uws;
//                    lowestLoad = load;
//                }
//
//            }
//        }
//        return selected;
//    }
//
//    /**
//     * check for availability of the uws
//     *
//     * @param uws
//     * @return
//     */
//    public boolean isOnline(UWS uws) {
//        try {
//            int code = Toolbox.getResponseCode(uws.getLocationUrl());
//            if (code == 200) {
//                return true;
//            }
//        } catch (IOException ex) {
//            return false;
//        }
//        return false;
//    }
//
//    /**
//     * counts jobs in phase RUNNING or QUEUED
//     *
//     * @param uws
//     * @return number of jobs, returns -1 if unavailable
//     */
//    public int countRunningJobs(UWS uws) {
//        String ret;
//        try {
//            ret = Toolbox.httpGet(uws.getLocationUrl());
//        } catch (IOException ex) {
//            return -1;
//        }
//
//        Pattern p = Pattern.compile("QUEUED|EXECUTING");
//        Matcher m = p.matcher(ret);
//        int counter = 0;
//        while (m.find()) {
//            counter++;
//        }
//        logger.log(Level.INFO, "found {0} running jobs at {1}", new Object[]{counter, uws.getLocationUrl()});
//        return counter;
//    }
//
//    public float getLoad(UWS uws) {
//        int running = countRunningJobs(uws);
//        if (running == -1) {
//            return -1;
//        }
//
//        return ((float) running / (float) uws.getThreads());
//    }
//
//    //TODO rework rating based on priority
//    public float getRating(UWS uws) {
//        float load = getLoad(uws);
//        if (load == -1) {
//            return -1;
//        }
//
//        return load / 100;
//    }
}
