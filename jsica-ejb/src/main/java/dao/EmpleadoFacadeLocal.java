/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package dao;

import com.project.jsica.ejb.entidades.Empleado;
import java.util.List;
import java.util.Map;
import javax.ejb.Local;

/**
 *
 * @author RyuujiMD
 */
@Local
public interface EmpleadoFacadeLocal {

    void create(Empleado empleado);

    void edit(Empleado empleado);

    void remove(Empleado empleado);

    Empleado find(Object id);

    List<Empleado> findAll();

    List<Empleado> findRange(int[] range);
    
    List<Empleado> search(String namedQuery);
    
    List<Empleado> search(String namedQuery, Map<String, Object> parametros);
    
    List<Empleado> search(String namedQuery, Map<String, Object> parametros, int inicio, int tamanio);

    int count();

    List<Empleado> buscarTodos();
    List<Empleado> buscarXEmpleado(String dni);
    
}
