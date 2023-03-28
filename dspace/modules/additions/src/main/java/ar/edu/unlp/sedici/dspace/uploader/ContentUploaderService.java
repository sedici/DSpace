package ar.edu.unlp.sedici.dspace.uploader;

import java.lang.Throwable;
import org.dspace.content.Item;


public interface ContentUploaderService {

    void uploadContent(Item item) throws Throwable;   
    void removeContent(Item item) throws Throwable;  
    void modifyContent(Item item) throws Throwable;  
	
}

