package ar.edu.unlp.sedici.dspace.uploader;

import java.sql.SQLException;
import java.lang.Throwable;

import org.dspace.content.Bitstream;
import org.dspace.content.DSpaceObject;
import org.dspace.content.Item;
import org.dspace.core.Context;

public interface ContentUploaderService {

    void uploadContent(Item item) throws Throwable;   
    void removeContent(Context context, String hdl);
    void modifyContent(Context context, String hdl);
	
}

