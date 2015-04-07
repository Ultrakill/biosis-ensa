/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.project.algoritmo;

import com.personal.utiles.FechaUtil;
import com.project.jsica.ejb.entidades.Area;
import com.project.jsica.ejb.entidades.DetalleHorario;
import com.project.jsica.ejb.entidades.DetalleRegistroAsistencia;
import com.project.jsica.ejb.entidades.Empleado;
import com.project.jsica.ejb.entidades.EmpleadoPermiso;
import com.project.jsica.ejb.entidades.Feriado;
import com.project.jsica.ejb.entidades.Marcacion;
import com.project.jsica.ejb.entidades.MotivoPermiso;
import com.project.jsica.ejb.entidades.Permiso;
import com.project.jsica.ejb.entidades.RegistroAsistencia;
import com.project.jsica.ejb.entidades.TCAnalisis;
import com.project.jsica.ejb.entidades.TCSistema;
import com.project.jsica.ejb.entidades.Vacacion;
import dao.CambioTurnoFacadeLocal;
import dao.DetalleHorarioFacadeLocal;
import dao.EmpleadoFacadeLocal;
import dao.EmpleadoHorarioFacadeLocal;
import dao.EmpleadoPermisoFacadeLocal;
import dao.FeriadoFacadeLocal;
import dao.HorarioFacadeLocal;
import dao.JornadaFacadeLocal;
import dao.MarcacionFacadeLocal;
import dao.MotivoPermisoFacadeLocal;
import dao.PermisoFacade;
import dao.PermisoFacadeLocal;
import dao.RegistroAsistenciaFacadeLocal;
import dao.TCAnalisisFacadeLocal;
import dao.TCSistemaFacadeLocal;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.inject.Inject;
import org.apache.log4j.Logger;

/**
 *
 * @author fesquivelc
 */
@Stateless
public class AnalisisFinal implements AnalisisFinalLocal {

    @EJB
    private FeriadoFacadeLocal feriadoControlador;
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
    private EmpleadoFacadeLocal empleadoControlador;

    @EJB
    private MotivoPermisoFacadeLocal motivoPermisoControlador;
    @EJB
    private PermisoFacadeLocal permisoControlador;

    private final int MIN_FIN_MARCACION = 500;
    private final int MIN_ANTES_INICIO_PERMISO = 30;

    private TCAnalisis obtenerPuntoPartida(Empleado empleado) {
        sistemaControlador.limpiar();
        TCAnalisis partida = analisisControlador.find((String) empleado.getDocIdentidad());
        if (partida == null) {
            partida = new TCAnalisis();
            TCSistema sistema = sistemaControlador.find("BIOSIS");
            Date contrato = empleado.getFichaLaboral().getFechaContrato();
            Date fechaCero = sistema.getFechaCero();

            if (contrato == null) {
                partida.setFecha(fechaCero);
                partida.setHora(sistema.getHoraCero());
            } else {
                if (contrato.compareTo(fechaCero) < 0) {
                    partida.setFecha(fechaCero);
                    partida.setHora(sistema.getHoraCero());
                } else {
                    partida.setFecha(contrato);
                    partida.setHora(contrato);
                }
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

    @Override
    public void analizarEmpleados(List<Empleado> empleados) {
        TCAnalisis llegada = obtenerPuntoLlegada();
        for (Empleado e : empleados) {
            TCAnalisis partida = this.obtenerPuntoPartida(e);

            List<RegistroAsistencia> registros = procesarEmpleado(e, partida, llegada);

            if (!registros.isEmpty()) {
                LOG.info("GUARDANDO REGISTROS");
                for (RegistroAsistencia ra : registros) {
                    LOG.info("REGISTRO: " + ra.getFecha() + " " + ra.getTipo() + " " + ra.getEmpleado().getApellidos());
                }

                registroControlador.guardarLote(registros);
                llegada.setEmpleado(e.getDocIdentidad());
                analisisControlador.edit(llegada);
            }

            registros.clear();

        }
    }

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
                    registro = analizarAdministrativo(empleado, turno, fInicio, fFin, hInicio, hFin);
                }

                if (registro != null) {
                    registros.add(registro);
                }
            }

//            AsignacionPermiso permisoXFecha = this.apc.buscarXDia(empleado.get, fInicio);
//
//            if (permisoXFecha != null) {
//                //SE GUARDA EL REGISTRO COMO UN PERMISO
//                registro.setPermiso(permisoXFecha.getPermiso());
//                registro.setTipoAsistencia('P');
//            } else {
//                Vacacion vacacion = this.vc.buscarXDia(empleado.getNroDocumento(), fInicio);
//
//                if (vacacion != null) {
//                    //SE GUARDA EL REGISTRO COMO VACACION
//                    registro.setVacacion(vacacion);
//                    registro.setTipoAsistencia('V');
//
//                } else {
//                    boolean diaLaboral = isDiaLaboral(fInicio, horario);
//                    if (diaLaboral) {
//                        //TOMAMOS EN CUENTA QUE SEA FERIADO
//                        Feriado feriado = this.fc.buscarXDia(fInicio);
//                        if (feriado != null) {
//                            //SE REGISTRA COMO FERIADO
//                            registro.setFeriado(feriado);
//                            registro.setTipoAsistencia('E'); //RECORDAR QUE E PERTENECE A LOS FERIADOS
//                        } else {
//                            //TOMAMOS EN CUENTA EL ONOMASTICO
//                            if (isOnomastico(empleado, fInicio)) {
//                                //SE REGISTRA COMO ONOMASTICO
//                            } else {
//                                //SE PROCEDE AL ANALISIS DE LA JORNADA
//                                registro = analizarJornada2(empleado, horario.getJornada(), fInicio, hInicio, fInicio, fFin, hFin);
//                                if (registro != null) {
//                                    registro.setHorario(horario);
//                                }
//                            }
//                        }
//                    } else if (isOnomastico(empleado, fInicio)) {
//                            //SE BUSCA EL DIA LABORAL MAS CERCANO PARA ASIGNARLE EL PERMISO POR ONOMASTICO
//                        //Y SE AGREGA AL REGISTRO
//                    } else {
//                        //NO HAY SUCESO SUSCEPTIBLE A REGISTRO
//                        registro = null;
//                    }
//                }
////                }// FIN DEL ELSE PRINCIPAL
//
//                if (registro != null) {
//                    registros.add(registro);
//                }
//            }
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
        List<DetalleHorario> detalles;

        detalles = turnosControlador.buscarXEmpleadoXFecha(empleado, fInicio);

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

    private RegistroAsistencia analizarAdministrativo(Empleado empleado, DetalleHorario turno, Date fInicio, Date fFin, Date hInicio, Date hFin) {
        LOG.info("ANALISIS ADMINISTRATIVO PARA EL TURNO: " + turno.getId() + " FECHA: " + fInicio.toString());
        cal.setTime(turno.getJornadaCodigo().getHSalida());
        cal.add(Calendar.HOUR, 2);
        RegistroAsistencia registro = new RegistroAsistencia();
        registro.setFecha(fInicio);
        registro.setEmpleado(empleado);
        registro.setHoraEntrada(turno.getJornadaCodigo().getHEntrada());
        registro.setHoraSalida(turno.getJornadaCodigo().getHSalida());
        registro.setHoraSalidaRefrigerio(turno.getJornadaCodigo().getHSalidaRefrigerio());
        registro.setHoraEntradaRefrigerio(turno.getJornadaCodigo().getHEntradaRefrigerio());
        
        Date turnoHastaSalida = cal.getTime();
        if (FechaUtil.compararFechaHora(fInicio, hInicio, fInicio, turnoHastaSalida) <= 0
                && FechaUtil.compararFechaHora(fFin, hFin, fInicio, turnoHastaSalida) >= 0) {
            LOG.info("PASO");
            EmpleadoPermiso permisoXFecha = this.empleadoPermisoControlador.buscarXDia(empleado, fInicio);
            LOG.info("ANALIZANDO EMPLEADO: " + empleado.getApellidos() + " " + empleado.getNombres());

            if (permisoXFecha != null) {
                //SE GUARDA EL REGISTRO COMO UN PERMISO
                registro.setPermisoId(permisoXFecha.getPermisoId());
                registro.setTipo('P');
            } else {
//                Vacacion vacacion = this.vc.buscarXDia(empleado.getNroDocumento(), fInicio);

//                if (vacacion != null) {
//                    //SE GUARDA EL REGISTRO COMO VACACION
//                    registro.setVacacion(vacacion);
//                    registro.setTipoAsistencia('V');
//
//                } else {
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
                            registro.setPermisoId(generarPermisoOnomastico(empleado.getDocIdentidad(), fInicio));
                            registro.setTipo('P');
                        } else {
                            //SE PROCEDE AL ANALISIS DE LA JORNADA
                            List<DetalleRegistroAsistencia> detalles = new ArrayList<>();
                            Calendar calendar = Calendar.getInstance();
                            calendar.set(1970, 0, 1, 4, 0, 0);
                            Date desde = FechaUtil.soloHora(calendar.getTime());
                            calendar.setTime(turno.getJornadaCodigo().getHEntrada());
                            calendar.add(Calendar.MINUTE, turno.getJornadaCodigo().getMinutosToleranciaRegularEntradaJornada());

                            Date tolerancia = calendar.getTime();

                            calendar.add(Calendar.MINUTE, turno.getJornadaCodigo().getMinutosToleranciaTardanzaEntradaJornada());

                            Date entradaMax = calendar.getTime();

                            calendar.set(Calendar.HOUR_OF_DAY, 23);
                            calendar.set(Calendar.MINUTE, 59);
                            calendar.set(Calendar.SECOND, 0);

                            DetalleRegistroAsistencia detalleTurno = analizarTurno(
                                    empleado,
                                    registro,
                                    fInicio,
                                    fInicio,
                                    desde,
                                    tolerancia,
                                    entradaMax,
                                    turno.getJornadaCodigo().getHSalida(),
                                    FechaUtil.soloHora(calendar.getTime()));

                            detalles.add(detalleTurno);

                            LOG.info("SALIDA AL REFRIGERIO" + turno.getJornadaCodigo().getHEntradaRefrigerio());

                            Long milisInicioRefrigerio = turno.getJornadaCodigo().getHEntradaRefrigerio().getTime() - turno.getJornadaCodigo().getHSalidaRefrigerio().getTime();
//                            Long milisHoraMaximaSalida = turno.getJornadaCodigo().getHSalidaRefrigerio().getTime() + 30*(1000*60);

                            Calendar calc = Calendar.getInstance();
                            calc.setTime(turno.getJornadaCodigo().getHEntradaRefrigerio());
                            calc.add(Calendar.MINUTE, 240);

                            String empleadoID = getCodigoEmpleado(empleado);

                            DetalleRegistroAsistencia detalleRefrigerio = analizarRefrigerio(
                                    empleadoID,
                                    registro,
                                    fInicio,
                                    turno.getJornadaCodigo().getHSalidaRefrigerio(),
                                    turno.getJornadaCodigo().getHEntradaRefrigerio(),
                                    calc.getTime(),
                                    milisInicioRefrigerio.intValue(),
                                    (int) (milisInicioRefrigerio / (1000 * 60)) % 60);

                            detalles.add(detalleRefrigerio);

                            registro.setDetalleRegistroAsistenciaList(detalles);
                            registro.setTipo(detalleTurno.getResultado());
                            registro.setMilisTardanzaTotal(detalleTurno.getMilisegundosTardanza() == null ? 0 : detalleTurno.getMilisegundosTardanza());

                            if (registro != null) {
                                registro.setTurnoOriginal(turno);
                                for (DetalleRegistroAsistencia detallito : detalles) {

                                    if (detallito.getTipoRegistro() == 'R') {
                                        LOG.info("ENTRO AL IF DEL REFRIGERIO");
                                        LOG.info("MARCACION SALIDA REFRIGERIO: " + detallito.getHoraInicio());
                                        LOG.info("MARCACION REGRESO DEL REFRIGERIO: " + detallito.getHoraFin());
//                                        registro.setHoraSalidaRefrigerio(turno.getJornadaCodigo().getHSalidaRefrigerio());
//                                        registro.setHoraEntradaRefrigerio(turno.getJornadaCodigo().getHEntradaRefrigerio());
                                        registro.setMarcacionInicioRefrigerio(detallito.getHoraInicio());
                                        registro.setMarcacionFinRefrigerio(detallito.getHoraFin());
                                        registro.setMilisTardanzaRefrigerio(detalleRefrigerio.getMilisegundosTardanza() == null ? 0 : detalleRefrigerio.getMilisegundosTardanza());

                                    } else {
                                        LOG.info("NO ENTRO AL REFRIGERIO");
//                                        registro.setHoraEntrada(turno.getJornadaCodigo().getHEntrada());
//                                        registro.setHoraSalida(turno.getJornadaCodigo().getHSalida());
                                        registro.setMarcacionInicio(detalleTurno.getHoraInicio());
                                        registro.setMarcacionFin(detalleTurno.getHoraFin());
                                        registro.setMilisTardanzaRefrigerio(0);
                                    }
                                }

                                if (registro.getMilisTardanzaRefrigerio() != 0) {
                                    registro.setMilisTardanzaTotalFinal(registro.getMilisTardanzaTotal() + registro.getMilisTardanzaRefrigerio());
                                } else {
                                    registro.setMilisTardanzaTotalFinal(registro.getMilisTardanzaTotal());
                                }

                            }
                        }
                    }
                } else if (isOnomastico(empleado, fInicio)) {
                    //SE BUSCA EL DIA LABORAL MAS CERCANO PARA ASIGNARLE EL PERMISO POR ONOMASTICO
                    //Y SE AGREGA AL REGISTRO
                } else {
                    //NO HAY SUCESO SUSCEPTIBLE A REGISTRO
                    registro = null;
                    LOG.info("Registro null");
                }
//                }
//                }// FIN DEL ELSE PRINCIPAL
            }
        } else {
            registro = null;
            LOG.info("Registro null");
        }

        return registro;
    }

    private final Calendar cal = Calendar.getInstance();

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
            LOG.info("Registro null o.o");

            return null;
        }
    }

    private DetalleRegistroAsistencia analizarTurno(Empleado empleado,
            RegistroAsistencia registro,
            Date fechaInicio,
            Date fechaFin,
            Date turnoDesde,
            Date turnoTolerancia,
            Date turnoMaximaEntrada,
            Date turnoSalida,
            Date turnoMaximaSalida) {

        LOG.info("FECHA INICIO: " + fechaInicio);
        LOG.info("FECHA FIN: " + fechaFin);
        LOG.info("TURNO DESDE: " + turnoDesde);
        LOG.info("TURNO TOLERANCIA: " + turnoTolerancia);
        LOG.info("TURNO MAXIMA ENTRADA: " + turnoMaximaEntrada);
        LOG.info("TURNO SALIDA: " + turnoSalida);
        LOG.info("TURNO MAXIMA SALIDA: " + turnoMaximaSalida);

        String empleadoId = getCodigoEmpleado(empleado);
        LOG.info("CODIGO DE TRABAJADOR: " + empleadoId);

        DetalleRegistroAsistencia detalle = new DetalleRegistroAsistencia();
        detalle.setOrden(0);
        detalle.setRegistroAsistencia(registro);

        Marcacion marcacionInicio = marcacionControlador.buscarXFechaXhora(empleadoId, fechaInicio, turnoDesde, turnoMaximaEntrada);
        if (marcacionInicio != null) {
            LOG.info(String.format("MARCACION INICIO: %s %s", marcacionInicio.getFecha(), marcacionInicio.getHora()));
        }

        char resultadoInicio;
        detalle.setFechaInicio(fechaInicio);
        if (marcacionInicio == null) {
            LOG.info("MARCACION INICIO ES NULL");
            resultadoInicio = 'F';
        } else {
            long tardanzaEntrada = tardanza(marcacionInicio.getHora(), turnoTolerancia);

            detalle.setHoraInicio(marcacionInicio.getHora());
            if (tardanzaEntrada > 0) {
                resultadoInicio = 'T';
            } else {
                resultadoInicio = 'R';
            }

            detalle.setMilisegundosTardanza(tardanzaEntrada);
            LOG.info(String.format("MILIS TARDANZA: %s", detalle.getMilisegundosTardanza()));
        }

        Marcacion marcacionFin = marcacionControlador.buscarXFechaXhora(empleadoId, fechaFin, turnoSalida, turnoMaximaSalida);
        char resultadoFin;

        if (marcacionFin == null) {
            LOG.info("MARCACION FIN ES NULL");
            resultadoFin = 'F';
        } else {
//            long tardanzaEntrada = tardanza(marcacionInicio.getHora(), turnoTolerancia);

            detalle.setHoraFin(marcacionFin.getHora());
            resultadoFin = 'R';
        }
        detalle.setFechaFin(fechaFin);

        if (resultadoInicio == 'F' || resultadoFin == 'F') {
            LOG.info(String.format("RESULTADO INICIO: %s RESULTADO FIN: %s", resultadoInicio, resultadoFin));
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
        return empleado.getFichaLaboral().getCodigoTrabajador();
    }
    private static final Logger LOG = Logger.getLogger(AnalisisFinal.class.getName());

    @Override
    public void analizarEmpleados(Area area) {
        List<Empleado> empleados = area.getEmpleadoList();
        LOG.info("EMPLEADOS: " + empleados.size());
        for (Empleado e : empleados) {
            LOG.info("EMPLEADO: " + e.getApellidos() + " " + e.getNombres());
        }
        this.analizarEmpleados(empleados);
    }

    private boolean isDiaLaboral(Date fInicio, DetalleHorario turno) {
        return true;
    }

    private DetalleRegistroAsistencia analizarRefrigerio(String empleadoDNI, RegistroAsistencia registro, Date fechaInicio, Date horaInicio, Date horaFin, Date horaMaximaFin, int milisHoraInicio, int minutosRefrigerio) {
        DetalleRegistroAsistencia registroRefrigerio = new DetalleRegistroAsistencia();
        registroRefrigerio.setOrden(1);
        registroRefrigerio.setRegistroAsistencia(registro);
        registroRefrigerio.setTipoRegistro('R');

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(horaInicio);
        calendar.add(Calendar.MILLISECOND, milisHoraInicio);

        Date horaMaximaInicio = calendar.getTime();
        System.out.println("HORA INICIO: " + horaInicio);
        System.out.println("MINUTOS HORA INICIO: " + milisHoraInicio + " " + horaMaximaInicio);
        LOG.info("DOC EMPLEADO: " + empleadoDNI);
        LOG.info("FECHA DEL REFRIGERIO: " + fechaInicio);
        LOG.info("HORA INICIO REFRI: " + horaInicio);
        LOG.info("HORA MAXIMA REFRI: " + horaMaximaInicio);
//        System.out.println("MARCACION INICIO REFRIGERIO PARAMS: " + fechaInicio + " " + horaInicio + " " + horaMaximaInicio);
        Marcacion marcacionInicio = marcacionControlador.buscarXFechaXhora(empleadoDNI, fechaInicio, horaInicio, horaMaximaInicio);

        if (marcacionInicio == null) {
            LOG.info("MARCACION INICIO ES NULL");
            //NO SE TOMA EN CUENTA EL REFRIGERIO
            registroRefrigerio.setHoraInicio(null);
            registroRefrigerio.setHoraFin(null);
            registroRefrigerio.setResultado('R');
        } else {
            registroRefrigerio.setHoraInicio(marcacionInicio.getHora());
            calendar.setTime(marcacionInicio.getHora());
            calendar.add(Calendar.MINUTE, minutosRefrigerio);
            Date horaEsperadaFin;
            if (horaFin.before(calendar.getTime())) {
                horaEsperadaFin = horaFin;
            } else {
                horaEsperadaFin = calendar.getTime();
            }

            calendar.setTime(marcacionInicio.getHora());
            calendar.add(Calendar.SECOND, 1);
            Date limiteInferiorHoraFin = calendar.getTime();

            Marcacion marcacionFin = marcacionControlador.buscarXFechaXhora(empleadoDNI, fechaInicio, limiteInferiorHoraFin, horaMaximaFin);

            if (marcacionFin == null) {
                registroRefrigerio.setHoraFin(null);
                registroRefrigerio.setResultado('F');
            } else {
//                System.out.println("HORA FIN, HORA ESPERADA FIN: "+marcacionFin.getHora()+" "+horaEsperadaFin);
                long minTardanza = this.tardanza(marcacionFin.getHora(), horaEsperadaFin);
                registroRefrigerio.setHoraFin(marcacionFin.getHora());
                registroRefrigerio.setMilisegundosTardanza(minTardanza);

                if (minTardanza > 0) {
                    registroRefrigerio.setResultado('T');
                } else {
                    registroRefrigerio.setResultado('R');
                }
            }
        }

        return registroRefrigerio;
    }

    private Permiso generarPermisoOnomastico(String dni, Date fInicio) {
//        pc.;
        Permiso onomastico = new Permiso();

        //CREAMOS LA ASIGNACION
        EmpleadoPermiso ap = new EmpleadoPermiso();
        List<Empleado> empleado = empleadoControlador.buscarXEmpleado(dni);
        ap.setEmpleadoId(empleado.get(0));
        ap.setPermisoId(onomastico);

        //
//        onomastico.getEmpleadoPermisoList().add(ap);
        onomastico.setFechaInicio(fInicio);
        onomastico.setFechaFin(fInicio);
        onomastico.setPorFecha(true);
        List<MotivoPermiso> motivo = motivoPermisoControlador.buscarXTipo("ONO");
        onomastico.setMotivoPermisoCodigo(motivo.get(0));
        onomastico.setTipo("F");

        permisoControlador.create(onomastico);
//        onomastico.set.setMotivo("LICENCIA POR ONOMÁSTICO");
//        onomastico.setDocumento("LICENCIA POR ONOMÁSTICO");

//        long diferencia = onomastico.getFechaFin().getTime() - onomastico.getFechaInicio().getTime();
//        BigDecimal diferenciaMin = new BigDecimal(diferencia / (60 * 1000 * 60));
//        onomastico.setDiferencia(diferenciaMin);
//        if () {
        empleadoPermisoControlador.create(ap);
        LOG.info("SE GUARDO EL PERMISO POR ONOMASTICO");
//        } else {
//            LOG.info("HUBO UN ERROR");
//        }

//        pc.getDao().getEntityManager();
        return onomastico;
    }
}
