/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.project.jsica.ejb.dao;

import com.project.jsica.ejb.entidades.FichaLaboralEmpleado;
import java.util.List;
import javax.ejb.Local;

/**
 *
 * @author RyuujiMD
 */
@Local
public interface FichaLaboralEmpleadoFacadeLocal {

    void create(FichaLaboralEmpleado fichaLaboralEmpleado);

    void edit(FichaLaboralEmpleado fichaLaboralEmpleado);

    void remove(FichaLaboralEmpleado fichaLaboralEmpleado);

    FichaLaboralEmpleado find(Object id);

    List<FichaLaboralEmpleado> findAll();

    List<FichaLaboralEmpleado> findRange(int[] range);

    int count();
    
}
