package com.krlsedu.TimeTracker.core;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.core.FileAppender;
import ch.qos.logback.core.util.StatusPrinter;
import com.krlsedu.timetracker.core.ConfigFile;
import org.slf4j.LoggerFactory;

public class LoggerConf {
	private static LoggerContext loggerContext;

	public static Logger getLogger(Class clazz) {

		loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();

		FileAppender fileAppender = new FileAppender();
		fileAppender.setContext(loggerContext);
		fileAppender.setName("timestamp");
		// set the file name
		fileAppender.setFile(ConfigFile.getResourcesLocation() + "\\timetracker-desktop-plugin.log");
		System.out.println(ConfigFile.getResourcesLocation() + "\\timetracker-desktop-plugin.log");
		PatternLayoutEncoder encoder = new PatternLayoutEncoder();
		encoder.setContext(loggerContext);
		encoder.setPattern("%d %-5p [%c{1}] %m%n");
		encoder.start();

		fileAppender.setEncoder(encoder);
		fileAppender.start();

		// attach the rolling file appender to the logger of your choice
		Logger logbackLogger = loggerContext.getLogger(clazz);
		logbackLogger.addAppender(fileAppender);

		// OPTIONAL: print logback internal status messages
		StatusPrinter.print(loggerContext);

		return logbackLogger;
	}
}
