package org.dspace.repelExpresionModule.dspaceProxy;
import java.sql.SQLException;

import org.dspace.repelExpresionModule.repel.RepelExpresionException;
import org.dspace.content.DSpaceObject;
import org.dspace.content.Metadatum;
import org.dspace.core.Context;

public class DspaceObjectWrapper<T extends DSpaceObject> {
	
    private T dso;
    private Context context;

	public DspaceObjectWrapper(T dso){
    	this.dso = dso;
    	try {
			this.context = new Context();
		} catch (SQLException e) {
			throw new RepelExpresionException("Error al instanciar un contexto",e);
		}
    }
    
	public int getType(){
    	return this.getDso().getType();
    	}
	
	protected final T getDso(){
		return dso;
	}
    
    public String getTypeText()
    {
        return this.getDso().getTypeText();
    }
    
    public int getID(){
    	return this.getDso().getID();
    }
    
    public  String getHandle(){
    	return this.getDso().getHandle();
    }
    
    public  String getName(){
    	return this.dso.getName();
    }
    
//    public String[] getIdentifiers()
//    {
//       return this.getDso().getIdentifiers();
//    }

    public DSpaceObject getParentObject() 
    {
       try{
    	return this.getDso().getParentObject();
       }catch (SQLException e){
    	   throw new RepelExpresionException("Error inesperado",e);
       }
    }
    
	 public MetadatumWrapper[] getMetadata(String schema, String element, String qualifier, String lang){
		 return this.getWrapperCollection(this.getDso().getMetadata(schema,element,qualifier,lang));
	 }

	public MetadatumWrapper[] getMetadata(String mdString){
		 return this.getWrapperCollection(this.getDso().getMetadataByMetadataString(mdString));
	 }
	
	 private MetadatumWrapper[] getWrapperCollection(Metadatum[] metadata) {
		MetadatumWrapper[] returner = new MetadatumWrapper[metadata.length];
		for(int i = 0; i< metadata.length;i++){
			returner[i]= new MetadatumWrapper(metadata[i]);
		}
		return returner;
	}

	protected Context getContext() {
		return context;
	}

}
