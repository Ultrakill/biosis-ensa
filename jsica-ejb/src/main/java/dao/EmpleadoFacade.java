/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package dao;

import com.project.jsica.ejb.entidades.Empleado;
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
public class EmpleadoFacade extends AbstractFacade<Empleado> implements EmpleadoFacadeLocal {
    @PersistenceContext(unitName = biosis_PU)
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public EmpleadoFacade() {
        super(Empleado.class);
    }

    @Override
    public List<Empleado> buscarTodos() {
        return this.search("SELECT e FROM Empleado e");
    }
    
    @Override
    public List<Empleado> buscarXEmpleado(String dni) {

        String jpql = "SELECT em FROM Empleado em WHERE em.docIdentidad = :dni";

        Map<String, Object> parametros = new HashMap<>();
        parametros.put("dni", dni);

        return this.search(jpql, parametros);
    }
    
}
