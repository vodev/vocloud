package cz.mrq.vocloud.ejb;

import cz.mrq.vocloud.entity.UWSType;
import java.util.List;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

/**
 *
 * @author radio.koza
 */
@Stateless
@LocalBean
public class UWSTypeFacade extends AbstractFacade<UWSType> {

    //define persistence context
    @PersistenceContext(unitName = "vokorelPU")
    private EntityManager em;

    public UWSTypeFacade() {
        super(UWSType.class);
    }

    @Override
    protected EntityManager getEntityManager() {
        return this.em;
    }

    public UWSType createNewUWSType(String identifier, String shortDescription, String description, String documentationUrl, boolean restricted) {
        UWSType type = new UWSType(identifier, shortDescription, description, documentationUrl, restricted);
        this.create(type);
        return type;
    }

    public List<UWSType> findAllOrderedByIdentifier() {
        TypedQuery<UWSType> q = em.createNamedQuery("UWSType.findAllByIdentifierOrdered", UWSType.class);
        return q.getResultList();
    }

    public List<UWSType> findAllowedNonRestrictedTypes() {
        TypedQuery<UWSType> q = em.createNamedQuery("UWSType.findAllowedNonRestricted", UWSType.class);
        return q.getResultList();
    }
    
    public List<UWSType> findAllowedRestrictedTypes() {
        TypedQuery<UWSType> q = em.createNamedQuery("UWSType.findAllowedRestricted", UWSType.class);
        return q.getResultList();
    }

    public UWSType findByStringIdentifier(String identifier) {
        if (identifier == null) {
            throw new IllegalArgumentException("Identifier must not be null");
        }
        TypedQuery<UWSType> q = em.createNamedQuery("UWSType.findByStringIdentifier", UWSType.class);
        q.setParameter("strId", identifier);
        try {
            return q.getSingleResult();
        } catch (NoResultException ex) {
            return null;//not found
        }
    }

}
