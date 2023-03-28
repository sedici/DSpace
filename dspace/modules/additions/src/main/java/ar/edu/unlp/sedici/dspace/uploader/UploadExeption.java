package ar.edu.unlp.sedici.dspace.uploader;

import java.io.IOException;

public class UploadExeption extends IOException {

	private Boolean resumable = false;

    public UploadExeption(String message, Boolean resumable) {
        super(message);
        this.resumable = resumable;
    }

    public UploadExeption(String message,Boolean resumable, Throwable t) {
        super(message, t);
        this.resumable = resumable;
    }
    
    public Boolean isResumable() {
    	return resumable;
    }


}