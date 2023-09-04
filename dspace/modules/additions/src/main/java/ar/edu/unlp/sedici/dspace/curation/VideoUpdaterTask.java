package ar.edu.unlp.sedici.dspace.curation;

import java.io.IOException;

import org.dspace.content.DSpaceObject;
import org.dspace.content.Item;
import org.dspace.curate.AbstractCurationTask;
import org.dspace.curate.Curator;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import ar.edu.unlp.sedici.dspace.uploader.VideoUploaderServiceImpl;

public class VideoUpdaterTask extends AbstractCurationTask {

	private int status;
	@Override
	public int perform(DSpaceObject dso) throws IOException {
	
		status = Curator.CURATE_SKIP;
		Item item = (Item) dso;
		
		try {
			AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext();
			applicationContext.scan("ar.edu.unlp.sedici.dspace.uploader");

			VideoUploaderServiceImpl vuploader = applicationContext.getBean(VideoUploaderServiceImpl.class);
			//VideoUploaderServiceImpl vuploader= new VideoUploaderServiceImpl();
			vuploader.modifyContent(item);
			status = Curator.CURATE_SUCCESS;
		}
		catch(Throwable t) {
		  
	      System.err.println("Throwable: " + t.getMessage());
	      t.printStackTrace();
		}
		
		return status;
	}

}
