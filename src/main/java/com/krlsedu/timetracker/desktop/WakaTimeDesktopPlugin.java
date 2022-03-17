package com.krlsedu.timetracker.desktop;


import com.krlsedu.timetracker.core.LoggerConf;

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