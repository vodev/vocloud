package cz.mrq.vocloud.ejb;

import cz.mrq.vocloud.entity.UWS;
import cz.mrq.vocloud.entity.Worker;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJBException;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

/**
 *
 * @author radio.koza
 */
@Stateless
@LocalBean
public class WorkerFacade extends AbstractFacade<Worker> {

    private static final Logger LOG = Logger.getLogger(WorkerFacade.class.getName());

    //define persistence context
    @PersistenceContext(unitName = "vokorelPU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return this.em;
    }

    //non-parametric constructor definition
    public WorkerFacade() {
        super(Worker.class);
    }

    public Worker createNewWorker(String resourceUrl, String shortDescription, String description, int maxJobs) {
        try {
            //test url validity
            URL url = new URL(resourceUrl);
        } catch (MalformedURLException ex) {
            LOG.log(Level.WARNING, "Malformed url {0}", resourceUrl);
            throw new EJBException("Malformed URL " + resourceUrl);
        }
        Worker worker = new Worker(resourceUrl, shortDescription, description, maxJobs);
        //persist worker
        create(worker);
        return worker;
    }

    public void removeWorker(Integer id) {
        Worker worker = find(id);
        //remove all uws under this worker
        for (UWS i : worker.getUwsList()) {
            em.remove(i);
        }
        remove(worker);
    }
    
    public List<Worker> findAllOrderedById(){
        TypedQuery<Worker> q = em.createNamedQuery("Worker.findAllByIdOrdered", Worker.class);
        return q.getResultList();
    }
    
}
