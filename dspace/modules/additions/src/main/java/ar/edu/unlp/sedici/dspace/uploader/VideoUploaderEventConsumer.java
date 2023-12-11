package ar.edu.unlp.sedici.dspace.uploader;

import static org.dspace.event.Event.ADD;
import static org.dspace.event.Event.CREATE;
import static org.dspace.event.Event.DELETE;
import static org.dspace.event.Event.INSTALL;
import static org.dspace.event.Event.MODIFY;
import static org.dspace.event.Event.MODIFY_METADATA;
import static org.dspace.event.Event.REMOVE;

import java.io.IOException;
import java.sql.SQLException;

import org.dspace.core.Constants;


import org.apache.log4j.Logger;
import org.dspace.content.Bitstream;
import org.dspace.content.Bundle;
import org.dspace.content.Item;
import org.dspace.core.Constants;
import org.dspace.core.Context;
import org.dspace.event.Consumer;
import org.dspace.event.Event;
import org.dspace.utils.DSpace;
import org.dspace.authorize.AuthorizeManager;


import com.google.api.services.youtube.YouTube;

import org.dspace.curate.Curator;

// Consumer encargado de detectar los eventos de DSpace y si afectan a un item con videos a replicar o ya replicados en Youtube.
// Upload = ADD de coleccion sobre item(caso de original), o ADD de item sobre bundle
// Update = MODIFY_METADATA sobre item
// Delete = REMOVE de bitstream sobre bundle o de bundle sobre item(caso de REMOVE del ultimo bitstream del bundle ORIGINAL)

public class VideoUploaderEventConsumer implements Consumer {
	
    /**
     * log4j logger
     */
    private static Logger log = Logger.getLogger(VideoUploaderEventConsumer.class);

	private final String MPEG_MIME_TYPE = "video/mpeg";
	private final String QUICKTIME_MIME_TYPE = "video/quicktime";
	private final String MP4_MIME_TYPE = "video/mp4";
	private final String QUEUE = "youtube";

    DSpace dspace = new DSpace();

    ContentUploaderService uploader = dspace.getServiceManager().getServiceByName(ContentUploaderService.class.getName(),ContentUploaderService.class);

	@Override
	public void initialize() throws Exception {
		
	}

	@Override
	public void consume(Context ctx, Event event) {
		int evType = event.getEventType();
        int st = event.getSubjectType();
        if (!(st == Constants.ITEM || st == Constants.BUNDLE || st == Constants.COLLECTION )) {
            log.warn("VideoUploaderConsumer should not have been given this kind of Subject in an event, skipping: " + event.toString());
            return;
        }
            
		try {
			switch (evType){
				case ADD:
					if(st==Constants.COLLECTION){
						Item item = (Item) event.getObject(ctx);
						Bundle[] bundles = item.getBundles("ORIGINAL");
						
						Bitstream[] bitstreams = bundles[0].getBitstreams();
						
	
						if((item.getHandle() != null)){
							String mimeType;
							for (Bitstream bitstream : bitstreams) {
								mimeType = bitstream.getFormat().getMIMEType();
								if ((mimeType.equalsIgnoreCase(MP4_MIME_TYPE) | mimeType.equalsIgnoreCase(MPEG_MIME_TYPE) | mimeType.equalsIgnoreCase(QUICKTIME_MIME_TYPE))) {
									Context contexto = new Context();
						        	if(AuthorizeManager.authorizeActionBoolean(contexto, bitstream, 0, false)) {
						        		Curator curator = new Curator();
										curator.addTask("VideoUploaderTask").queue(ctx,item.getHandle(),QUEUE);
										break;
						    		}else {
						    			log.info("El bitstream con id "+bitstream.getID()+" no esta autorizado para subirse");
						    		}
								}
		
							}
						}
	
					}else{
						if(st==Constants.BUNDLE && ((Bundle) event.getSubject(ctx)).getName().equals("ORIGINAL")){
							Bundle bundle = (Bundle) event.getSubject(ctx);
							Item[] items = bundle.getItems();
							
							String hdl = items[0].getHandle();
							Bitstream[] bitstreams = bundle.getBitstreams();
						
							//Se comprueba que este publicado en dspace.
							if(hdl != null){
								//Se comprueba si el item original todavia no fue subido a youtube ya que en ese caso no habria que encolar. Analizar casos pre actualizacion
								if(items[0].getBundles("YOUTUBE").length != 0){
									String mimeType;
									for (Bitstream bitstream : bitstreams) {
										mimeType = bitstream.getFormat().getMIMEType();
										//Se comprueba que el bitstream sea un video y que no este subido en youtube para asi subirlo. 
										if ((mimeType.equalsIgnoreCase(MP4_MIME_TYPE) | mimeType.equalsIgnoreCase(MPEG_MIME_TYPE) | mimeType.equalsIgnoreCase(QUICKTIME_MIME_TYPE))&&(bitstream.getMetadata("sedici.identifier.youtubeId") == null)) {
											Context contexto = new Context();
								        	if(AuthorizeManager.authorizeActionBoolean(contexto, bitstream, 0, false)) {
								        		Curator curator = new Curator();
												curator.addTask("VideoUploaderTask").queue(ctx,hdl,QUEUE);
												break;
								    		}else {
								    			log.info("El bitstream con id "+bitstream.getID()+" no esta autorizado para subirse");
								    		}
										}
									}
								}
							}
						}
					}
					break;
				case MODIFY_METADATA:
					if(((Item) event.getSubject(ctx)).getHandle() != null){
						if(shouldUpdateMetadata(event)){ 
							if(st==Constants.ITEM){
								Item item = (Item) event.getSubject(ctx);
								Bundle[] bundles = item.getBundles("ORIGINAL");
								Bitstream[] bitstreams = bundles[0].getBitstreams();
								String mimeType;
								for (Bitstream bitstream : bitstreams) {
									mimeType = bitstream.getFormat().getMIMEType();
									//Se comprueba que el bitstream sea un video y que este publicado en youtube.
									if ((mimeType.equalsIgnoreCase(MP4_MIME_TYPE) | mimeType.equalsIgnoreCase(MPEG_MIME_TYPE) | mimeType.equalsIgnoreCase(QUICKTIME_MIME_TYPE))&&(bitstream.getMetadata("sedici.identifier.youtubeId") != null)) {
										Curator curator = new Curator();
										curator.addTask("VideoUpdaterTask").queue(ctx,item.getHandle(),QUEUE);
										break;
									}
								}					
							}
						}
					}
					break;
				case REMOVE:
					if(st==Constants.BUNDLE ){
						Bundle bundle = (Bundle) event.getSubject(ctx);
						//Se comprueba que se el caso factible ya que si se borra el ultimo bitstream de un bundle este se elimina automaticamente
						if(bundle != null) {
							Item item = (Item) bundle.getParentObject(); 
							if(item.getBundles("YOUTUBE").length > 0){
								Curator curator = new Curator();
								curator.addTask("VideoDeleteTask").queue(ctx,item.getHandle(),QUEUE);
							} 
						}
					}else if (st==Constants.ITEM) {
						Item item = (Item) event.getSubject(ctx);
						if(item.getBundles("YOUTUBE").length > 0){
							Curator curator = new Curator();
							curator.addTask("VideoDeleteTask").queue(ctx,item.getHandle(),QUEUE);
						} 
					}
					break;
	
			}
		}catch(SQLException e){
			log.error("Error de SQL en el event consumer");
			log.error("SQLException: "+e.getMessage(),e);
		} catch (IOException e) {
			log.error("Error de lectura/escritura en el event consumer");
			log.error("IOException: "+e.getMessage(),e);
		}
		
		//}
	}

	@Override
	public void end(Context ctx) throws Exception {

	}

	@Override
	public void finish(Context ctx) throws Exception {
		
	}
	
	// Se comprueba que dado un evento, se modifique algun metadato relevante para 
	// la construccion del video a replicar
	private boolean shouldUpdateMetadata(Event event) {
		return ((event.getDetail().contains("dc.title")) || (event.getDetail().contains("dc.description.abstract"))
						|| (event.getDetail().contains("sedici.creator.person")) || (event.getDetail().contains("sedici.subtype"))
						|| (event.getDetail().contains("dc.date.available")) || (event.getDetail().contains("dc.identifier.uri")) 
						|| (event.getDetail().contains("sedici.rights.license")) || (event.getDetail().contains("dc.subject")) 
		);
	}

}
