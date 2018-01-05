package ar.edu.unlp.sedici.dspace.embargo;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.dspace.content.DCDate;
import org.dspace.content.Item;
import org.dspace.content.MetadataValue;

/**

 */
public class SeDiCIEmbargoSetter extends DaysEmbargoSetter {

	private static Logger log = Logger.getLogger(SeDiCIEmbargoSetter.class);
	private static SimpleDateFormat sediciDatetimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private static SimpleDateFormat sediciDateFormat = new SimpleDateFormat("yyyy-MM-dd");

	private static String exposureDateMetadata = "sedici.date.exposure";
	private static String typeMetadata = "dc.type";
	private static String TYPE_TESIS= "tesis";

	public SeDiCIEmbargoSetter() {
		super();
	    log.info("Se inicialza correctamente el SeDiCIEmbargoSetter del módulo de embargo");
	}

	protected Date getEmbargoStartDate(Item item) {
		List<MetadataValue> docTypes = getItemService().getMetadataByMetadataString(item,typeMetadata);
		
		if (getItemService().getMetadataByMetadataString(item,"sedici2003.identifier").size() != 0){
			//Es un doc importado
			List<MetadataValue> embargosViejos = getItemService().getMetadataByMetadataString(item, "sedici2003.fecha-hora-creacion");
			if (embargosViejos.size()> 0){
				try{
        			return new DCDate(sediciDatetimeFormat.parse(embargosViejos.get(0).getValue())).toDate();
        		}catch(ParseException e){
        			log.warn("Error de parseo de fecha al procesar (sedici2003.fecha-hora-creacion)==("+embargosViejos.get(0).getValue()+") del documento importado con id "+getItemService().getMetadataByMetadataString(item, "sedici2003.identifier").get(0));
                } 
			}
			
			throw new IllegalArgumentException("No se pudo procesar la fecha de creacion (sedici2003.fecha-creacion) del documento importado con id "+getItemService().getMetadataByMetadataString(item, "sedici2003.identifier").get(0).getValue());
		}
		
		if (docTypes.size() == 0){
			log.info("No se encontro un type para el doc "+item.getHandle());
			return null;
		}
		if (!TYPE_TESIS.equalsIgnoreCase(docTypes.get(0).getValue())){
			log.trace("El doc "+item.getHandle()+" no es una tesis, no busco el campo "+exposureDateMetadata);
			return null;
		}
		
		List<MetadataValue>  exposureDates = getItemService().getMetadataByMetadataString(item, exposureDateMetadata);
		if (exposureDates.size() == 0){
			log.trace("No se encontro un campo "+exposureDateMetadata+" para el doc "+item.getHandle());
			return null;
		}
		
		return new DCDate(exposureDates.get(0).getValue()).toDate();
	}
	
	
}
