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

import org.apache.log4j.Logger;
import org.json.JSONObject;

import com.google.api.client.auth.oauth2.Credential;
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

public class YoutubeAdapter {
	
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

	/**
	 * Authorizes the installed application to access user's protected data.
	 *
	 * @param scopes list of scopes needed to run youtube upload.
	 */
	private Credential authorize(List<String> scopes) throws Exception {

		// Load client secrets.
		Reader reader = new InputStreamReader(new FileInputStream(new File("../config/client_secrets.json")));
		GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, reader);

		// Checks that the defaults have been replaced (Default = "Enter X here").
		if (clientSecrets.getDetails().getClientId().startsWith("Enter")
				|| clientSecrets.getDetails().getClientSecret().startsWith("Enter ")) {
			System.out.println(
					"Enter Client ID and Secret from https://console.developers.google.com/project/_/apiui/credential"
							+ "into dspace/src/main/resources/client_secrets.json");
			System.exit(1);
		}

		// Set up file credential store.
		FileCredentialStore credentialStore = new FileCredentialStore(
				new File("../config/youtube-api-uploadvideo.json"), JSON_FACTORY);

		// Set up authorization code flow.
		GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, JSON_FACTORY,
				clientSecrets, scopes).setCredentialStore(credentialStore).setAccessType("offline").build();

		// Build the local server and bind it to port 9000
		LocalServerReceiver localReceiver = new LocalServerReceiver.Builder().setPort(9080).setHost("testing.sedici.unlp.edu.ar").build();

		// Authorize.
		return new AuthorizationCodeInstalledApp(flow, localReceiver).authorize("user");
	}

	/**
	 * Uploads video to the user's YouTube account using OAuth2 for authentication.
	 *
	 * @param args video file.
	 */
	public String uploadVideo(InputStream videoFile, String tittle, String description, List<String> tags) {
		// Scope required to upload to YouTube.
		List<String> scopes = Lists.newArrayList("https://www.googleapis.com/auth/youtube");

		try {
			// Authorization.
			
			Credential credential = authorize(scopes);
			/*Reader reader = new InputStreamReader(new FileInputStream(new File("./client_secrets.json")));
			GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, reader);
			GoogleTokenResponse responseToken = new GoogleRefreshTokenRequest(HTTP_TRANSPORT, JSON_FACTORY,credential.getRefreshToken(),"","").execute();
			System.out.println(responseToken);
			No parece necesario esto pero lo dejo por ahora para futuras pruebas*/
			
			//HttpRequestInitializer requestInitializer = new HttpCredentialsAdapter(credential);
			// YouTube object used to make all API requests.
			youtube = new YouTube.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
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
			status.setPrivacyStatus("public");
			videoObjectDefiningMetadata.setStatus(status);
			
			// We set a majority of the metadata with the VideoSnippet object.
			VideoSnippet snippet = new VideoSnippet();

			/*
			 * The Calendar instance is used to create a unique name and description for
			 * test purposes, so you can see multiple files being uploaded. You will want to
			 * remove this from your project and use your own standard names.
			 */
			Calendar cal = Calendar.getInstance();
			snippet.setTitle(tittle);
			snippet.setDescription(description);
			snippet.setCategoryId(this.getEducationId());

			// Set your keywords.
			snippet.setTags(tags);

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
						System.out.println("Initiation Started");
						logger.info("Initiation Started");
					break;
				case INITIATION_COMPLETE:
					System.out.println("Initiation Completed");
					break;
				case MEDIA_COMPLETE:
					System.out.println("Upload Completed!");
					logger.info("Upload Completed!");
					break;
				case NOT_STARTED:
					System.out.println("Upload Not Started!");
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
					if((reason.equals("invalidCategoryId"))|
					   (reason.equals("invalidDescription"))|
					   (reason.equals("invalidFilename"))|
					   (reason.equals("invalidRecordingDetails"))|
					   (reason.equals("invalidTags"))|
					   (reason.equals("invalidTitle"))|
					   (reason.equals("invalidVideoMetadata"))|
					   (reason.equals("mediaBodyRequired"))) {
							throw new UploadExeption("Youtube format problem: "+reason+ " - Titulo "+tittle).notice();
					}else{
						throw new UploadExeption(e.getStatusMessage()).notice().resumable();
					}
				}
			case 403:{
					if(reason == "quotaExeded") {
						throw new UploadExeption("The daily quota of Youtube has exeded").notice().resumable();
					}
				}
			case 500:{
				throw new UploadExeption(e.getStatusMessage()).resumable();
				}
			case 503:{
				throw new UploadExeption(e.getStatusMessage()).resumable();
				}
			default:{
				throw new UploadExeption(e.getStatusMessage()).resumable().notice();
				}
			}
		} catch (IOException e) {
			//System.err.println("IOException: " + e.getMessage());
			logger.error("IOException: " + e.getMessage());
			throw new UploadExeption(e.getMessage()).resumable();
		} catch (Throwable t) {
			//System.err.println("Throwable: " + t.getMessage());
			logger.error("Throwable: " + t.getMessage());
			throw new UploadExeption(t.getMessage()).resumable();
		}
	}
	
	public String updateMetadata(String videoId, String tittle, String description, List<String> tags) throws UploadExeption{
		
		List<String> scopes = Lists.newArrayList("https://www.googleapis.com/auth/youtube");

	    try {
	      // Authorization.
	    	Credential credential = authorize(scopes);
			//HttpRequestInitializer requestInitializer = new HttpCredentialsAdapter(credential);

	      // YouTube object used to make all API requests.
	      youtube = new YouTube.Builder(HTTP_TRANSPORT, JSON_FACTORY,credential).
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
	        System.out.println("Can't find a video with video id: " + videoId);
	        logger.warn("Can't find a video with video id: " + videoId);
	        return null;
	      }

	      // Since a unique video id is given, it will only return 1 video.
	      Video video = videoList.get(0);
	      VideoSnippet snippet = video.getSnippet();
	      
	      //Cambios en los metadatos del video
	      snippet.setTitle(tittle);
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
							throw new UploadExeption("Youtube format problem: "+reason+ " - Titulo "+tittle).notice();
					}else{
						throw new UploadExeption(e.getStatusMessage()).notice().resumable();
					}
				}
			case 403:{
					if(reason == "quotaExeded") {
						throw new UploadExeption("The daily quota of Youtube has exeded").notice().resumable();
					}
				}
			case 500:{
				throw new UploadExeption(e.getStatusMessage()).resumable();
				}
			case 503:{
				throw new UploadExeption(e.getStatusMessage()).resumable();
				}
			default:{
				throw new UploadExeption(e.getStatusMessage()).resumable().notice();
				}
			}
		} catch (IOException e) {
			//System.err.println("IOException: " + e.getMessage());
			logger.error("IOException: " + e.getMessage());
			throw new UploadExeption(e.getMessage()).resumable();
		} catch (Throwable t) {
			//System.err.println("Throwable: " + t.getMessage());
			logger.error("Throwable: " + t.getMessage());
			throw new UploadExeption(t.getMessage()).resumable();
		}	 
	}
	
	public String deleteVideo(String videoId) throws UploadExeption{
		List<String> scopes = Lists.newArrayList("https://www.googleapis.com/auth/youtube");
	    try {
	      // Authorization.
	    	Credential credential = authorize(scopes);
			//HttpRequestInitializer requestInitializer = new HttpCredentialsAdapter(credential);

	      // YouTube object used to make all API requests.
	      youtube = new YouTube.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential).
	    		  setApplicationName("DSpace SEDICI").build();
	      
	      List<String> lvideoId = new ArrayList<String>();
	      lvideoId.add(videoId);

	      // Create the video list request
	      YouTube.Videos.Delete deleteRequest = youtube.videos().delete(videoId);
	      //deleteRequest.setAccessToken(credential.getAccessToken());
	      System.out.println(deleteRequest.getOauthToken());
	      deleteRequest.execute();
	      logger.info("The video "+videoId+" was eliminated");

	      // Print out returned results.
	      
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
							throw new UploadExeption("").notice();
					}else{
						throw new UploadExeption(e.getStatusMessage()).notice().resumable();
					}
				}
			case 403:{
					if(reason == "quotaExeded") {
						throw new UploadExeption("The daily quota of Youtube has exeded").notice().resumable();
					}
				}
			case 500:{
				throw new UploadExeption(e.getStatusMessage()).resumable();
				}
			case 503:{
				throw new UploadExeption(e.getStatusMessage()).resumable();
				}
			default:{
				throw new UploadExeption(e.getStatusMessage()).resumable().notice();
				}
			}
		} catch (IOException e) {
			//System.err.println("IOException: " + e.getMessage());
			logger.error("IOException: " + e.getMessage());
			throw new UploadExeption(e.getMessage()).resumable();
		} catch (Throwable t) {
			//System.err.println("Throwable: " + t.getMessage());
			logger.error("Throwable: " + t.getMessage());
			throw new UploadExeption(t.getMessage()).resumable();
		}
	}
	
	public Boolean verifyMetadata(String videoId, String tittle, String description, List<String> tags) {
		Boolean change = false;
		List<String> scopes = Lists.newArrayList("https://www.googleapis.com/auth/youtube");

	    try {
	      // Authorization.
	    	Credential credential = authorize(scopes);
			//HttpRequestInitializer requestInitializer = new HttpCredentialsAdapter(credential);

	      // YouTube object used to make all API requests.
	      youtube = new YouTube.Builder(HTTP_TRANSPORT, JSON_FACTORY,credential).
	    		  setApplicationName("DSpace SEDICI").build();
	      
	      List<String> parts = new ArrayList<String>();
	      parts.add("snippet");
	      parts.add("status");
	      List<String> videos = new ArrayList<String>();
	      videos.add(videoId);
	      Video video = youtube.videos().list(parts).setId(videos).execute().getItems().get(0);
	      if ((description != video.getSnippet().getDescription())|(tittle != video.getSnippet().getTitle())| (tags != video.getSnippet().getTags())) {
	    	  change = true;
	      }
	    } catch (GoogleJsonResponseException e) {
		      logger.warn("GoogleJsonResponseException code: " + e.getDetails().getCode() + " : "
		          + e.getDetails().getMessage());
		      System.err.println("GoogleJsonResponseException code: " + e.getDetails().getCode() + " : "
		          + e.getDetails().getMessage());
		      e.printStackTrace();
		    } catch (IOException e) {
		      logger.warn("IOException: " + e.getMessage());
		      System.err.println("IOException: " + e.getMessage());
		      e.printStackTrace();
		    } catch (Throwable t) {
		      logger.warn("Throwable: " + t.getMessage());
		      System.err.println("Throwable: " + t.getMessage());
		      t.printStackTrace();
		    }
		return change;
	}
	
	private String getEducationId() {
		List<String> categories = new ArrayList<String>();
	    categories.add("snippet");
	    String cId = null;
	    try {
	    	List<VideoCategory> list = youtube.videoCategories().list(categories).setRegionCode("ar").execute().getItems();
			int aux = 0;
			while ((aux < list.size()&(cId== null))) {
				if (list.get(aux).getSnippet().getTitle().equals("Education")) {
					cId = list.get(aux).getId();
				}
				aux++;
			}
	    } catch (GoogleJsonResponseException e) {
		      logger.warn("GoogleJsonResponseException code: " + e.getDetails().getCode() + " : "
		          + e.getDetails().getMessage());
		      System.err.println("GoogleJsonResponseException code: " + e.getDetails().getCode() + " : "
		          + e.getDetails().getMessage());
		      e.printStackTrace();
		    } catch (IOException e) {
		      logger.warn("IOException: " + e.getMessage());
		      System.err.println("IOException: " + e.getMessage());
		      e.printStackTrace();
		    } catch (Throwable t) {
		      logger.warn("Throwable: " + t.getMessage());
		      System.err.println("Throwable: " + t.getMessage());
		      t.printStackTrace();
		    }
		return cId;
	}
	

}