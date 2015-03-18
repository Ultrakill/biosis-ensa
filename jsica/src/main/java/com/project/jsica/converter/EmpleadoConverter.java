package com.project.jsica.converter;

import com.project.jsica.cdi.util.JsfUtil;
import com.project.jsica.ejb.entidades.Empleado;
import dao.EmpleadoFacade;
import dao.EmpleadoFacadeLocal;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.inject.Inject;
import static org.apache.log4j.LogMF.log;
import org.jfree.util.Log;

@FacesConverter(value = "empleadoConverter")
public class EmpleadoConverter implements Converter {

    private static final Logger LOG = Logger.getLogger(EmpleadoConverter.class.getName());
    
    @EJB
    private EmpleadoFacadeLocal ejbFacade;
    
    //PRUEBA
    private int i = 0;

    @Override
    public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
        LOG.info("-------------EL VALOR ES "+value);
        if (value == null || value.length() == 0 || JsfUtil.isDummySelectItem(component, value)) {
            LOG.info(value);
            return null;
        }
        LOG.info(value);
        return this.ejbFacade.find(value);
    }

//    java.lang.Long getKey(String value) {
//        java.lang.Long key;
//        key = Long.valueOf(value);
//        return key;
//    }
//
//    String getStringKey(java.lang.Long value) {
//        StringBuffer sb = new StringBuffer();
//        sb.append(value);
//        return sb.toString();
//    }

    @Override
    public String getAsString(FacesContext facesContext, UIComponent component, Object object) {
        if (object == null
                || (object instanceof String && ((String) object).length() == 0)) {
            return null;
        }
        if (object instanceof Empleado) {
            Empleado o = (Empleado) object;
            LOG.info(o.getApellidos()+" "+ o.getNombreCompleto()+" "+ o.getDocIdentidad());
            return o.getDocIdentidad();
        } else {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "object {0} is of type {1}; expected type: {2}", new Object[]{object, object.getClass().getName(), Empleado.class.getName()});
            LOG.info("null");
            return null;
        }
    }

}
