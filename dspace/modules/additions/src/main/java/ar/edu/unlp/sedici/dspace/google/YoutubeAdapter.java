/*
 * Copyright (c) 2012 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package ar.edu.unlp.sedici.dspace.google;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.json.JSONObject;

import com.google.api.client.auth.oauth2.AuthorizationCodeRequestUrl;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.auth.oauth2.TokenResponseException;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.java6.auth.oauth2.FileCredentialStore;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.googleapis.media.MediaHttpUploader;
import com.google.api.client.googleapis.media.MediaHttpUploaderProgressListener;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.InputStreamContent;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.YouTube.VideoCategories;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoCategory;
import com.google.api.services.youtube.model.VideoListResponse;
import com.google.api.services.youtube.model.VideoSnippet;
import com.google.api.services.youtube.model.VideoStatus;
import com.google.common.collect.Lists;

import ar.edu.unlp.sedici.dspace.uploader.UploadExeption;

import org.dspace.content.Metadatum;
import org.dspace.core.ConfigurationManager;
import org.springframework.stereotype.Service;

@Service
public class YoutubeAdapter {
	
	public YoutubeAdapter() {
		super();
	}

	static final Logger logger = Logger.getLogger(YoutubeAdapter.class);

	/** Global instance of the HTTP transport. */
	private final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();

	/** Global instance of the JSON factory. */
	private final JsonFactory JSON_FACTORY = new JacksonFactory();

	/** Global instance of Youtube object to make all API requests. */
	private YouTube youtube;

	/*
	 * Global instance of the format used for the video being uploaded (MIME type).
	 */
	private String VIDEO_FILE_FORMAT = "video/*";
	
	private Credential CREDENTIAL;

	/**
	 * Authorizes the installed application to access user's protected data.
	 *
	 * @param scopes list of scopes needed to run youtube upload.
	 */
	private void authorize(List<String> scopes) throws Exception {

		// Load client secrets.
		Reader reader = new InputStreamReader(new FileInputStream(new File(ConfigurationManager.getProperty("youtube.upload","youtube.upload.secrets"))));
		GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, reader);

		// Set up file credential store.
		FileCredentialStore credentialStore = new FileCredentialStore(
				new File(ConfigurationManager.getProperty("youtube.upload","youtube.upload.refresh")), JSON_FACTORY);

		// Set up authorization code flow.
		GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, JSON_FACTORY,
				clientSecrets, scopes).setCredentialStore(credentialStore).setAccessType("offline").build();
		
		this.CREDENTIAL = flow.loadCredential(clientSecrets.getDetails().getClientId());
	    if (CREDENTIAL != null
	          && (CREDENTIAL.getRefreshToken() != null
	              || CREDENTIAL.getExpiresInSeconds() == null
	              || CREDENTIAL.getExpiresInSeconds() > 60)) {
	        return;
	    }
		
	    // open in browser
	    String redirectUri = ConfigurationManager.getProperty("youtube.upload.redirect_uri");
	    AuthorizationCodeRequestUrl authorizationUrl =
	        flow.newAuthorizationUrl().setRedirectUri(redirectUri);

	    // receive authorization code and exchange it for an access token
	    System.out.println(authorizationUrl.build());
	    System.out.print("Please enter code: ");
        String code = new Scanner(System.in).nextLine();
	    TokenResponse response = flow.newTokenRequest(code).setRedirectUri(redirectUri).execute();

	    // store credential and return it
	    CREDENTIAL = flow.createAndStoreCredential(response, clientSecrets.getDetails().getClientId());
		
	}

	/**
	 * Uploads video to the user's YouTube account using OAuth2 for authentication.
	 *
	 * @param args video file.
	 */
	public String uploadVideo(InputStream videoFile, String tittle, Map <String, Object> metadata, List<String> tags) throws UploadExeption {
		// Scope required to upload to YouTube.
		List<String> scopes = Lists.newArrayList("https://www.googleapis.com/auth/youtube.force-ssl");
		try {
			// Authorization.
			if ((CREDENTIAL == null)||
				(CREDENTIAL.getAccessToken() == null)){
					authorize(scopes);
			}
			//Credential credential = authorize(scopes);

			// YouTube object used to make all API requests.
			youtube = new YouTube.Builder(HTTP_TRANSPORT, JSON_FACTORY, this.CREDENTIAL)
					.setApplicationName("DSpace SEDICI").build();
			
			List<String> categories = new ArrayList<String>();
			categories.add("snippet");
			// Add extra information to the video before uploading.
			Video videoObjectDefiningMetadata = new Video();

			/*
			 * Set the video to public, so it is available to everyone (what most people
			 * want). This is actually the default, but I wanted you to see what it looked
			 * like in case you need to set it to "unlisted" or "private" via API.
			 */
			VideoStatus status = new VideoStatus();
			status.setLicense("creativeCommon");
			status.setSelfDeclaredMadeForKids(false);
			status.setMadeForKids(false);
			status.setPrivacyStatus(ConfigurationManager.getProperty("youtube.upload","youtube.upload.video.state"));
			videoObjectDefiningMetadata.setStatus(status);
			
			// We set a majority of the metadata with the VideoSnippet object.
			VideoSnippet snippet = new VideoSnippet();
			
			snippet.setTitle(tittle);
			String description = buildDescription(metadata);
			snippet.setDescription(description);

			// Set the category of your video (allways Education)
			snippet.setCategoryId(this.getEducationId());

			// Set your keywords.
			snippet.setTags(tags);
			snippet.setDefaultLanguage((String)metadata.get("language"));

			// Set completed snippet to the video object.
			videoObjectDefiningMetadata.setSnippet(snippet);

			InputStreamContent mediaContent = new InputStreamContent(VIDEO_FILE_FORMAT,
					new BufferedInputStream(videoFile));

			/*
			 * The upload command includes: 1. Information we want returned after file is
			 * successfully uploaded. 2. Metadata we want associated with the uploaded
			 * video. 3. Video file itself.
			 */
			List<String> list = new ArrayList<String>();
			list.add("snippet");
			list.add("statistics");
			list.add("status");
			YouTube.Videos.Insert videoInsert = youtube.videos().insert(list,
					videoObjectDefiningMetadata, mediaContent);

			// Set the upload type and add event listener.
			MediaHttpUploader uploader = videoInsert.getMediaHttpUploader();

			/*
			 * Sets whether direct media upload is enabled or disabled. True = whole media
			 * content is uploaded in a single request. False (default) = resumable media
			 * upload protocol to upload in data chunks.
			 */
			uploader.setDirectUploadEnabled(false);

			MediaHttpUploaderProgressListener progressListener = new MediaHttpUploaderProgressListener() {
			public void progressChanged(MediaHttpUploader uploader) throws IOException {
				switch (uploader.getUploadState()) {
					case INITIATION_STARTED:
						//System.out.println("Initiation Started");
						logger.info("Initiation Started");
					break;
				case INITIATION_COMPLETE:
					//System.out.println("Initiation Completed");
					break;
				case MEDIA_COMPLETE:
					//System.out.println("Upload Completed!");
					logger.info("Upload Completed!");
					break;
				case NOT_STARTED:
					//System.out.println("Upload Not Started!");
					break;
				}
			}
			};
			uploader.setProgressListener(progressListener);

			// Execute upload.
			Video returnedVideo = videoInsert.execute();
			logger.info("Video upload executed -  new video Id: " + returnedVideo.getId());
			return returnedVideo.getId();
		} catch (GoogleJsonResponseException e) {
			logger.error("GoogleJsonResponseException: "+ e.getMessage());
			JSONObject jsonObject = new JSONObject(e.getDetails());
		    String reason = jsonObject.getJSONArray("errors").getJSONObject(0).getString("reason");
			switch (e.getStatusCode()) {
			case 400:{
					//Manejo de los casos en los que el problema este en el dato y por lo tanto no sea reasumible. 
					//No maneja el caso de que el video este roto ya que youtube se da cuenta en la compilacion del video, no en la subida.
					if((reason.equals("invalidCategoryId"))|
					   (reason.equals("invalidDescription"))|
					   (reason.equals("invalidFilename"))|
					   (reason.equals("invalidRecordingDetails"))|
					   (reason.equals("invalidTags"))|
					   (reason.equals("invalidTitle"))|
					   (reason.equals("invalidVideoMetadata"))|
					   (reason.equals("mediaBodyRequired"))) {
							throw new UploadExeption("Youtube format problem: "+reason+ " - Titulo "+tittle,false,e);
					}else{
						throw new UploadExeption(e.getStatusMessage(),true,e);
					}
				}
			case 403:{
					if(reason == "quotaExeded") {
						throw new UploadExeption("The daily quota of Youtube has exeded",true,e);
					}
				}
			default:{
				throw new UploadExeption(e.getStatusMessage(),true,e);
				}
			}
		} catch (TokenResponseException e) {
			//System.err.println("IOException: " + e.getMessage());
			//falta trabajar un poco el mensaje para hacer mas expresivo los errores de youtube, por ejemplo que se tiene que cambiar las credenciales ;(
			logger.error("TokenResponseException: " + e.getMessage());
			throw new UploadExeption(e.getMessage(),true,e);
		} catch (IOException e) {
			//System.err.println("IOException: " + e.getMessage());
			logger.error("IOException: " + e.getMessage());
			throw new UploadExeption(e.getMessage(),true,e);
		} catch (Throwable t) {
			//System.err.println("Throwable: " + t.getMessage());
			logger.error("Throwable: " + t.getMessage());
			throw new UploadExeption(t.getMessage(),true,t);
		}
	}
	
	public String updateMetadata(String videoId, String tittle, Map<String,Object> metadata, List<String> tags) throws UploadExeption{
		
		List<String> scopes = Lists.newArrayList("https://www.googleapis.com/auth/youtube.force-ssl");

	    try {
	    	
	    	if ((CREDENTIAL == null)||
					(CREDENTIAL.getAccessToken() == null)){
						authorize(scopes);
				}	
	      // Authorization.
	      //Credential credential = authorize(scopes);

	      // YouTube object used to make all API requests.
	      youtube = new YouTube.Builder(HTTP_TRANSPORT, JSON_FACTORY,this.CREDENTIAL).
	    		  setApplicationName("DSpace SEDICI").build();
	      
	      List<String> parts = new ArrayList<String>();
	      parts.add("snippet");
	      List<String> lvideoId = new ArrayList<String>();
	      lvideoId.add(videoId);

	      // Create the video list request
	      YouTube.Videos.List listVideosRequest = youtube.videos().list(parts).setId(lvideoId);

	      // Request is executed and video list response is returned
	      VideoListResponse listResponse = listVideosRequest.execute();

	      List<Video> videoList = listResponse.getItems();
	      if (videoList.isEmpty()) {
	        logger.warn("Can't find a video with video id: " + videoId);
	        return null;
	      }

	      // Since a unique video id is given, it will only return 1 video.
	      Video video = videoList.get(0);
	      VideoSnippet snippet = video.getSnippet();
	      
	      //Cambios en los metadatos del video
	      
	      snippet.setTitle(tittle);
	      
	      String description = buildDescription(metadata);
	      snippet.setDescription(description);
	      
	      snippet.setTags(tags);

	      // Create the video update request
	      
	      YouTube.Videos.Update updateVideosRequest = youtube.videos().update(parts, video);

	      // Request is executed and updated video is returned
	      Video videoResponse = updateVideosRequest.execute();
	      logger.info("Video " + videoResponse.getId()+ " was updated");

	      // Print out returned results.
	      
	      return videoResponse.getId();

	    } catch (GoogleJsonResponseException e) {
			logger.error("GoogleJsonResponseException: "+ e.getMessage());
			JSONObject jsonObject = new JSONObject(e.getDetails());
		    String reason = jsonObject.getJSONArray("errors").getJSONObject(0).getString("reason");
			switch (e.getStatusCode()) {
			case 400:{
					if((reason.equals("invalidCategoryId"))|
					   (reason.equals("invalidDescription"))|
					   (reason.equals("invalidFilename"))|
					   (reason.equals("invalidRecordingDetails"))|
					   (reason.equals("invalidTags"))|
					   (reason.equals("invalidTitle"))|
					   (reason.equals("invalidVideoMetadata"))|
					   (reason.equals("mediaBodyRequired"))) {
							throw new UploadExeption("Youtube format problem: "+reason+ " - Titulo "+tittle,false,e);
					}else{
						throw new UploadExeption(e.getStatusMessage(),true,e);
					}
				}
			case 403:{
					if(reason == "quotaExeded") {
						throw new UploadExeption("The daily quota of Youtube has exeded",true,e);
					}
				}
			default:{
				throw new UploadExeption(e.getStatusMessage(),true,e);
				}
			}
		} catch (TokenResponseException e) {
			//System.err.println("IOException: " + e.getMessage());
			//falta trabajar un poco el mensaje para hacer mas expresivo los errores de youtube ;(
			logger.error("TokenResponseException: " + e.getMessage());
			throw new UploadExeption(e.getMessage(),true,e);
		} catch (IOException e) {
			//System.err.println("IOException: " + e.getMessage());
			logger.error("IOException: " + e.getMessage());
			throw new UploadExeption(e.getMessage(),true,e);
		} catch (Throwable t) {
			//System.err.println("Throwable: " + t.getMessage());
			logger.error("Throwable: " + t.getMessage());
			throw new UploadExeption(t.getMessage(),true,t);
		}	 
	}
	
	public String deleteVideo(String videoId) throws UploadExeption{
		List<String> scopes = Lists.newArrayList("https://www.googleapis.com/auth/youtube.force-ssl");
	    try {
	      // Authorization.
	    	if ((this.CREDENTIAL == null)||
					(this.CREDENTIAL.getAccessToken() == null)){
						authorize(scopes);
				}
	    	//Credential credential = authorize(scopes);

	      // YouTube object used to make all API requests.
	      youtube = new YouTube.Builder(HTTP_TRANSPORT, JSON_FACTORY, this.CREDENTIAL).
	    		  setApplicationName("DSpace SEDICI").build();
	      
	      List<String> lvideoId = new ArrayList<String>();
	      lvideoId.add(videoId);

	      // Create the video list request
	      YouTube.Videos.Delete deleteRequest = youtube.videos().delete(videoId);
	      //deleteRequest.setAccessToken(credential.getAccessToken());
	      deleteRequest.execute();
	      logger.info("The video "+videoId+" was eliminated");
	      
	      return videoId;

	    } catch (GoogleJsonResponseException e) {
			logger.error("GoogleJsonResponseException: "+ e.getMessage());
			JSONObject jsonObject = new JSONObject(e.getDetails());
		    String reason = jsonObject.getJSONArray("errors").getJSONObject(0).getString("reason");
			switch (e.getStatusCode()) {
			case 400:{
					if((reason.equals("invalidCategoryId"))|
					   (reason.equals("invalidDescription"))|
					   (reason.equals("invalidFilename"))|
					   (reason.equals("invalidRecordingDetails"))|
					   (reason.equals("invalidTags"))|
					   (reason.equals("invalidTitle"))|
					   (reason.equals("invalidVideoMetadata"))|
					   (reason.equals("mediaBodyRequired"))) {
							throw new UploadExeption("Youtube format problem: "+reason,false,e);
					}else{
						throw new UploadExeption(e.getStatusMessage(),true,e);
					}
				}
			case 403:{
					if(reason == "quotaExeded") {
						throw new UploadExeption("The daily quota of Youtube has exeded",true,e);
					}
			}
			default:{
				throw new UploadExeption(e.getStatusMessage(),true,e);
				}
			}
		}catch (TokenResponseException e) {
			//System.err.println("IOException: " + e.getMessage());
			//falta trabajar un poco el mensaje para hacer mas expresivo los errores de youtube ;(
			logger.error("TokenResponseException: " + e.getMessage());
			throw new UploadExeption(e.getMessage(),true,e);
		}catch (IOException e) {
			//System.err.println("IOException: " + e.getMessage());
			logger.error("IOException: " + e.getMessage());
			throw new UploadExeption(e.getMessage(),true,e);
		} catch (Throwable t) {
			//System.err.println("Throwable: " + t.getMessage());
			logger.error("Throwable: " + t.getMessage());
			throw new UploadExeption(t.getMessage(),true,t);
		}
	}
	
	private String getEducationId() throws UploadExeption{
		List<String> categories = new ArrayList<String>();
	    categories.add("snippet");
	    String cId = null;
	    try {
	    	List<VideoCategory> list = youtube.videoCategories().list(categories).setRegionCode("ar").execute().getItems();
			int contadorLista = 0;
			while ((contadorLista < list.size()&(cId== null))) {
				if (list.get(contadorLista).getSnippet().getTitle().equals("Education")) {
					cId = list.get(contadorLista).getId();
				}
				contadorLista++;
			}
			return cId;
	    } catch (GoogleJsonResponseException e) {
			logger.error("GoogleJsonResponseException: "+ e.getMessage());
			JSONObject jsonObject = new JSONObject(e.getDetails());
		    String reason = jsonObject.getJSONArray("errors").getJSONObject(0).getString("reason");
			switch (e.getStatusCode()) {
			case 400:{
					if((reason.equals("invalidCategoryId"))|
					   (reason.equals("invalidDescription"))|
					   (reason.equals("invalidFilename"))|
					   (reason.equals("invalidRecordingDetails"))|
					   (reason.equals("invalidTags"))|
					   (reason.equals("invalidTitle"))|
					   (reason.equals("invalidVideoMetadata"))|
					   (reason.equals("mediaBodyRequired"))) {
							throw new UploadExeption("Youtube format problem: "+reason,false,e);
					}else{
						throw new UploadExeption(e.getStatusMessage(),true,e);
					}
				}
			case 403:{
					if(reason == "quotaExeded") {
						throw new UploadExeption("The daily quota of Youtube has exeded",true,e);
					}
				}
			default:{
				throw new UploadExeption(e.getStatusMessage(),true,e);
				}
			}
		} catch (TokenResponseException e) {
			//System.err.println("IOException: " + e.getMessage());
			//falta trabajar un poco el mensaje para hacer mas expresivo los errores de youtube ;(
			logger.error("TokenResponseException: " + e.getMessage());
			throw new UploadExeption(e.getMessage(),true,e);
		} catch (IOException e) {
			//System.err.println("IOException: " + e.getMessage());
			logger.error("IOException: " + e.getMessage());
			throw new UploadExeption(e.getMessage(),true,e);
		}catch (Throwable t) {
			//System.err.println("Throwable: " + t.getMessage());
			logger.error("Throwable: " + t.getMessage());
			throw new UploadExeption(t.getMessage(),true,t);
		}
	}
	
	private String buildDescription(Map<String,Object> metadata) {
		
	    //String description = (String) metadata.get("title") + "\n";
	    //usar string buffer
	    String description = "";
	    StringBuffer desc = new StringBuffer(description);
	    //Se obtienen los creadores
	    List<Metadatum> creators = (List<Metadatum>) metadata.get("creators");
	    Integer cantCreadores = 1;
	    if (creators.size() > 1) {
	        desc.append("Creadores: "+creators.get(0).value);
	        while(cantCreadores<creators.size()) {
	            desc.append("; "+creators.get(cantCreadores).value);
	            cantCreadores = cantCreadores + 1;
	        }
	        desc.append("\n");
	    }else {
	        desc.append("Creador: "+creators.get(0).value+"\n");
	    }
	    
	    desc.append("Tipo: "+metadata.get("subtype")+"\n");
	    //desc.append("Fecha de publicación: "+metadata.get("dateAvailable")+"\n");
	    desc.append("Enlace de la fuente: "+metadata.get("iUri")+"\n");
	    
	    //Se obtienen las keywords
	    List<Metadatum> subjects = (List<Metadatum>) metadata.get("subjects");
	    if (subjects.size() > 0){
	        desc.append("Palabras clave: ");
	        cantCreadores = 0;
	        while (cantCreadores<(subjects.size()-1)) {
	            desc.append(subjects.get(cantCreadores).value+", ");
	            cantCreadores = cantCreadores +1;
	        }
	        desc.append(subjects.get(cantCreadores).value+"\n");
	    }
	    desc.append("Licencia de uso: "+metadata.get("license")+"\n");
	    if((metadata.containsKey("abstract"))&&(desc.length() + metadata.get("abstract").toString().length() + "\n".length() < 5000)){
	        desc.append("Resumen: " + metadata.get("abstract")+"\n");
	    }else {
	    	logger.info("El item que se encuentra en "+metadata.get("iUri")+", supera el tamaño maximo de descripcion por el resumen");
	    }
	    description = desc.toString();
	    return description;
	}
	

}