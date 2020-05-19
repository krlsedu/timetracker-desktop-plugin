package com.krlsedu.timetracker.service;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class SystenInfo {
	public static String osName = null;
	public static String hostName = null;
	
	public static String getOsName() {
		if (osName == null) {
			osName = System.getProperty("os.name");
		}
		return osName;
	}
	
	public static String getHostName() {
		if (hostName == null) {
			try {
				InetAddress addr;
				addr = InetAddress.getLocalHost();
				hostName = addr.getHostName();
			} catch (UnknownHostException ex) {
				System.out.println("Hostname can not be resolved");
			}
		}
		return hostName;
	}
}
