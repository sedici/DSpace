package ar.edu.unlp.sedici.dspace.uploader;

import java.sql.SQLException;

import org.apache.log4j.Logger;
import org.dspace.content.DSpaceObject;
import org.dspace.core.Context;
import org.dspace.handle.HandleManager;
import org.springframework.stereotype.Service;

@Service
public class VideoUploaderServiceImpl implements ContentUploaderService{

	private static final Logger log = Logger.getLogger(VideoUploaderServiceImpl.class);

	@Override
	public void uploadContent(Context context, DSpaceObject dso) throws SQLException {
        String handle = dso.getHandle();

        if (handle == null)
        {
            handle = HandleManager.findHandle(context, dso);
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
