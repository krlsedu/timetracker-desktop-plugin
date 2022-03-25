package com.krlsedu.timetracker;


import com.krlsedu.timetracker.desktop.Core;
import com.krlsedu.timetracker.desktop.Tray;

public class TimeTrackerDesktopPlugin {

	public static void main(String[] args) {
		Tray.config();
		Core.start();
		Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
			Tray.notifyError(e.getMessage());
			Core.restart();
		});
	}
}