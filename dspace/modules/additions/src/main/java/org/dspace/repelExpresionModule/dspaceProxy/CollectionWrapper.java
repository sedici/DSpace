package org.dspace.repelExpresionModule.dspaceProxy;

import java.sql.SQLException;

import org.dspace.repelExpresionModule.repel.RepelExpresionException;
import org.dspace.content.Collection;
import org.dspace.content.Community;
import org.dspace.content.ItemIterator;
import org.dspace.core.Context;


public class CollectionWrapper extends DspaceObjectWrapper<Collection> {


	public CollectionWrapper(Collection collection) {
		super(collection);
	}

	public int getType() {
		return this.getDso().getType();
	}

	public String getTypeText() {
		return this.getDso().getTypeText();
	}

	public int getID() {
		return this.getDso().getID();
	}

	public String getHandle() {
		return this.getDso().getHandle();
	}

	public String getName() {
		return this.getDso().getName();
	}

	public BitstreamWrapper getLogo() {
		return new BitstreamWrapper(this.getDso().getLogo());
	}

	public String getLicense() {
		return this.getDso().getLicense();
	}

	public String getLicenseCollection() {
		return this.getDso().getLicenseCollection();
	}

	public boolean hasCustomLicense() {
		return this.getDso().hasCustomLicense();
	}

	public ItemWrapper getTemplateItem() {
		try {
			return new ItemWrapper(this.getDso().getTemplateItem());
		} catch (SQLException e) {
			throw new RepelExpresionException("Error inesperado",e);
		}
	}

	public CommunityWrapper[] getCommunities() {
		try {
			Community[] array = this.getDso().getCommunities();
			CommunityWrapper[] returner = new CommunityWrapper[array.length];
			for(int i=0; i< array.length;i++){
				returner[i]=new CommunityWrapper(array[i]);
			}
			return returner;
		} catch (SQLException e) {
			throw new RepelExpresionException("Error inesperado",e);
		}
		
	}

	public boolean equals(Object other) {
		return this.getDso().equals(other);
	}

	public int countItems() {
		try {
			return this.getDso().countItems();
		} catch (SQLException e) {
			throw new RepelExpresionException("Error inesperado",e);
		}
	}
	
	public ItemCollectionWrapper getItems(Context context){
		ItemIterator it;
		try {
			it = this.getDso().getItems();
			return new ItemCollectionWrapper(it,context);
		} catch (SQLException e) {
			throw new RepelExpresionException("Error al intentar recuperar items de una coleccion",e);
		}
	}
	
	public ItemCollectionWrapper getAllItems(Context context){
		ItemIterator it;
		try {
			it = this.getDso().getAllItems();
			return new ItemCollectionWrapper(it,context);
		} catch (SQLException e) {
			throw new RepelExpresionException("Error al intentar recuperar items de una coleccion",e);
		}
	}

	protected Collection getCollection() {
		return this.getDso();
	}

}
