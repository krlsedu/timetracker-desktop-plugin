package com.krlsedu.timetracker.service;

public class OfflineMode {
	private static boolean on = false;
	
	public static void salva(String url, String body) throws Exception {
		ErrorService.saveError(url, body);
	}
	
	public static boolean isOn() {
		return on;
	}
	
	public static void setOn(boolean on) {
		OfflineMode.on = on;
	}
	
	public static void syncErros() throws Exception{
		ErrorService.reSendErrors();
	}
}
