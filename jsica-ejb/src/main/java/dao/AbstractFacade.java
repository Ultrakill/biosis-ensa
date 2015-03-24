/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dao;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import org.apache.log4j.Logger;

/**
 *
 * @author RyuujiMD
 * @param <T>
 */
public abstract class AbstractFacade<T> implements Serializable {

    protected static final String biosis_PU = "biosis-PU";
    protected static final String biostar_PU = "biostar-PU";

    private final Class<T> entityClass;
    private static final Logger LOG = Logger.getLogger(AbstractFacade.class.getName());

    public AbstractFacade(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    public void limpiar() {
        getEntityManager().clear();
    }

    protected abstract EntityManager getEntityManager();

    public void create(T entity) {

        try {            
            getEntityManager().persist(entity);
            LOG.info("SE CREA LA ENTIDAD: " + entity);
        } catch (Exception e) {
            LOG.warn(e);
        }

    }

    public void edit(T entity) {
        try {            
            getEntityManager().merge(entity);
            LOG.info("SE EDITA LA ENTIDAD");
        } catch (Exception e) {
            LOG.warn(e);
        }

    }

    public void remove(T entity) {
        LOG.info("SE ELIMINA LA ENTIDAD");
        getEntityManager().remove(getEntityManager().merge(entity));
    }

    public T find(Object id) {
        LOG.info("FIN ESTO: "+id);
        return getEntityManager().find(entityClass, id);
    }

    public List<T> findAll() {
        javax.persistence.criteria.CriteriaQuery cq = getEntityManager().getCriteriaBuilder().createQuery();
        cq.select(cq.from(entityClass));

        return getEntityManager().createQuery(cq).getResultList();
    }

    public List<T> findRange(int[] range) {
        javax.persistence.criteria.CriteriaQuery cq = getEntityManager().getCriteriaBuilder().createQuery();
        cq.select(cq.from(entityClass));
        javax.persistence.Query q = getEntityManager().createQuery(cq);
        q.setMaxResults(range[1] - range[0] + 1);
        q.setFirstResult(range[0]);
        return q.getResultList();
    }

    public int count() {
        javax.persistence.criteria.CriteriaQuery cq = getEntityManager().getCriteriaBuilder().createQuery();
        javax.persistence.criteria.Root<T> rt = cq.from(entityClass);
        cq.select(getEntityManager().getCriteriaBuilder().count(rt));
        javax.persistence.Query q = getEntityManager().createQuery(cq);
        return ((Long) q.getSingleResult()).intValue();
    }

    public List<T> search(String namedQuery) {
        return this.search(namedQuery, null, -1, -1);
    }

    public List<T> search(String namedQuery, Map<String, Object> parametros) {
        return this.search(namedQuery, parametros, -1, -1);
    }

    public List<T> search(String namedQuery, Map<String, Object> parametros, int inicio, int tamanio) {
        Query query = getEntityManager().createQuery(namedQuery);

        if (parametros != null) {
            for (Map.Entry<String, Object> entry : parametros.entrySet()) {
                query.setParameter(entry.getKey(), entry.getValue());
            }
        }

        if (inicio != -1) {
            query.setFirstResult(inicio);
        }

        if (tamanio != -1) {
            query.setMaxResults(tamanio);
        }

        List<T> lista = query.getResultList();

        return lista;
    }

    public int contar(String queryJPQL, Map<String, Object> parametros) {
        try {
            Query query = getEntityManager().createQuery(queryJPQL);

            if (parametros != null) {
                for (Map.Entry<String, Object> entry : parametros.entrySet()) {
                    query.setParameter(entry.getKey(), entry.getValue());
                }
            }

            Long conteo = (Long) query.getSingleResult();

            return conteo.intValue();
        } catch (Exception e) {
            LOG.error("ERROR AL CONTAR: " + e.getLocalizedMessage() + " " + e.getMessage());
            return 0;
        }

    }

}
