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

import org.apache.commons.io.IOUtils;
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

import ar.edu.unlp.sedici.dspace.uploader.youtube.YoutubeAdapter;
import ar.edu.unlp.sedici.util.MailReporter;

import org.dspace.authorize.AuthorizeException;
import org.dspace.authorize.AuthorizeManager;


@Service
public class VideoUploaderServiceImpl implements ContentUploaderService{

	private static final Logger log = Logger.getLogger(VideoUploaderServiceImpl.class);
	
	private final String MPEG_MIME_TYPE = "video/mpeg";
	private final String QUICKTIME_MIME_TYPE = "video/quicktime";
	private final String MP4_MIME_TYPE = "video/mp4";
	
	private final String YOUTUBE_METADATA = "sedici.identifier.youtubeId";

	
	public VideoUploaderServiceImpl() {
		super();
	}

	@Override
	public void uploadContent(Item item) throws IOException {
		
        String handle = item.getHandle();
        log.info("Starting the upload for the item with handle " + handle +" to YouTube");
        String itemTitle= Jsoup.parse(item.getMetadata("dc.title")).text();
        try {
        	//Create the Bundle if necessary
			if (item.getBundles("YOUTUBE").length==0) {
				item.createBundle("YOUTUBE");
			}
		
	        Bitstream[] bitstreams = item.getBundles("ORIGINAL")[0].getBitstreams();
	
	        for (Bitstream bitstream : bitstreams) {
	        	
	        		if (autorizarSubida(bitstream)) {
	        			String title;
	        			if(bitstream.getMetadata("dc.description").isEmpty()) {
	        				title = itemTitle;
	        				log.warn("The bitstream with ID "+bitstream.getID()+" is going to be uploaded to Youtube, but it has no description, and the title will be "+title);
	        			} else {
	        				title = itemTitle+" - "+bitstream.getMetadata("dc.description");
	        			}
	    				YoutubeAdapter vadapter = new DSpace().getSingletonService(YoutubeAdapter.class);
						String videoID =  vadapter.uploadVideo(bitstream.retrieve(), title, this.buildMetadata(item), this.buildTags(item));
						if(videoID != null) {
		            		log.info("The upload for the bitstream with ID "+bitstream.getID()+",contained in the item with handle "+handle+", finished successfully, uploading the video with Youtube ID "+videoID);
		            		persistirId(videoID,item,bitstream);
						}
	            	}
	        }
        }catch(UploadExeption e){
			//log.error(e.getMessage()); YoutubeAdapter is responsible for the error log in this case
			this.resolveExeption(e,"upload",item);
        } catch (SQLException e) {
        	log.error("SQLException: "+e.getMessage(),e);
			throw new IOException(e);
        } catch (AuthorizeException e) {
        	log.error("AuthorizeException: "+e.getMessage(),e);
			throw new IOException(e);
		}
		
	}
	
	private boolean autorizarSubida(Bitstream bitstream) throws UploadExeption, SQLException{
		Context ctx = new Context();
    	if(AuthorizeManager.authorizeActionBoolean(ctx, bitstream, 0, false)) {
        	ctx.complete();
        	String mimeType = bitstream.getFormat().getMIMEType();
        	if (mimeType.equalsIgnoreCase(MP4_MIME_TYPE) | mimeType.equalsIgnoreCase(MPEG_MIME_TYPE) | mimeType.equalsIgnoreCase(QUICKTIME_MIME_TYPE)) {
				List<Metadatum> replicationId = bitstream.getMetadata("sedici","identifier","youtubeId",Item.ANY,Item.ANY);
        		if (replicationId.size() == 0) {
        			return true;
        		}else {
        			log.info("The bitstream with ID "+bitstream.getID()+" is allready replicated in Youtube "+bitstream.getMetadata("sedici.identifier.youtubeId"));
        		}
        	}
    	} else {
    		ctx.complete();
    		log.info("The bitstream with ID "+bitstream.getID()+" shouldn't be replicated to YouTube due to it not being accessible to anonymous user");
    	}
    	return false;
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
			log.error("SQLException: " + e.getMessage());
		} catch (AuthorizeException e) {
			log.error("AuthorizeException: " + e.getMessage());
		} catch (IOException e) {
			log.error("IOException: " + e.getMessage());
		}
	}

	@Override
	public void removeContent(Item item) throws  IOException {
		try {
			if(item.getBundles("YOUTUBE").length != 0) {
				Bitstream[] mapsYoutube = item.getBundles("YOUTUBE")[0].getBitstreams();
				if(item.getBundles("ORIGINAL").length>0) {
					for (Bitstream map : mapsYoutube) {
						String idVideo = determinarBorradoDeBitstream(map,item);
						if (!idVideo.isEmpty()) {
								YoutubeAdapter vadapter = new DSpace().getSingletonService(YoutubeAdapter.class);
								vadapter.deleteVideo(idVideo);
								item.getBundles("YOUTUBE")[0].removeBitstream(map);
						}
					}
				}else {
					/**
					 * If you remove the last item in the bundle ORIGINAL, it removes the bundle from the item. 
					 * In this case, if the bundle YOUTUBE exists, you have to eliminate every video in this bundle
					 */
					for (Bitstream map : mapsYoutube) {
						String[] mapeo = parsearBitstream(map);
						YoutubeAdapter vadapter = new DSpace().getSingletonService(YoutubeAdapter.class);
						vadapter.deleteVideo(mapeo[1]);
						item.getBundles("YOUTUBE")[0].removeBitstream(map);
					}
			        	
				}
			}
		}catch(UploadExeption e){
			//log.error(e.getMessage()); YoutubeAdapter is responsible for the error log in this case
			this.resolveExeption(e,"delete",item);
		} catch (SQLException e) {
	    	log.error("SQLException: "+e.getMessage(),e);
			throw new IOException(e);
	    } catch (AuthorizeException e) {
	    	log.error("AuthorizeException: "+e.getMessage(),e);
			throw new IOException(e);
		}
		
	}
	
	/**
	 * Transform the relation between bitstream ID and Youtube ID, to a list of Strings
	 * @return String list [0] Bitstream ID [1] Youtube ID 
	 */
	private String[] parsearBitstream(Bitstream bitstream) throws IOException, SQLException, AuthorizeException {
		String data = IOUtils.toString(bitstream.retrieve(), StandardCharsets.UTF_8);
		return data.toString().split(";");
	}
	
	private String determinarBorradoDeBitstream(Bitstream relacionBY, Item item) throws IOException, SQLException, AuthorizeException {
		Bitstream[] bitstreams = item.getBundles("ORIGINAL")[0].getBitstreams();
		String[] listaIDs = parsearBitstream(relacionBY);
		Boolean existe = false;
		for (Bitstream bitstream : bitstreams) {
			//Checks if the bitstream replicated in YouTube has been deleted
			if(listaIDs[0].equals(Integer.toString(bitstream.getID())) ) {
				existe = true;
			}
		}
		if (!existe){
			return listaIDs[1];
		}
		else {
			return null;
		}
	}

	@Override
	public void modifyContent(Item item) throws IOException {
		String handle = item.getHandle();
        log.info("Update of item " + handle +" to YouTube");
        String itemTitle= Jsoup.parse(item.getMetadata("dc.title")).text();
        try {
        	
	        Bitstream[] bitstreams = item.getBundles("ORIGINAL")[0].getBitstreams();
	
	        for (Bitstream bitstream : bitstreams) {
	        	if(autorizarModificacion(bitstream)) {
	        			String title = itemTitle+" - "+bitstream.getMetadata("dc.description");
	        			YoutubeAdapter vadapter = new DSpace().getSingletonService(YoutubeAdapter.class);
	        			String videoID = vadapter.updateMetadata(bitstream.getMetadata(YOUTUBE_METADATA), title, this.buildMetadata(item), this.buildTags(item));
	                	log.info("The video whit ID "+videoID+" and title '"+title+"' has been updated");
	        	}
	        }
        }catch(UploadExeption e){
			//log.error(e.getMessage()); YoutubeAdapter is responsible for the error log in this case
			this.resolveExeption(e,"update",item);
		}catch(SQLException e) {
	    	log.error("SQLException: "+e.getMessage(),e);
			throw new IOException(e);
	    }
		
	}
	
	private boolean autorizarModificacion(Bitstream bitstream) {
		String mimeType = bitstream.getFormat().getMIMEType();
    	if (mimeType.equalsIgnoreCase(MP4_MIME_TYPE) | mimeType.equalsIgnoreCase(MPEG_MIME_TYPE) | mimeType.equalsIgnoreCase(QUICKTIME_MIME_TYPE)) {
    		if (bitstream.getMetadata(YOUTUBE_METADATA) != null) {
    			return true;
    		}
    	}
    	return false;
	}
	
	private Map<String,Object> buildMetadata(Item item) {
				
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
	        	MailReporter.reportUnknownException("Ocurrio un error no reasumible en el "+ contexto +" del item con handle "+item.getHandle(), e, "http://sedici.unlp.edu.ar/handle/"+item.getHandle());
	        }else if (e.getMessage().equals("The daily quota of Youtube has exeded")){
	        	MailReporter.reportUnknownException("Ocurrio un error reasumible en el "+ contexto +" del item con handle "+item.getHandle()+". Se agoto la quota de Youtube", e, "http://sedici.unlp.edu.ar/handle/"+item.getHandle());
	        }else if (e.getMessage().equals("No quota")){
	        	/** 
	        	 * In this case, you don't have to send an email, as the email indicating
	        	 * that the quota limit has already been reached has been sent
	        	 * This case exist to re-queue the curation tasks
	        	 */
	        }else {
	        	MailReporter.reportUnknownException("An unhandled error as ocurred in the "+ contexto +" del item con handle "+item.getHandle(), e, "http://sedici.unlp.edu.ar/handle/"+item.getHandle());
	        }
		} catch (Throwable t) {
			log.error("Error during exeption management in the VideoUploaderService");
			log.error("Throwable: " + t.getMessage());
		}
	}
}
