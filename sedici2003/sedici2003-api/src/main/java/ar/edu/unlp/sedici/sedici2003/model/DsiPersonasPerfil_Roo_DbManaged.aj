// WARNING: DO NOT EDIT THIS FILE. THIS FILE IS MANAGED BY SPRING ROO.
// You may push code into the target .java compilation unit if you wish to edit any member(s).

package ar.edu.unlp.sedici.sedici2003.model;

import java.lang.Boolean;
import java.lang.Integer;
import java.lang.String;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import org.springframework.format.annotation.DateTimeFormat;

privileged aspect DsiPersonasPerfil_Roo_DbManaged {
    
    @Column(name = "anio_publicacion")
    private Integer DsiPersonasPerfil.anioPublicacion;
    
    @Column(name = "anio_publicacion2")
    private Integer DsiPersonasPerfil.anioPublicacion2;
    
    @Column(name = "disponible")
    private Boolean DsiPersonasPerfil.disponible;
    
    @Column(name = "fecha_hora")
    @Temporal(TemporalType.TIMESTAMP)
    @DateTimeFormat(style = "M-")
    private Date DsiPersonasPerfil.fechaHora;
    
    @Column(name = "id_persona")
    private Integer DsiPersonasPerfil.idPersona;
    
    @Column(name = "nombre_perfil", length = 30)
    private String DsiPersonasPerfil.nombrePerfil;
    
    @Column(name = "recibir_por_mail")
    private Boolean DsiPersonasPerfil.recibirPorMail;
    
    public Integer DsiPersonasPerfil.getAnioPublicacion() {
        return this.anioPublicacion;
    }
    
    public void DsiPersonasPerfil.setAnioPublicacion(Integer anioPublicacion) {
        this.anioPublicacion = anioPublicacion;
    }
    
    public Integer DsiPersonasPerfil.getAnioPublicacion2() {
        return this.anioPublicacion2;
    }
    
    public void DsiPersonasPerfil.setAnioPublicacion2(Integer anioPublicacion2) {
        this.anioPublicacion2 = anioPublicacion2;
    }
    
    public Boolean DsiPersonasPerfil.getDisponible() {
        return this.disponible;
    }
    
    public boolean DsiPersonasPerfil.isDisponible() {
        return this.disponible != null && this.disponible;
    }
    
    public void DsiPersonasPerfil.setDisponible(Boolean disponible) {
        this.disponible = disponible;
    }
    
    public Date DsiPersonasPerfil.getFechaHora() {
        return this.fechaHora;
    }
    
    public void DsiPersonasPerfil.setFechaHora(Date fechaHora) {
        this.fechaHora = fechaHora;
    }
    
    public Integer DsiPersonasPerfil.getIdPersona() {
        return this.idPersona;
    }
    
    public void DsiPersonasPerfil.setIdPersona(Integer idPersona) {
        this.idPersona = idPersona;
    }
    
    public String DsiPersonasPerfil.getNombrePerfil() {
        return this.nombrePerfil;
    }
    
    public void DsiPersonasPerfil.setNombrePerfil(String nombrePerfil) {
        this.nombrePerfil = nombrePerfil;
    }
    
    public Boolean DsiPersonasPerfil.getRecibirPorMail() {
        return this.recibirPorMail;
    }
    
    public boolean DsiPersonasPerfil.isRecibirPorMail() {
        return this.recibirPorMail != null && this.recibirPorMail;
    }
    
    public void DsiPersonasPerfil.setRecibirPorMail(Boolean recibirPorMail) {
        this.recibirPorMail = recibirPorMail;
    }
    
}