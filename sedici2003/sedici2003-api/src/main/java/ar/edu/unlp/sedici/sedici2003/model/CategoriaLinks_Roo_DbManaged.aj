// WARNING: DO NOT EDIT THIS FILE. THIS FILE IS MANAGED BY SPRING ROO.
// You may push code into the target .java compilation unit if you wish to edit any member(s).

package ar.edu.unlp.sedici.sedici2003.model;

import java.lang.Integer;
import java.lang.String;
import javax.persistence.Column;

privileged aspect CategoriaLinks_Roo_DbManaged {
    
    @Column(name = "nombre_en", length = 255)
    private String CategoriaLinks.nombreEn;
    
    @Column(name = "nombre_es", length = 255)
    private String CategoriaLinks.nombreEs;
    
    @Column(name = "nombre_pt", length = 255)
    private String CategoriaLinks.nombrePt;
    
    @Column(name = "orden")
    private Integer CategoriaLinks.orden;
    
    public String CategoriaLinks.getNombreEn() {
        return this.nombreEn;
    }
    
    public void CategoriaLinks.setNombreEn(String nombreEn) {
        this.nombreEn = nombreEn;
    }
    
    public String CategoriaLinks.getNombreEs() {
        return this.nombreEs;
    }
    
    public void CategoriaLinks.setNombreEs(String nombreEs) {
        this.nombreEs = nombreEs;
    }
    
    public String CategoriaLinks.getNombrePt() {
        return this.nombrePt;
    }
    
    public void CategoriaLinks.setNombrePt(String nombrePt) {
        this.nombrePt = nombrePt;
    }
    
    public Integer CategoriaLinks.getOrden() {
        return this.orden;
    }
    
    public void CategoriaLinks.setOrden(Integer orden) {
        this.orden = orden;
    }
    
}