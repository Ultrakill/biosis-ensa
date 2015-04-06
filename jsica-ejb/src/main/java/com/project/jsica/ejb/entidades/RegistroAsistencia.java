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
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author RyuujiMD
 */
@Entity
@Table(name = "registro_asistencia")
@XmlRootElement
public class RegistroAsistencia implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = true)
    private Long id;
    @Basic(optional = true)
    @Temporal(TemporalType.DATE)
    private Date fecha;    
    @JoinColumn(name = "biometrico_id", referencedColumnName = "id", nullable = true)
    @ManyToOne(optional = true)
    private Biometrico biometricoId;
    @JoinColumn(name = "empleado_doc_identidad", referencedColumnName = "doc_identidad", nullable = true)
    @ManyToOne(optional = true)
    private Empleado empleado;
    private char tipo; // V = VACACION, L = LICENCIA, P = PERMISO, E = FERIADO, T = TARDANZA, F = FALTA, R = ASIST. REGULAR
    @JoinColumn(name = "turno_original", referencedColumnName = "id", nullable = true)
    @ManyToOne
    private DetalleHorario turnoOriginal;
    @JoinColumn(name = "turno_reemplazo", referencedColumnName = "id", nullable = true)
    @ManyToOne
    private DetalleHorario turnoReemplazo;
    @Column(name = "tardanza_total")
    private long milisTardanzaTotal;
    @Column(name = "trabajo_total")
    private long milisTrabajoTotal;
    @JoinColumn(name = "permiso_id", referencedColumnName = "id", nullable = true)
    @ManyToOne
    private Permiso permisoId;
    @JoinColumn(name = "feriado_id", referencedColumnName = "id", nullable = true)
    @ManyToOne
    private Feriado feriado;
    @OneToMany(mappedBy = "registroAsistencia",cascade = CascadeType.ALL)
    private List<DetalleRegistroAsistencia> detalleRegistroAsistenciaList;
    
    @Column(name = "hora_entrada")
    @Temporal(TemporalType.TIME)
    private Date horaEntrada;
    @Column(name = "hora_salida")
    @Temporal(TemporalType.TIME)
    private Date horaSalida;
    
    @Column(name = "marcacion_inicio")
    @Temporal(TemporalType.TIME)
    private Date marcacionInicio;
    
    @Column(name = "marcacion_fin")
    @Temporal(TemporalType.TIME)
    private Date marcacionFin;
    
    //REFRIGERIO
    @Column(name = "hora_salida_refrigerio")
    @Temporal(TemporalType.TIME)
    private Date horaSalidaRefrigerio;
    @Column(name = "hora_entrada_refrigerio")
    @Temporal(TemporalType.TIME)
    private Date horaEntradaRefrigerio;
    
    @Column(name = "marcacion_inicio_refrigerio")
    @Temporal(TemporalType.TIME)
    private Date marcacionInicioRefrigerio;
    
    @Column(name = "marcacion_fin_refrigerio")
    @Temporal(TemporalType.TIME)
    private Date marcacionFinRefrigerio;
    
    @Column(name = "tardanza_refrigerio")
    private long milisTardanzaRefrigerio;
    
    @Column(name = "tardanza_total_final")
    private long milisTardanzaTotalFinal;

    public long getMilisTardanzaTotalFinal() {
        return milisTardanzaTotalFinal;
    }

    public void setMilisTardanzaTotalFinal(long milisTardanzaTotalFinal) {
        this.milisTardanzaTotalFinal = milisTardanzaTotalFinal;
    }
    
    

    public long getMilisTardanzaRefrigerio() {
        return milisTardanzaRefrigerio;
    }

    public void setMilisTardanzaRefrigerio(long milisTardanzaRefrigerio) {
        this.milisTardanzaRefrigerio = milisTardanzaRefrigerio;
    }
    
    public Date getHoraSalidaRefrigerio() {
        return horaSalidaRefrigerio;
    }

    public void setHoraSalidaRefrigerio(Date horaSalidaRefrigerio) {
        this.horaSalidaRefrigerio = horaSalidaRefrigerio;
    }

    public Date getHoraEntradaRefrigerio() {
        return horaEntradaRefrigerio;
    }

    public void setHoraEntradaRefrigerio(Date horaEntradaRefrigerio) {
        this.horaEntradaRefrigerio = horaEntradaRefrigerio;
    }

    public Date getMarcacionInicioRefrigerio() {
        return marcacionInicioRefrigerio;
    }

    public void setMarcacionInicioRefrigerio(Date marcacionInicioRefrigerio) {
        this.marcacionInicioRefrigerio = marcacionInicioRefrigerio;
    }

    public Date getMarcacionFinRefrigerio() {
        return marcacionFinRefrigerio;
    }

    public void setMarcacionFinRefrigerio(Date marcacionFinRefrigerio) {
        this.marcacionFinRefrigerio = marcacionFinRefrigerio;
    }
    
    

    public Date getHoraEntrada() {
        return horaEntrada;
    }

    public void setHoraEntrada(Date horaEntrada) {
        this.horaEntrada = horaEntrada;
    }

    public Date getHoraSalida() {
        return horaSalida;
    }

    public void setHoraSalida(Date horaSalida) {
        this.horaSalida = horaSalida;
    }

    public Date getMarcacionInicio() {
        return marcacionInicio;
    }

    public void setMarcacionInicio(Date marcacionInicio) {
        this.marcacionInicio = marcacionInicio;
    }

    public Date getMarcacionFin() {
        return marcacionFin;
    }

    public void setMarcacionFin(Date marcacionFin) {
        this.marcacionFin = marcacionFin;
    }
    
    
    

    public Feriado getFeriado() {
        return feriado;
    }

    public void setFeriado(Feriado feriado) {
        this.feriado = feriado;
    }

    
    public char getTipo() {
        return tipo;
    }

    public void setTipo(char tipo) {
        this.tipo = tipo;
    }

    public long getMilisTardanzaTotal() {
        return milisTardanzaTotal;
    }

    public void setMilisTardanzaTotal(long milisTardanzaTotal) {
        this.milisTardanzaTotal = milisTardanzaTotal;
    }

    public long getMilisTrabajoTotal() {
        return milisTrabajoTotal;
    }

    public void setMilisTrabajoTotal(long milisTrabajoTotal) {
        this.milisTrabajoTotal = milisTrabajoTotal;
    }

    public List<DetalleRegistroAsistencia> getDetalleRegistroAsistenciaList() {
        return detalleRegistroAsistenciaList;
    }

    public void setDetalleRegistroAsistenciaList(List<DetalleRegistroAsistencia> detalleRegistroAsistenciaList) {
        this.detalleRegistroAsistenciaList = detalleRegistroAsistenciaList;
    }
    

    public DetalleHorario getTurnoOriginal() {
        return turnoOriginal;
    }

    public void setTurnoOriginal(DetalleHorario turnoOriginal) {
        this.turnoOriginal = turnoOriginal;
    }

    public DetalleHorario getTurnoReemplazo() {
        return turnoReemplazo;
    }

    public void setTurnoReemplazo(DetalleHorario turnoReemplazo) {
        this.turnoReemplazo = turnoReemplazo;
    }

    public Permiso getPermisoId() {
        return permisoId;
    }

    public void setPermisoId(Permiso permisoId) {
        this.permisoId = permisoId;
    }

    public RegistroAsistencia() {
    }

    public RegistroAsistencia(Long id) {
        this.id = id;
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

    public Biometrico getBiometricoId() {
        return biometricoId;
    }

    public void setBiometricoId(Biometrico biometricoId) {
        this.biometricoId = biometricoId;
    }

    public Empleado getEmpleado() {
        return empleado;
    }

    public void setEmpleado(Empleado empleado) {
        this.empleado = empleado;
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
        if (!(object instanceof RegistroAsistencia)) {
            return false;
        }
        RegistroAsistencia other = (RegistroAsistencia) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

}
