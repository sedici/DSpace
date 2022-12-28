package ar.edu.unlp.sedici.dspace.uploader;

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.dspace.content.DSpaceObject;
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
        if (objectsToUpdate == null) {
            objectsToUpdate = new HashSet<DSpaceObject>();
        }
        
        int st = event.getSubjectType();
        if (!(st == Constants.ITEM || st == Constants.BUNDLE)) {
            log.warn("VideoUploaderConsumer should not have been given this kind of Subject in an event, skipping: " + event.toString());
            return;
        }
        
        DSpaceObject subject = event.getSubject(ctx);

        DSpaceObject object = event.getObject(ctx);

        

		
	}

	@Override
	public void end(Context ctx) throws Exception {
        if (objectsToUpdate != null && handlesToDelete != null) {

            // update the changed Items not deleted because they were on create list
            for (DSpaceObject iu : objectsToUpdate) {
                /* we let all types through here and 
                 * allow the search DSIndexer to make 
                 * decisions on indexing and/or removal
                 */
                String hdl = iu.getHandle();
                if (hdl != null && !handlesToDelete.contains(hdl)) {
                    try {
                        uploader.uploadContent(ctx, iu);
                        log.debug("Indexed "
                                + Constants.typeText[iu.getType()]
                                + ", id=" + String.valueOf(iu.getID())
                                + ", handle=" + hdl);
                    }
                    catch (Exception e) {
                        log.error("Failed while indexing object: ", e);
                    }
                }
            }

            for (String hdl : handlesToDelete) {
                try {
                    uploader.removeContent(ctx, hdl);
                    if (log.isDebugEnabled())
                    {
                        log.debug("UN-Indexed Item, handle=" + hdl);
                    }
                }
                catch (Exception e) {
                    log.error("Failed while UN-indexing object: " + hdl, e);
                }

            }

        }

        // "free" the resources
        objectsToUpdate = null;
	}

	@Override
	public void finish(Context ctx) throws Exception {
		// TODO Auto-generated method stub
		
	}

}
