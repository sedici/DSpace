package ar.edu.unlp.sedici.dspace.uploader;

import java.io.IOException;

public class UploadExeption extends IOException {

	private Boolean resumable = false;
	private Boolean notice = false;

    public UploadExeption(String message) {
        super(message);
    }

    public UploadExeption(Throwable t) {
        super(t);
    }

    public UploadExeption(String message, Throwable t) {
        super(message, t);
    }
    
    public UploadExeption resumable() {
    	resumable = true;
    	return this;
    }
    
    public UploadExeption notice() {
    	notice = true;
    	return this;
    }
    
	public Boolean getResumable() {
		return resumable;
	}

	public Boolean getNotice() {
		return notice;
	}
}