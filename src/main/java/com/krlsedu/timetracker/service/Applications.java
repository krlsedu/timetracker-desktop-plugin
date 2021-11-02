package com.krlsedu.timetracker.service;

import com.krlsedu.timetracker.model.Heartbeat;

import java.util.ArrayList;
import java.util.List;

public class Applications {
	public static List<String> notSendHeartbeat = new ArrayList<>();
	
	public static void init() {
		notSendHeartbeat.add("idea64.exe");
	}
	
	public static boolean sendHeartbeat(Heartbeat heartbeat) {
		String name = getApplicationName(heartbeat);
		if (!(
				heartbeat.getProject().contains("JetBrains\\Toolbox\\apps") ||
						heartbeat.getProject().contains("Microsoft VS Code") ||
						notSendHeartbeat.contains(name))) {
			heartbeat.setProject(name);
			return true;
		}
		return false;
	}
	
	public static String getApplicationName(Heartbeat heartbeat) {
		String name = heartbeat.getProject();
		return name.substring(name.lastIndexOf("\\") + 1);
	}
}
