// WARNING: DO NOT EDIT THIS FILE. THIS FILE IS MANAGED BY SPRING ROO.
// You may push code into the target .java compilation unit if you wish to edit any member(s).

package ar.edu.unlp.sedici.sedici2003.model;

import java.lang.Object;
import java.lang.Short;
import java.lang.String;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import org.springframework.format.annotation.DateTimeFormat;

privileged aspect IpWebservicePK_Roo_Identifier {
    
    declare @type: IpWebservicePK: @Embeddable;
    
    @Column(name = "operacion", nullable = false)
    private Short IpWebservicePK.operacion;
    
    @Column(name = "abreviatura", nullable = false, length = 2)
    private String IpWebservicePK.abreviatura;
    
    @Column(name = "fecha_hora", nullable = false)
    @Temporal(TemporalType.DATE)
    @DateTimeFormat(style = "M-")
    private Date IpWebservicePK.fechaHora;
    
    @Column(name = "ip", nullable = false, length = 20)
    private String IpWebservicePK.ip;
    
    public IpWebservicePK.new(Short operacion, String abreviatura, Date fechaHora, String ip) {
        super();
        this.operacion = operacion;
        this.abreviatura = abreviatura;
        this.fechaHora = fechaHora;
        this.ip = ip;
    }

    private IpWebservicePK.new() {
        super();
    }

    public Short IpWebservicePK.getOperacion() {
        return this.operacion;
    }
    
    public String IpWebservicePK.getAbreviatura() {
        return this.abreviatura;
    }
    
    public Date IpWebservicePK.getFechaHora() {
        return this.fechaHora;
    }
    
    public String IpWebservicePK.getIp() {
        return this.ip;
    }
    
    public boolean IpWebservicePK.equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (!(obj instanceof IpWebservicePK)) return false;
        IpWebservicePK other = (IpWebservicePK) obj;
        if (operacion == null) {
            if (other.operacion != null) return false;
        } else if (!operacion.equals(other.operacion)) return false;
        if (abreviatura == null) {
            if (other.abreviatura != null) return false;
        } else if (!abreviatura.equals(other.abreviatura)) return false;
        if (fechaHora == null) {
            if (other.fechaHora != null) return false;
        } else if (!fechaHora.equals(other.fechaHora)) return false;
        if (ip == null) {
            if (other.ip != null) return false;
        } else if (!ip.equals(other.ip)) return false;
        return true;
    }
    
    public int IpWebservicePK.hashCode() {
        final int prime = 31;
        int result = 17;
        result = prime * result + (operacion == null ? 0 : operacion.hashCode());
        result = prime * result + (abreviatura == null ? 0 : abreviatura.hashCode());
        result = prime * result + (fechaHora == null ? 0 : fechaHora.hashCode());
        result = prime * result + (ip == null ? 0 : ip.hashCode());
        return result;
    }
    
}