package ar.edu.unlp.sedici.dspace.curation;

import java.io.IOException;
import java.sql.SQLException;

import org.apache.log4j.Logger;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.DSpaceObject;
import org.dspace.content.Item;
import org.dspace.core.ConfigurationManager;
import org.dspace.core.Context;
import org.dspace.curate.AbstractCurationTask;
import org.dspace.curate.Curator;
import org.dspace.eperson.EPerson;
import org.dspace.utils.DSpace;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import ar.edu.unlp.sedici.dspace.uploader.VideoUploaderServiceImpl;

public class VideoUpdaterTask extends AbstractCurationTask {

	private int status;
	
	private static final Logger log = Logger.getLogger(VideoUpdaterTask.class);
	
	@Override
	public int perform(DSpaceObject dso) throws IOException {
	
		status = Curator.CURATE_SKIP;
		Item item = (Item) dso;
		
		
		VideoUploaderServiceImpl vuploader = new DSpace().getSingletonService(VideoUploaderServiceImpl.class);
		try {
			vuploader.modifyContent(item);
			status = Curator.CURATE_SUCCESS;
		} catch(SQLException e) {
			log.error("SQL error in the update of the item with ID "+item.getID());
			log.error("SQLException: "+e.getMessage(),e);
			throw new IOException(e);
		} catch (IOException e) {
			log.error("IO error in the update of the item with ID "+item.getID());
			log.error("IOException: "+e.getMessage(),e);
			throw e;
		} catch (AuthorizeException e) {
			status = Curator.CURATE_ERROR;
			log.error("Autorization error in the update of the item with ID "+item.getID());
			log.error("AuthorizeException: "+e.getMessage(),e);
			throw new IOException(e);
		}
		
		return status;
	}

}
