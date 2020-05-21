package com.krlsedu.timetracker;

import com.krlsedu.timetracker.service.OfflineMode;

public class Monitor {
	
	public static void main(String[] args) throws Exception {
		for (String arg :
				args) {
			if (arg.equals("--offline")) {
				OfflineMode.setOn(true);
			}
		}
		Core.start();
	}
}