package ar.edu.unlp.sedici.dspace.curation;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.dspace.content.DSpaceObject;
import org.dspace.content.Item;
import org.dspace.content.MetadataValue;
import org.dspace.content.authority.Choices;
import org.dspace.content.factory.ContentServiceFactory;
import org.dspace.content.service.MetadataValueService;
import org.dspace.core.Constants;
import org.dspace.curate.AbstractCurationTask;
import org.dspace.curate.Curator;

/**
 * Recorre los metadatos de cada Item y se fija si su valor contiene el patron determinado en AUTHORITY_SEPARATOR
 * Cuando se detecta este patron, se extrae el ID de la Authority y se guarad en el campo authority,
 * estrableciendo así el vínculo entre el metadato y el vocabulario controlado.
 * El valor del metadato se guarda nuevamente sin el id ni el separador.
 * @author nestor
 *
 */
public class LinkControlledMetadata extends AbstractCurationTask {

	private Pattern pattern = Pattern.compile("^(.*):::(.*)$");
	private MetadataValueService metadataValueService;
	
	@Override
	public void init(Curator curator, String taskId) throws IOException {

        metadataValueService = ContentServiceFactory.getInstance().getMetadataValueService();
		super.init(curator, taskId);
	}
	@Override
	public int perform(DSpaceObject dso) throws IOException {
		
		// Consideramos solo Items
        if (dso.getType() == Constants.ITEM) {
            Item item = (Item)dso;
            
            try {
				report("Procesando Item "+item.getID()+ " - canEdit: "+itemService.canEdit(Curator.curationContext(), item));
			} catch (SQLException e) {
				e.printStackTrace();
				setResult("Exception en Item "+item.getID()+": ["+e.getClass().getName()+"]"+e.getMessage());
				return Curator.CURATE_ERROR;
			}

            try {
	            // Obtenemos los metadatos y los recorremos
	            //FIXME Sería mas eficiente levantar la configuración y testear solo aquellos metadatos que sean controlados
	            processSchema(item, "sedici");
	            processSchema(item, "thesis");
	            processSchema(item, "eprints");
	            processSchema(item, "mods");
            
            
//				itemService.update(Curator.curationContext(), item);
            report("------------------------------------------------------------");
			} catch (SQLException e) {
				e.printStackTrace();
				setResult("Exception en Item "+item.getID()+": ["+e.getClass().getName()+"]"+e.getMessage());
				return Curator.CURATE_ERROR;
			}
            
            setResult("Item "+item.getID()+" actualizado");
            return Curator.CURATE_SUCCESS;
            
        } else {
           setResult("Omitido por no ser Item");
           return Curator.CURATE_SKIP;
        }
	}
	
	private void processSchema(Item item, String schema) throws SQLException {
        List<MetadataValue> metadata = itemService.getMetadata(item, schema, Item.ANY, Item.ANY, Item.ANY);
        itemService.clearMetadata(Curator.curationContext(), item, schema, Item.ANY, Item.ANY, Item.ANY);
		for (MetadataValue m : metadata) {
        	Matcher matcher = pattern.matcher(m.getValue());
        	if(matcher.matches()) {
        		String old_value = m.getValue();
        		
        		m.setAuthority(matcher.group(1));
        		m.setValue(matcher.group(2));
        		
        		if("0".equals( m.getAuthority())) {
        			m.setAuthority(null);
        			m.setConfidence(Choices.CF_NOTFOUND);
        		} else {
        			m.setConfidence(Choices.CF_ACCEPTED);
        		}
        		report("["+m.getMetadataField().toString('.')+"] "+old_value+" ==> ("+m.getAuthority()+") "+m.getValue());

            	// Insertamos el metadato actualizado
            	metadataValueService.update(Curator.curationContext(), m);
        	}

        }
	}

}
