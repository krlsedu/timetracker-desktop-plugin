package com.krlsedu.timetracker.core;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.PatternLayout;

public class LoggerConf {
	public static void config() {
		BasicConfigurator.configure();
		FileAppender fa = new FileAppender();
		fa.setFile(ConfigFile.getResourcesLocation() + "\\timetracker-desktop-plugin.log");
		fa.setLayout(new PatternLayout("%d %-5p [%c{1}] %m%n"));
		fa.setThreshold(Level.DEBUG);
		fa.setAppend(true);
		fa.activateOptions();
		TimeTrackerCore.log.addAppender(fa);
	}
}
