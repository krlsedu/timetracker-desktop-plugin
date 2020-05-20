package com.krlsedu.timetracker.service;

import com.krlsedu.timetracker.model.Error;

public class ErrorService {
	
	public static void saveError(String url, String json) throws Exception {
		Error error = new Error();
		error.setJson(json);
		error.setUrl(url);
		saveError(error);
	}
	
	public static void saveError(Error error) throws Exception {
		SqlLitle.salva(error);
	}
}
