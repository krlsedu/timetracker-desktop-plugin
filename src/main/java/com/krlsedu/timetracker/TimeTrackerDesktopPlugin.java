package com.krlsedu.timetracker;


import com.krlsedu.timetracker.core.LoggerConf;
import com.krlsedu.timetracker.desktop.Core;
import com.krlsedu.timetracker.desktop.Tray;

public class TimeTrackerDesktopPlugin {

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