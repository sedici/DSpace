package ar.edu.unlp.sedici.dspace.curation;

import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

import org.dspace.content.DCDate;
import org.dspace.content.DSpaceObject;
import org.dspace.content.Item;
import org.dspace.content.MetadataValue;
import org.dspace.core.Constants;
import org.dspace.core.Context;
import org.dspace.curate.AbstractCurationTask;
import org.dspace.curate.Curator;

/**
 * 
 * El valor del metadato se guarda nuevamente sin el id ni el separador.
 * 
 * @author nestor
 * 
 */
public class CopyMetadata extends AbstractCurationTask {

	private static SimpleDateFormat sediciDatetimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private static SimpleDateFormat sediciDateFormat = new SimpleDateFormat("yyyy-MM-dd");

	@Override
	public int perform(DSpaceObject dso) throws IOException {
		Context c;
		try {
			c = Curator.curationContext();
		} catch (SQLException e) {
			throw new IOException(e);
		}
		
		if (dso.getType() != Constants.ITEM) {
	           setResult("Omitido por no ser Item");
	           return Curator.CURATE_SKIP;
	    }
		
		// Consideramos solo Items
        try {
            Item item = (Item)dso;
            
            report("Procesando Item "+item.getID()+ " - canEdit: "+itemService.canEdit(c, item));
			
            
            //copio el metadato fecha_hora_creacion
            List<MetadataValue> metadata = itemService.getMetadataByMetadataString(item, "sedici2003.fecha-hora-creacion");
            Boolean creacion_bool=true;
            Boolean disponibilidad_bool=true;        	
        	if (metadata.size()>0){
        		DCDate fhCreacion = new DCDate(sediciDatetimeFormat.parse(metadata.get(0).getValue()));
        		
        		itemService.clearMetadata(c, item, "dc", "date", "accessioned", null);
	            // Insertamos el metadato actualizado
	        	itemService.addMetadata(c, item, "dc", "date", "accessioned", null, fhCreacion.toString());
            } else {
            	//en caso de que no exista el metadato en cuestion reportamos esto
            	creacion_bool=false;
            	report("El item no posee el metadato sedici2003.fecha-hora-creacion");
            };
            //copio el metadato fecha_disponiblidad
            metadata=itemService.getMetadataByMetadataString(item, "sedici2003.fecha-disponibilidad");                   	
            if (metadata.size()>0){
        		DCDate fDisponibilidad= new DCDate(sediciDateFormat.parse(metadata.get(0).getValue()));
            	itemService.clearMetadata(c, item, "dc", "date", "available", null);
            	itemService.addMetadata(c,item, "dc", "date", "available", null, fDisponibilidad.toString());
            } else {
            	//en caso de que no exista el metadato en cuestion reportamos esto
            	disponibilidad_bool=false;
            	report("El item no posee el metadato sedici2003.fecha-disponibilidad");
            };

            report("------------------------------------------------------------");
            
            //Guardo los cambios
            itemService.update(c, item);
			
            //Si se guardaron ambos metadatos, informo la corrección de la curation, en caso contrario notifico el fallo.
            if(creacion_bool && disponibilidad_bool) {
            	setResult("Item "+item.getID()+" actualizado");
                return Curator.CURATE_SUCCESS;
            } else {
            	String result="El Item "+item.getID()+" no posee los siguientes metadatos:";
            	if (!creacion_bool){
            		result=result+" sedici2003.fecha-hora-creacion";
            	};            	
            	if (!disponibilidad_bool){
            		result=result+" sedici2003.fecha-disponibilidad";
            	}
            	
            	if (creacion_bool || disponibilidad_bool){
            		setResult("Item actualizado con errores. "+result);
            	} else {
            		setResult("Item no modificado. "+result);
            	};
            	
            	return Curator.CURATE_ERROR;
            }
            
            
        } catch(ParseException e){
			e.printStackTrace();
			setResult("No se pueden procesar las fechas en Item "+dso.getID()+": ["+e.getClass().getName()+"]"+e.getMessage());
			return Curator.CURATE_FAIL;
        } catch (Exception e) {
			e.printStackTrace();
			setResult("Exception en Item "+dso.getID()+": ["+e.getClass().getName()+"]"+e.getMessage());
			return Curator.CURATE_ERROR;
		}
	}
}
