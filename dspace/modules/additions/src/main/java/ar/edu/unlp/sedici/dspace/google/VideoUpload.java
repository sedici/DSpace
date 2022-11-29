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
		File videoFile = getVideoFromUser();
		// We get the user selected local video file to upload.
		System.out.println("You chose " + videoFile + " to upload.");
		Video returnedVideo = new YoutubeApiConnector().uploadVideo(videoFile);
		// Print out returned results.
		System.out.println("\n================== Returned Video ==================\n");
		System.out.println("  - Id: " + returnedVideo.getId());
		System.out.println("  - Title: " + returnedVideo.getSnippet().getTitle());
		System.out.println("  - Tags: " + returnedVideo.getSnippet().getTags());
		System.out.println("  - Privacy Status: " + returnedVideo.getStatus().getPrivacyStatus());
		System.out.println("  - Video Count: " + returnedVideo.getStatistics().getViewCount());

	}

	/**
	 * Gets the user selected local video file to upload.
	 */
	private static File getVideoFromUser() throws IOException {
		File[] listOfVideoFiles = getLocalVideoFiles();
		return getUserChoice(listOfVideoFiles);
	}

	/**
	 * Gets an array of videos in the current directory.
	 */
	private static File[] getLocalVideoFiles() throws IOException {

		File currentDirectory = new File(".");
		System.out.println("Video files from " + currentDirectory.getAbsolutePath() + ":");

		// Filters out video files. This list of video extensions is not comprehensive.
		FilenameFilter videoFilter = new FilenameFilter() {
			public boolean accept(File dir, String name) {
				String lowercaseName = name.toLowerCase();
				if (lowercaseName.endsWith(".webm") || lowercaseName.endsWith(".flv") || lowercaseName.endsWith(".f4v")
						|| lowercaseName.endsWith(".mov") || lowercaseName.endsWith(".mp4")) {
					return true;
				} else {
					return false;
				}
			}
		};
		return currentDirectory.listFiles(videoFilter);
	}

	/**
	 * Outputs video file options to the user, records user selection, and returns
	 * the video (File object).
	 *
	 * @param videoFiles Array of video File objects
	 */
	private static File getUserChoice(File videoFiles[]) throws IOException {

		if (videoFiles.length < 1) {
			throw new IllegalArgumentException("No video files in this directory.");
		}

		for (int i = 0; i < videoFiles.length; i++) {
			System.out.println(" " + i + " = " + videoFiles[i].getName());
		}

		BufferedReader bReader = new BufferedReader(new InputStreamReader(System.in));
		String inputChoice;

		do {
			System.out.print("Choose the number of the video file you want to upload: ");
			inputChoice = bReader.readLine();
		} while (!isValidIntegerSelection(inputChoice, videoFiles.length));

		return videoFiles[Integer.parseInt(inputChoice)];
	}

	/**
	 * Checks if string contains a valid, positive integer that is less than max.
	 * Please note, I am not testing the upper limit of an integer (2,147,483,647).
	 * I just go up to 999,999,999.
	 *
	 * @param input String to test.
	 * @param max   Integer must be less then this Maximum number.
	 */
	public static boolean isValidIntegerSelection(String input, int max) {
		if (input.length() > 9)
			return false;

		boolean validNumber = false;
		// Only accepts positive numbers of up to 9 numbers.
		Pattern intsOnly = Pattern.compile("^\\d{1,9}$");
		Matcher makeMatch = intsOnly.matcher(input);

		if (makeMatch.find()) {
			int number = Integer.parseInt(makeMatch.group());
			if ((number >= 0) && (number < max)) {
				validNumber = true;
			}
		}
		return validNumber;
	}
}
