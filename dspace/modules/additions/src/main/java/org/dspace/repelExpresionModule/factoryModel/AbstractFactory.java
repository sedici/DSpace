package org.dspace.repelExpresionModule.factoryModel;

import org.dspace.content.*;
import org.dspace.core.Context;
import org.dspace.repelExpresionModule.dspaceProxy.*;

/**
 * Clase abstracta para instanciar los proxy del modelo Dspace
 * @author terru
 * los factory comcretos deben implementar todos los builders
 * sobrecargando el método getWrapper;
 */
public abstract class AbstractFactory {
	

	public DspaceObjectWrapper<?>[]  getCollectionWrapper(DSpaceObject[] c){
		DspaceObjectWrapper<?>[] returner = new DspaceObjectWrapper<?>[c.length];
		for(int i = 0; i<c.length; i++){
			returner[i] =  this.getWrapper(c[i]);
		}
		return returner;
	}
	
	public ItemCollectionWrapper getItemIterator(ItemIterator it, Context context){
		return this.getRealItemIterator(it,context);
	}
	
	/**
	 * metodo principal de factory
	 * returna un objeto wrapper de acuerdo a la clase adecuada del objeto Dspace
	 * @param DspaceObject dso 
	 * @return DspaceObjectWrapper
	 */
	public final <T extends DSpaceObject> DspaceObjectWrapper<T> getWrapper(T dso){
		
		if (dso instanceof Item) {
			ItemWrapper iw = this.getItemWrapper((Item) dso);
			return (DspaceObjectWrapper<T>) iw;	
		}
		if (dso instanceof Bundle){
			BundleWrapper bw = this.getBundleWrapper((Bundle) dso);
			return (DspaceObjectWrapper<T>) bw;
		}
		if (dso instanceof Collection){
			CollectionWrapper cw = this.getCollectionWrapper((Collection) dso);
			return (DspaceObjectWrapper<T>) cw;
		}
		if (dso instanceof  Bitstream){
			BitstreamWrapper bw =  this.getBitstreamWrapper((Bitstream) dso);
			return (DspaceObjectWrapper<T>) bw;
		}
		if (dso instanceof Collection){
			CommunityWrapper cw = this.getCommunityWrapper((Community) dso);
			return (DspaceObjectWrapper<T>) cw;
		}
		if (dso instanceof Site){
			SiteWrapper sw = this.getSiteWrapper((Site) dso);
			return (DspaceObjectWrapper<T>) sw;
		}
		return null;
	}
	
	

	protected abstract ItemCollectionWrapper getRealItemIterator(ItemIterator it, Context context);
	protected abstract ItemWrapper getItemWrapper(Item i);
	protected abstract MetadatumWrapper getMetadatumWrapper(Metadatum m);
	protected abstract BundleWrapper getBundleWrapper(Bundle bun);
	protected abstract CollectionWrapper getCollectionWrapper(Collection col);
	protected abstract BitstreamWrapper getBitstreamWrapper(Bitstream bits);
	protected abstract CommunityWrapper getCommunityWrapper(Community com);
	protected abstract SiteWrapper getSiteWrapper (Site site);
}
