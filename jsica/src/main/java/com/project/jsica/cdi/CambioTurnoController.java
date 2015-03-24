package com.project.jsica.cdi;

import static com.project.jsica.converter.EmpleadoConverter.empleadoDesesperacion;
import com.project.jsica.ejb.entidades.Bitacora;
import com.project.jsica.ejb.entidades.CambioTurno;
import com.project.jsica.ejb.entidades.DetalleHorario;
import com.project.jsica.ejb.entidades.Empleado;
import com.project.jsica.ejb.entidades.EmpleadoHorario;
import com.project.jsica.ejb.entidades.Horario;
import dao.CambioTurnoFacadeLocal;
import dao.DetalleHorarioFacade;
import dao.DetalleHorarioFacadeLocal;
import dao.EmpleadoHorarioFacade;
import dao.EmpleadoHorarioFacadeLocal;
import dao.HorarioFacade;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ejb.EJB;
import javax.enterprise.context.ConversationScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import org.apache.log4j.Logger;
import org.primefaces.event.SelectEvent;
import org.primefaces.event.UnselectEvent;

@Named(value = "cambioTurnoController")
@ViewScoped
public class CambioTurnoController extends AbstractController<CambioTurno> {

    @EJB
    private CambioTurnoFacadeLocal cambioTurnoFacade;

    @EJB
    private DetalleHorarioFacadeLocal detalleHorarioFacade;

    @EJB
    private EmpleadoHorarioFacadeLocal empleadoDAO;

    @Inject
    private BitacoraController bitacoraC;

    @Inject
    private DetalleHorarioController detalleHorarioOriginalController;
    @Inject
    private DetalleHorarioController detalleHorarioReemplazoController;
    @Inject
    private DetalleHorarioController detalleHorario;
    @Inject
    private EmpleadoController jefeInmediatoIdController;
    @Inject
    private CambioTurnoController cambioTurnoControl;

    @Inject
    private EmpleadoHorarioController empleadoHorarioControl;

    private Date fechaTurno1;
    private Date fechaTurno2;

    private boolean isFechaTurno1;
    private boolean isFechaTurno2;

    private Empleado empleado1;
    private Empleado empleado2;

    private List<DetalleHorario> horarioXFecha;
    private boolean isEmpleado1;
    private boolean isEmpleado2;

    public Empleado getEmpleado1() {
        return empleado1;
    }

    public void setEmpleado1(Empleado empleado1) {

        this.empleado1 = empleado1;
        LOG.info("EMPLEADO INGRESADO: " + this.empleado1);
    }

    public Empleado getEmpleado2() {
        return empleado2;
    }

    public void setEmpleado2(Empleado empleado2) {
        this.empleado2 = empleado2;
    }

    public Date getFechaTurno1() {
        return fechaTurno1;
    }

    public void setFechaTurno1(Date fechaTurno1) {
        this.fechaTurno1 = fechaTurno1;

        LOG.info("FECHA1: " + this.fechaTurno1);
    }

    public Date getFechaTurno2() {
        return fechaTurno2;
    }

    public void setFechaTurno2(Date fechaTurno2) {
        this.fechaTurno2 = fechaTurno2;
    }

    public boolean isIsFechaTurno1() {
        return isFechaTurno1;
    }

    public void setIsFechaTurno1(boolean isFechaTurno1) {
        this.isFechaTurno1 = isFechaTurno1;
    }

    public boolean isIsFechaTurno2() {
        return isFechaTurno2;
    }

    public void setIsFechaTurno2(boolean isFechaTurno2) {
        this.isFechaTurno2 = isFechaTurno2;
    }

    public CambioTurnoController() {
        // Inform the Abstract parent controller of the concrete CambioTurno?cap_first Entity
        super(CambioTurno.class);
    }

    /**
     * Resets the "selected" attribute of any parent Entity controllers.
     */
    public void resetParents() {
        detalleHorarioOriginalController.setSelected(null);
        detalleHorarioReemplazoController.setSelected(null);
        jefeInmediatoIdController.setSelected(null);
    }

    /**
     * Sets the "selected" attribute of the DetalleHorario controller in order
     * to display its data in a dialog. This is reusing existing the existing
     * View dialog.
     *
     * @param event Event object for the widget that triggered an action
     */
    public void prepareDetalleHorarioOriginal(ActionEvent event) {
        if (this.getSelected() != null && detalleHorarioOriginalController.getSelected() == null) {
            detalleHorarioOriginalController.setSelected(this.getSelected().getDetalleHorarioOriginal());
        }
    }

    /**
     * Sets the "selected" attribute of the DetalleHorario controller in order
     * to display its data in a dialog. This is reusing existing the existing
     * View dialog.
     *
     * @param event Event object for the widget that triggered an action
     */
    public void prepareDetalleHorarioReemplazo(ActionEvent event) {
        if (this.getSelected() != null && detalleHorarioReemplazoController.getSelected() == null) {
            detalleHorarioReemplazoController.setSelected(this.getSelected().getDetalleHorarioReemplazo());
        }
    }

    /**
     * Sets the "selected" attribute of the Empleado controller in order to
     * display its data in a dialog. This is reusing existing the existing View
     * dialog.
     *
     * @param event Event object for the widget that triggered an action
     */
    public void prepareJefeInmediatoId(ActionEvent event) {
        if (this.getSelected() != null && jefeInmediatoIdController.getSelected() == null) {
            jefeInmediatoIdController.setSelected(this.getSelected().getJefeInmediatoId());
        }
    }

    @Override
    protected void edit(CambioTurno objeto) {

        this.cambioTurnoFacade.edit(objeto);
//        if (this.esNuevo) {
//            Bitacora bitacora = new Bitacora();
//            //----Bitacora----
//            //Fecha y hora//          
//            Date fechas = new Date();
////            System.out.println("fecha: "+dt.format(fechas));
////           
//            //Ip Cliente
//            String ip_cliente = ((HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest()).getRemoteAddr();
//
//            String detalle_horario_original = this.selected.getDetalleHorarioOriginal().toString();
//            String detalle_horario_reemplazo = this.selected.getDetalleHorarioReemplazo().toString();
//            String fecha_pedido = this.selected.getFechaPedido().toString();
//            String hora_pedido = this.selected.getHoraPedido().toString();
//            String jefe_inmediato_id = this.selected.getJefeInmediatoId().getApellidos() + ", " + this.selected.getJefeInmediatoId().getNombres();
//
//            bitacora.setUsuario("JC");
//            bitacora.setIpCliente(ip_cliente);
//            bitacora.setFecha(fechas);
//            bitacora.setHora(fechas);
//            bitacora.setTabla("CAMBIO_TURNO");
//            bitacora.setColumna("DETALLE_HORARIO_ORIGINAL");
//            bitacora.setAccion("CREAR");
//            bitacora.setValorAct(detalle_horario_original);
//            bitacora.setValorAnt(" ");
//            bitacoraC.edit(bitacora);
//
//            bitacora.setColumna("DETALLE_HORARIO_REEMPLAZO");
//            bitacora.setValorAct(detalle_horario_reemplazo);
//            bitacora.setValorAnt(" ");
//            bitacoraC.edit(bitacora);
//
//            bitacora.setColumna("FECHA_PEDIDO");
//            bitacora.setValorAct(fecha_pedido);
//            bitacora.setValorAnt(" ");
//            bitacoraC.edit(bitacora);
//
//            bitacora.setColumna("HORA_PEDIDO");
//            bitacora.setValorAct(hora_pedido);
//            bitacora.setValorAnt(" ");
//            bitacoraC.edit(bitacora);
//
//            bitacora.setColumna("JEFE_INMEDIATO");
//            bitacora.setValorAct(jefe_inmediato_id);
//            bitacora.setValorAnt(" ");
//            bitacoraC.edit(bitacora);
//
//        } else {
//            //Datos antes de modificar
//            CambioTurno antes = this.find(this.selected.getId());
//            String detalle_horario_original1 = antes.getDetalleHorarioOriginal().toString();
//            String detalle_horario_reemplazo1 = antes.getDetalleHorarioReemplazo().toString();
//            String fecha_pedido1 = antes.getFechaPedido().toString();
//            String hora_pedido1 = antes.getHoraPedido().toString();
//            String jefe_inmediato_id1 = antes.getJefeInmediatoId().getApellidos() + ", " + this.selected.getJefeInmediatoId().getNombres();
//
//            //Datos despues de modificar
//            String detalle_horario_original2 = this.selected.getDetalleHorarioOriginal().toString();
//            String detalle_horario_reemplazo2 = this.selected.getDetalleHorarioReemplazo().toString();
//            String fecha_pedido2 = this.selected.getFechaPedido().toString();
//            String hora_pedido2 = this.selected.getHoraPedido().toString();
//            String jefe_inmediato_id2 = this.selected.getJefeInmediatoId().getApellidos() + ", " + this.selected.getJefeInmediatoId().getNombres();
//
//            //----Bitacora----
//            Bitacora bitacora = new Bitacora();
//            //Fecha y hora//          
//            Date fechas = new Date();
//
//            //Ip Cliente
//            String ip_cliente = ((HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest()).getRemoteAddr();
//
//            //Datos
//            bitacora.setUsuario("JC");
//            bitacora.setIpCliente(ip_cliente);
//            bitacora.setFecha(fechas);
//            bitacora.setHora(fechas);
//            bitacora.setTabla("BIOMETRICO");
//            bitacora.setColumna("DETALLE_HORARIO_ORIGINAL");
//            bitacora.setAccion("MODIFICAR");
//            bitacora.setValorAnt(detalle_horario_original1);
//            bitacora.setValorAct(detalle_horario_original2);
//
//            if (!detalle_horario_original1.equals(detalle_horario_original2)) {
//                bitacoraC.edit(bitacora);
//            }
//
//            bitacora.setColumna("DETALLE_HORARIO_REEMPLAZO");
//            bitacora.setValorAnt(detalle_horario_reemplazo1);
//            bitacora.setValorAct(detalle_horario_reemplazo2);
//
//            if (!detalle_horario_reemplazo1.equals(detalle_horario_reemplazo2)) {
//                bitacoraC.edit(bitacora);
//            }
//
////            System.out.println(anio1+" "+nombre1+" "+vigente1);
//            bitacora.setColumna("FECHA_PEDIDO");
//            bitacora.setValorAnt(fecha_pedido1);
//            bitacora.setValorAct(fecha_pedido2);
//
//            if (!fecha_pedido1.equals(fecha_pedido2)) {
//                bitacoraC.edit(bitacora);
//            }
//
//            bitacora.setColumna("HORA_PEDIDO");
//            bitacora.setValorAnt(hora_pedido1);
//            bitacora.setValorAct(hora_pedido2);
//
//            if (!hora_pedido1.equals(hora_pedido2)) {
//                bitacoraC.edit(bitacora);
//            }
//
//            bitacora.setColumna("JEFE_INMEDIATO");
//            bitacora.setValorAnt(jefe_inmediato_id1);
//            bitacora.setValorAct(jefe_inmediato_id2);
//
//            if (!jefe_inmediato_id1.equals(jefe_inmediato_id2)) {
//                bitacoraC.edit(bitacora);
//            }
//
//        }
    }

    @Override
    protected void remove(CambioTurno objeto) {
//        Bitacora bitacora = new Bitacora();
//        //----Bitacora----
//        //Fecha y hora//          
//        Date fechas = new Date();
//
//        //Ip Cliente
//        String ip_cliente = ((HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest()).getRemoteAddr();
//
//        //Campos
//        String detalle_horario_original1 = this.selected.getDetalleHorarioOriginal().toString();
//        String detalle_horario_reemplazo1 = this.selected.getDetalleHorarioReemplazo().toString();
//        String fecha_pedido1 = this.selected.getFechaPedido().toString();
//        String hora_pedido1 = this.selected.getHoraPedido().toString();
//        String jefe_inmediato_id1 = this.selected.getJefeInmediatoId().getApellidos() + ", " + this.selected.getJefeInmediatoId().getNombres();
//
//        bitacora.setUsuario(" ");
//        bitacora.setIpCliente(ip_cliente);
//        Logger.getLogger(AnioController.class.getName()).info("la ip es " + ip_cliente);
//        bitacora.setFecha(fechas);
//        bitacora.setHora(fechas);
//        bitacora.setTabla("CAMBIO_TURNO");
//        bitacora.setColumna("DETALLE_HORARIO_ORIGINAL");
//        bitacora.setAccion("CREAR");
//        bitacora.setValorAnt(detalle_horario_original1);
//        bitacora.setValorAct(" ");
//        bitacoraC.edit(bitacora);
//
//        bitacora.setColumna("DETALLE_HORARIO_REEMPLAZO");
//        bitacora.setValorAnt(detalle_horario_reemplazo1);
//        bitacora.setValorAct(" ");
//        bitacoraC.edit(bitacora);
//
//        bitacora.setColumna("FECHA_PEDIDO");
//        bitacora.setValorAnt(fecha_pedido1);
//        bitacora.setValorAct(" ");
//        bitacoraC.edit(bitacora);
//
//        bitacora.setColumna("HORA_PEDIDO");
//        bitacora.setValorAnt(hora_pedido1);
//        bitacora.setValorAct(" ");
//        bitacoraC.edit(bitacora);
//
//        bitacora.setColumna("JEFE_INMEDIATO");
//        bitacora.setValorAnt(jefe_inmediato_id1);
//        bitacora.setValorAct(" ");
//        bitacoraC.edit(bitacora);

        this.cambioTurnoFacade.remove(objeto);
    }

    @Override
    public CambioTurno find(Object id) {
        return this.cambioTurnoFacade.find(id);
    }

    @Override
    public List<CambioTurno> getItems() {
        return this.cambioTurnoFacade.findAll();
    }

    @Override
    public List<CambioTurno> search(String namedQuery) {
        return this.cambioTurnoFacade.search(namedQuery);
    }

    @Override
    public List<CambioTurno> search(String namedQuery, Map<String, Object> parametros) {
        return this.cambioTurnoFacade.search(namedQuery, parametros);
    }

    @Override
    public List<CambioTurno> search(String namedQuery, Map<String, Object> parametros, int inicio, int tamanio) {
        return this.cambioTurnoFacade.search(namedQuery, parametros, inicio, tamanio);
    }

    public List<DetalleHorario> getTurnosxEmpleados1() {
        String query = "SELECT eh FROM empleadoHorario WHERE eh.empleadoId=:dni";
        Map<String, Object> parametros = new HashMap<>();
        parametros.put("dni", this.getSelected().getEmpleado1Id().getDocIdentidad());
        List<EmpleadoHorario> lista = empleadoDAO.search(query, parametros);

        List<DetalleHorario> resultado = new ArrayList<>();

        for (EmpleadoHorario eh : lista) {
            resultado.addAll(eh.getHorarioId().getDetalleHorarioList());
        }

        return resultado;
    }

    public List<DetalleHorario> getTurnosxEmpleados2() {
        String query = "SELECT eh FROM empleadoHorario WHERE eh.empleadoId=:dni";
        Map<String, Object> parametros = new HashMap<>();
        parametros.put("dni", this.getSelected().getEmpleado2Id().getDocIdentidad());
        List<EmpleadoHorario> lista = empleadoDAO.search(query, parametros);

        List<DetalleHorario> resultado = new ArrayList<>();

        for (EmpleadoHorario eh : lista) {
            resultado.addAll(eh.getHorarioId().getDetalleHorarioList());
        }

        return resultado;
    }

    public void onEmpleadoSelecciona1() {
        if (this.empleado1 != null) {
            this.isEmpleado1 = true;
            return;
        }
        this.isEmpleado1 = false;
    }

    public void onEmpleadoSelecciona2() {
        if (this.empleado2 != null) {
            this.isEmpleado2 = true;
            return;
        }
        this.isEmpleado2 = false;
    }

    public void onFechaSelecciona1() {
        if (this.fechaTurno1 != null) {
            this.isFechaTurno1 = true;
            return;
        }
        this.isFechaTurno1 = false;
    }

    public void onFechaSelecciona2() {
        if (this.fechaTurno2 != null) {
            this.isFechaTurno2 = true;
            return;
        }
        this.isFechaTurno2 = false;
    }

    public List<DetalleHorario> getHorarioXFecha() {
        if (isFechaTurno1) {
            return detalleHorarioFacade.buscarXEmpleadoXFecha(empleado1, fechaTurno1);
        }
        return detalleHorarioFacade.findAll();
    }

    public void setHorarioXFecha(List<DetalleHorario> horarioXFecha) {
        this.horarioXFecha = horarioXFecha;
    }

    private static final java.util.logging.Logger LOG = java.util.logging.Logger.getLogger(CambioTurnoController.class.getName());

    List<DetalleHorario> detalle1 = new ArrayList();

    List<DetalleHorario> listaAux1;
    List<DetalleHorario> listaAux2;
    Date fechaTurnoAux1;
    Date fechaTurnoAux2;
    Empleado empleadoAux1;
    Empleado empleadoAux2;

    private boolean flag = false;

    public List<DetalleHorario> getHorarios1() {

        this.empleado1 = empleadoDesesperacion;
        LOG.info("EMPLEADO 1: " + this.empleado1);
        if (fechaTurno1 != null && empleado1 != null) {
            fechaTurnoAux1 = fechaTurno1;
            empleadoAux1 = empleado1;
            LOG.info("Lista de Horarios por Fecha, parametros:");
            LOG.info("Empleado: " + empleadoAux1);
            LOG.info("FECHA: " + fechaTurnoAux1);
            detalle1 = this.detalleHorarioFacade.buscarXEmpleadoXFecha(empleadoAux1, fechaTurnoAux1);
            listaAux1 = detalle1;
            LOG.info("TAMAÑO detalle1: " + listaAux1.size());
            return detalle1;
//            return this.detalleHorarioFacade.buscarXEmpleadoXFecha2(empleado1, fechaTurno1);
        } else {
            LOG.info("LA LISTA ES NULL :c");
            return detalle1;
        }
    }

    private List<DetalleHorario> detalle2 = new ArrayList();

    public List<DetalleHorario> getHorarios2() {
        this.empleado2 = empleadoDesesperacion;
        LOG.info("EMPLEADO2 : " + this.empleado2);

        if (fechaTurno2 != null && this.empleado2 != null) {

            fechaTurnoAux2 = fechaTurno2;
            empleadoAux2 = empleado2;
            LOG.info("Lista de Horarios por Fecha, parametros:");
            LOG.info("Empleado2: " + empleadoAux2);
            LOG.info("FECHA2: " + fechaTurnoAux2);
            detalle2 = this.detalleHorarioFacade.buscarXEmpleadoXFecha(empleadoAux2, fechaTurnoAux2);
            listaAux2 = detalle2;
            LOG.info("TAMAÑO detalle2: " + listaAux2.size());
            return detalle2;
//            return this.detalleHorarioFacade.buscarXEmpleadoXFecha2(empleado2, fechaTurno2);
        } else {
            LOG.info("LA LISTA 2 ES NULL :c");
            return detalle2;
        }
    }

    List<CambioTurno> listaCambios = new ArrayList();

    public List getCambioTurno() {
        LOG.info("ENTRO A CAMBIO TURNO");
        DetalleHorario detalleCambio1 = new DetalleHorario();
        DetalleHorario detalleCambio2 = new DetalleHorario();

//        List<CambioTurno> listaCambios = new ArrayList();

        LOG.info("EMPLEADO1cambio: " + empleadoAux1);
        LOG.info("EMPLEADO2cambio: " + empleadoAux2);

        LOG.info("FLAG: " + flag);

        if (empleadoAux1 != null && empleadoAux2 != null && !flag) {

            LOG.info("DETALLES LLENOS");

            Empleado empleadoCambio1 = empleadoAux1;
            Empleado empleadoCambio2 = empleadoAux2;

            LOG.info("TAMAÑO DETALLE1: " + listaAux1.size());
            LOG.info("TAMAÑO DETALLE2: " + listaAux2.size());

            for (DetalleHorario det1 : listaAux1) {
                if (det1.getFecha().equals(fechaTurnoAux1)) {
                    detalleCambio1 = det1;
                }
            }

            for (DetalleHorario det2 : listaAux2) {
                if (det2.getFecha().equals(fechaTurnoAux2)) {
                    detalleCambio2 = det2;
                }
            }
            
            Horario horariocambio1 = detalleCambio1.getHorarioId();
            Horario horariocambio2 = detalleCambio2.getHorarioId();
            
            LOG.info("Empleado Original del detalle1: " + detalleCambio1.getHorarioId().getEmpleadoHorario().getEmpleadoId().getNombreCompleto());
            detalleCambio1.setHorarioId(horariocambio2);
            detalleHorario.setSelected(detalleCambio1);
            detalleHorario.edit(detalleCambio1);            
            LOG.info("Empleado Reemplazo del detalle1: " + detalleCambio1.getHorarioId().getEmpleadoHorario().getEmpleadoId().getNombreCompleto());
            LOG.info("SE MODIFICO DETALLE1");
            
            
            detalleCambio2.setHorarioId(horariocambio1);
            detalleHorario.setSelected(detalleCambio2);
            detalleHorario.edit(detalleCambio2); 
            LOG.info("SE MODIFICO DETALLE2");
            

//            detalleCambio1.getHorarioId().getEmpleadoHorario().setEmpleadoId(empleadoCambio2);
//            List<EmpleadoHorario> empH = empleadoHorarioControl.buscarXHorario(detalleCambio1.getHorarioId());
//            LOG.info(empH.get(0).getEmpleadoId().getDocIdentidad());

//            empleadoHorarioControl.setSelected(empH.get(0));
//            empH.get(0).setEmpleadoId(empleadoCambio2);
//            empleadoHorarioControl.edit(empH.get(0));
//            
            
//            List<EmpleadoHorario> empH2 = empleadoHorarioControl.buscarXHorario(detalleCambio1.getHorarioId());
//            LOG.info(empH2.get(0).getEmpleadoId().getDocIdentidad());
            

//            detalleCambio2.getHorarioId().getEmpleadoHorario().setEmpleadoId(empleadoCambio1);
//            detalleHorario.setSelected(detalleCambio2);
//            detalleHorario.edit(detalleCambio2);
//            List<EmpleadoHorario> empH3 = empleadoHorarioControl.buscarXHorario(detalleCambio2.getHorarioId());
//            empleadoHorarioControl.setSelected(empH3.get(0));
//            empH3.get(0).setEmpleadoId(empleadoCambio1);
//            empleadoHorarioControl.edit(empH3.get(0));
            

            //EMPLEADO1
            CambioTurno cambioTurno = new CambioTurno();

            cambioTurno.setDetalleHorarioOriginal(detalleCambio1);
            cambioTurno.setDetalleHorarioReemplazo(detalleCambio2);
            cambioTurno.setEmpleado1Id(empleadoCambio1);
            cambioTurno.setEmpleado2Id(empleadoCambio2);
            Date hora = new Date();
            cambioTurno.setFechaPedido(hora);
            cambioTurno.setHoraPedido(hora);
            cambioTurno.setJefeInmediatoId(empleado1);
            LOG.info("CAMBIO TURNO1: " + cambioTurno);
            cambioTurnoControl.setSelected(cambioTurno);
            LOG.info("LO SETEO1");
            cambioTurnoControl.edit(cambioTurno);
            LOG.info("Y LO GUARDO1");
            listaCambios.add(cambioTurno);

            //EMPLEADO2
            CambioTurno cambioTurno2 = new CambioTurno();

            cambioTurno2.setDetalleHorarioOriginal(detalleCambio2);
            cambioTurno2.setDetalleHorarioReemplazo(detalleCambio1);
            cambioTurno2.setEmpleado1Id(empleadoCambio2);
            cambioTurno2.setEmpleado2Id(empleadoCambio1);
            cambioTurno2.setFechaPedido(hora);
            cambioTurno2.setHoraPedido(hora);
            LOG.info("CAMBIO TURNO2: " + cambioTurno2);
            LOG.info("LO SETEO2");
            cambioTurnoControl.setSelected(cambioTurno2);
            cambioTurnoControl.edit(cambioTurno2);
            LOG.info("Y LO GUARDO2");

            listaCambios.add(cambioTurno2);

            flag = true;
            LOG.info("FLAG: " + flag);
        }

        LOG.info("TAMANO DE LISTA CAMBIOS: " + listaCambios.size());
        return listaCambios;
    }

}
