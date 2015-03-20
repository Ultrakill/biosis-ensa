/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package dao;

import com.project.jsica.ejb.entidades.Horario;
import java.util.Date;
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
public class HorarioFacade extends AbstractFacade<Horario> implements HorarioFacadeLocal {
    @PersistenceContext(unitName = biosis_PU)
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public HorarioFacade() {
        super(Horario.class);
    }
    
    public List<Horario> buscarXFecha(Date fecha){
        String jpql = "SELECT h FROM Horario h WHERE h.fecha = :fecha";
        Map<String,Object> mapa = new HashMap<>();
        mapa.put("fecha", fecha);
        
        return search(jpql, mapa);
    }
    
}
