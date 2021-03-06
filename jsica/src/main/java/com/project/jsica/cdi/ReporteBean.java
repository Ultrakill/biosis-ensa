/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.project.jsica.cdi;

import com.biosis.reportes.entidades.ReportePermisoBean;
import com.personal.utiles.FechaUtil;
import com.project.algoritmo.AnalisisFinalLocal;
import com.project.jsica.cdi.util.JsfUtil;
import com.project.jsica.ejb.entidades.Area;
import com.project.jsica.ejb.entidades.CambioTurno;
import com.project.jsica.ejb.entidades.DetalleHorario;
import com.project.jsica.ejb.entidades.DetalleRegistroAsistencia;
import com.project.jsica.ejb.entidades.Empleado;
import com.project.jsica.ejb.entidades.EmpleadoPermiso;
import com.project.jsica.ejb.entidades.RegistroAsistencia;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import javax.ejb.EJB;
import javax.enterprise.context.ConversationScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletResponse;
import jxl.Sheet;
import jxl.Workbook;
import jxl.format.Colour;
import jxl.format.VerticalAlignment;
import jxl.write.Label;
import jxl.write.WritableCellFormat;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Row;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

/**
 *
 * @author Documentos
 */
@Named(value = "reporteBean")
@ConversationScoped
public class ReporteBean implements Serializable {

    @Inject
    private EmpleadoPermisoController empleadoPermisoController;
    @Inject
    private AreaController areaController;
    @Inject
    private RegistroAsistenciaController registroAsistenciaController;
    @Inject
    private EmpleadoController empleadoController;

    @EJB
    private AnalisisFinalLocal analisisService;
    private boolean nuevo = true;
    private boolean nuevoReporte = true;
    private Date desde;
    private Date hasta;
    private Empleado empleado;
    private Area areaSeleccionada;
    private Boolean conGoce;
    private int opcion = 1;
    private int opcionReporte = 1;
    private List<ReportePermisoBean> reportePermisos;
    private List<RegistroAsistencia> registroAsistencia;

    private RegistroAsistencia registroSeleccionado;

    public RegistroAsistencia getRegistroSeleccionado() {
        return registroSeleccionado;
    }

    public void setRegistroSeleccionado(RegistroAsistencia registroSeleccionado) {
        this.registroSeleccionado = registroSeleccionado;
    }

    public Boolean getConGoce() {
        return conGoce;
    }

    public void setConGoce(Boolean conGoce) {
        this.conGoce = conGoce;
    }

    public void setReportePermisos(List<ReportePermisoBean> reportePermisos) {
        this.reportePermisos = reportePermisos;
    }

    private static final java.util.logging.Logger LOG = java.util.logging.Logger.getLogger(ReporteBean.class.getName());

    public List<Area> getAreas() {
        String sql = "SELECT a FROM Area a";
        return areaController.search(sql);
    }

    public int getOpcion() {
        return opcion;
    }

    public void setOpcion(int opcion) {
        LOG.info("OPCION " + opcion);
        this.opcion = opcion;
        this.nuevo = true;
        realizarAnalisis();

    }

    public int getOpcionReporte() {
        return opcionReporte;
    }

    public void setOpcionReporte(int opcionReporte) {
        LOG.info("OPCION REPORTE" + opcionReporte);
        this.opcionReporte = opcionReporte;
        this.nuevoReporte = true;
        reporte3();
    }

    public Date getDesde() {
        return desde;
    }

    public void setDesde(Date desde) {
        this.desde = desde;
    }

    public Date getHasta() {
        return hasta;
    }

    public void setHasta(Date hasta) {
        this.hasta = hasta;
    }

    public Empleado getEmpleado() {
        return empleado;
    }

    public void setEmpleado(Empleado empleado) {
        LOG.info("SE SELECCIONA UN EMPLEADO");
        this.empleado = empleado;
    }

    public Area getAreaSeleccionada() {
        return areaSeleccionada;
    }

    public void setAreaSeleccionada(Area areaSeleccionada) {
        LOG.info("SE SELECCIONA UN AREA");
        this.areaSeleccionada = areaSeleccionada;
    }

    private List<RegistroAsistencia> reporte;

    public List<RegistroAsistencia> getReporteAsistencias() {
        if (nuevo) {
            if (opcion == 1) {
                LOG.info("REPORTE DE ASISTENCIA POR EMPLEADO");
                registroAsistencia = registroAsistenciaController.buscarXEmpleado(empleado, desde, hasta);
            } else {
                LOG.info("REPORTE DE ASISTENCIA POR AREA");
                registroAsistencia = registroAsistenciaController.buscarXArea(areaSeleccionada, desde, hasta);
                LOG.info("TAMANO R X AREA: " + registroAsistencia.size());
                reporte = registroAsistencia;
                LOG.info("TAMANO DEL R para XLS: " + reporte.size());
            }
            nuevo = false;
        }

        return registroAsistencia;

    }

    public ReporteBean() {
    }

    private void agregarData(WritableSheet hoja, String[] titulos, List<String[]> contenido, Integer[] anchuras) {
        this.agregarData(hoja, titulos, contenido, anchuras, 0);
    }

    private void agregarData(WritableSheet hoja, String[] titulos, List<String[]> contenido, Integer[] anchuras, int fila) {
        int columnas = titulos.length;
        int filas = contenido.size();

        for (int filaActual = fila; filaActual <= filas; filaActual++) {
            for (int columna = 0; columna < columnas; columna++) {
                if (filaActual == fila) {
                    agregarCelda(hoja, filaActual, columna, titulos[columna]);
                    hoja.setColumnView(columna, anchuras[columna]);
                } else {
                    agregarCelda(hoja, filaActual, columna, contenido.get(filaActual - 1)[columna]);
                }
            }
        }
    }

    private List<Empleado> getEmpleados(int opcion) {
        List<Empleado> empleados = new ArrayList<>();
        if (opcion == 1) {
            LOG.info("SE AGREGA EMPLEADO");
            empleados.add(empleado);
        } else if (opcion == 2) {
            empleados.addAll(this.empleadoController.buscarTodos());
        }
        return empleados;
    }

    public void reporteHorasTrabajadas(int opcion) {
        this.opcion = opcion;
        SimpleDateFormat dtFecha = new SimpleDateFormat("dd.MM.yyyy");
        SimpleDateFormat dtHora = new SimpleDateFormat("HH:mm");

        List<Empleado> empleados = this.getEmpleados(opcion);

        OutputStream out = null;

    }

//    public List getReporteHorasExtraTotal() {
//        List<Empleado> empleados = this.getEmpleados(opcion);
//        List<EmpleadoPermiso> permisos;
//        List<ReportePermisoBean> reporte = new ArrayList<>();
//        LOG.info("VIENE AL METODO REPORTE PERMISOS");
//        for (Empleado emp : empleados) {
//            permisos = this.empleadoPermisoController.buscarXEmpleado(emp, desde, hasta, conGoce);
//
//            List<DetalleRegistroAsistencia> detalles;
//            for (EmpleadoPermiso permiso : permisos) {
//                
//                detalles = permiso.getRegistroList();
//
//                if (!detalles.isEmpty()) {
//                    DetalleRegistroAsistencia detalleEntrada = detalles.get(0);
//                    DetalleRegistroAsistencia detalleSalida = detalles.get(1);
//                    DetalleRegistroAsistencia aux;
//
//                    if (detalleEntrada.getCarga() != 23 && detalleEntrada.getCarga() != 23) {
//                        if (detalleEntrada.getHora().compareTo(detalleSalida.getHora()) < 0) {
//                            aux = detalleEntrada;
//                            detalleEntrada = detalleSalida;
//                            detalleSalida = aux;
//                        }
//
//                        long milisegundosDiferencia = detalleEntrada.getHora().getTime() - detalleSalida.getHora().getTime();
//
//                        int hora[] = FechaUtil.milisToTime(milisegundosDiferencia);
//
//                        ReportePermisoBean reporteBean = new ReportePermisoBean();
//                        reporteBean.setCodigo(emp.getFichaLaboral().getCodigoTrabajador());
//                        reporteBean.setNombre(emp.getApellidos() + " " +emp.getNombres());
//                        reporteBean.setHoraInicio(detalleSalida.getHora());
//                        reporteBean.setHoraFin(detalleEntrada.getHora());
//                        reporteBean.setHoras(hora[0]);
//                        reporteBean.setMinutos(hora[1]);
//                        reporteBean.setSegundos(hora[2]);
//                        reporteBean.setFechaReal(detalleEntrada.getFecha());
//                        reporteBean.setMotivo(permiso.getPermisoId().getMotivoPermisoCodigo().getNombre());
//
//                        reporte.add(reporteBean);
//
//                    }
//                }
//
//            }
//        }
//
//        return reporte;
//    }
//    public void reporteHorasExtra(int opcion) {
//        this.opcion = opcion;
//        SimpleDateFormat dtFecha = new SimpleDateFormat("dd.MM.yyyy");
//        SimpleDateFormat dtHora = new SimpleDateFormat("HH:mm");
//
//        List<Empleado> empleados = this.getEmpleados(opcion);
//
//        OutputStream out = null;
//
//        try {
//            String header = "REPORTE_DE_HORAS_EXTRA_DEL_" + dtFecha.format(desde) + "_AL_" + dtFecha.format(hasta) + ".xls";
//            out = JsfUtil.getOutputStream(header, JsfUtil.ContentType.XLS);
//
//            WritableWorkbook w = Workbook.createWorkbook(out);
//            WritableSheet hojaResumen = w.createSheet("RESUMEN", 0);
//            WritableSheet hojaDetalle = w.createSheet("DETALLE", 1);
//
//            String[] tituloResumen = {"CODIGO", "APELLIDOS Y NOMBRES", "TOTAL HORAS", "TOTAL MINUTOS", "TOTAL SEGUNDOS"};
//            String[] subtituloDetalle = {"FECHA", "HORAS", "MINUTOS", "SEGUNDOS"};
//            String[] tituloDetalle = {"CODIGO", "APELLIDOS Y NOMBRES"};
//            Integer[] anchurasResumen = {10, 40, 10, 10, 10};
//            Integer[] anchurasDetalle = {10, 10, 10, 10};
//
//            List<String[]> resumen = new ArrayList<>();
//
//            // CODIGO - APELLIDOS Y NOMBRES
//            int filaResumen = 0;
//            int filaDetalle = 0;
//
//            for (Empleado emp : empleados) {
//                List<RegistroAsistencia> registrosSalida = registroAsistenciaController.getRegistros(false, false, false, emp, desde, hasta);
//                List<String[]> detalles = new ArrayList<>();
//
//                //LLENANDO EL DETALLE
//                hojaDetalle.mergeCells(1, filaDetalle, 3, filaDetalle);
//                filaDetalle++;
//                agregarCelda(hojaDetalle, filaDetalle, 0, emp.getFichaLaboral().getCodigoTrabajador());
//                agregarCelda(hojaDetalle, filaDetalle, 1, emp.getApellidos() + emp.getNombres());
//                filaDetalle++;
//
//                int filaInicio = filaDetalle;
//                long milisegundosTotales = 0;
//
//                for (RegistroAsistencia ra : registrosSalida) {
//                    long diferencia = ra.getHora().getTime() - ra.getTurnoOriginal().getJornadaCodigo().getHSalida().getTime();
//                    if (diferencia > 0) {
//                        int horas = (int) diferencia / (60 * 60 * 1000);
//                        int minutos = (int) (Math.abs(diferencia - (horas * 60 * 60 * 1000)) / (60 * 1000));
//                        String detalle[] = {dtFecha.format(ra.getFecha()), horas + "", minutos + "", ""};
//                        detalles.add(detalle);
//                        milisegundosTotales += diferencia;
//
//                        filaDetalle++;
//                    }
//                }
//
//                //SE AGREGA LA DATA DEL DETALLE A LA HOJA
//                this.agregarData(hojaDetalle, subtituloDetalle, detalles, anchurasDetalle, filaInicio);
//
//                filaDetalle++;
//
//                //LLENADO DEL RESUMEN
//                int horas = (int) milisegundosTotales / (60 * 60 * 1000);
//                int minutos = (int) (Math.abs(milisegundosTotales - (horas * 60 * 60 * 1000)) / (60 * 1000));
//
//                String[] resmn = {emp.getFichaLaboral().getCodigoTrabajador(), emp.getApellidos() + " " + emp.getNombres(), horas + "", minutos + "", ""};
//                resumen.add(resmn);
//            }
//
//            this.agregarData(hojaResumen, tituloResumen, resumen, anchurasResumen);
//
//            w.write();
//            w.close();
//
//            FacesContext.getCurrentInstance().responseComplete();
//        } catch (Exception e) {
//        }
//    }
//
    public List getReportePermisos() {
//        List<Empleado> empleados = this.getEmpleados(opcion);
        List<EmpleadoPermiso> permisos;
        List<ReportePermisoBean> reporte = new ArrayList<>();
        LOG.info("EMPLEADO: " + empleado);
        permisos = this.empleadoPermisoController.buscarXEmpleado(empleado, desde, hasta, conGoce);

        LOG.info("VIENE AL METODO REPORTE PERM");
        LOG.info("TAMANO: " + permisos.size());

        List<DetalleRegistroAsistencia> detalles;
        for (EmpleadoPermiso permiso : permisos) {
            LOG.info("ENTRO AL FOR DE LOS PERMISOS DEL EMPLEADO: " + empleado.getApellidos() + " " + empleado.getNombreCompleto());

            if (!permiso.getPermisoId().getPorFecha()) {
                LOG.info("ES POR HORAS");
                detalles = permiso.getRegistroList();

                if (!detalles.isEmpty()) {
                    LOG.info("NO ESTA VACIA");
                    //Falta resolver para mostrar por fecha + horas...
                    for (DetalleRegistroAsistencia detalle : detalles) {

                        ReportePermisoBean reporteBean = new ReportePermisoBean();
                        reporteBean.setCodigo(empleado.getFichaLaboral().getCodigoTrabajador());
                        reporteBean.setNombre(empleado.getApellidos() + " " + empleado.getNombres());
                        reporteBean.setHoraInicio(detalle.getHoraInicio());
                        reporteBean.setHoraFin(detalle.getHoraFin());
                        reporteBean.setHoras(0);
                        reporteBean.setMinutos(0);
                        reporteBean.setSegundos(0);
                        reporteBean.setFechaReal(null); // ?????
                        reporteBean.setMotivo(permiso.getPermisoId().getMotivoPermisoCodigo().getNombre());

                        reporte.add(reporteBean);
                    }
                } else {
                    LOG.info("LISTA DETALLE VACIA");

                    ReportePermisoBean reporteBean = new ReportePermisoBean();
                    reporteBean.setCodigo(empleado.getFichaLaboral().getCodigoTrabajador());
                    reporteBean.setNombre(empleado.getApellidos() + " " + empleado.getNombres());
                    reporteBean.setHoraInicio(permiso.getPermisoId().getHoraInicio());
                    reporteBean.setHoraFin(permiso.getPermisoId().getHoraFin());
                    //Calcular horas y minutos
                    long horas = diferenciaHoras(permiso.getPermisoId().getHoraInicio(), permiso.getPermisoId().getHoraFin());
                    long minutos = diferenciaMinutos(permiso.getPermisoId().getHoraInicio(), permiso.getPermisoId().getHoraFin());
                    //Fin del calculo
                    reporteBean.setHoras((int) horas);
                    reporteBean.setMinutos((int) minutos);
                    reporteBean.setSegundos(0);
                    reporteBean.setFechaReal(permiso.getPermisoId().getFechaInicio()); // ?????
                    reporteBean.setMotivo(permiso.getPermisoId().getMotivoPermisoCodigo().getNombre());

                    reporte.add(reporteBean);
                    LOG.info("REPORTE ANADIDO");

                }
            }
        }

        if (!reporte.isEmpty()) {
            reporte2(reporte);
            LOG.info("XLS GENERADO");
        }
        LOG.info("DEVOLVIO LA LISTA DE REPORTES");
        return reporte;
    }
    static long milisegundos_dia = 24 * 60 * 60 * 1000;

    public long diferenciaHoras(Date fechaInicial, Date fechaFinal) {
        Calendar calFechaInicial = Calendar.getInstance();
        calFechaInicial.setTime(fechaInicial);
        Calendar calFechaFinal = Calendar.getInstance();
        calFechaFinal.setTime(fechaFinal);

        long diferenciaHoras = 0;
        diferenciaHoras = ((calFechaFinal.getTimeInMillis() - calFechaInicial.getTimeInMillis()) / 3600000);

        return diferenciaHoras;
    }

    public long diferenciaMinutos(Date fechaInicial, Date fechaFinal) {
        Calendar calFechaInicial = Calendar.getInstance();
        calFechaInicial.setTime(fechaInicial);
        Calendar calFechaFinal = Calendar.getInstance();
        calFechaFinal.setTime(fechaFinal);

        long diferenciaminutos = 0;
        long restoHoras = ((calFechaFinal.getTimeInMillis() - calFechaInicial.getTimeInMillis()) % 3600000);
        diferenciaminutos = restoHoras / 60000;
        return diferenciaminutos;
    }

//                if (!detalles.isEmpty()) {
//                    
//                    
//                    
////                    DetalleRegistroAsistencia detalleEntrada = detalles.get(0).get;
////                    DetalleRegistroAsistencia detalleSalida = detalles.get(1);
////                    DetalleRegistroAsistencia aux;
//
//                    if (detalleEntrada.get() != 23 && detalleEntrada.getCarga() != 23) {
//                        if (detalleEntrada.getHora().compareTo(detalleSalida.getHora()) < 0) {
//                            aux = detalleEntrada;
//                            detalleEntrada = detalleSalida;
//                            detalleSalida = aux;
//                        }
//
//                        long milisegundosDiferencia = detalleEntrada.getHoraFin().getTime() - detalleSalida.getHoraFin().getTime();
//
//                        int hora[] = FechaUtil.milisToTime(milisegundosDiferencia);
//
//                        ReportePermisoBean reporteBean = new ReportePermisoBean();
//                        reporteBean.setCodigo(emp.getFichaLaboral().getCodigoTrabajador());
//                        reporteBean.setNombre(emp.getApellidos() + " " +emp.getNombres());
//                        reporteBean.setHoraInicio(detalleSalida.getHoraInicio());
//                        reporteBean.setHoraFin(detalleEntrada.getHoraFin());
//                        reporteBean.setHoras(hora[0]);
//                        reporteBean.setMinutos(hora[1]);
//                        reporteBean.setSegundos(hora[2]);
//                        reporteBean.setFechaReal(detalleEntrada.getFechaInicio()); // ?????
//                        reporteBean.setMotivo(permiso.getPermisoId().getMotivoPermisoCodigo().getNombre());
//
//                        reporte.add(reporteBean);
//
//                    }
//                }
//
//            }
//        }
//                return reporte;
//            }
//        }
    //    public void reportePermisos(int opcion) {
    //        this.opcion = opcion;
    //        SimpleDateFormat dtFecha = new SimpleDateFormat("dd.MM.yyyy");
    //        SimpleDateFormat dtHora = new SimpleDateFormat("HH:mm");
    //
    //        List<Empleado> empleados = this.getEmpleados(opcion);
    //
    //        OutputStream out = null;
    //        try {
    //            String header = "REPORTE_DE_PERMISOS_DEL_" + dtFecha.format(desde) + "_AL_" + dtFecha.format(hasta) + ".xls";
    //            out = JsfUtil.getOutputStream(header, JsfUtil.ContentType.XLS);
    //
    //            WritableWorkbook w = Workbook.createWorkbook(out);
    //            WritableSheet hoja1 = w.createSheet("SIN SUSTENTO", 0);
    //            WritableSheet hoja2 = w.createSheet("CON SUSTENTO", 1);
    //
    //            String[] titulos = {"CODIGO", "APELLIDOS Y NOMBRES", "FECHA REAL", "INICIO", "FIN", "MINUTOS", "HORAS", "MOTIVO"};
    //            Integer[] anchuras = {10, 30, 10, 10, 10, 10, 10, 10};
    //            List<String[]> contenidos1 = new ArrayList<>();
    //            List<String[]> contenidos2 = new ArrayList<>();
    //
    //            if (empleados.isEmpty()) {
    //                LOG.info("NO HAY EMPLEADOS");
    //                JsfUtil.addErrorMessage("NO HAY DATOS");
    //            } else {
    //                LOG.info("SI HAY EMPLEADOS");
    //                for (Empleado emp : empleados) {
    //                    List<Permiso> permisos = empleadoPermisoController.permisosXEmpleado(desde, hasta, emp);
    //
    //                    for (Permiso permiso : permisos) {
    //                        List<RegistroAsistencia> registros = permiso.getRegistroList();
    //
    //                        RegistroAsistencia registro1 = registros.get(0);
    //                        RegistroAsistencia registro2 = registros.get(1);
    //
    //                        Date horaInicio;
    //                        Date horaFin;
    //
    //                        if (registro1.getHora().compareTo(registro2.getHora()) < 0) {
    //                            horaInicio = registro1.getHora();
    //                            horaFin = registro2.getHora();
    //                        } else {
    //                            horaInicio = registro2.getHora();
    //                            horaFin = registro1.getHora();
    //                        }
    //
    //                        long diferencia = Math.abs(registro1.getHora().getTime() - registro2.getHora().getTime());
    //                        int horas = (int) diferencia / (60 * 60 * 1000);
    //                        int minutos = (int) (Math.abs(diferencia - (horas * 60 * 60 * 1000)) / (60 * 1000));
    //
    //                        String[] contenido = new String[titulos.length];
    //                        contenido[0] = emp.getFichaLaboral().getCodigoTrabajador();
    //                        contenido[1] = emp.getApellidos() + " " + emp.getNombres();
    //                        contenido[2] = dtFecha.format(permiso.getFechaInicio());
    //                        contenido[3] = dtHora.format(horaInicio);
    //                        contenido[4] = dtHora.format(horaFin);
    //                        contenido[5] = minutos + "";
    //                        contenido[6] = horas + "";
    //                        contenido[7] = permiso.getMotivoPermisoCodigo().getNombre();
    //
    //                        if (permiso.getMotivoPermisoCodigo().getConGoce()) {
    //                            LOG.info("CON GOCE");
    //                            contenidos2.add(contenido);
    ////                        agregarCelda(hoja1, fila2, 7, "SUSTENTO");
    //                        } else {
    //                            LOG.info("SIN GOCE");
    //                            contenidos1.add(contenido);
    //                        }
    //
    //                    }
    //                }
    //
    //                agregarData(hoja2, titulos, contenidos2, anchuras);
    //                agregarData(hoja1, titulos, contenidos1, anchuras);
    //
    //                w.write();
    //                w.close();
    //
    //                FacesContext.getCurrentInstance().responseComplete();
    //            }
    //
    //        } catch (IOException | WriteException ex) {
    //            Logger.getLogger(ReporteBean.class.getName()).log(Level.WARN, null, ex);
    //        } finally {
    //            if (out != null) {
    //                try {
    //                    out.close();
    //                } catch (IOException ex) {
    //                    Logger.getLogger(ReporteBean.class.getName()).log(Level.WARN, null, ex);
    //                }
    //            }
    //        }
    //    }
    public void agregarHoja(Workbook libro, String nombre) {

    }

    public void agregarCelda(WritableSheet hoja, int fila, int columna, String contenido) {
        try {
            WritableCellFormat cellFormat = new WritableCellFormat();
            cellFormat.setVerticalAlignment(VerticalAlignment.CENTRE);
            cellFormat.setAlignment(jxl.format.Alignment.CENTRE);
            if (fila == 0) {
                cellFormat.setBackground(Colour.ORANGE);
            }

            hoja.addCell(new Label(columna, fila, contenido, cellFormat));

        } catch (WriteException ex) {
            java.util.logging.Logger.getLogger(ReporteBean.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
    }

    public void reporte() {
        OutputStream out = null;
        try {
            FacesContext fc = FacesContext.getCurrentInstance();
            HttpServletResponse response = (HttpServletResponse) fc.getExternalContext().getResponse();

            response.reset(); // Some JSF component library or some Filter might have set some headers in the buffer beforehand. We want to get rid of them, else it may collide.
            response.setContentType("application/vnd.ms-excel");
            response.setHeader("Content-Disposition", "attachment; filename=reporte"); // The Save As popup magic is done here. You can give it any file name you want, this only won't work in MSIE, it will use current request URL as file name instead.

            out = response.getOutputStream();

            WritableWorkbook w = Workbook.createWorkbook(out);
            WritableSheet hoja1 = w.createSheet("Reporte", 0);

            //FILA DE TITULO 
            agregarCelda(hoja1, 0, 0, "CODIGO");
            agregarCelda(hoja1, 0, 1, "APELLIDOS Y NOMBRES");
            agregarCelda(hoja1, 0, 2, "FECHA REAL");
            agregarCelda(hoja1, 0, 3, "INICIO");
            agregarCelda(hoja1, 0, 4, "FIN");
            agregarCelda(hoja1, 0, 5, "MINUTOS");
            agregarCelda(hoja1, 0, 6, "MOTIVO");
            agregarCelda(hoja1, 0, 7, "SUSTENTO");

            w.write();
            w.close();

            fc.responseComplete();
        } catch (IOException | WriteException ex) {
            Logger.getLogger(ReporteBean.class.getName()).log(Level.WARN, null, ex);
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException ex) {
                    Logger.getLogger(ReporteBean.class.getName()).log(Level.WARN, null, ex);
                }
            }
        }
    }

    public void reporte2(List<ReportePermisoBean> reporte) {
        LOG.info("TAMAÑO reporte: " + reporte.size());
        FacesContext fc = FacesContext.getCurrentInstance();
        HttpServletResponse response = (HttpServletResponse) fc.getExternalContext().getResponse();

        HSSFWorkbook libro = new HSSFWorkbook();

        HSSFFont fuente = libro.createFont();
        fuente.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
        HSSFCellStyle estiloCeldaCabecera = libro.createCellStyle();
        estiloCeldaCabecera.setFont(fuente);
        estiloCeldaCabecera.setAlignment(HSSFCellStyle.ALIGN_CENTER);

        DataFormat format = libro.createDataFormat();

        HSSFCellStyle style = libro.createCellStyle();
        style.setDataFormat(format.getFormat("hh:mm:ss"));

        HSSFCellStyle fechas = libro.createCellStyle();
        fechas.setDataFormat(format.getFormat("dd.MM.yyyy"));

        HSSFSheet hoja = libro.createSheet("hoja 1");

        //CREAR LAS CABECERAS
        String[] cabeceras = {"CODIGO", "NOMBRE", "HORA INICIO", "HORA FIN", "HORAS", "MINUTOS", "FECHA", "MOTIVO"};

        HSSFRow filaCabecera = hoja.createRow(0);

        for (int x = 0; x < cabeceras.length; x++) {
            HSSFCell cabecera = filaCabecera.createCell(x);
            cabecera.setCellValue(cabeceras[x]);
            cabecera.setCellStyle(estiloCeldaCabecera);
        }
        //FIN DE CABECERAS
        for (int i = 0; i < reporte.size(); i++) {

            HSSFRow fila = hoja.createRow(i + 1);

            HSSFCell columna1 = fila.createCell(0);
            columna1.setCellValue(reporte.get(i).getCodigo());

            HSSFCell columna2 = fila.createCell(1);
            columna2.setCellValue(reporte.get(i).getNombre());

            HSSFCell columna3 = fila.createCell(2);
            columna3.setCellValue(reporte.get(i).getHoraInicio());
            columna3.setCellStyle(style);

            HSSFCell columna4 = fila.createCell(3);
            columna4.setCellValue(reporte.get(i).getHoraFin());
            columna4.setCellStyle(style);

            HSSFCell columna5 = fila.createCell(4);
            columna5.setCellValue(reporte.get(i).getHoras());

            HSSFCell columna6 = fila.createCell(5);
            columna6.setCellValue(reporte.get(i).getMinutos());

            HSSFCell columna7 = fila.createCell(6);
            columna7.setCellValue(reporte.get(i).getFechaReal());
            columna7.setCellStyle(fechas);

            HSSFCell columna8 = fila.createCell(7);
            columna8.setCellValue(reporte.get(i).getMotivo());
        }

        try {

            OutputStream output = response.getOutputStream();

            libro.write(output);
            output.close();

            fc.responseComplete();

        } catch (IOException ex) {
            LOG.info("ERROR: " + ex);
        }
    }

    public void reporte3() {
        if (nuevoReporte) {
            LOG.info("OPCION: " + opcionReporte);
            String nombreReporte = "";
            int filas = 0;
            if (opcionReporte == 2) {
                reporte = registroAsistenciaController.buscarXArea(areaSeleccionada, desde, hasta);
                LOG.info("TAMAÑO reporte: " + reporte.size());
                nombreReporte = "Reporte de asistencia por area";
                filas = 1;
            } else if (opcionReporte == 1) {
                reporte = registroAsistenciaController.buscarXEmpleado(empleado, desde, hasta);
                LOG.info("TAMAÑO reporte: " + reporte.size());
                nombreReporte = "Reporte de asistencia por empleado";
                filas = 0;
            }

            FacesContext fc = FacesContext.getCurrentInstance();
            HttpServletResponse response = (HttpServletResponse) fc.getExternalContext().getResponse();

            response.reset();
            response.setContentType("application/vnd.ms-excel");
            response.setHeader("Content-Disposition", "attachment; filename=" + nombreReporte + ".xls");

            HSSFWorkbook libro = new HSSFWorkbook();

            HSSFFont fuente = libro.createFont();
            fuente.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
            HSSFCellStyle estiloCeldaCabecera = libro.createCellStyle();
            estiloCeldaCabecera.setFont(fuente);
            estiloCeldaCabecera.setAlignment(HSSFCellStyle.ALIGN_CENTER);

            DataFormat format = libro.createDataFormat();

            HSSFCellStyle style = libro.createCellStyle();
            style.setDataFormat(format.getFormat("hh:mm:ss"));

            HSSFCellStyle fechas = libro.createCellStyle();
            fechas.setDataFormat(format.getFormat("dd.MM.yyyy"));

            HSSFSheet hoja = libro.createSheet("Reporte de Asistencias");

            //CREAR LAS CABECERAS
            String[] cabeceras = {"CODIGO", "APELLIDOS Y NOMBRES", "FECHA", "TIPO", "HORA DE INGRESO", "HORA DE SALIDA", "MARCACION DE ENTRADA", "MARCACION DE SALIDA", "TARDANZA(Minutos)", "SALIDA REFRIGERIO", "ENTRADA REFRIGERIO", "MARCACION SALIDA R", "MARCACION DE ENTRADA R", "TARDANZA(refrigerio)", "TARDANZA TOTAL"};

            if (filas == 1) {
                HSSFRow filaArea = hoja.createRow(0);
                HSSFCell Area = filaArea.createCell(0);
                Area.setCellValue("AREA");
                Area.setCellStyle(estiloCeldaCabecera);

                HSSFCell nombre = filaArea.createCell(1);
                nombre.setCellValue(areaSeleccionada.getNombre() + "");
            }

            HSSFRow filaCabecera = hoja.createRow(filas);

            for (int x = 0; x < cabeceras.length; x++) {
                HSSFCell cabecera = filaCabecera.createCell(x);
                cabecera.setCellValue(cabeceras[x]);
                cabecera.setCellStyle(estiloCeldaCabecera);
            }
            //FIN DE CABECERAS
            for (int i = filas; i < reporte.size(); i++) {

                HSSFRow fila = hoja.createRow(i + 1);

                HSSFCell columna1 = fila.createCell(0);
                columna1.setCellValue(reporte.get(i).getEmpleado().getCodigo());

                HSSFCell columna2 = fila.createCell(1);
                columna2.setCellValue(reporte.get(i).getEmpleado().getNombreCompleto());

                HSSFCell columna3 = fila.createCell(2);
                columna3.setCellValue(reporte.get(i).getFecha());
                columna3.setCellStyle(fechas);

                HSSFCell columna4 = fila.createCell(3);
                columna4.setCellValue(reporte.get(i).getTipo() + "");

                HSSFCell columna5 = fila.createCell(4);
                columna5.setCellValue(reporte.get(i).getHoraEntrada());
                columna5.setCellStyle(style);

                HSSFCell columna6 = fila.createCell(5);
                columna6.setCellValue(reporte.get(i).getHoraSalida());
                columna6.setCellStyle(style);

                HSSFCell columna7 = fila.createCell(6);
                if (reporte.get(i).getMarcacionInicio() != null) {
                    columna7.setCellValue(reporte.get(i).getMarcacionInicio());
                    columna7.setCellStyle(style);
                } else {
                    columna7.setCellValue("No marco.");
                }

                HSSFCell columna8 = fila.createCell(7);
                if (reporte.get(i).getMarcacionFin() != null) {
                    columna8.setCellValue(reporte.get(i).getMarcacionFin());
                    columna8.setCellStyle(style);
                } else {
                    columna8.setCellValue("No marco.");
                }

                HSSFCell columna9 = fila.createCell(8);
                int minutos = (int) ((reporte.get(i).getMilisTardanzaTotal() / (1000 * 60)) % 60);
                columna9.setCellValue(minutos);
                
                HSSFCell columna10 = fila.createCell(9);
                columna10.setCellValue(reporte.get(i).getHoraSalidaRefrigerio());
                columna10.setCellStyle(style);

                HSSFCell columna11 = fila.createCell(10);
                columna11.setCellValue(reporte.get(i).getHoraEntradaRefrigerio());
                columna11.setCellStyle(style);
                
                HSSFCell columna12 = fila.createCell(11);
                if (reporte.get(i).getMarcacionInicioRefrigerio()!= null) {
                    columna12.setCellValue(reporte.get(i).getMarcacionInicioRefrigerio());
                    columna12.setCellStyle(style);
                } else {
                    columna12.setCellValue("No marco.");
                }

                HSSFCell columna13 = fila.createCell(12);
                if (reporte.get(i).getMarcacionFinRefrigerio()!= null) {
                    columna13.setCellValue(reporte.get(i).getMarcacionFinRefrigerio());
                    columna13.setCellStyle(style);
                } else {
                    columna13.setCellValue("No marco.");
                }
                
                HSSFCell columna14 = fila.createCell(13);                
                columna14.setCellValue((int) ((reporte.get(i).getMilisTardanzaRefrigerio()/ (1000 * 60)) % 60));
                
                HSSFCell columna15 = fila.createCell(14);                
                columna15.setCellValue((int) ((reporte.get(i).getMilisTardanzaTotalFinal()/ (1000 * 60)) % 60));
                
            }
            
            try {
                OutputStream output = response.getOutputStream();

                libro.write(output);
                output.close();

                fc.responseComplete();
            } catch (IOException ex) {
                LOG.info("ERROR: " + ex);
            }

            nuevoReporte = false;
        }

    }

    private void realizarAnalisis() {
        if (opcion == 2) {
            LOG.info("SE REALIZA EL ANALISIS POR AREA");
            analisisService.analizarEmpleados(areaSeleccionada);
        }
    }
//    
//    public static  List<DetalleHorario> listaAux1;
//    public static  List<DetalleHorario> listaAux2;
//    public static  Date fechaTurnoAux1;
//    public static  Date fechaTurnoAux2;
//    public static  Empleado empleadoAux1;
//    public static  Empleado empleadoAux2;
//    
//    public void siNoSaleFuimos1(Empleado empleado1, Date fecha1, List<DetalleHorario> lista1){
//        this.empleadoAux1 = empleado1;
//        this.fechaTurnoAux1 = fecha1;
//        this.listaAux2 = lista1;
//    }
//    
//    public void siNoSaleFuimos2(Empleado empleado2, Date fecha2, List<DetalleHorario> lista2){
//        this.empleadoAux2 = empleado2;
//        this.fechaTurnoAux2 = fecha2;
//        this.listaAux2 = lista2;
//    }
//
//    public List analisisCambios(){
//        LOG.info("ENTRO A CAMBIO TURNO");
//        DetalleHorario detalleCambio1 = new DetalleHorario();
//        DetalleHorario detalleCambio2 = new DetalleHorario();
//
//        List<CambioTurno> listaCambios = new ArrayList();
//
//        LOG.info("EMPLEADO1cambio: " + empleadoAux1);
//        LOG.info("EMPLEADO2cambio: " + empleadoAux2);
//
//        if (empleadoAux1 != null && empleadoAux2 != null) {
//
//            LOG.info("DETALLES LLENOS");
//
//            Empleado empleadoCambio1 = empleadoAux1;
//            Empleado empleadoCambio2 = empleadoAux2;
//
//            LOG.info("TAMAÑO DETALLE1: " + listaAux1.size());
//            LOG.info("TAMAÑO DETALLE2: " + listaAux2.size());
//
//            for (DetalleHorario det1 : listaAux1) {
//                if (det1.getFecha().equals(fechaTurnoAux1)) {
//                    detalleCambio1 = det1;
//                }
//            }
//
//            for (DetalleHorario det2 : listaAux2) {
//                if (det2.getFecha().equals(fechaTurnoAux2)) {
//                    detalleCambio2 = det2;
//                }
//            }
//
//            detalleCambio1.getHorarioId().getEmpleadoHorario().setEmpleadoId(empleadoCambio2);
//            detalleCambio2.getHorarioId().getEmpleadoHorario().setEmpleadoId(empleadoCambio1);
//
//            //EMPLEADO1
//            CambioTurno cambioTurno = new CambioTurno();
//
//            cambioTurno.setDetalleHorarioOriginal(detalleCambio1);
//            cambioTurno.setDetalleHorarioReemplazo(detalleCambio2);
//            cambioTurno.setEmpleado1Id(empleadoCambio1);
//            cambioTurno.setEmpleado2Id(empleadoCambio2);
//            Date hora = new Date();
//            cambioTurno.setFechaPedido(hora);
//            cambioTurno.setHoraPedido(hora);
//            listaCambios.add(cambioTurno);
//
//            //EMPLEADO2
//            CambioTurno cambioTurno2 = new CambioTurno();
//
//            cambioTurno2.setDetalleHorarioOriginal(detalleCambio2);
//            cambioTurno2.setDetalleHorarioReemplazo(detalleCambio1);
//            cambioTurno2.setEmpleado1Id(empleadoCambio2);
//            cambioTurno2.setEmpleado2Id(empleadoCambio1);
//            cambioTurno2.setFechaPedido(hora);
//            cambioTurno2.setHoraPedido(hora);
//
//            listaCambios.add(cambioTurno2);
//
//        }
//
//        LOG.info("TAMANO DE LISTA CAMBIOS: " + listaCambios.size());
//        return listaCambios;
//    }
}
