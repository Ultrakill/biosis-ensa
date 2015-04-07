/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package dao;

import com.project.jsica.ejb.entidades.MotivoPermiso;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 *
 * @author RyuujiMD
 */
@Stateless
public class MotivoPermisoFacade extends AbstractFacade<MotivoPermiso> implements MotivoPermisoFacadeLocal {
    @PersistenceContext(unitName = biosis_PU)
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public MotivoPermisoFacade() {
        super(MotivoPermiso.class);
    }
    
    @Override
    public List<MotivoPermiso> buscarXTipo(String tipo) {

        String jpql = "SELECT mp FROM MotivoPermiso mp WHERE mp.codigo = :tipo";

        Map<String, Object> parametros = new HashMap<>();
        parametros.put("tipo", tipo);

        return this.search(jpql, parametros);
    }
    
}
