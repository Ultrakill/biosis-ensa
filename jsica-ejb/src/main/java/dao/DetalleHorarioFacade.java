/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dao;

import com.personal.utiles.FechaUtil;
import com.project.jsica.ejb.entidades.DetalleHorario;
import com.project.jsica.ejb.entidades.Empleado;
import com.project.jsica.ejb.entidades.EmpleadoHorario;
import com.project.jsica.ejb.entidades.Horario;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 *
 * @author RyuujiMD
 */
@Stateless
public class DetalleHorarioFacade extends AbstractFacade<DetalleHorario> implements DetalleHorarioFacadeLocal {

    @PersistenceContext(unitName = biosis_PU)
    private EntityManager em;

    @EJB
    private EmpleadoHorarioFacadeLocal empHorarioControlador;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public DetalleHorarioFacade() {
        super(DetalleHorario.class);
    }

    @Override
    public List<DetalleHorario> buscarXEmpleado(Empleado empleado, Integer mes, Integer anio) {
        int primero = 1;
        int ultimo = FechaUtil.ultimoDiaMes(mes, anio);

        String fechaI = "{d '" + anio + "-" + mes + "-" + primero + "'}";
        String fechaF = "{d '" + anio + "-" + mes + "-" + ultimo + "'}";

        String sql;

        Map<String, Object> parametros = new HashMap<>();

        if (empleado.getGrupoHorarioId() != null) {
            sql = "SELECT eh FROM EmpleadoHorario eh "
                    + "WHERE (eh.porGrupo = TRUE AND eh.grupoHorarioId = :grupo) "
                    + "AND (eh.horarioId.porFecha = FALSE "
                    + "OR (eh.horarioId.porFecha = TRUE AND eh.horarioId.fecha BETWEEN " + fechaI + " AND " + fechaF + ")) ";
            parametros.put("grupo", empleado.getGrupoHorarioId());
        } else {
            sql = "SELECT eh FROM EmpleadoHorario eh "
                    + "WHERE (eh.porGrupo = FALSE AND eh.empleadoId.docIdentidad = :dni) "
                    + "AND (eh.horarioId.porFecha = FALSE "
                    + "OR (eh.horarioId.porFecha = TRUE AND eh.horarioId.fecha BETWEEN " + fechaI + " AND " + fechaF + ")) ";
            parametros.put("dni", empleado.getDocIdentidad());
        }

        List<EmpleadoHorario> empleadoHorarios = empHorarioControlador.search(sql, parametros);
        List<DetalleHorario> lista = new ArrayList<>();
        for (EmpleadoHorario empleadoHorario : empleadoHorarios) {
            List<DetalleHorario> horarioJornadas = empleadoHorario.getHorarioId().getDetalleHorarioList();
            lista.addAll(horarioJornadas);
        }
        return lista;
    }

    @Override
    public List<DetalleHorario> buscarXEmpleadoXFecha(Empleado empleado, Date fecha) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(fecha);
        
        cal.set(Calendar.DAY_OF_MONTH, 1);
        
        Date inicio = cal.getTime();
        
        cal.set(Calendar.DAY_OF_MONTH, cal.getMaximum(Calendar.DAY_OF_MONTH));
        
        Date fin = cal.getTime();
        Map<String,Object> mapa = new HashMap<>();
        String jpql;
        if(empleado.getGrupoHorarioId() == null){
            jpql = "SELECT eh FROM EmpleadoHorario eh WHERE eh.empleadoId = :empleado";
        }else{
            jpql = "SELECT eh FROM EmpleadoHorario eh WHERE eh.empleadoId = :empleado OR eh.grupoHorarioId = :grupo";
            mapa.put("grupo", empleado.getGrupoHorarioId());
        }        
        
        mapa.put("empleado", empleado);
        
        List<EmpleadoHorario> listaHorario = empHorarioControlador.search(jpql, mapa);
        
        
        List<DetalleHorario> detalles = new ArrayList<>();
        
        for(EmpleadoHorario empHor : listaHorario){
            if(empHor.getHorarioId().getPorFecha()){
                List<DetalleHorario> turnos = this.buscarXHorarioXFecha(empHor.getHorarioId(), fecha);
                if(!turnos.isEmpty()){
                    detalles.addAll(turnos);
                }
            }else{
                DetalleHorario turno = empHor.getHorarioId().getDetalleHorarioList().get(0);
                if(isLaboral(turno,fecha)){
                    detalles.add(turno);
                }
            }
        }
        
        return detalles;
    }
    
    public List<DetalleHorario> buscarXHorarioXFecha(Horario horario, Date fecha){
        String jpql = "SELECT dh FROM DetalleHorario dh WHERE dh.empleadoId = :horario AND dh.fecha = :fecha";
        Map<String,Object> mapa = new HashMap<>();
        mapa.put("horario", horario);
        mapa.put("fecha", fecha);
        
        return search(jpql, mapa);
    }
    
    public boolean isLaboral(DetalleHorario turno, Date fecha){
        Calendar cal = Calendar.getInstance();
        cal.setTime(fecha);
        Horario horario = turno.getHorarioId();
        switch(cal.get(Calendar.DAY_OF_WEEK)){
            case Calendar.SUNDAY:
                return horario.getDomingo();
            case Calendar.MONDAY:
                return horario.getLunes();
            case Calendar.TUESDAY:
                return horario.getMartes();
            case Calendar.WEDNESDAY:
                return horario.getMiercoles();
            case Calendar.THURSDAY:
                return horario.getJueves();
            case Calendar.FRIDAY:
                return horario.getViernes();
            case Calendar.SATURDAY:
                return horario.getSabado();
            default:
                return false;
        }
    }

}
