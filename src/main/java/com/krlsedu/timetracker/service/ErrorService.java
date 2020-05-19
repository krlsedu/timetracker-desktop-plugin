package com.krlsedu.timetracker.service;

import com.krlsedu.timetracker.model.Error;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

public class ErrorService {
	
	public static void saveError(String url, String json) throws Exception {
		Error error = new Error();
		error.setJson(json);
		error.setUrl(url);
		saveError(error);
	}
	
	public static void saveError(Error error) throws Exception {
		File f = new File("timeTrackerError.txt");
		BufferedWriter writer = new BufferedWriter(new FileWriter("timeTrackerError.txt", true));
		if (f.exists() && !f.isDirectory()) {
			writer.append(",");
		}
		writer.append(Sender.getObjectMapper().writeValueAsString(error));
		writer.close();
	}
}
