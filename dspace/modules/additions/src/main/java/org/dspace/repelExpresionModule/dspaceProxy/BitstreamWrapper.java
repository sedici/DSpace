package org.dspace.repelExpresionModule.dspaceProxy;

import java.sql.SQLException;

import org.dspace.repelExpresionModule.repel.*;
import org.dspace.content.Bitstream;
import org.dspace.content.Bundle;

public class BitstreamWrapper extends DspaceObjectWrapper<Bitstream> {

	public BitstreamWrapper(Bitstream bits) {
		super(bits);
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
	
		public String getName(){
			return this.getDso().getName();
		}
	
		public int getSequenceID() {
			return this.getDso().getSequenceID();
		}
	
		public String getSource() {
			return this.getDso().getSource();
		}
	
		public String getDescription() {
			return this.getDso().getDescription();
		}
	
		public String getChecksum() {
			return this.getDso().getChecksum();
		}
	
		public String getChecksumAlgorithm() {
			return this.getDso().getChecksumAlgorithm();
		}
		
		public long getSize() {
			return this.getDso().getSize();
		}
		
		public String getUserFormatDescription() {
			return this.getDso().getUserFormatDescription();
		}
	
		public String getFormatDescription() {
			return this.getDso().getFormatDescription();
		}
	
		public BundleWrapper[] getBundles() {
			try {
				Bundle[] array = this.getDso().getBundles();
				BundleWrapper[] returner = new BundleWrapper[array.length];
				for(int i=0; i< array.length;i++){
					returner[i]=new BundleWrapper(array[i]);
				}
				return returner;
			} catch (SQLException e) {
				throw new RepelExpresionException("Error inesperado",e);
			}
		}
	
		public boolean isRegisteredBitstream() {
			return this.getDso().isRegisteredBitstream();
		}
	
		public int getStoreNumber() {
			return this.getDso().getStoreNumber();
		}
}
