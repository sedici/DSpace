// WARNING: DO NOT EDIT THIS FILE. THIS FILE IS MANAGED BY SPRING ROO.
// You may push code into the target .java compilation unit if you wish to edit any member(s).

package ar.edu.unlp.sedici.sedici2003.model;

import ar.edu.unlp.sedici.sedici2003.model.DsiRelTipoDoc;
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

privileged aspect DsiRelTipoDoc_Roo_Entity {
    
    declare @type: DsiRelTipoDoc: @Entity;
    
    declare @type: DsiRelTipoDoc: @Table(name = "dsi_rel_tipo_doc");
    
    @PersistenceContext
    transient EntityManager DsiRelTipoDoc.entityManager;
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Integer DsiRelTipoDoc.id;
    
    public Integer DsiRelTipoDoc.getId() {
        return this.id;
    }
    
    public void DsiRelTipoDoc.setId(Integer id) {
        this.id = id;
    }
    
    @Transactional
    public void DsiRelTipoDoc.persist() {
        if (this.entityManager == null) this.entityManager = entityManager();
        this.entityManager.persist(this);
    }
    
    @Transactional
    public void DsiRelTipoDoc.remove() {
        if (this.entityManager == null) this.entityManager = entityManager();
        if (this.entityManager.contains(this)) {
            this.entityManager.remove(this);
        } else {
            DsiRelTipoDoc attached = DsiRelTipoDoc.findDsiRelTipoDoc(this.id);
            this.entityManager.remove(attached);
        }
    }
    
    @Transactional
    public void DsiRelTipoDoc.flush() {
        if (this.entityManager == null) this.entityManager = entityManager();
        this.entityManager.flush();
    }
    
    @Transactional
    public void DsiRelTipoDoc.clear() {
        if (this.entityManager == null) this.entityManager = entityManager();
        this.entityManager.clear();
    }
    
    @Transactional
    public DsiRelTipoDoc DsiRelTipoDoc.merge() {
        if (this.entityManager == null) this.entityManager = entityManager();
        DsiRelTipoDoc merged = this.entityManager.merge(this);
        this.entityManager.flush();
        return merged;
    }
    
    public static final EntityManager DsiRelTipoDoc.entityManager() {
        EntityManager em = new DsiRelTipoDoc().entityManager;
        if (em == null) throw new IllegalStateException("Entity manager has not been injected (is the Spring Aspects JAR configured as an AJC/AJDT aspects library?)");
        return em;
    }
    
    public static long DsiRelTipoDoc.countDsiRelTipoDocs() {
        return entityManager().createQuery("SELECT COUNT(o) FROM DsiRelTipoDoc o", Long.class).getSingleResult();
    }
    
    public static List<DsiRelTipoDoc> DsiRelTipoDoc.findAllDsiRelTipoDocs() {
        return entityManager().createQuery("SELECT o FROM DsiRelTipoDoc o", DsiRelTipoDoc.class).getResultList();
    }
    
    public static DsiRelTipoDoc DsiRelTipoDoc.findDsiRelTipoDoc(Integer id) {
        if (id == null) return null;
        return entityManager().find(DsiRelTipoDoc.class, id);
    }
    
    public static List<DsiRelTipoDoc> DsiRelTipoDoc.findDsiRelTipoDocEntries(int firstResult, int maxResults) {
        return entityManager().createQuery("SELECT o FROM DsiRelTipoDoc o", DsiRelTipoDoc.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }
    
}