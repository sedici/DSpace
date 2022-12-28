package ar.edu.unlp.sedici.dspace.uploader;

import java.sql.SQLException;

import org.dspace.content.DSpaceObject;
import org.dspace.core.Context;

public interface ContentUploaderService {

    void uploadContent(Context context, DSpaceObject dso) throws SQLException;   
    void removeContent(Context context, String hdl);
    void modifyContent(Context context, String hdl);
	
}

