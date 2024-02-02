package ar.edu.unlp.sedici.dspace.uploader.adapter;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import ar.edu.unlp.sedici.dspace.uploader.UploadExeption;

public interface Adapter {
	
	public String uploadVideo(InputStream videoFile, final String title, Map <String, Object> metadata, List<String> tags) throws UploadExeption;
	
	public String updateMetadata(String videoId, String title, Map<String,Object> metadata, List<String> tags) throws UploadExeption;
	
	public String deleteVideo(String videoId) throws UploadExeption;

}
