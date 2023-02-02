package ar.edu.unlp.sedici.dspace.uploader;

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.dspace.content.Bitstream;
import org.dspace.content.Bundle;
import org.dspace.content.DSpaceObject;
import org.dspace.content.Item;
import org.dspace.content.BitstreamFormat;
import org.dspace.core.Constants;
import org.dspace.core.Context;
import ar.edu.unlp.sedici.dspace.uploader.ContentUploaderService;
import org.dspace.event.Consumer;
import org.dspace.event.Event;
import org.dspace.utils.DSpace;

public class VideoUploaderEventConsumer implements Consumer {
	
    /**
     * log4j logger
     */
    private static Logger log = Logger.getLogger(VideoUploaderEventConsumer.class);

	private final String MPEG_MIME_TYPE = "video/mpeg";
	private final String QUICKTIME_MIME_TYPE = "video/quicktime";
	private final String MP4_MIME_TYPE = "video/mp4";

    // collect Items, Collections, Communities that need indexing
    private Set<DSpaceObject> objectsToUpdate = null;

    // handles to delete since IDs are not useful by now.
    private Set<String> handlesToDelete = null;

    DSpace dspace = new DSpace();

    ContentUploaderService uploader = dspace.getServiceManager().getServiceByName(ContentUploaderService.class.getName(),ContentUploaderService.class);

	@Override
	public void initialize() throws Exception {
		
	}

	@Override
	public void consume(Context ctx, Event event) throws Exception {
        int st = event.getSubjectType();
        if (!(st == Constants.ITEM || st == Constants.BUNDLE)) {
            log.warn("VideoUploaderConsumer should not have been given this kind of Subject in an event, skipping: " + event.toString());
            return;
        }
               
        Item item = (Item) event.getSubject(ctx);
        
        Bundle[] bundles = item.getBundles("ORIGINAL");
        
        Bitstream[] bitstreams = bundles[0].getBitstreams();
        BitstreamFormat bitstreamFormat;
        String mimeType;
        for (Bitstream bitstream : bitstreams) {
        	mimeType = bitstream.getFormat().getMIMEType();
        	if (mimeType.equalsIgnoreCase(MP4_MIME_TYPE) | mimeType.equalsIgnoreCase(MPEG_MIME_TYPE) | mimeType.equalsIgnoreCase(QUICKTIME_MIME_TYPE)) {
        		uploader.uploadContent(ctx, item, bitstream);
        	}
        }
	}

	@Override
	public void end(Context ctx) throws Exception {

	}

	@Override
	public void finish(Context ctx) throws Exception {
		// TODO Auto-generated method stub
		
	}

}
