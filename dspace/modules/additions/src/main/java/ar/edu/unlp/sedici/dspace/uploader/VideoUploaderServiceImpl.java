package ar.edu.unlp.sedici.dspace.uploader;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.io.File;
import java.lang.Throwable;
import org.apache.commons.io.*;

import org.apache.log4j.Logger;
import org.dspace.content.Bitstream;
import org.dspace.content.DSpaceObject;
import org.dspace.content.Item;
import org.dspace.content.Metadatum;
import org.dspace.core.Context;
import org.dspace.curate.Curator;
import org.dspace.handle.HandleManager;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Service;
import ar.edu.unlp.sedici.dspace.google.YoutubeAdapter;

@Service
public class VideoUploaderServiceImpl implements ContentUploaderService{

	private static final Logger log = Logger.getLogger(VideoUploaderServiceImpl.class);
	
	private final String MPEG_MIME_TYPE = "video/mpeg";
	private final String QUICKTIME_MIME_TYPE = "video/quicktime";
	private final String MP4_MIME_TYPE = "video/mp4";

	@Override
	public void uploadContent(Item item) throws Throwable {
        String handle = item.getHandle();
        log.info("Upload del item " + handle +" a YouTube");
        String title= item.getMetadata("dc.title");//Falta determinar que hacer is hay muchos videos, como se construye el titulo
        System.out.println(title);
        String description = this.buildDescription(item);
        System.out.println(description);
        List<String> tags = new ArrayList<String>();
        tags.add("prueba");//Definir que poner en los tags
        Bitstream[] bitstreams = item.getBundles("ORIGINAL")[0].getBitstreams();
        for (Bitstream bitstream : bitstreams) {
        	String mimeType = bitstream.getFormat().getMIMEType();
        	if (mimeType.equalsIgnoreCase(MP4_MIME_TYPE) | mimeType.equalsIgnoreCase(MPEG_MIME_TYPE) | mimeType.equalsIgnoreCase(QUICKTIME_MIME_TYPE)) {
        		if (bitstream.getMetadata("sedici.video.videoId") == null) {
        			String videoID = new YoutubeAdapter().uploadVideo(bitstream.retrieve(), title, description, tags);
            		log.info("Se subio el video con id "+videoID);
            		String schema = "sedici";
            		String element = "identifier";
            		String qualifier = "youtubeId";
            		String lang = null;
            		bitstream.addMetadata(schema,element,qualifier,lang,videoID);
            		bitstream.updateMetadata();
        		}else {
        			log.warn("El video con id "+bitstream.getID()+" ya se encuentra replicado en youtube con el id "+bitstream.getMetadata("sedici.video.videoId"));
        		}
        		
        	}
        }
		
	}

	@Override
	public void removeContent(Context context, String hdl) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void modifyContent(Context context, String hdl) {
		// TODO Auto-generated method stub
		
	}
	
	private String buildDescription(Item item) {
	    	String description = item.getMetadata("dc.title")+"\n";//falta derterminar que mas se agrega a la descripcion
	        List<Metadatum> creators = item.getMetadata("sedici","creator","person",Item.ANY,Item.ANY);
	        Integer auxNumerico = 1;
	        if (creators.size() > 1) {
	        	description = description+"Creadores: "+creators.get(0).value;
	        	while(auxNumerico<creators.size()) {
	        		description = description+"; "+creators.get(auxNumerico).value;
	        		auxNumerico = auxNumerico + 1;
	        	}
	        	description= description+"\n";
	        }else {
	        	description = description+"Creador: "+creators.get(0).value+"\n";
	        }
	        description = description+"Fecha de publicación: "+item.getMetadata("dc.date.available")+"\n";
	        description = description+"Enlace de la fuente: "+item.getMetadata("dc.identifier.uri")+"\n";
	        List<Metadatum> subjects = item.getMetadata("dc","subject",Item.ANY,Item.ANY,Item.ANY);
	        description = description+"Palabras clave: ";
	        auxNumerico = 0;
	        while (auxNumerico<(subjects.size()-1)) {
	        	description = description+subjects.get(auxNumerico).value+", ";
	        	auxNumerico = auxNumerico +1;
	        }
	        description = description+subjects.get(auxNumerico).value+"\n";
	        description = description+"Resumen: "+Jsoup.parse(item.getMetadata("dc.description.abstract")).text()+"\n";
	        description = description+"Licencia de uso: "+item.getMetadata("sedici.rights.license")+"\n";
	        return description;
	}

}
