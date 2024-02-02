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

import org.dspace.core.ConfigurationManager;
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

/**
 *  Event listener that filters events triggered by items with video bitstreams.
 *  Upload = ADD item to collection(publish the item), or ADD bitstream to bundle(allready published).
 *  Update = MODIFY_METADATA of item.
 *  Delete = REMOVE bitstream of bundle or bundle of item(last bitstream of the bundle)
*/
public class VideoUploaderEventConsumer implements Consumer {
	
    /**
     * log4j logger
     */
    private static Logger log = Logger.getLogger(VideoUploaderEventConsumer.class);

	private final String MPEG_MIME_TYPE = "video/mpeg";
	private final String QUICKTIME_MIME_TYPE = "video/quicktime";
	private final String MP4_MIME_TYPE = "video/mp4";
	private final String QUEUE = "replicateVideo";

	private final String REPLICATION_BUNDLE = "REPLICATION";
	private final String REPLICATION_METADATA = ConfigurationManager.getProperty("upload","video.identifier.metadata");

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
						        	if(AuthorizeManager.authorizeActionBoolean(ctx, bitstream, 0, false)) {
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
						
							//Checks if published.
							if(hdl != null){
								String mimeType;
								for (Bitstream bitstream : bitstreams) {
									mimeType = bitstream.getFormat().getMIMEType();
									//Checks that the bitstream is a video and that it is not uploaded on an external service in order to upload it. 
									if ((mimeType.equalsIgnoreCase(MP4_MIME_TYPE) | mimeType.equalsIgnoreCase(MPEG_MIME_TYPE) | mimeType.equalsIgnoreCase(QUICKTIME_MIME_TYPE))&&(bitstream.getMetadata(REPLICATION_METADATA) == null)) {
								        if(AuthorizeManager.authorizeActionBoolean(ctx, bitstream, 0, false)) {
								        	Curator curator = new Curator();
											curator.addTask("VideoUploaderTask").queue(ctx,hdl,QUEUE);
											break;
								    	}else {
								    		log.info("The bitstream with ID "+bitstream.getID()+" is not autorized to upload");
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
									//Checks if the bitstream is a video and is published on an external service.
									if ((mimeType.equalsIgnoreCase(MP4_MIME_TYPE) | mimeType.equalsIgnoreCase(MPEG_MIME_TYPE) | mimeType.equalsIgnoreCase(QUICKTIME_MIME_TYPE))&&(bitstream.getMetadata(REPLICATION_METADATA) != null)) {
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
						//If the bitstream eliminated is not the last one of the bundle, you can get the bundle
						if(bundle != null) {
							Item item = (Item) bundle.getParentObject(); 
							if(item.getBundles(REPLICATION_BUNDLE).length > 0){
								Curator curator = new Curator();
								curator.addTask("VideoDeleteTask").queue(ctx,item.getHandle(),QUEUE);
							} 
						}
					//If the bitstream eliminated is the last one of the bundle, you want the event that deletes the bundle from the item
					}else if (st==Constants.ITEM) {
						Item item = (Item) event.getSubject(ctx);
						if(item.getBundles(REPLICATION_BUNDLE).length > 0){
							Curator curator = new Curator();
							curator.addTask("VideoDeleteTask").queue(ctx,item.getHandle(),QUEUE);
						} 
					}
					break;
	
			}
		}catch(SQLException e){
			log.error("SQLException: "+e.getMessage(),e);
		} catch (IOException e) {
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
	
	/*
	 * Determines if at least one of the metadata used to build the description has been modified
	 */
	private boolean shouldUpdateMetadata(Event event) {
		return ((event.getDetail().contains("dc.title")) || (event.getDetail().contains("dc.description.abstract"))
						|| (event.getDetail().contains("sedici.creator.person")) || (event.getDetail().contains("sedici.subtype"))
						|| (event.getDetail().contains("dc.date.available")) || (event.getDetail().contains("dc.identifier.uri")) 
						|| (event.getDetail().contains("sedici.rights.license")) || (event.getDetail().contains("dc.subject")) 
		);
	}

}
