package ar.edu.unlp.sedici.dspace.uploader;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.io.File;
import java.lang.Throwable;
import org.apache.commons.io.*;

import org.apache.log4j.Logger;
import org.dspace.content.Bitstream;
import org.dspace.content.DSpaceObject;
import org.dspace.content.Item;
import org.dspace.core.Context;
import org.dspace.curate.Curator;
import org.dspace.handle.HandleManager;
import org.springframework.stereotype.Service;
import ar.edu.unlp.sedici.dspace.google.YoutubeAdapter;

@Service
public class VideoUploaderServiceImpl implements ContentUploaderService{

	private static final Logger log = Logger.getLogger(VideoUploaderServiceImpl.class);
	
	private final String MPEG_MIME_TYPE = "video/mpeg";
	private final String QUICKTIME_MIME_TYPE = "video/quicktime";
	private final String MP4_MIME_TYPE = "video/mp4";

	@Override
	public void uploadContent(Item item) throws Throwable {
        String handle = item.getHandle();
        String title= item.getMetadata("dc.title");//Falta determinar que hacer is hay muchos videos, como se construye el titulo
        String description = item.getMetadata("dc. description. abstract");//falta derterminar que mas se agrega a la descripcion
        List<String> tags = new ArrayList<String>(); //Definir que poner en los tags
        Bitstream[] bitstreams = item.getBundles("ORIGINAL")[0].getBitstreams();
        for (Bitstream bitstream : bitstreams) {
        	String mimeType = bitstream.getFormat().getMIMEType();
        	if (mimeType.equalsIgnoreCase(MP4_MIME_TYPE) | mimeType.equalsIgnoreCase(MPEG_MIME_TYPE) | mimeType.equalsIgnoreCase(QUICKTIME_MIME_TYPE)) {
        		String videoID = new YoutubeAdapter().uploadVideo(bitstream.retrieve(), title, description, tags);
        		log.info("Se subio el video con id "+videoID);
        	}
        }
        
		log.info("Upload de " + handle +" a YouTube del item");
		
	}

	@Override
	public void removeContent(Context context, String hdl) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void modifyContent(Context context, String hdl) {
		// TODO Auto-generated method stub
		
	}

}
