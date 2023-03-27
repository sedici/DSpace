package ar.edu.unlp.sedici.dspace.uploader;

import java.util.ArrayList;
import java.util.List;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
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
import org.dspace.core.Context;
import org.dspace.curate.Curator;
import org.dspace.eperson.EPerson;
import org.dspace.utils.DSpace;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Service;
import ar.edu.unlp.sedici.dspace.google.YoutubeAdapter;
import ar.edu.unlp.sedici.util.MailReporter;

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
        String title= Jsoup.parse(item.getMetadata("dc.title")).text();
        String description = this.buildDescription(item);
        List<String> tags = new ArrayList<String>();
        tags.add("prueba");//Definir que poner en los tags
        Bundle youtubeBundle;
        if (item.getBundles("YOUTUBE").length==0) {
        	System.out.println(item.createBundle("YOUTUBE").getID());
        }
        youtubeBundle = item.getBundles("YOUTUBE")[0];
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
				List<Metadatum> replicationId = bitstream.getMetadata("sedici","identifier","youtubeId",Item.ANY,Item.ANY);
        		if (replicationId.size() == 0) {
        			if(cantV > 1 ){
						title = title + "-Parte " + cantV2;
					}
        			try {
						String videoID = new YoutubeAdapter().uploadVideo(bitstream.retrieve(), title, description, tags);
						if(videoID != null) {
		            		log.info("Se subio el video con id "+videoID);
		            		String schema = "sedici";
		            		String element = "identifier";
		            		String qualifier = "youtubeId";
		            		String lang = null;
	            			bitstream.addMetadata(schema,element,qualifier,lang,videoID);
	                		bitstream.updateMetadata();
	                		String initialString = bitstream.getID()+";"+videoID;
	    				    InputStream targetStream = new ByteArrayInputStream(initialString.getBytes());
	    					youtubeBundle.createBitstream(targetStream).setName("Mapa bitstream - youtube");
	    					youtubeBundle.update();
	            	}
        			}catch(UploadExeption e){
        				//log.error(e.getMessage()); Se loggea en el adapter
        				if(e.getResumable()) {
        					Curator curator = new Curator();
        					Context ctx = new Context();
        					ctx.turnOffAuthorisationSystem();
        			        ctx.setCurrentUser(EPerson.findByEmail(ctx, "test@test.com"));//Crear y usar el usuario info+video@sedici.unlp.edu.ar
							curator.addTask("VideoUploaderTask").queue(ctx,item.getHandle(),"youtube");
							ctx.complete();
        				}
        				if(e.getNotice()) {
        					System.err.println("Problema que debe ser notificado en el item con handle "+item.getHandle());
        					System.err.println(e.getMessage());
        					e.printStackTrace();
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
		Bitstream[] mapsYoutube = item.getBundles("YOUTUBE")[0].getBitstreams();
		System.out.println(item.getBundles("ORIGINAL").length);
		if(item.getBundles("ORIGINAL").length>0) {
			Bitstream[] bitstreams = item.getBundles("ORIGINAL")[0].getBitstreams();
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
				System.out.println(textBuilder.toString());
				Boolean existe = false;
				for (Bitstream bitstream : bitstreams) {
					System.out.println(mapeo[0]+" "+bitstream.getID());
					
					if(mapeo[0] == Integer.toString(bitstream.getID())) {
						System.out.println("Aca");
						existe = true;
					}
				}
				if (existe == false) {
					String videoId = new YoutubeAdapter().deleteVideo(mapeo[1]);
					item.getBundles("YOUTUBE")[0].removeBitstream(map);
				}
	        	
			}
		}else {
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
				String videoId = new YoutubeAdapter().deleteVideo(mapeo[1]);
				item.getBundles("YOUTUBE")[0].removeBitstream(map);
			}
	        	
		}
		
	}

	@Override
	public void modifyContent(Item item) throws Throwable {
		String handle = item.getHandle();
        log.info("Update del item " + handle +" a YouTube");
        String title= Jsoup.parse(item.getMetadata("dc.title")).text();//Falta determinar que hacer is hay muchos videos, como se construye el titulo
        System.out.println(title);
        String description = this.buildDescription(item);
        System.out.println(description);
        List<String> tags = new ArrayList<String>();
        tags.add("prueba");//Definir que poner en los tags
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
					String videoID = new YoutubeAdapter().updateMetadata(bitstream.getMetadata("sedici.identifier.youtubeId"), title, description, tags);
            		log.info("Se actualizo el video con id "+videoID);
					title= Jsoup.parse(item.getMetadata("dc.title")).text();
        		}
        	}
        }
		
	}
	
	private String buildDescription(Item item) {
	    	String description = Jsoup.parse(item.getMetadata("dc.title")).text()+"\n";//falta derterminar que mas se agrega a la descripcion
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
	        description = description+"Tipo: "+item.getMetadata("sedici.subtype")+"\n";
	        description = description+"Fecha de publicación: "+item.getMetadata("dc.date.available")+"\n";
	        description = description+"Enlace de la fuente: "+item.getMetadata("dc.identifier.uri")+"\n";
	        //Se obtienen las keywords
			List<Metadatum> subjects = item.getMetadata("dc","subject",Item.ANY,Item.ANY,Item.ANY);
			if (subjects.size() > 0){
				description = description+"Palabras clave: ";
				auxNumerico = 0;
				while (auxNumerico<(subjects.size()-1)) {
					description = description+subjects.get(auxNumerico).value+", ";
					auxNumerico = auxNumerico +1;
				}
				description = description+subjects.get(auxNumerico).value+"\n";
			}
			if(item.getMetadata("dc.description.abstract") !=  null){
	        	description = description+"Resumen: "+Jsoup.parse(item.getMetadata("dc.description.abstract")).text()+"\n";
			}
			description = description+"Licencia de uso: "+item.getMetadata("sedici.rights.license")+"\n";
	        return description;
	}

}
