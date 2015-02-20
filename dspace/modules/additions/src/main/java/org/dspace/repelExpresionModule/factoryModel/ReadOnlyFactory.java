/**
 * Clase que implementa los builders para wrappers que son de solo lectura
 * para implementar una clase que no sea Read-Only también se deben especializar los wrappers
 * la interfaz de estos wrappers es read-only 
 */
package org.dspace.repelExpresionModule.factoryModel;

import org.dspace.content.*;
import org.dspace.core.Context;
import org.dspace.repelExpresionModule.dspaceProxy.*;

/**
 * @author terru
 *
 */
public class ReadOnlyFactory extends AbstractFactory {
	
	/* (non-Javadoc)
	 * @see factoryModel.AbstractFactory#getItemWrapper(dspaceModel.Item)
	 */
	@Override
	protected ItemWrapper getItemWrapper(Item i) {
		ItemWrapper itemProxy = new ItemWrapper(i);
		return itemProxy;
	}

	/* (non-Javadoc)
	 * @see factoryModel.AbstractFactory#getMetadatumWrapper(dspaceModel.Metadatum)
	 */
	@Override
	protected MetadatumWrapper getMetadatumWrapper(Metadatum m) {
		MetadatumWrapper metadatumProxy = new MetadatumWrapper(m);
		return metadatumProxy;
	}

	/* (non-Javadoc)
	 * @see factoryModel.AbstractFactory#getBundleWrapper(dspaceModel.Bundle)
	 */
	@Override
	protected BundleWrapper getBundleWrapper(Bundle bun) {
		BundleWrapper bundleProxy = new BundleWrapper(bun);
		return bundleProxy;
	}

	/* (non-Javadoc)
	 * @see factoryModel.AbstractFactory#getCollectionWrapper(dspaceModel.Collection)
	 */
	@Override
	public CollectionWrapper getCollectionWrapper(Collection col) {
		CollectionWrapper colProxy = new CollectionWrapper(col);
		return colProxy;
	}
	

	/* (non-Javadoc)
	 * @see factoryModel.AbstractFactory#getBitstreamWrapper(dspaceModel.Bitstream)
	 */
	@Override
	public BitstreamWrapper getBitstreamWrapper(Bitstream bits) {
		BitstreamWrapper bitsProxy = new BitstreamWrapper(bits);
		return bitsProxy;
	}

	/* (non-Javadoc)
	 * @see factoryModel.AbstractFactory#getCommunityWrapper(dspaceModel.Metadatum)
	 */
	@Override
	public CommunityWrapper getCommunityWrapper(Community com) {
		CommunityWrapper comProxy = new CommunityWrapper(com);
		return comProxy;
	}

	@Override
	protected SiteWrapper getSiteWrapper(Site site) {
		SiteWrapper siteProxy = new SiteWrapper(site);
		return siteProxy;
	}

	@Override
	protected ItemCollectionWrapper getRealItemIterator(ItemIterator it, Context context) {
		return new ItemCollectionWrapper(it,context);
	}
}
