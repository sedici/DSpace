package ar.edu.unlp.sedici.dspace.curation;

import java.io.IOException;
import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.dspace.content.DSpaceObject;
import org.dspace.content.Item;
import org.dspace.content.Metadatum;
import org.dspace.content.authority.Choices;
import org.dspace.core.Constants;
import org.dspace.curate.AbstractCurationTask;
import org.dspace.curate.Curator;


import ar.edu.unlp.sedici.dspace.uploader.VideoUploaderServiceImpl;

public class VideoUploaderTask extends AbstractCurationTask {

	private int status;
	@Override
	public int perform(DSpaceObject dso) throws IOException {
	
		status = Curator.CURATE_SKIP;
		Item item = (Item) dso;
		
		try {
			VideoUploaderServiceImpl vuploader= new VideoUploaderServiceImpl();
			vuploader.uploadContent(item);
			status = Curator.CURATE_SUCCESS;
		}
		catch(Throwable t) {
		  
	      System.err.println("Throwable: " + t.getMessage());
	      t.printStackTrace();
		}
		
		return status;
	}

}
