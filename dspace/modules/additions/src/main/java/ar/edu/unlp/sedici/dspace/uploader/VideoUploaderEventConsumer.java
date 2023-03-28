package ar.edu.unlp.sedici.dspace.uploader;

import static org.dspace.event.Event.ADD;
import static org.dspace.event.Event.CREATE;
import static org.dspace.event.Event.DELETE;
import static org.dspace.event.Event.INSTALL;
import static org.dspace.event.Event.MODIFY;
import static org.dspace.event.Event.MODIFY_METADATA;
import static org.dspace.event.Event.REMOVE;

import org.apache.log4j.Logger;
import org.dspace.content.Bitstream;
import org.dspace.content.Bundle;
import org.dspace.content.Item;
import org.dspace.core.Constants;
import org.dspace.core.Context;
import org.dspace.event.Consumer;
import org.dspace.event.Event;
import org.dspace.utils.DSpace;

import com.google.api.services.youtube.YouTube;

import org.dspace.curate.Curator;

public class VideoUploaderEventConsumer implements Consumer {
	
    /**
     * log4j logger
     */
    private static Logger log = Logger.getLogger(VideoUploaderEventConsumer.class);

	private final String MPEG_MIME_TYPE = "video/mpeg";
	private final String QUICKTIME_MIME_TYPE = "video/quicktime";
	private final String MP4_MIME_TYPE = "video/mp4";

    DSpace dspace = new DSpace();

    ContentUploaderService uploader = dspace.getServiceManager().getServiceByName(ContentUploaderService.class.getName(),ContentUploaderService.class);

	@Override
	public void initialize() throws Exception {
		
	}

	@Override
	public void consume(Context ctx, Event event) throws Exception {
		int evType = event.getEventType();
        int st = event.getSubjectType();
        if (!(st == Constants.ITEM || st == Constants.BUNDLE || st == Constants.COLLECTION )) {
            log.warn("VideoUploaderConsumer should not have been given this kind of Subject in an event, skipping: " + event.toString());
            return;
        }
            
		log.info(event.toString());
		switch (evType){
			case ADD:
				
				
				if(st==Constants.COLLECTION){
					
					Item item = (Item) event.getObject(ctx);
					Bundle[] bundles = item.getBundles("ORIGINAL");
					
					Bitstream[] bitstreams = bundles[0].getBitstreams();
					

					if(item.getHandle() != null){
						String mimeType;
						for (Bitstream bitstream : bitstreams) {
							mimeType = bitstream.getFormat().getMIMEType();
							if ((mimeType.equalsIgnoreCase(MP4_MIME_TYPE) | mimeType.equalsIgnoreCase(MPEG_MIME_TYPE) | mimeType.equalsIgnoreCase(QUICKTIME_MIME_TYPE))) {
								Curator curator = new Curator();
								curator.addTask("VideoUploaderTask").queue(ctx,item.getHandle(),"upload");
								break;
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
							//Se comprueba si el item original todavia no fue subido a youtube ya que en ese caso no habria que encolar.
							if(items[0].getBundles("YOUTUBE").length != 0){
								String mimeType;
								for (Bitstream bitstream : bitstreams) {
									mimeType = bitstream.getFormat().getMIMEType();
									//Se comprueba que el bitstream sea un video y que no este subido en youtube para asi subirlo. 
									if ((mimeType.equalsIgnoreCase(MP4_MIME_TYPE) | mimeType.equalsIgnoreCase(MPEG_MIME_TYPE) | mimeType.equalsIgnoreCase(QUICKTIME_MIME_TYPE))&&(bitstream.getMetadata("sedici.identifier.youtubeId") == null)) {
										Curator curator = new Curator();
										curator.addTask("VideoUploaderTask").queue(ctx, hdl,"upload");
										break;
									}
								}
							}
						}
					}
				}
			case MODIFY_METADATA:
				if(((Item) event.getSubject(ctx)).getHandle() != null){
					//Se comprueba que se modifique un metadato relacionado con los videos, sirve solo en el caso de las importaciones.
					if( (event.getDetail().contains("dc.title")) || (event.getDetail().contains("dc.description.abstract"))
						|| (event.getDetail().contains("sedici.creator.person")) || (event.getDetail().contains("sedici.subtype"))
						|| (event.getDetail().contains("dc.date.available")) || (event.getDetail().contains("dc.identifier.uri")) 
						|| (event.getDetail().contains("sedici.rights.license")) || (event.getDetail().contains("dc.subject")) ){ 
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
									curator.addTask("VideoUpdaterTask").queue(ctx,item.getHandle(),"update");
									break;
								}
							}
						}
					}
				}
				
			case REMOVE:
				if(st==Constants.BUNDLE ){
					Bundle bundle = (Bundle) event.getSubject(ctx);
					//Se comprueba que se el caso factible ya que si se borra el ultimo bitstream de un bundle este se elimina automaticamente
					if(bundle != null) {
						Item item = (Item) bundle.getParentObject(); 
						if(item.getBundles("YOUTUBE").length > 0){
							Curator curator = new Curator();
							curator.addTask("VideoDeleteTask").queue(ctx,item.getHandle(),"delete");
						} 
					}
				}
				if (st==Constants.ITEM) {
					Item item = (Item) event.getSubject(ctx);
					if(item.getBundles("YOUTUBE").length > 0){
						Curator curator = new Curator();
						curator.addTask("VideoDeleteTask").queue(ctx,item.getHandle(),"delete");
					} 
				}

			}
			
		//}
	}

	@Override
	public void end(Context ctx) throws Exception {

	}

	@Override
	public void finish(Context ctx) throws Exception {
		
	}

}
