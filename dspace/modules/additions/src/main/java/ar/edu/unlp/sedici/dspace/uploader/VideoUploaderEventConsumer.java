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
					System.out.println(evType);
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
					
						if(hdl != null){
							String mimeType;
							for (Bitstream bitstream : bitstreams) {
								mimeType = bitstream.getFormat().getMIMEType();
								if ((mimeType.equalsIgnoreCase(MP4_MIME_TYPE) | mimeType.equalsIgnoreCase(MPEG_MIME_TYPE) | mimeType.equalsIgnoreCase(QUICKTIME_MIME_TYPE))&&(bitstream.getMetadata("sedici.identifier.youtubeId") == null)) {
									Curator curator = new Curator();
									curator.addTask("VideoUploaderTask").queue(ctx, hdl,"upload");
									break;
								}
							}
						}
					}
				}
			case MODIFY_METADATA:
				if(((Item) event.getSubject(ctx)).getHandle() != null){
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
								if ((mimeType.equalsIgnoreCase(MP4_MIME_TYPE) | mimeType.equalsIgnoreCase(MPEG_MIME_TYPE) | mimeType.equalsIgnoreCase(QUICKTIME_MIME_TYPE))) {
									
									Curator curator = new Curator();
									curator.addTask("VideoUpdaterTask").queue(ctx,item.getHandle(),"update");
									break;
								}
							}
						}
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
