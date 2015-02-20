package org.dspace.repelExpresionModule.dspaceProxy;


import org.dspace.content.Metadatum;

public class MetadatumWrapper{

	private Metadatum mtd;

	public MetadatumWrapper(Metadatum mtd) {
		this.mtd = mtd;
	}
	
	public String getElement() {
		return this.mtd.element;
	}
	
	public String getQualifier() {
		return this.mtd.qualifier;
	}
	
	public String getValue() {
		return this.mtd.value;
	}

	public String getLanguage() {
		return this.mtd.language;
	}

	public String getSchema() {
		 return this.mtd.schema;
	}

	public String getAuthority() {
		return this.mtd.authority;
	}

	public int getConfidence() {
		return this.mtd.confidence;
	}
    
	public String getField() {
		return this.mtd.getField();
	}

	public boolean hasSameFieldAs(Metadatum dcValue) {
		return this.mtd.hasSameFieldAs(dcValue);
	}
	
	public boolean equals(Object o) {
		return this.mtd.equals(o);
	}

}
