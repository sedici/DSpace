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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.Throwable;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

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
	public void uploadContent(Item item) throws Throwable {
		
        String handle = item.getHandle();
        log.info("Upload del item " + handle +" a YouTube");
        String title= Jsoup.parse(item.getMetadata("dc.title")).text();
        
        Map<String, Object> metadata;
        metadata= this.buildMetadata(item);
        
        List<String> tags = this.buildTags(item);

        
		//Se crea u obtiene el bundle YOUTUBE donde se guardaran los bitsreams usados para el borrado  
        Bundle youtubeBundle;
        if (item.getBundles("YOUTUBE").length==0) {
        	item.createBundle("YOUTUBE");
        }
        youtubeBundle = item.getBundles("YOUTUBE")[0];
        Bitstream[] bitstreams = item.getBundles("ORIGINAL")[0].getBitstreams();

		//Cuenta la cantidad de videos en un item para poder diferenciar los titulos de los videos que pertenecen a un mismo item
		int cantV=0;
		for (Bitstream bitstream : bitstreams) {
			String mimeType = bitstream.getFormat().getMIMEType();
        	if (mimeType.equalsIgnoreCase(MP4_MIME_TYPE) | mimeType.equalsIgnoreCase(MPEG_MIME_TYPE) | mimeType.equalsIgnoreCase(QUICKTIME_MIME_TYPE)) {
				cantV++;
			}
		}
		int cantV2=0;

        for (Bitstream bitstream : bitstreams) {
        	String mimeType = bitstream.getFormat().getMIMEType();
        	if (mimeType.equalsIgnoreCase(MP4_MIME_TYPE) | mimeType.equalsIgnoreCase(MPEG_MIME_TYPE) | mimeType.equalsIgnoreCase(QUICKTIME_MIME_TYPE)) {
				cantV2++;
				List<Metadatum> replicationId = bitstream.getMetadata("sedici","identifier","youtubeId",Item.ANY,Item.ANY);
        		if (replicationId.size() == 0) {
        			if(cantV > 1 ){
						title = title + "- Parte " + cantV2;
					}
        			try {
        				YoutubeAdapter vadapter = new DSpace().getSingletonService(YoutubeAdapter.class);
						String videoID =  vadapter.uploadVideo(bitstream.retrieve(), title, metadata, tags);		
						if(videoID != null) {
		            		log.info("Se subio el video con id "+videoID);
		            		String schema = "sedici";
		            		String element = "identifier";
		            		String qualifier = "youtubeId";
		            		String lang = null;
	            			bitstream.addMetadata(schema,element,qualifier,lang,videoID);
	                		bitstream.updateMetadata();
	                		item.updateLastModified();
							String initialString = bitstream.getID()+";"+videoID;
	    				    InputStream targetStream = new ByteArrayInputStream(initialString.getBytes());
	    					youtubeBundle.createBitstream(targetStream).setName("Mapa bitstream - youtube");
	    					youtubeBundle.update();
							title = Jsoup.parse(item.getMetadata("dc.title")).text();
	            	}
        			}catch(UploadExeption e){
        				//log.error(e.getMessage()); Se loggea en el adapter
        				if(e.isResumable()) {
        					Curator curator = new Curator();
        					Context ctx = new Context();
        					ctx.turnOffAuthorisationSystem();
        			        ctx.setCurrentUser(EPerson.findByEmail(ctx, ConfigurationManager.getProperty("youtube.upload","youtube.upload.user")));//Crear y usar el usuario info+video@sedici.unlp.edu.ar
							curator.addTask("VideoUploaderTask").queue(ctx,item.getHandle(),"youtube");
							ctx.complete();
        				}
						//Por ahora se asume que todo error se debe avisar
        				if(!e.isResumable()) {
        					MailReporter.reportUnknownException("Ocurrio un error no reasumible en el upload del item "+item.getHandle(), e, "http://sedici.unlp.edu.ar/handle/"+item.getHandle());
        				}else if (e.getMessage().equals("The daily quota of Youtube has exeded")){
        					MailReporter.reportUnknownException("Error de cuota de Youtube exedida en la subida del item con handle "+item.getHandle(), e, "http://sedici.unlp.edu.ar/handle/"+item.getHandle());
        				}else {
        					MailReporter.reportUnknownException("Exepcion no manejada en el upload de un video a Youtube con handle "+item.getHandle(), e, "http://sedici.unlp.edu.ar/handle/"+item.getHandle());
        				}
        				
        			}
					
        		}else {
        			log.warn("El video con id "+bitstream.getID()+" ya se encuentra replicado en youtube con el id "+bitstream.getMetadata("sedici.video.videoId"));
        		}        		

        	}
        }
		
	}

	@Override
	public void removeContent(Item item) throws Throwable {
		if(item.getBundles("YOUTUBE").length != 0) {
			Bitstream[] mapsYoutube = item.getBundles("YOUTUBE")[0].getBitstreams();
			if(item.getBundles("ORIGINAL").length>0) {
				Bitstream[] bitstreams = item.getBundles("ORIGINAL")[0].getBitstreams();
				for (Bitstream map : mapsYoutube) {
					//Transformar el archivo en el bundle YOUTUBE a un string
					StringBuilder textBuilder = new StringBuilder();
					try (Reader reader = new BufferedReader(new InputStreamReader
						      (map.retrieve(), Charset.forName(StandardCharsets.UTF_8.name())))) {
						        int c = 0;
						        while ((c = reader.read()) != -1) {
						            textBuilder.append((char) c);
						        }
						    }
					String[] mapeo = textBuilder.toString().split(";");
					Boolean existe = false;
					for (Bitstream bitstream : bitstreams) {
						//Compara todos los bitstreams_id para saber si existe el bitstream todavia en el item
						if(mapeo[0].equals(Integer.toString(bitstream.getID())) ) {
							existe = true;
						}
					}
					// En caso de que no exista se borra el video con id coreespondiente a el bitstream que no existe mas
					if (existe == false) {
						try {
							YoutubeAdapter vadapter = new DSpace().getSingletonService(YoutubeAdapter.class);
							vadapter.deleteVideo(mapeo[1]);
							item.getBundles("YOUTUBE")[0].removeBitstream(map);
						}catch(UploadExeption e){
	        				//log.error(e.getMessage()); Se loggea en el adapter
	        				if(e.isResumable()) {
	        					Curator curator = new Curator();
	        					Context ctx = new Context();
	        					ctx.turnOffAuthorisationSystem();
	        			        ctx.setCurrentUser(EPerson.findByEmail(ctx, ConfigurationManager.getProperty("youtube.upload","youtube.upload.user")));//Crear y usar el usuario info+video@sedici.unlp.edu.ar
								curator.addTask("VideoUploaderTask").queue(ctx,item.getHandle(),"youtube");
								ctx.complete();
	        				}
	        				if(!e.isResumable()) {
	        					MailReporter.reportUnknownException("Ocurrio un error no reasumible en el delete del bitstream con id en youtube "+mapeo[1]+" en el item con handle "+item.getHandle(), e, "http://sedici.unlp.edu.ar/handle/"+item.getHandle());
	        				}else if (e.getMessage().equals("The daily quota of Youtube has exeded")){
	        					MailReporter.reportUnknownException("Error de cuota de Youtube exedida en la eliminacion de Youtube del item con handle "+item.getHandle(), e, "http://sedici.unlp.edu.ar/handle/"+item.getHandle());
	        				}else {
	        					MailReporter.reportUnknownException("Exepcion no manejada en el delete de un video en Youtube con handle "+item.getHandle(), e, "http://sedici.unlp.edu.ar/handle/"+item.getHandle());
	        				}
	        				
	        			}
					}
		        	
				}
			}else {
				//Este caso existe ya que si se borra el ultimo video de ORIGINAL, se borra el bundle entero, por lo tanto se borra todo lo que exista en YOUTUBE
				for (Bitstream map : mapsYoutube) {
					StringBuilder textBuilder = new StringBuilder();
					try (Reader reader = new BufferedReader(new InputStreamReader
						      (map.retrieve(), Charset.forName(StandardCharsets.UTF_8.name())))) {
						        int c = 0;
						        while ((c = reader.read()) != -1) {
						            textBuilder.append((char) c);
						        }
						    }
					String[] mapeo = textBuilder.toString().split(";");
					YoutubeAdapter vadapter = new DSpace().getSingletonService(YoutubeAdapter.class);
					vadapter.deleteVideo(mapeo[1]);
					item.getBundles("YOUTUBE")[0].removeBitstream(map);
				}
		        	
			}
		}	
		
	}

	@Override
	public void modifyContent(Item item) throws Throwable {
		String handle = item.getHandle();
        log.info("Update del item " + handle +" a YouTube");
        String title= Jsoup.parse(item.getMetadata("dc.title")).text();
        Map<String, Object> metadata;
        metadata = this.buildMetadata(item);
        
        List<String> tags = this.buildTags(item);
        Bitstream[] bitstreams = item.getBundles("ORIGINAL")[0].getBitstreams();

		int cantV=0;
		for (Bitstream bitstream : bitstreams) {
			String mimeType = bitstream.getFormat().getMIMEType();
        	if (mimeType.equalsIgnoreCase(MP4_MIME_TYPE) | mimeType.equalsIgnoreCase(MPEG_MIME_TYPE) | mimeType.equalsIgnoreCase(QUICKTIME_MIME_TYPE)) {
				cantV++;
			}
		}
		int cantV2=0;

        for (Bitstream bitstream : bitstreams) {
        	String mimeType = bitstream.getFormat().getMIMEType();
        	if (mimeType.equalsIgnoreCase(MP4_MIME_TYPE) | mimeType.equalsIgnoreCase(MPEG_MIME_TYPE) | mimeType.equalsIgnoreCase(QUICKTIME_MIME_TYPE)) {
				cantV2++;
        		if (bitstream.getMetadata("sedici.identifier.youtubeId") != null) {
        			if(cantV > 1 ){
						title = title + "-Parte " + cantV2;
					}
        			try {
        				YoutubeAdapter vadapter = new DSpace().getSingletonService(YoutubeAdapter.class);
        				String videoID = vadapter.updateMetadata(bitstream.getMetadata("sedici.identifier.youtubeId"), title, metadata, tags);
                		log.info("Se actualizo el video con id "+videoID);
						title=Jsoup.parse(item.getMetadata("dc.title")).text();
        			}catch(UploadExeption e){
        				//log.error(e.getMessage()); Se loggea en el adapter
        				if(e.isResumable()) {
        					Curator curator = new Curator();
        					Context ctx = new Context();
        					ctx.turnOffAuthorisationSystem();
        			        ctx.setCurrentUser(EPerson.findByEmail(ctx, ConfigurationManager.getProperty("youtube.upload","youtube.upload.user")));//Crear y usar el usuario info+video@sedici.unlp.edu.ar
							curator.addTask("VideoUploaderTask").queue(ctx,item.getHandle(),"youtube");
							ctx.complete();
        				}
        				if(!e.isResumable()) {
        					MailReporter.reportUnknownException("Ocurrio un error no reasumible en el update del item "+item.getHandle(), e, "http://sedici.unlp.edu.ar/handle/"+item.getHandle());
        				}else if (e.getMessage().equals("The daily quota of Youtube has exeded")){
        					MailReporter.reportUnknownException("Error de cuota de Youtube exedida en update del item con handle "+item.getHandle(), e, "http://sedici.unlp.edu.ar/handle/"+item.getHandle());
        				}else {
        					MailReporter.reportUnknownException("Exepcion no manejada en el update de un video en Youtube con handle "+item.getHandle(), e, "http://sedici.unlp.edu.ar/handle/"+item.getHandle());
        				}
        				
        			}
        		}
        	}
        }
		
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
}
