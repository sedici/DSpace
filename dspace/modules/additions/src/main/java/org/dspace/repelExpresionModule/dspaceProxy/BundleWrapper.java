package org.dspace.repelExpresionModule.dspaceProxy;

import java.sql.SQLException;

import org.dspace.repelExpresionModule.repel.RepelExpresionException;
import org.dspace.content.Bitstream;
import org.dspace.content.Bundle;
import org.dspace.content.Item;

public class BundleWrapper extends DspaceObjectWrapper<Bundle> {

	public BundleWrapper(Bundle bundle) {
		super(bundle);
	}
	
	public int getType() {
		return this.getDso().getType();
	}

	public String getTypeText(){ 
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
	
	public int getPrimaryBitstreamID()
    {
		return this.getDso().getPrimaryBitstreamID();
    }
	
	public BitstreamWrapper getBitstreamByName(String name){
		return new BitstreamWrapper(this.getDso().getBitstreamByName(name));
	}
	
	public BitstreamWrapper[] getBitstreams(){
		Bitstream[] array = this.getDso().getBitstreams();
		BitstreamWrapper[] returner = new BitstreamWrapper[array.length];
		for(int i=0; i< array.length;i++){
			returner[i]=new BitstreamWrapper(array[i]);
		}
		return returner;
	 }
	
	public ItemWrapper[] getItems() {
		try {
			Item[] array = this.getDso().getItems();
			ItemWrapper[] returner = new ItemWrapper[array.length];
			for(int i=0; i< array.length;i++){
				returner[i]=new ItemWrapper(array[i]);
			}
			return returner;
		} catch (SQLException e) {
			throw new RepelExpresionException("Error inesperado",e);
		}
	}
}
