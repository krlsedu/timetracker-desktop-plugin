package com.krlsedu.timetracker;


import com.krlsedu.timetracker.service.LoggerConf;

public class WakaTimeDesktopPlugin {
	
	public static void main(String[] args) {
		LoggerConf.config();
		Tray.config();
		Core.start();
		Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
			Tray.notifyError(e.getMessage());
			Core.restart();
		});
	}
}