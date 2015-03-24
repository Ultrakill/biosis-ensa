package com.project.jsica.cdi;

import dao.EmpleadoHorarioFacadeLocal;
import com.project.jsica.ejb.entidades.Anio;
import com.project.jsica.ejb.entidades.DetalleHorario;
import com.project.jsica.ejb.entidades.Empleado;
import com.project.jsica.ejb.entidades.EmpleadoHorario;
import com.project.jsica.ejb.entidades.Horario;
import com.project.jsica.ejb.entidades.Jornada;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.enterprise.context.ConversationScoped;
import javax.enterprise.context.SessionScoped;
import javax.inject.Named;
import javax.faces.view.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.inject.Inject;

@Named(value = "empleadoHorarioController")
@SessionScoped
public class EmpleadoHorarioController extends AbstractController<EmpleadoHorario> {
    @EJB
    private EmpleadoHorarioFacadeLocal empleadoHorarioFacade;
    @Inject
    private EmpleadoController empleadoIdController;
    @Inject
    private HorarioController horarioIdController;
    @Inject
    private GrupoHorarioController grupoHorarioIdController;
    private static final Logger LOG = Logger.getLogger(EmpleadoHorarioController.class.getName());
    
    
    
    private List<DetalleHorario> detallesHorario;
    private EmpleadoHorario horarioSeleccionado;
    private int mes;
    private Anio anio;
    private String diaSeleccionado;
    private DetalleHorario detalleSeleccionado;
    private DetalleHorario detalleTablaSeleccionado;
    private Date diasAgregados;
    private Jornada jornadaSeleccionada;
    
    public DetalleHorario getDetalleTablaSeleccionado() {
        return detalleTablaSeleccionado;
    }

    public void setDetalleTablaSeleccionado(DetalleHorario detalleTablaSeleccionado) {
        this.detalleTablaSeleccionado = detalleTablaSeleccionado;
    }

    public DetalleHorario crearDetalleHorario() {
        detalleSeleccionado = new DetalleHorario();
        detalleSeleccionado.setHorarioId(this.horarioSeleccionado.getHorarioId());
        LOG.info("VIENE AL CREAR DETALLE HORARIO");
        return detalleSeleccionado;
    }

    public void agregarFecha() {
        StringTokenizer token = new StringTokenizer(diaSeleccionado, ",");
        Calendar cal = Calendar.getInstance();
        DetalleHorario detalle;
        while (token.hasMoreTokens()) {
            int dia = Integer.parseInt(token.nextToken());
            detalle = new DetalleHorario();
            detalle.setHorarioId(this.horarioSeleccionado.getHorarioId());
            detalle.setJornadaCodigo(this.detalleSeleccionado.getJornadaCodigo());
            cal.set(Integer.parseInt(anio.getAnio()), mes, dia);
            Date fecha = cal.getTime();
            detalle.setFecha(fecha);
            this.horarioSeleccionado.getHorarioId().getDetalleHorarioList().add(detalle);
        }

        
        LOG.log(Level.INFO, "TAMANIO LISTA {0}", this.horarioSeleccionado.getHorarioId().getDetalleHorarioList().size());
    }

    public String getDiaSeleccionado() {
        return diaSeleccionado;
    }

    public void setDiaSeleccionado(String diaSeleccionado) {
        this.diaSeleccionado = diaSeleccionado;
    }

    public Date getDiasAgregados() {
        return diasAgregados;
    }

    public void setDiasAgregados(Date diasAgregados) {
        this.diasAgregados = diasAgregados;
    }

    public int getUltimoDiaMes() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.MONTH, mes);
        return cal.getMaximum(Calendar.DAY_OF_MONTH);
    }

    public Jornada getJornadaSeleccionada() {
        return jornadaSeleccionada;
    }

    public void setJornadaSeleccionada(Jornada jornadaSeleccionada) {
        this.jornadaSeleccionada = jornadaSeleccionada;
    }

    public DetalleHorario getDetalleSeleccionado() {
        return detalleSeleccionado;
    }

    public void setDetalleSeleccionado(DetalleHorario detalleSeleccionado) {
        this.detalleSeleccionado = detalleSeleccionado;
    }

    public List<DetalleHorario> getDetallesHorario() {
        return detallesHorario;
    }

    public void setDetallesHorario(List<DetalleHorario> detallesHorario) {
        this.detallesHorario = detallesHorario;
    }

    public Anio getAnio() {
        return anio;
    }

    public void setAnio(Anio anio) {
        LOG.info("ANIO SELECCIONADO");
        this.anio = anio;
    }

    public int getMes() {
        return mes;
    }

    public void setMes(int mes) {
        this.mes = mes;
    }

    public boolean getBandera() {
        LOG.info("ANIO: "+anio);
        LOG.info("MES: "+mes);
        LOG.info("HORARIO SELECCIONADO: "+this.horarioSeleccionado);
        if (this.anio != null && this.mes != -1 && this.horarioSeleccionado.getEmpleadoId() != null) {
            LOG.info("VERDADERO");
            return true;
        }
        LOG.info("FALSO");
        return false;
    }

    public void mesSeleccionado() {
        LOG.log(Level.INFO, "MES SELECCIONADO {0}", mes);
    }
    
    public String crearHorario(){
        this.reset();
        horarioSeleccionado = new EmpleadoHorario();
        initializeEmbeddableKey();
        Horario horario = new Horario();
        horario.setDetalleHorarioList(new ArrayList<DetalleHorario>());
        horario.setPorFecha(true);
        horarioSeleccionado.setPorGrupo(false);
        horarioSeleccionado.setHorarioId(horario);
        this.setSelected(horarioSeleccionado);
        LOG.info("HORARIO: "+horario.toString());
        LOG.info("HORARIO SELECCIONADO CREAR HORARIO: "+horarioSeleccionado);
        return "Crear";
    }
    
    public String reset() {
        this.horarioSeleccionado = null;
        this.anio = null;
        this.mes = -1;
        LOG.info("LLEGA AQUI");
        return "List";
    }
    
    public String guardar(){
        Calendar cal = Calendar.getInstance();
        cal.set(Integer.parseInt(anio.getAnio()), mes, 1);
        this.horarioSeleccionado.getHorarioId().setFecha(cal.getTime());
        
        this.empleadoHorarioFacade.edit(horarioSeleccionado);
        
        return "List";
    }

    @Override
    public void saveNew(ActionEvent event) {
        LOG.info(this.getSelected().toString());
        super.saveNew(event); //To change body of generated methods, choose Tools | Templates.
    }
    
    
    

    public EmpleadoHorarioController() {
        // Inform the Abstract parent controller of the concrete EmpleadoHorario?cap_first Entity
        super(EmpleadoHorario.class);
    }
    
    public List<DetalleHorario> turnosXEmpleado(Empleado empleado, Date fechaInicio, Date fechaFin) {
        
        SimpleDateFormat dtFecha = new SimpleDateFormat("yyyy-MM-dd");

        String fechaI = "{d '" + dtFecha.format(fechaInicio) + "'}";
        String fechaF = "{d '" + dtFecha.format(fechaFin) + "'}";

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

        List<EmpleadoHorario> empleadoHorarios = this.empleadoHorarioFacade.search(sql, parametros);
        List<DetalleHorario> lista = new ArrayList<>();
        for (EmpleadoHorario empleadoHorario : empleadoHorarios) {
            List<DetalleHorario> horarioJornadas = empleadoHorario.getHorarioId().getDetalleHorarioList();
            lista.addAll(horarioJornadas);
        }
        return lista;
    }

    /**
     * Resets the "selected" attribute of any parent Entity controllers.
     */
    public void resetParents() {
        empleadoIdController.setSelected(null);
        horarioIdController.setSelected(null);
        grupoHorarioIdController.setSelected(null);
    }

    /**
     * Sets the "selected" attribute of the Empleado controller in order to
     * display its data in a dialog. This is reusing existing the existing View
     * dialog.
     *
     * @param event Event object for the widget that triggered an action
     */
    public void prepareEmpleadoId(ActionEvent event) {
        if (this.getSelected() != null && empleadoIdController.getSelected() == null) {
            empleadoIdController.setSelected(this.getSelected().getEmpleadoId());
        }
    }

    /**
     * Sets the "selected" attribute of the Horario controller in order to
     * display its data in a dialog. This is reusing existing the existing View
     * dialog.
     *
     * @param event Event object for the widget that triggered an action
     */
    public void prepareHorarioId(ActionEvent event) {
        if (this.getSelected() != null && horarioIdController.getSelected() == null) {
            horarioIdController.setSelected(this.getSelected().getHorarioId());
        }
    } 
    
    public Collection<EmpleadoHorario> getHorarioAsistencial() { 
//        if (items == null) {
            items = this.empleadoHorarioFacade.getHorariosAsistenciales();
//            LOG.info("items empleado horario: "+items.size());
//        }
        return items;
    }

    /**
     * Sets the "selected" attribute of the GrupoHorario controller in order to
     * display its data in a dialog. This is reusing existing the existing View
     * dialog.
     *
     * @param event Event object for the widget that triggered an action
     */
    public void prepareGrupoHorarioId(ActionEvent event) {
        if (this.getSelected() != null && grupoHorarioIdController.getSelected() == null) {
            grupoHorarioIdController.setSelected(this.getSelected().getGrupoHorarioId());
        }
    }

    @Override
    protected void edit(EmpleadoHorario objeto) {
        this.empleadoHorarioFacade.edit(objeto);
    }

    @Override
    protected void remove(EmpleadoHorario objeto) {
        this.empleadoHorarioFacade.remove(objeto);
    }

    @Override
    public EmpleadoHorario find(Object id) {
        return this.empleadoHorarioFacade.find(id);
    }

    @Override
    public List<EmpleadoHorario> getItems() {
        return this.empleadoHorarioFacade.findAll();
    }
    
    public List<EmpleadoHorario> getAsignacionAdministrativo(){
        String sql = "SELECT eh FROM EmpleadoHorario eh WHERE eh.horarioId.porFecha = FALSE";
        return this.search(sql);
    }

    @Override
    public List<EmpleadoHorario> search(String namedQuery) {
        return this.empleadoHorarioFacade.search(namedQuery);
    }

    @Override
    public List<EmpleadoHorario> search(String namedQuery, Map<String, Object> parametros) {
        return this.empleadoHorarioFacade.search(namedQuery, parametros);
    }

    @Override
    public List<EmpleadoHorario> search(String namedQuery, Map<String, Object> parametros, int inicio, int tamanio) {
        return this.empleadoHorarioFacade.search(namedQuery, parametros, inicio, tamanio);
    }
    
    private int ultimoDiaMes(int mes, int anio) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(anio, mes - 1, 1);
        return calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
    }
    
    public List<EmpleadoHorario> buscarXHorario(Horario id){
        String query = "SELECT e FROM EmpleadoHorario e WHERE e.horarioId = :id";
        Map<String, Object> parametros = new HashMap<>();
        parametros.put("id", id);
        return this.empleadoHorarioFacade.search(query, parametros);
    }
}
