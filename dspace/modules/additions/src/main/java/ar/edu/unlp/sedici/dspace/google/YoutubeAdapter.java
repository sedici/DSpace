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
import java.io.InputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.java6.auth.oauth2.FileCredentialStore;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.googleapis.media.MediaHttpUploader;
import com.google.api.client.googleapis.media.MediaHttpUploaderProgressListener;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.InputStreamContent;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoListResponse;
import com.google.api.services.youtube.model.VideoSnippet;
import com.google.api.services.youtube.model.VideoStatus;
import com.google.common.collect.Lists;

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
		InputStream targetStream = new FileInputStream(new File(RUTA_DE_JSON));
        GoogleCredential google = GoogleCredential.fromStream(targetStream).createScoped(scopes);
		return google;
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
			// YouTube object used to make all API requests.
			youtube = new YouTube.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
					.setApplicationName("DSpace SEDICI").build();

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

			// Set your keywords.
			snippet.setTags(tags);

			// Set completed snippet to the video object.
			videoObjectDefiningMetadata.setSnippet(snippet);

			InputStreamContent mediaContent = new InputStreamContent(VIDEO_FILE_FORMAT,
					new BufferedInputStream(videoFile));
			//mediaContent.setLength(videoFile.length());

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
					case MEDIA_IN_PROGRESS:
						System.out.println("Upload in progress");
						System.out.println("Upload percentage: " + uploader.getProgress());
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
			System.err.println("GoogleJsonResponseException code: " + e.getDetails().getCode() + " : "
					+ e.getDetails().getMessage());
			e.printStackTrace();
			logger.warn("GoogleJsonResponseException code: " + e.getDetails().getCode() + " : "
					+ e.getDetails().getMessage());
		} catch (IOException e) {
			System.err.println("IOException: " + e.getMessage());
			logger.warn("IOException: " + e.getMessage());
			e.printStackTrace();
		} catch (Throwable t) {
			System.err.println("Throwable: " + t.getMessage());
			logger.warn("Throwable: " + t.getMessage());
			t.printStackTrace();
		}
		return null;
	}
	
	public String updateMetadata(String videoId, String tittle, String description, List<String> tags) {
		
		List<String> scopes = Lists.newArrayList("https://www.googleapis.com/auth/youtube");

	    try {
	      // Authorization.
	      Credential credential = authorize(scopes);

	      // YouTube object used to make all API requests.
	      youtube = new YouTube.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential).
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
	    return null;
	 
	}
	
	public String deleteVideo(String videoId) {
		List<String> scopes = Lists.newArrayList("https://www.googleapis.com/auth/youtube.force-ssl");
		scopes.add("https://www.googleapis.com/auth/youtube");
	    try {
	      // Authorization.
	      Credential credential = authorize(scopes);

	      // YouTube object used to make all API requests.
	      youtube = new YouTube.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential).
	    		  setApplicationName("DSpace SEDICI").build();
	      
	      List<String> lvideoId = new ArrayList<String>();
	      lvideoId.add(videoId);

	      // Create the video list request
	      YouTube.Videos.Delete deleteRequest = youtube.videos().delete(videoId);
	      deleteRequest.setAccessToken(credential.getAccessToken());
	      System.out.println(deleteRequest.getOauthToken());
	      deleteRequest.execute();
	      logger.info("The video "+videoId+" was eliminated");

	      // Print out returned results.
	      
	      return videoId;

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
	    return null;
	}

}