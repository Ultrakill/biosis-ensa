package com.project.jsica.converter;

import com.project.jsica.cdi.util.JsfUtil;
import dao.CondicionLaboralFacade;
import dao.CondicionLaboralFacadeLocal;
import com.project.jsica.ejb.entidades.CondicionLaboral;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.inject.Inject;

@FacesConverter(value = "condicionLaboralConverter")
public class CondicionLaboralConverter implements Converter {

    @EJB
    private CondicionLaboralFacadeLocal ejbFacade;

    @Override
    public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
        if (value == null || value.length() == 0 || JsfUtil.isDummySelectItem(component, value)) {
            return null;
        }
        return this.ejbFacade.find(value);
    }

    @Override
    public String getAsString(FacesContext facesContext, UIComponent component, Object object) {
        if (object == null
                || (object instanceof String && ((String) object).length() == 0)) {
            return null;
        }
        if (object instanceof CondicionLaboral) {
            CondicionLaboral o = (CondicionLaboral) object;
            return o.getCodigo();
        } else {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "object {0} is of type {1}; expected type: {2}", new Object[]{object, object.getClass().getName(), CondicionLaboral.class.getName()});
            return null;
        }
    }

}
