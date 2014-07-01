/**
 * Copyright (C) 2011 SeDiCI <info@sedici.unlp.edu.ar>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ar.edu.unlp.sedici.dspace.curation;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.dspace.content.DCDate;
import org.dspace.content.DCValue;
import org.dspace.content.DSpaceObject;
import org.dspace.content.Item;
import org.dspace.core.Constants;
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
		if (dso.getType() != Constants.ITEM) {
	           setResult("Omitido por no ser Item");
	           return Curator.CURATE_SKIP;
	    }
		
		// Consideramos solo Items
        try {
            Item item = (Item)dso;
            
            report("Procesando Item "+item.getID()+ " - canEdit: "+item.canEdit());
			
            
            //copio el metadato fecha_hora_creacion
            DCValue[] metadata=item.getMetadata("sedici2003.fecha-hora-creacion");
            Boolean creacion_bool=true;
            Boolean disponibilidad_bool=true;        	
        	if (metadata.length>0){
        		DCDate fhCreacion = new DCDate(sediciDatetimeFormat.parse(metadata[0].value));
        		
        		item.clearMetadata("dc", "date", "accessioned", null);
	            // Insertamos el metadato actualizado
	        	item.addMetadata("dc", "date", "accessioned", null, fhCreacion.toString());
            } else {
            	//en caso de que no exista el metadato en cuestion reportamos esto
            	creacion_bool=false;
            	report("El item no posee el metadato sedici2003.fecha-hora-creacion");
            };
            //copio el metadato fecha_disponiblidad
            metadata=item.getMetadata("sedici2003.fecha-disponibilidad");                   	
            if (metadata.length>0){
        		DCDate fDisponibilidad= new DCDate(sediciDateFormat.parse(metadata[0].value));
            	item.clearMetadata("dc", "date", "available", null);
	        	item.addMetadata("dc", "date", "available", null, fDisponibilidad.toString());
            } else {
            	//en caso de que no exista el metadato en cuestion reportamos esto
            	disponibilidad_bool=false;
            	report("El item no posee el metadato sedici2003.fecha-disponibilidad");
            };

            report("------------------------------------------------------------");
            
            //Guardo los cambios
            item.update();
			
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
