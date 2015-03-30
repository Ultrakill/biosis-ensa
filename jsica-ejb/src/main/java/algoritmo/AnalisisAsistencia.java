/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package algoritmo;

import com.personal.utiles.FechaUtil;
import com.project.jsica.ejb.entidades.CambioTurno;
import com.project.jsica.ejb.entidades.DetalleHorario;
import com.project.jsica.ejb.entidades.DetalleRegistroAsistencia;
import com.project.jsica.ejb.entidades.Empleado;
import com.project.jsica.ejb.entidades.EmpleadoPermiso;
import com.project.jsica.ejb.entidades.Feriado;
import com.project.jsica.ejb.entidades.Horario;
import com.project.jsica.ejb.entidades.Marcacion;
import com.project.jsica.ejb.entidades.RegistroAsistencia;
import com.project.jsica.ejb.entidades.TCAnalisis;
import com.project.jsica.ejb.entidades.TCSistema;
import com.project.jsica.ejb.entidades.Vacacion;
import dao.CambioTurnoFacadeLocal;
import dao.DetalleHorarioFacadeLocal;
import dao.EmpleadoHorarioFacadeLocal;
import dao.EmpleadoPermisoFacadeLocal;
import dao.FeriadoFacadeLocal;
import dao.HorarioFacadeLocal;
import dao.JornadaFacadeLocal;
import dao.MarcacionFacadeLocal;
import dao.RegistroAsistenciaFacadeLocal;
import dao.TCAnalisisFacadeLocal;
import dao.TCSistemaFacadeLocal;
import dao.VacacionFacadeLocal;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import javax.ejb.EJB;
import org.apache.log4j.Logger;
import org.jfree.date.DateUtilities;

/**
 *
 * @author fesquivelc
 */
public class AnalisisAsistencia {
    
    @EJB
    private DetalleHorarioFacadeLocal turnosControlador;
    @EJB
    private HorarioFacadeLocal horarioControlador;
    @EJB
    private MarcacionFacadeLocal marcacionControlador;
    @EJB
    private TCSistemaFacadeLocal sistemaControlador;
    @EJB
    private TCAnalisisFacadeLocal analisisControlador;
    @EJB
    private JornadaFacadeLocal jornadaControlador;
    @EJB
    private EmpleadoHorarioFacadeLocal asignacionHorarioControlador;
    @EJB
    private RegistroAsistenciaFacadeLocal registroControlador;
    @EJB
    private EmpleadoPermisoFacadeLocal empleadoPermisoControlador;
    @EJB
    private CambioTurnoFacadeLocal cambioTurnoControlador;
    @EJB
    private VacacionFacadeLocal vacacionControlador;
    @EJB
    private FeriadoFacadeLocal feriadoControlador;
    
    private final int MIN_FIN_MARCACION = 500;
    private final int MIN_ANTES_INICIO_PERMISO = 30;
    private static final Logger LOG = Logger.getLogger(AnalisisAsistencia.class.getName());
    
    private TCAnalisis obtenerPuntoPartida(Empleado empleado) {
        sistemaControlador.limpiar();
        TCAnalisis partida = analisisControlador.find(empleado.getDocIdentidad());
        if (partida == null) {
            partida = new TCAnalisis();
            TCSistema sistema = sistemaControlador.find("BIOSIS");
            Date contrato = empleado.getFichaLaboral().getFechaContrato();
            Date fechaCero = sistema.getFechaCero();
            
            if (contrato.compareTo(fechaCero) < 0) {
                partida.setFecha(fechaCero);
                partida.setHora(sistema.getHoraCero());
            } else {
                partida.setFecha(contrato);
                partida.setHora(contrato);
            }
        }
        return partida;
    }
    
    private TCAnalisis obtenerPuntoLlegada() {
        TCAnalisis llegada = new TCAnalisis();
        Date fechaHoraActual = new Date();
        llegada.setFecha(FechaUtil.soloFecha(fechaHoraActual));
        llegada.setHora(FechaUtil.soloHora(fechaHoraActual));
        
        return llegada;
    }
    
    public void analizarEmpleados(List<Empleado> empleados) {
        TCAnalisis llegada = obtenerPuntoLlegada();
        for (Empleado e : empleados) {
            TCAnalisis partida = this.obtenerPuntoPartida(e);
            
            List<RegistroAsistencia> registros = procesarEmpleado(e, partida, llegada);
            
            if (!registros.isEmpty()) {
                registroControlador.guardarLote(registros);
                llegada.setEmpleado(e.getDocIdentidad());
                analisisControlador.edit(llegada);
            }
            
            registros.clear();
            
        }
    }

//    public List<RegistroAsistencia> analizarEmpleados(List<Empleado> empleados) {
//        //LA FECHA Y HORA DE FINAL DEL ANALISIS ES LA MISMA DE LA CONSULTA
//        List<RegistroAsistencia> registros = new ArrayList<>();
//        TCAnalisis llegada = obtenerPuntoLlegada();
//        for (Empleado empleado : empleados) {
//            //OBTENEMOS LA FECHA Y HORA DE PARTIDA DEL ANALISIS
//            TCAnalisis partida = obtenerPuntoPartida(empleado);
//
//            //OBTENEMOS LOS HORARIOS DEL EMPLEADO
//            List<Horario> horarios = obtenerHorarios(empleado);
//
//            //ANALIZAMOS POR HORARIO
//            for (Horario horario : horarios) {
//
//                registros.addAll(analizarHorario(empleado, horario, partida, llegada));
//            }
//            if (!registros.isEmpty()) {
//                this.rac.guardarLote(registros);
//                llegada.setEmpleado(empleado.getNroDocumento());
//                this.tcac.modificar(llegada);
//            }
//
//            registros.clear();
//        }
//        return registros;
//    }
    private List<RegistroAsistencia> procesarEmpleado(Empleado empleado, TCAnalisis partida, TCAnalisis llegada) {
        List<RegistroAsistencia> registros = new ArrayList<>();
        
        Calendar cal = Calendar.getInstance();
        
        Date fInicio = new Date(partida.getFecha().getTime());
        Date fFin = new Date(llegada.getFecha().getTime());
        
        Date hInicio = new Date(partida.getHora().getTime());
        Date hFin = new Date(llegada.getHora().getTime());
        
        RegistroAsistencia registro;
        
        while (fInicio.compareTo(fFin) < 0) {
            List<DetalleHorario> turnosDia = obtenerTurnos(empleado, fInicio);
            
            for (DetalleHorario turno : turnosDia) {
                //AQUI ANALIZAMOS EL CORRESPONDIENTE A ESTE DIA :3 
                if (turno.getHorarioId().getPorFecha()) {
                    //SE ANALIZA TURNO ASISTENCIAL
//                    CambioTurno turnoReemplazo = cambioTurnoControlador.
                    DetalleHorario turnoReemplazo = cambioTurnoControlador.buscarReemplazoXEmpleadoXTurno(empleado, turno);
                    
                    registro = analizarAsistencial(empleado, turno, turnoReemplazo, fInicio, fFin, hInicio, hFin);
                } else {
                    //SE ANALIZA TURNO ADMINISTRATIVO =) 
                    registro = analizarAdministrativo(empleado, turno, fInicio);
                }
                
                if (registro != null) {
                    registros.add(registro);
                }
            }

//            
            cal.setTime(fInicio);
            cal.add(Calendar.DAY_OF_MONTH, 1);
            fInicio = cal.getTime();
            
            cal.setTime(hInicio);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            
            hInicio = cal.getTime();
        }// FIN DEL WHILE

        return registros;
        
    }
    
    private List<DetalleHorario> obtenerTurnos(Empleado empleado, Date fInicio) {
        List<DetalleHorario> detalles = new ArrayList<>();
        
        return detalles;
    }
    
    private boolean isOnomastico(Empleado empleado, Date fInicio) {
        Calendar calEmpleado = Calendar.getInstance();
        Calendar calFecha = Calendar.getInstance();
        
        calEmpleado.setTime(empleado.getFechaNacimiento());
        calFecha.setTime(fInicio);
        
        calEmpleado.set(Calendar.YEAR, calFecha.get(Calendar.YEAR));
        
        return calEmpleado.getTime().compareTo(fInicio) == 0;
    }
    
    private RegistroAsistencia analizarAdministrativo(Empleado empleado, DetalleHorario turno, Date fInicio) {
        EmpleadoPermiso permisoXFecha = empleadoPermisoControlador.buscarXDia(empleado, fInicio);
        RegistroAsistencia registro = new RegistroAsistencia();
        
        if (permisoXFecha != null) {
            //SE GUARDA EL REGISTRO COMO UN PERMISO
            registro.setPermisoId(permisoXFecha.getPermisoId());
            if (permisoXFecha.getPermisoId().getMotivoPermisoCodigo().getCodigo().equals("VAC")) {
                registro.setTipo('V');
            } else if (permisoXFecha.getPermisoId().getMotivoPermisoCodigo().getCodigo().equals("LIC")) {
                registro.setTipo('L');
            } else {
                registro.setTipo('P');
            }
            
        } else {
            boolean diaLaboral = isDiaLaboral(fInicio, turno);
            if (diaLaboral) {
                //TOMAMOS EN CUENTA QUE SEA FERIADO
                Feriado feriado = this.feriadoControlador.buscarXDia(fInicio);
                if (feriado != null) {
                    //SE REGISTRA COMO FERIADO
                    registro.setFeriado(feriado);
                    registro.setTipo('E'); //RECORDAR QUE E PERTENECE A LOS FERIADOS
                } else {
                    //TOMAMOS EN CUENTA EL ONOMASTICO
                    if (isOnomastico(empleado, fInicio)) {
                        //SE REGISTRA COMO ONOMASTICO
                    } else {
                        //SE PROCEDE AL ANALISIS DE LA JORNADA
                        //SE ANALIZA EL TURNO
                        DetalleRegistroAsistencia detalle = new DetalleRegistroAsistencia();
                        
                        cal.setTime(turno.getJornadaCodigo().getHEntrada());
                        cal.add(Calendar.HOUR, -2);
                        LOG.info(String.format("TURNO DESDE: %s", cal.getTime()));
                        Date turnoDesde = cal.getTime();
                        
                        cal.setTime(turno.getJornadaCodigo().getHEntrada());
                        cal.add(Calendar.MINUTE, turno.getJornadaCodigo().getMinutosToleranciaRegularEntradaJornada());
                        LOG.info(String.format("TOLERANCIA: %s", cal.getTime()));
                        Date turnoTolerancia = cal.getTime();
                        
                        cal.setTime(turno.getJornadaCodigo().getHEntrada());
                        
                        cal.add(Calendar.MINUTE, turno.getJornadaCodigo().getMinutosToleranciaTardanzaEntradaJornada());
                        LOG.info(String.format("MAXIMA ENTRADA: %s", cal.getTime()));
                        Date turnoMaximaEntrada = cal.getTime();
                        
                        cal.setTime(turno.getJornadaCodigo().getHSalida());
                        cal.add(Calendar.HOUR, 2);
                        LOG.info(String.format("MAXIMA SALIDA: %s", cal.getTime()));
                        Date turnoMaximaSalida = cal.getTime();
                        
                        detalle = analizarTurno(empleado, registro, fInicio, fInicio, turnoDesde, turnoTolerancia, turnoMaximaEntrada, turno.getJornadaCodigo().getHSalida(), turnoMaximaSalida);
                        
                    }
                }
            } else if (isOnomastico(empleado, fInicio)) {
                //SE BUSCA EL DIA LABORAL MAS CERCANO PARA ASIGNARLE EL PERMISO POR ONOMASTICO
                //Y SE AGREGA AL REGISTRO
            } else {
                //NO HAY SUCESO SUSCEPTIBLE A REGISTRO
                registro = null;
            }
        }
        
        return registro;
    }
    
    private Calendar cal = Calendar.getInstance();
    
    private RegistroAsistencia analizarAsistencial(
            Empleado empleado,
            DetalleHorario turnoOriginal,
            DetalleHorario turnoReemplazo,
            Date fInicio,
            Date fFin,
            Date hInicio,
            Date hFin) {
        
        DetalleHorario turnoAnalizar = (turnoReemplazo == null) ? turnoOriginal : turnoReemplazo;
        
        cal.setTime(turnoAnalizar.getFecha());
        cal.add(Calendar.DATE, (turnoAnalizar.getJornadaCodigo().isTerminaDiaSiguiente()) ? 1 : 0);
        
        Date fechaFinJornada = cal.getTime();
        
        cal.setTime(turnoAnalizar.getJornadaCodigo().getHSalida());
        cal.add(Calendar.HOUR, 2);
        
        Date turnoHastaSalida = cal.getTime();
        
        if (FechaUtil.compararFechaHora(fInicio, hInicio, fechaFinJornada, turnoHastaSalida) <= 0
                && FechaUtil.compararFechaHora(fFin, hFin, fechaFinJornada, turnoHastaSalida) >= 0) {
            //SE PROCEDE A ANALIZAR
            RegistroAsistencia registro = new RegistroAsistencia();
            registro.setEmpleado(empleado);
            registro.setFecha(fInicio);
            registro.setTurnoOriginal(turnoOriginal);
            registro.setTurnoReemplazo(turnoReemplazo);
            
            List<DetalleRegistroAsistencia> detalles = new ArrayList<>();

            //SE DEBE ANALIZAR SI TIENE UN PERMISO =) 
            cal.setTime(turnoAnalizar.getJornadaCodigo().getHEntrada());
            cal.add(Calendar.HOUR, -2);
            Date turnoDesde = cal.getTime();
            
            cal.setTime(turnoAnalizar.getJornadaCodigo().getHEntrada());
            cal.add(Calendar.MINUTE, turnoAnalizar.getJornadaCodigo().getMinutosToleranciaTardanzaEntradaJornada());
            Date turnoHasta = cal.getTime();
            
            cal.setTime(turnoAnalizar.getJornadaCodigo().getHEntrada());
            cal.add(Calendar.MINUTE, turnoAnalizar.getJornadaCodigo().getMinutosToleranciaRegularEntradaJornada());
            Date turnoTolerancia = cal.getTime();
            
            detalles.add(
                    analizarTurno(
                            empleado,
                            registro,
                            turnoAnalizar.getFecha(),
                            fechaFinJornada,
                            turnoDesde,
                            turnoTolerancia,
                            turnoHasta,
                            turnoAnalizar.getJornadaCodigo().getHSalida(),
                            turnoHastaSalida));
            
            registro.setDetalleRegistroAsistenciaList(detalles);
            return registro;
        } else {
            return null;
        }
    }
//    
//    private static final Logger LOG = Logger.getLogger(AnalisisAsistencia.class.getName());

    private DetalleRegistroAsistencia analizarTurno(Empleado empleado,
            RegistroAsistencia registro,
            Date fechaInicio,
            Date fechaFin,
            Date turnoDesde,
            Date turnoTolerancia,
            Date turnoMaximaEntrada,
            Date turnoSalida,
            Date turnoMaximaSalida) {
        
        String empleadoId = getCodigoEmpleado(empleado);
        
        DetalleRegistroAsistencia detalle = new DetalleRegistroAsistencia();
        detalle.setOrden(0);
        detalle.setTipoRegistro('T');
        detalle.setRegistroAsistencia(registro);
        
        LOG.info(String.format("FECHA INICIO: %s TURNO DESDE: %s TURNO MAXIMA ENTRADA: %s", fechaInicio, turnoDesde, turnoMaximaEntrada));
        Marcacion marcacionInicio = marcacionControlador.buscarXFechaXhora(empleadoId, fechaInicio, turnoDesde, turnoMaximaEntrada);
        char resultadoInicio;
        detalle.setFechaInicio(fechaInicio);
        if (marcacionInicio == null) {
            resultadoInicio = 'F';
        } else {
            long tardanzaEntrada = tardanza(marcacionInicio.getHora(), turnoTolerancia);
            
            detalle.setHoraInicio(marcacionInicio.getHora());
            LOG.info("Hora Inicio: "+ marcacionInicio.getHora());
            if (tardanzaEntrada > 0) {
                resultadoInicio = 'T';
            } else {
                resultadoInicio = 'R';
            }
            detalle.setMilisegundosTardanza(tardanzaEntrada);
        }
        
        Marcacion marcacionFin = marcacionControlador.buscarXFechaXhora(empleadoId, fechaFin, turnoSalida, turnoMaximaSalida);
        char resultadoFin;
        
        if (marcacionFin == null) {
            resultadoFin = 'F';
        } else {
//            long tardanzaEntrada = tardanza(marcacionInicio.getHora(), turnoTolerancia);

            detalle.setHoraFin(marcacionFin.getHora());
            resultadoFin = 'R';
        }
        detalle.setFechaFin(fechaFin);
        
        if (resultadoInicio == 'F' || resultadoFin == 'F') {
            detalle.setResultado('F');
        } else if (resultadoInicio == 'T' || resultadoFin == 'T') {
            detalle.setResultado('T');
        } else if (resultadoInicio == 'R' && resultadoFin == 'R') {
            detalle.setResultado('R');
        }
        
        return detalle;
    }
    
    public long tardanza(Date horaMarcada, Date horaComparar) {
        Long diferencia = horaMarcada.getTime() - horaComparar.getTime();
        if (diferencia > 0) {
//            System.out.println("MINUTOS: "+Double.parseDouble(diferencia+"")/(1000 * 60));
            return diferencia;
        } else {
            return 0;
        }
    }
    
    private String getCodigoEmpleado(Empleado empleado) {
        return empleado.getDocIdentidad();
    }
    
    private boolean isDiaLaboral(Date fecha, DetalleHorario turno) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(fecha);
        
        if (!turno.getHorarioId().getPorFecha()) {
            int nDia = calendar.get(Calendar.DAY_OF_WEEK);
            Horario horario = turno.getHorarioId();
            //LOS DIAS VAN DESDE EL 1 AL 7 Y CUENTAN DESDE DOMINGO A SABADO
            switch (nDia) {
                case 1:
                    return horario.getDomingo();
                case 2:
                    return horario.getLunes();
                case 3:
                    return horario.getMartes();
                case 4:
                    return horario.getMiercoles();
                case 5:
                    return horario.getJueves();
                case 6:
                    return horario.getViernes();
                case 7:
                    return horario.getSabado();
                default:
                    return false;
            }
        } else {
            return false;
        }
    }
    
}
