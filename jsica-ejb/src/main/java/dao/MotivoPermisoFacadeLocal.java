/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package dao;

import com.project.jsica.ejb.entidades.MotivoPermiso;
import java.util.List;
import java.util.Map;
import javax.ejb.Local;

/**
 *
 * @author RyuujiMD
 */
@Local
public interface MotivoPermisoFacadeLocal {

    void create(MotivoPermiso motivoPermiso);

    void edit(MotivoPermiso motivoPermiso);

    void remove(MotivoPermiso motivoPermiso);

    MotivoPermiso find(Object id);

    List<MotivoPermiso> findAll();

    List<MotivoPermiso> findRange(int[] range);
    
    List<MotivoPermiso> search(String namedQuery);
    
    List<MotivoPermiso> search(String namedQuery, Map<String, Object> parametros);
    
    List<MotivoPermiso> search(String namedQuery, Map<String, Object> parametros, int inicio, int tamanio);

    int count();
    
    List<MotivoPermiso> buscarXTipo(String tipo);
    
}
