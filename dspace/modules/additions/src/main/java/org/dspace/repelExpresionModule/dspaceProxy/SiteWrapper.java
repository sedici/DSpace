package org.dspace.repelExpresionModule.dspaceProxy;



import org.dspace.content.Site;

/**
 * @author terru
 * este es el wrapper del site que permite invocar objetos sin contexto
 */
public class SiteWrapper extends DspaceObjectWrapper<Site> {

	public SiteWrapper(Site site) {
		super(site);
	}

	public int getType() {
		return this.getDso().getType();
	}

	public int getID() {
		return this.getDso().getID();
	}

	public String getHandle() {
		return Site.getSiteHandle();
	}

	public String getSiteHandle() {
		return Site.getSiteHandle();
	}

	public String getName() {
		return this.getDso().getName();
	}

	public String getURL() {
		return this.getDso().getURL();
	}
	
	/**
	 * añade la posibilidad de iterar todas las colecciones de un sitio
	 * @return
	 */
	public CollectionWrapper[] getCollection(){
		return new CollectionWrapper[]{null};
	}
	
	public CommunityWrapper[] getCommunity(){
		return new CommunityWrapper[]{null};
	}
	
	public ItemWrapper[] getItem(){
		return new ItemWrapper[]{null};
	}


}
