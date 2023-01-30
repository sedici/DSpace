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

import java.io.BufferedReader;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.api.services.youtube.model.Video;

public class VideoUpload {

	/**
	 * Uploads user selected video in the project folder to the user's YouTube
	 * account using OAuth2 for authentication.
	 *
	 * @param args command line args (not used).
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		File videoFile = new File("/home/rafael/Escritorio/Youtube 2/SilenceWench.mp4");
		String tittle = "Titulo";
		String description = "Probando descripcion";
		List<String> tags = new ArrayList<String>();
		tags.add("Hola");
		tags.add("Prueba");
		String returnedVideoId = new YoutubeAdapter().uploadVideo(videoFile,tittle,description,tags);
		// Print out returned results.
		System.out.println("\n================== Returned Video ==================\n");
		System.out.println("  - Id: " + returnedVideoId);

	}

	
}
