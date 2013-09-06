// WARNING: DO NOT EDIT THIS FILE. THIS FILE IS MANAGED BY SPRING ROO.
// You may push code into the target .java compilation unit if you wish to edit any member(s).

package ar.edu.unlp.sedici.sedici2003.model;

import ar.edu.unlp.sedici.sedici2003.model.TipoOperacion;
import java.lang.Integer;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PersistenceContext;
import javax.persistence.Table;
import org.springframework.transaction.annotation.Transactional;

privileged aspect TipoOperacion_Roo_Entity {
    
    declare @type: TipoOperacion: @Entity;
    
    declare @type: TipoOperacion: @Table(name = "tipo_operacion");
    
    @PersistenceContext
    transient EntityManager TipoOperacion.entityManager;
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Integer TipoOperacion.id;
    
    public Integer TipoOperacion.getId() {
        return this.id;
    }
    
    public void TipoOperacion.setId(Integer id) {
        this.id = id;
    }
    
    @Transactional
    public void TipoOperacion.persist() {
        if (this.entityManager == null) this.entityManager = entityManager();
        this.entityManager.persist(this);
    }
    
    @Transactional
    public void TipoOperacion.remove() {
        if (this.entityManager == null) this.entityManager = entityManager();
        if (this.entityManager.contains(this)) {
            this.entityManager.remove(this);
        } else {
            TipoOperacion attached = TipoOperacion.findTipoOperacion(this.id);
            this.entityManager.remove(attached);
        }
    }
    
    @Transactional
    public void TipoOperacion.flush() {
        if (this.entityManager == null) this.entityManager = entityManager();
        this.entityManager.flush();
    }
    
    @Transactional
    public void TipoOperacion.clear() {
        if (this.entityManager == null) this.entityManager = entityManager();
        this.entityManager.clear();
    }
    
    @Transactional
    public TipoOperacion TipoOperacion.merge() {
        if (this.entityManager == null) this.entityManager = entityManager();
        TipoOperacion merged = this.entityManager.merge(this);
        this.entityManager.flush();
        return merged;
    }
    
    public static final EntityManager TipoOperacion.entityManager() {
        EntityManager em = new TipoOperacion().entityManager;
        if (em == null) throw new IllegalStateException("Entity manager has not been injected (is the Spring Aspects JAR configured as an AJC/AJDT aspects library?)");
        return em;
    }
    
    public static long TipoOperacion.countTipoOperacions() {
        return entityManager().createQuery("SELECT COUNT(o) FROM TipoOperacion o", Long.class).getSingleResult();
    }
    
    public static List<TipoOperacion> TipoOperacion.findAllTipoOperacions() {
        return entityManager().createQuery("SELECT o FROM TipoOperacion o", TipoOperacion.class).getResultList();
    }
    
    public static TipoOperacion TipoOperacion.findTipoOperacion(Integer id) {
        if (id == null) return null;
        return entityManager().find(TipoOperacion.class, id);
    }
    
    public static List<TipoOperacion> TipoOperacion.findTipoOperacionEntries(int firstResult, int maxResults) {
        return entityManager().createQuery("SELECT o FROM TipoOperacion o", TipoOperacion.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }
    
}