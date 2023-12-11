package ar.edu.unlp.sedici.dspace.uploader;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.HashMap;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.Throwable;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;

import org.apache.log4j.Logger;
import org.dspace.content.Bitstream;
import org.dspace.content.Bundle;
import org.dspace.content.Item;
import org.dspace.content.Metadatum;
import org.dspace.core.ConfigurationManager;
import org.dspace.core.Context;
import org.dspace.curate.Curator;
import org.dspace.eperson.EPerson;
import org.dspace.utils.DSpace;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Service;
import ar.edu.unlp.sedici.dspace.google.YoutubeAdapter;
import ar.edu.unlp.sedici.util.MailReporter;

import org.dspace.authorize.AuthorizeException;
import org.dspace.authorize.AuthorizeManager;


@Service
public class VideoUploaderServiceImpl implements ContentUploaderService{

	private static final Logger log = Logger.getLogger(VideoUploaderServiceImpl.class);
	
	private final String MPEG_MIME_TYPE = "video/mpeg";
	private final String QUICKTIME_MIME_TYPE = "video/quicktime";
	private final String MP4_MIME_TYPE = "video/mp4";
	
	public VideoUploaderServiceImpl() {
		super();
	}

	@Override
	public void uploadContent(Item item) throws SQLException, IOException, AuthorizeException {
		
        String handle = item.getHandle();
        log.info("Upload del item " + handle +" a YouTube");
        String itemTitle= Jsoup.parse(item.getMetadata("dc.title")).text();

		//Se crea un bundle si es nescesario  
        if (item.getBundles("YOUTUBE").length==0) {
        	item.createBundle("YOUTUBE");
        }
        Bitstream[] bitstreams = item.getBundles("ORIGINAL")[0].getBitstreams();

        for (Bitstream bitstream : bitstreams) {
        	try {
        		if (autorizarSubida(bitstream)) {
        			String title = itemTitle+" - "+bitstream.getMetadata("dc.description");
    				YoutubeAdapter vadapter = new DSpace().getSingletonService(YoutubeAdapter.class);
					String videoID =  vadapter.uploadVideo(bitstream.retrieve(), title, this.buildMetadata(item), this.buildTags(item));
    				//String videoID = null;
					if(videoID != null) {
	            		log.info("Se subio el video con id "+videoID);
	            		persistirId(videoID,item,bitstream);
					}
            	}
        	}catch(UploadExeption e){
				//log.error(e.getMessage()); Se loggea en el adapter
				this.resolveExeption(e,"upload",item);
			}
        }
		
	}
	
	private boolean autorizarSubida(Bitstream bitstream) throws UploadExeption, SQLException{
		Boolean condicion = false;
		Context ctx = new Context();
    	if(AuthorizeManager.authorizeActionBoolean(ctx, bitstream, 0, false)) {
        	ctx.complete();
        	String mimeType = bitstream.getFormat().getMIMEType();
        	if (mimeType.equalsIgnoreCase(MP4_MIME_TYPE) | mimeType.equalsIgnoreCase(MPEG_MIME_TYPE) | mimeType.equalsIgnoreCase(QUICKTIME_MIME_TYPE)) {
				List<Metadatum> replicationId = bitstream.getMetadata("sedici","identifier","youtubeId",Item.ANY,Item.ANY);
        		if (replicationId.size() == 0) {
        			if (bitstream.getMetadata("dc.description").isEmpty()) {
        					throw new UploadExeption("Bitstream sin nombre",false);
        			}else {
        				condicion = true;
        			}
        		}else {
        			log.warn("El video con id "+bitstream.getID()+" ya se encuentra replicado en youtube con el id "+bitstream.getMetadata("sedici.identifier.youtubeId"));
        		}
        	}
    	} else {
    		ctx.complete();
    		log.warn("El bitstream con id "+bitstream.getID()+" no debe ser replicado ya que no es accesible por anonymous");
    	}
    	return condicion;
	}
	
	private void persistirId(String id, Item item,Bitstream bitstream) {
		String schema = "sedici";
		String element = "identifier";
		String qualifier = "youtubeId";
		String lang = null;
		bitstream.addMetadata(schema,element,qualifier,lang,id);
		try {
			bitstream.updateMetadata();
			item.updateLastModified();
			String initialString = bitstream.getID()+";"+id;
		    InputStream targetStream = new ByteArrayInputStream(initialString.getBytes());
		    Bundle bundle = item.getBundles("YOUTUBE")[0];
			bundle.createBitstream(targetStream).setName("Mapa bitstream - youtube");
			bundle.update();
		} catch (SQLException e) {
			log.error("Error de SQL al persisir el id del video replicado");
			log.error("SQLException: " + e.getMessage());
		} catch (AuthorizeException e) {
			log.error("Error de Autorization al persisir el id del video replicado");
			log.error("AuthorizeException: " + e.getMessage());
		} catch (IOException e) {
			log.error("Error de IO al persisir el id del video replicado");
			log.error("IOException: " + e.getMessage());
		}
	}

	@Override
	public void removeContent(Item item) throws SQLException, IOException, AuthorizeException {
		if(item.getBundles("YOUTUBE").length != 0) {
			Bitstream[] mapsYoutube = item.getBundles("YOUTUBE")[0].getBitstreams();
			if(item.getBundles("ORIGINAL").length>0) {
				for (Bitstream map : mapsYoutube) {
					String idVideo = determinarBorradoDeBitstream(map,item);
					if (!idVideo.isEmpty()) {
						try {
							YoutubeAdapter vadapter = new DSpace().getSingletonService(YoutubeAdapter.class);
							vadapter.deleteVideo(idVideo);
							item.getBundles("YOUTUBE")[0].removeBitstream(map);
						}catch(UploadExeption e){
	        				//log.error(e.getMessage()); Se loggea en el adapter
							this.resolveExeption(e,"delete",item);
	        			}
					}
				}
			}else {
				//Este caso existe ya que si se borra el ultimo video de ORIGINAL, se borra el bundle entero, por lo tanto se borra todo lo que exista en YOUTUBE
				for (Bitstream map : mapsYoutube) {
					String[] mapeo = parsearBitstream(map);
					YoutubeAdapter vadapter = new DSpace().getSingletonService(YoutubeAdapter.class);
					vadapter.deleteVideo(mapeo[1]);
					item.getBundles("YOUTUBE")[0].removeBitstream(map);
				}
		        	
			}
		}	
		
	}
	
	private String[] parsearBitstream(Bitstream map) throws IOException, SQLException, AuthorizeException {
		StringBuilder textBuilder = new StringBuilder();
		try (Reader reader = new BufferedReader(new InputStreamReader
			      (map.retrieve(), Charset.forName(StandardCharsets.UTF_8.name())))) {
			        int c = 0;
			        while ((c = reader.read()) != -1) {
			            textBuilder.append((char) c);
			        }
			    }
		return textBuilder.toString().split(";");
	}
	
	private String determinarBorradoDeBitstream(Bitstream map, Item item) throws IOException, SQLException, AuthorizeException {
		Bitstream[] bitstreams = item.getBundles("ORIGINAL")[0].getBitstreams();
		//Transformar el archivo en el bundle YOUTUBE a un string
		String[] mapeo = parsearBitstream(map);
		Boolean existe = false;
		for (Bitstream bitstream : bitstreams) {
			//Compara todos los bitstreams_id para saber si existe el bitstream todavia en el item
			if(mapeo[0].equals(Integer.toString(bitstream.getID())) ) {
				existe = true;
			}
		}
		if (!existe){
			return mapeo[1];
		}
		else {
			return null;
		}
	}

	@Override
	public void modifyContent(Item item) throws SQLException, IOException, AuthorizeException {
		String handle = item.getHandle();
        log.info("Update del item " + handle +" a YouTube");
        String itemTitle= Jsoup.parse(item.getMetadata("dc.title")).text();
        
        Bitstream[] bitstreams = item.getBundles("ORIGINAL")[0].getBitstreams();

        for (Bitstream bitstream : bitstreams) {
        	if(autorizarModificacion(bitstream)) {
        			String title = itemTitle+" - "+bitstream.getMetadata("dc.description");
        			try {
        				YoutubeAdapter vadapter = new DSpace().getSingletonService(YoutubeAdapter.class);
        				String videoID = vadapter.updateMetadata(bitstream.getMetadata("sedici.identifier.youtubeId"), title, this.buildMetadata(item), this.buildTags(item));
                		log.info("Se actualizo el video con id "+videoID);
        			}catch(UploadExeption e){
        				//log.error(e.getMessage()); Se loggea en el adapter
        				this.resolveExeption(e,"update",item);
        			}
        	}
        }
		
	}
	
	private boolean autorizarModificacion(Bitstream bitstream) {
		String mimeType = bitstream.getFormat().getMIMEType();
    	if (mimeType.equalsIgnoreCase(MP4_MIME_TYPE) | mimeType.equalsIgnoreCase(MPEG_MIME_TYPE) | mimeType.equalsIgnoreCase(QUICKTIME_MIME_TYPE)) {
    		if (bitstream.getMetadata("sedici.identifier.youtubeId") != null) {
    			return true;
    		}
    	}
    	return false;
	}
	
	private Map<String,Object> buildMetadata(Item item) {
		
			//falta derterminar si hay mas metadatos que agregar
		
		 	Map<String, Object> metadata = new HashMap<String, Object>();
		 	
		 	//metadata.put("title", Jsoup.parse(item.getMetadata("dc.title")).text());
	        metadata.put("creators", item.getMetadata("sedici","creator","person",Item.ANY,Item.ANY));
	        metadata.put("subtype", item.getMetadata("sedici.subtype"));
	        //metadata.put("dateAvailable", item.getMetadata("dc.date.available"));
	        metadata.put("iUri", item.getMetadata("dc.identifier.uri"));
	        metadata.put("language", item.getMetadata("dc.language"));
	        metadata.put("subjects", item.getMetadata("dc","subject",Item.ANY,Item.ANY,Item.ANY));
			if(item.getMetadata("dc.description.abstract") !=  null){
				metadata.put("abstract", Jsoup.parse(item.getMetadata("dc.description.abstract")).text());
			}
			metadata.put("license", item.getMetadata("sedici.rights.license"));
	        return metadata;
	}
	

	private List<String> buildTags(Item item){
		List<String> tags = new ArrayList<String>();
        tags.add("UNLP");
        
        ListIterator<Metadatum> palabras = item.getMetadata("dc","subject",Item.ANY,Item.ANY,Item.ANY).listIterator();
	    while (palabras.hasNext()) {
	        tags.add(palabras.next().value);
	    }

	    ListIterator<Metadatum> origen = item.getMetadata("mods","originInfo","place",Item.ANY,Item.ANY).listIterator();
	    while (origen.hasNext()) {
	        tags.add(origen.next().value);
	    }

	    ListIterator<Metadatum> materias = item.getMetadata("sedici","subject","materias",Item.ANY,Item.ANY).listIterator();
	    while (materias.hasNext()) {
	        tags.add(materias.next().value);
	    }

	    ListIterator<Metadatum> catedras = item.getMetadata("sedici","description","catedra",Item.ANY,Item.ANY).listIterator();
	    while (catedras.hasNext()) {
	        tags.add(catedras.next().value);
	    }
	    return tags;
	}

	private void resolveExeption(UploadExeption e, String contexto, Item item){
		try {
			if(e.isResumable()) {
	        	Curator curator = new Curator();
	        	Context ctx = new Context();
	        	ctx.turnOffAuthorisationSystem();
	        	ctx.setCurrentUser(EPerson.findByEmail(ctx, ConfigurationManager.getProperty("youtube.upload","youtube.upload.user")));//Crear y usar el usuario info+video@sedici.unlp.edu.ar
	        	String task;
	        	if (contexto.equals("upload")) {
	        		task = "VideoUploaderTask";
	        	}else if(contexto.equals("update")){
	        		task = "VideoUpdaterTask";
	        	}else {
	        		task = "VideoDeleteTask";
	        	}
				curator.addTask(task).queue(ctx,item.getHandle(),"youtube");
				ctx.complete();
	        }
	        if(!e.isResumable()) {
	        	MailReporter.reportUnknownException("Ocurrio un error no reasumible en el "+ contexto +" del item "+item.getHandle(), e, "http://sedici.unlp.edu.ar/handle/"+item.getHandle());
	        }else if (e.getMessage().equals("The daily quota of Youtube has exeded")){
	        	MailReporter.reportUnknownException("Ocurrio un error reasumible en el "+ contexto +" del item con handle "+item.getHandle()+". Se agoto la quota de Youtube", e, "http://sedici.unlp.edu.ar/handle/"+item.getHandle());
	        }else if (e.getMessage().equals("No quota")){
	        	// En este caso no se avisa ya que, el servicio de youtube sabe que no tiene mas cuota, por lo tanto,
	        	// ya se quiso hacer una operacion con youtube y le aviso que no tiene mas quota, por lo tanto
	        	// ya se aviso que no se tiene mas quota. Este error existe para reencolar la cola de tareas de youtube
	        }else if (e.getMessage().equalsIgnoreCase("Bitstream sin nombre")){
	        	MailReporter.reportUnknownException("Ocurrio un error reasumible en el "+ contexto +" del item con handle "+item.getHandle()+". Un bitstream del item no tiene titulo", e, "http://sedici.unlp.edu.ar/handle/"+item.getHandle());
	        }
	        else {
	        	MailReporter.reportUnknownException("Ocurrio un error no manejado en el "+ contexto +" del item con handle "+item.getHandle(), e, "http://sedici.unlp.edu.ar/handle/"+item.getHandle());
	        }
		} catch (Throwable t) {
			log.error("Error en el manejo de exepciones del VideoUploaderService");
			log.error("Throwable: " + t.getMessage());
		}
	}
}
