package com.krlsedu.timetracker;

import com.krlsedu.timetracker.service.LoggerConf;
import com.krlsedu.timetracker.service.WakaTimeCli;


public class WakaTimeDesktopPlugin {
	
	public static void main(String[] args) {
		LoggerConf.config();
		Tray.config();
		WakaTimeCli.init();
		Core.start();
	}
}