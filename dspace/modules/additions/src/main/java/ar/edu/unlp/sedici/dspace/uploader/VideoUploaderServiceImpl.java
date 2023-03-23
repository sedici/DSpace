package ar.edu.unlp.sedici.dspace.uploader;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
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
        String title= Jsoup.parse(item.getMetadata("dc.title")).text();
        
        Map<String, Object> metadata;
        metadata= this.buildMetadata(item);
        
        List<String> tags = new ArrayList<String>();
        tags.add("prueba");
        
        //Definir que poner en los tags
        
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
					String videoID = new YoutubeAdapter().uploadVideo(bitstream.retrieve(), title, metadata, tags);
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
    					youtubeBundle.createBitstream(targetStream).setName("Mapa bitstream - youtube");;
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
					
					if(mapeo[0].equals(Integer.toString(bitstream.getID())) ) {
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
        Map<String, Object> metadata;
        metadata = this.buildMetadata(item);
        
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
					String videoID = new YoutubeAdapter().updateMetadata(bitstream.getMetadata("sedici.identifier.youtubeId"), title, metadata, tags);
            		log.info("Se actualizo el video con id "+videoID);
					title= Jsoup.parse(item.getMetadata("dc.title")).text();
        		}
        	}
        }
		
	}
	
	private Map<String,Object> buildMetadata(Item item) {
		
			//falta derterminar si hay mas metadatos que agregar
		
		 	Map<String, Object> metadata = new HashMap<String, Object>();
		 	
		 	metadata.put("title", Jsoup.parse(item.getMetadata("dc.title")).text());
	        metadata.put("creators", item.getMetadata("sedici","creator","person",Item.ANY,Item.ANY));
	        metadata.put("subtype", item.getMetadata("sedici.subtype"));
	        metadata.put("dateAvailable", item.getMetadata("dc.date.available"));
	        metadata.put("iUri", item.getMetadata("dc.identifier.uri"));
	        metadata.put("subjects", item.getMetadata("dc","subject",Item.ANY,Item.ANY,Item.ANY));
			if(item.getMetadata("dc.description.abstract") !=  null){
				metadata.put("abstract", Jsoup.parse(item.getMetadata("dc.description.abstract")).text());
	
			}
			   metadata.put("license", item.getMetadata("sedici.rights.license"));
	        return metadata;
	}

}
