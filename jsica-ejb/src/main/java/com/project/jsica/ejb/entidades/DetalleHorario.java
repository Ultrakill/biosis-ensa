/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.project.jsica.ejb.entidades;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 *
 * @author RyuujiMD
 */
@Entity
@Table(name = "detalle_horario")
@XmlRootElement
public class DetalleHorario implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    private Long id;
    @Basic(optional = false)
    @NotNull
    @Temporal(TemporalType.DATE)
    private Date fecha;
    @JoinColumn(name = "horario_id", referencedColumnName = "id")
    @ManyToOne(optional = false)
    private Horario horarioId;
    @JoinColumn(name = "jornada_codigo", referencedColumnName = "codigo")
    @ManyToOne(optional = false)
    private Jornada jornadaCodigo;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "detalleHorarioOriginal")
    private List<CambioTurno> cambioTurnoList;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "detalleHorarioReemplazo")
    private List<CambioTurno> cambioTurnoList1;
    @OneToMany(mappedBy = "turnoOriginal")
    private List<RegistroAsistencia> registroList;
    @OneToMany(mappedBy = "turnoReemplazo")
    private List<RegistroAsistencia> registroList1;

    @XmlTransient
    public List<RegistroAsistencia> getRegistroList() {
        return registroList;
    }

    public void setRegistroList(List<RegistroAsistencia> registroList) {
        this.registroList = registroList;
    }

    @XmlTransient
    public List<RegistroAsistencia> getRegistroList1() {
        return registroList1;
    }

    public void setRegistroList1(List<RegistroAsistencia> registroList1) {
        this.registroList1 = registroList1;
    }
    
    public DetalleHorario() {
    }

    public DetalleHorario(Long id) {
        this.id = id;
    }

    public DetalleHorario(Long id, Date fecha) {
        this.id = id;
        this.fecha = fecha;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    public Horario getHorarioId() {
        return horarioId;
    }

    public void setHorarioId(Horario horarioId) {
        this.horarioId = horarioId;
    }

    public Jornada getJornadaCodigo() {
        return jornadaCodigo;
    }

    public void setJornadaCodigo(Jornada jornadaCodigo) {
        this.jornadaCodigo = jornadaCodigo;
    }

    @XmlTransient
    public List<CambioTurno> getCambioTurnoList() {
        return cambioTurnoList;
    }

    public void setCambioTurnoList(List<CambioTurno> cambioTurnoList) {
        this.cambioTurnoList = cambioTurnoList;
    }

    @XmlTransient
    public List<CambioTurno> getCambioTurnoList1() {
        return cambioTurnoList1;
    }

    public void setCambioTurnoList1(List<CambioTurno> cambioTurnoList1) {
        this.cambioTurnoList1 = cambioTurnoList1;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof DetalleHorario)) {
            return false;
        }
        DetalleHorario other = (DetalleHorario) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return fecha+"";
    }
    
}
