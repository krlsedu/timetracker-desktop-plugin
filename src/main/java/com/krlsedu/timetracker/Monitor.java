package com.krlsedu.timetracker;

import org.apache.log4j.BasicConfigurator;

public class Monitor {
	
	public static void main(String[] args) {
		BasicConfigurator.configure();
		Core.start();
	}
}