package ar.edu.unlp.sedici.dspace.uploader;

import java.sql.SQLException;

import org.apache.log4j.Logger;
import org.dspace.content.Bitstream;
import org.dspace.content.DSpaceObject;
import org.dspace.content.Item;
import org.dspace.core.Context;
import org.dspace.handle.HandleManager;
import org.springframework.stereotype.Service;

@Service
public class VideoUploaderServiceImpl implements ContentUploaderService{

	private static final Logger log = Logger.getLogger(VideoUploaderServiceImpl.class);

	@Override
	public void uploadContent(Context context, Item item, Bitstream bitstream) throws SQLException {
        String handle = item.getHandle();

        if (handle == null)
        {
            handle = HandleManager.findHandle(context, item);
        }

		log.info("Upload de " + handle + " a YouTube");
		
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
