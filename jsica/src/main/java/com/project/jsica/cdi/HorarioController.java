package com.project.jsica.cdi;

import com.project.jsica.ejb.dao.HorarioFacadeLocal;
import com.project.jsica.ejb.entidades.Bitacora;
import com.project.jsica.ejb.entidades.Horario;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.ejb.EJB;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;

@Named(value = "horarioController")
@ViewScoped
public class HorarioController extends AbstractController<Horario> {

    @EJB
    private HorarioFacadeLocal horarioFacade;

    @Inject
    private BitacoraController bitacoraC;

    @Inject
    private DetalleHorarioController detalleHorarioListController;
    @Inject
    private EmpleadoHorarioController empleadoHorarioListController;

    public HorarioController() {
        // Inform the Abstract parent controller of the concrete Horario?cap_first Entity
        super(Horario.class);
    }

    /**
     * Resets the "selected" attribute of any parent Entity controllers.
     */
    public void resetParents() {
    }

    /**
     * Sets the "items" attribute with a collection of DetalleHorario entities
     * that are retrieved from Horario?cap_first and returns the navigation
     * outcome.
     *
     * @return navigation outcome for DetalleHorario page
     */
    public String navigateDetalleHorarioList() {
        if (this.getSelected() != null) {
            FacesContext.getCurrentInstance().getExternalContext().getRequestMap().put("DetalleHorario_items", this.getSelected().getDetalleHorarioList());
        }
        return "/detalleHorario/index";
    }

    /**
     * Sets the "items" attribute with a collection of EmpleadoHorario entities
     * that are retrieved from Horario?cap_first and returns the navigation
     * outcome.
     *
     * @return navigation outcome for EmpleadoHorario page
     */
    public String navigateEmpleadoHorarioList() {
        if (this.getSelected() != null) {
            FacesContext.getCurrentInstance().getExternalContext().getRequestMap().put("EmpleadoHorario_items", this.getSelected().getEmpleadoHorarioList());
        }
        return "/empleadoHorario/index";
    }

    @Override
    protected void edit(Horario objeto) {
        this.horarioFacade.edit(objeto);

        if (this.esNuevo) {
            Bitacora bitacora = new Bitacora();
            //----Bitacora----
            //Fecha y hora//          
            Date fechas = new Date();//           
            //Ip Cliente
            String ip_cliente = ((HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest()).getRemoteAddr();

            String nombre = this.selected.getNombre();
            String descripcion = this.selected.getDescripcion();
            String porFecha = String.valueOf(this.selected.getPorFecha());
            String fecha = this.selected.getFecha().toString();
            String lunes = String.valueOf(this.selected.getLunes());
            String martes = String.valueOf(this.selected.getMartes());
            String miercoles = String.valueOf(this.selected.getMiercoles());
            String jueves = String.valueOf(this.selected.getJueves());
            String viernes = String.valueOf(this.selected.getViernes());
            String sabado = String.valueOf(this.selected.getSabado());
            String domingo = String.valueOf(this.selected.getDomingo());

            bitacora.setUsuario("JC");
            bitacora.setIpCliente(ip_cliente);
            bitacora.setFecha(fechas);
            bitacora.setHora(fechas);
            bitacora.setTabla("HORARIO");
            bitacora.setColumna("NOMBRE");
            bitacora.setAccion("CREAR");
            bitacora.setValorAct(nombre);
            bitacora.setValorAnt(" ");
            bitacoraC.edit(bitacora);

            bitacora.setColumna("DESCRIPCION");
            bitacora.setValorAct(descripcion);
            bitacora.setValorAnt(" ");
            bitacoraC.edit(bitacora);

            bitacora.setColumna("POR_FECHA");
            bitacora.setValorAct(porFecha);
            bitacora.setValorAnt(" ");
            bitacoraC.edit(bitacora);

            bitacora.setColumna("FECHA");
            bitacora.setValorAct(fecha);
            bitacora.setValorAnt(" ");
            bitacoraC.edit(bitacora);

            bitacora.setColumna("LUNES");
            bitacora.setValorAct(lunes);
            bitacora.setValorAnt(" ");
            bitacoraC.edit(bitacora);

            bitacora.setColumna("MARTES");
            bitacora.setValorAct(martes);
            bitacora.setValorAnt(" ");
            bitacoraC.edit(bitacora);

            bitacora.setColumna("MIERCOLES");
            bitacora.setValorAct(miercoles);
            bitacora.setValorAnt(" ");
            bitacoraC.edit(bitacora);

            bitacora.setColumna("JUEVES");
            bitacora.setValorAct(jueves);
            bitacora.setValorAnt(" ");
            bitacoraC.edit(bitacora);

            bitacora.setColumna("VIERNES");
            bitacora.setValorAct(viernes);
            bitacora.setValorAnt(" ");
            bitacoraC.edit(bitacora);

            bitacora.setColumna("SABADO");
            bitacora.setValorAct(sabado);
            bitacora.setValorAnt(" ");
            bitacoraC.edit(bitacora);

            bitacora.setColumna("DOMINGO");
            bitacora.setValorAct(domingo);
            bitacora.setValorAnt(" ");
            bitacoraC.edit(bitacora);
        } else {
            //Datos antes de modificar
            Horario antes = this.find(this.selected.getId());

            String nombre1 = antes.getNombre();
            String descripcion1 = antes.getDescripcion();
            String porFecha1 = String.valueOf(antes.getPorFecha());
            String fecha1 = antes.getFecha().toString();
            String lunes1 = String.valueOf(antes.getLunes());
            String martes1 = String.valueOf(antes.getMartes());
            String miercoles1 = String.valueOf(antes.getMiercoles());
            String jueves1 = String.valueOf(antes.getJueves());
            String viernes1 = String.valueOf(antes.getViernes());
            String sabado1 = String.valueOf(antes.getSabado());
            String domingo1 = String.valueOf(antes.getDomingo());

            //Datos despues de modificar
            String nombre2 = this.selected.getNombre();
            String descripcion2 = this.selected.getDescripcion();
            String porFecha2 = String.valueOf(this.selected.getPorFecha());
            String fecha2 = this.selected.getFecha().toString();
            String lunes2 = String.valueOf(this.selected.getLunes());
            String martes2 = String.valueOf(this.selected.getMartes());
            String miercoles2 = String.valueOf(this.selected.getMiercoles());
            String jueves2 = String.valueOf(this.selected.getJueves());
            String viernes2 = String.valueOf(this.selected.getViernes());
            String sabado2 = String.valueOf(this.selected.getSabado());
            String domingo2 = String.valueOf(this.selected.getDomingo());

            //----Bitacora----
            Bitacora bitacora = new Bitacora();
            //Fecha y hora//          
            Date fechas = new Date();
//           
            //Ip Cliente
            String ip_cliente = ((HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest()).getRemoteAddr();

            //Datos
            bitacora.setUsuario("JC");
            bitacora.setIpCliente(ip_cliente);
            bitacora.setFecha(fechas);
            bitacora.setHora(fechas);
            bitacora.setTabla("HORARIO");
            bitacora.setColumna("NOMBRE");
            bitacora.setAccion("MODIFICAR");
            bitacora.setValorAct(nombre2);
            bitacora.setValorAnt(nombre1);

            if (!nombre1.equals(nombre2)) {
                bitacoraC.edit(bitacora);
            }

            bitacora.setColumna("DESCRIPCION");
            bitacora.setValorAct(descripcion2);
            bitacora.setValorAnt(descripcion1);

            if (!descripcion1.equals(descripcion2)) {
                bitacoraC.edit(bitacora);
            }

            bitacora.setColumna("POR_FECHA");
            bitacora.setValorAct(porFecha2);
            bitacora.setValorAnt(porFecha1);

            if (!porFecha1.equals(porFecha2)) {
                bitacoraC.edit(bitacora);
            }

            bitacora.setColumna("FECHA");
            bitacora.setValorAct(fecha2);
            bitacora.setValorAnt(fecha1);

            if (!fecha1.equals(fecha2)) {
                bitacoraC.edit(bitacora);
            }

            bitacora.setColumna("LUNES");
            bitacora.setValorAct(lunes2);
            bitacora.setValorAnt(lunes1);

            if (!lunes1.equals(lunes2)) {
                bitacoraC.edit(bitacora);
            }

            bitacora.setColumna("MARTES");
            bitacora.setValorAct(martes2);
            bitacora.setValorAnt(martes1);

            if (!martes1.equals(martes2)) {
                bitacoraC.edit(bitacora);
            }

            bitacora.setColumna("MIERCOLES");
            bitacora.setValorAct(miercoles2);
            bitacora.setValorAnt(miercoles1);

            if (!miercoles1.equals(miercoles2)) {
                bitacoraC.edit(bitacora);
            }

            bitacora.setColumna("JUEVES");
            bitacora.setValorAct(jueves2);
            bitacora.setValorAnt(jueves1);

            if (!jueves1.equals(jueves2)) {
                bitacoraC.edit(bitacora);
            }

            bitacora.setColumna("VIERNES");
            bitacora.setValorAct(viernes2);
            bitacora.setValorAnt(viernes1);

            if (!viernes1.equals(viernes2)) {
                bitacoraC.edit(bitacora);
            }

            bitacora.setColumna("SABADO");
            bitacora.setValorAct(sabado2);
            bitacora.setValorAnt(sabado1);

            if (!sabado1.equals(sabado2)) {
                bitacoraC.edit(bitacora);
            }

            bitacora.setColumna("DOMINGO");
            bitacora.setValorAct(domingo2);
            bitacora.setValorAnt(domingo1);

            if (!domingo1.equals(domingo2)) {
                bitacoraC.edit(bitacora);
            }
        }
    }

    @Override
    protected void remove(Horario objeto) {
        this.horarioFacade.remove(objeto);

        //Datos antes de modificar
        Horario antes = this.find(this.selected.getId());

        String nombre1 = antes.getNombre();
        String descripcion1 = antes.getDescripcion();
        String porFecha1 = String.valueOf(antes.getPorFecha());
        String fecha1 = antes.getFecha().toString();
        String lunes1 = String.valueOf(antes.getLunes());
        String martes1 = String.valueOf(antes.getMartes());
        String miercoles1 = String.valueOf(antes.getMiercoles());
        String jueves1 = String.valueOf(antes.getJueves());
        String viernes1 = String.valueOf(antes.getViernes());
        String sabado1 = String.valueOf(antes.getSabado());
        String domingo1 = String.valueOf(antes.getDomingo());

        //----Bitacora----
        Bitacora bitacora = new Bitacora();
        //Fecha y hora//          
        Date fechas = new Date();
//           
        //Ip Cliente
        String ip_cliente = ((HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest()).getRemoteAddr();

        //Datos
        bitacora.setUsuario("JC");
        bitacora.setIpCliente(ip_cliente);
        bitacora.setFecha(fechas);
        bitacora.setHora(fechas);
        bitacora.setTabla("HORARIO");
        bitacora.setColumna("NOMBRE");
        bitacora.setAccion("ELIMINAR");
        bitacora.setValorAct(" ");
        bitacora.setValorAnt(nombre1);
        bitacoraC.edit(bitacora);
        
        bitacora.setColumna("DESCRIPCION");
        bitacora.setValorAct(" ");
        bitacora.setValorAnt(descripcion1);
        bitacoraC.edit(bitacora);
        
        bitacora.setColumna("POR_FECHA");
        bitacora.setValorAct(" ");
        bitacora.setValorAnt(porFecha1);
        bitacoraC.edit(bitacora);
        
        bitacora.setColumna("FECHA");
        bitacora.setValorAct(" ");
        bitacora.setValorAnt(fecha1);
        bitacoraC.edit(bitacora);
        
        bitacora.setColumna("LUNES");
        bitacora.setValorAct(" ");
        bitacora.setValorAnt(lunes1);
        bitacoraC.edit(bitacora);
        
        bitacora.setColumna("MARTES");
        bitacora.setValorAct(" ");
        bitacora.setValorAnt(martes1);
        bitacoraC.edit(bitacora);
        
        bitacora.setColumna("MIERCOLES");
        bitacora.setValorAct(" ");
        bitacora.setValorAnt(miercoles1);
        bitacoraC.edit(bitacora);
        
        bitacora.setColumna("JUEVES");
        bitacora.setValorAct(" ");
        bitacora.setValorAnt(jueves1);
        bitacoraC.edit(bitacora);
        
        bitacora.setColumna("VIERNES");
        bitacora.setValorAct(" ");
        bitacora.setValorAnt(viernes1);
        bitacoraC.edit(bitacora);
        
        bitacora.setColumna("SABADO");
        bitacora.setValorAct(" ");
        bitacora.setValorAnt(sabado1);
        bitacoraC.edit(bitacora);
        
        bitacora.setColumna("DOMINGO");
        bitacora.setValorAct(" ");
        bitacora.setValorAnt(domingo1);
        bitacoraC.edit(bitacora);        
    }

    @Override
    public Horario find(Object id) {
        return this.horarioFacade.find(id);
    }

    @Override
    public List<Horario> getItems() {
        return this.horarioFacade.findAll();
    }

    @Override
    public List<Horario> search(String namedQuery) {
        return this.horarioFacade.search(namedQuery);
    }

    @Override
    public List<Horario> search(String namedQuery, Map<String, Object> parametros) {
        return this.horarioFacade.search(namedQuery, parametros);
    }

    @Override
    public List<Horario> search(String namedQuery, Map<String, Object> parametros, int inicio, int tamanio) {
        return this.horarioFacade.search(namedQuery, parametros, inicio, tamanio);
    }

}
