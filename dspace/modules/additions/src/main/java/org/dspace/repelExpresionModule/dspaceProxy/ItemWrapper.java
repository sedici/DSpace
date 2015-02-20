package org.dspace.repelExpresionModule.dspaceProxy;
import java.sql.SQLException;
import java.util.Date;

import org.dspace.repelExpresionModule.repel.RepelExpresionException;
import org.dspace.content.Bitstream;
import org.dspace.content.Bundle;
import org.dspace.content.Collection;
import org.dspace.content.Community;
import org.dspace.content.Item;

public class ItemWrapper extends DspaceObjectWrapper<Item> {

	public ItemWrapper(Item item) {
		super(item);
	}
	
	public int getID()
	{
		return this.getDso().getID();
	}

	public String getHandle()
	{
		return this.getDso().getHandle();
	}

	public boolean isArchived()
	{
		return this.getDso().isArchived();
	}

	public boolean isWithdrawn()
	{
		return this.getDso().isWithdrawn();
	}

	public boolean isDiscoverable()
	{
		return this.getDso().isDiscoverable();
	}

	public Date getLastModified()
	{
		return this.getDso().getLastModified();
	}

	public CollectionWrapper getOwningCollection() 
	{
		try{
			return new CollectionWrapper(this.getDso().getOwningCollection());
		}catch (SQLException e){
			 throw new RepelExpresionException("Error inesperado",e);
		}
	}

	public boolean isIn(CollectionWrapper collection)
	{
		try{
			return this.getDso().isIn(collection.getCollection());
		}catch(SQLException e){
			throw new RepelExpresionException("Error inesperado",e);
		}
	}

	public CollectionWrapper[] getCollections(){
		try{
			Collection[] array = this.getDso().getCollections();
			CollectionWrapper[] returner = new CollectionWrapper[array.length];
			for(int i=0; i< array.length;i++){
				returner[i]=new CollectionWrapper(array[i]);
			}
			return returner;
		}catch(SQLException e){
			throw new RepelExpresionException("Error inesperado",e);
		}
		
	}

	public CommunityWrapper[] getCommunities(){
		try{
			Community[] array = this.getDso().getCommunities();
			CommunityWrapper[] returner = new CommunityWrapper[array.length];
			for(int i=0; i< array.length;i++){
				returner[i]=new CommunityWrapper(array[i]);
			}
			return returner;
		}catch(SQLException e){
			throw new RepelExpresionException("Error inesperado",e);
		}
	}

	public BundleWrapper[] getBundles(){
		try{
			Bundle[] array = this.getDso().getBundles();
			BundleWrapper[] returner = new BundleWrapper[array.length];
			for(int i=0; i< array.length;i++){
				returner[i]=new BundleWrapper(array[i]);
			}
			return returner;
		}catch(SQLException e){
			throw new RepelExpresionException("Error inesperado",e);
		}
	}

	public BundleWrapper[] getBundles(String name){
		try{
			Bundle[] array = this.getDso().getBundles(name);
			BundleWrapper[] returner = new BundleWrapper[array.length];
			for(int i=0; i< array.length;i++){
				returner[i]=new BundleWrapper(array[i]);
			}
			return returner;
		}catch(SQLException e){
			throw new RepelExpresionException("Error inesperado",e);
		}
	}

	public BitstreamWrapper[] getNonInternalBitstreams(){
		try{
			Bitstream[] array = this.getDso().getNonInternalBitstreams();
			BitstreamWrapper[] returner = new BitstreamWrapper[array.length];
			for(int i=0; i< array.length;i++){
				returner[i]=new BitstreamWrapper(array[i]);
			}
			return returner;
		}catch(SQLException e){
			throw new RepelExpresionException("Error inesperado",e);
		}
	}

	public boolean isOwningCollection(CollectionWrapper c)
	{
		return this.getDso().isOwningCollection(c.getCollection());
	}

	public int getType()
	{
		return this.getDso().getType();
	}

	public boolean hasUploadedFiles()
	{
		try{
			return this.getDso().hasUploadedFiles();
		}catch(SQLException e){
			throw new RepelExpresionException("Error inesperado",e);
		}
	}

	public CollectionWrapper[] getCollectionsNotLinked()
	{
		try{
			Collection[] array = this.getDso().getCollectionsNotLinked();
			CollectionWrapper[] returner = new CollectionWrapper[array.length];
			for(int i=0; i< array.length;i++){
				returner[i]=new CollectionWrapper(array[i]);
			}
			return returner;
		}catch(SQLException e){
			throw new RepelExpresionException("Error inesperado",e);
		}
	}

	public String getName()
	{
		return this.getDso().getName();
	}

	protected Item getItem(){
		return this.getDso();
	}


}
