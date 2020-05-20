package com.krlsedu.timetracker;

import java.util.HashMap;
import java.util.Map;

public class Monitor {
	
	private static final int MAX_TITLE_LENGTH = 1024;
	
	private static final Map<String, Long> timeMap = new HashMap<>();
	
	private static final int waitTime = 100;
	
	public static void main(String[] args) throws Exception {
		Core.start();
	}
}