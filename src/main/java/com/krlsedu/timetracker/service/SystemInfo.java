package com.krlsedu.timetracker.service;

import com.krlsedu.timetracker.Win32IdleTime;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class SystemInfo {
	private static String osName = null;
	
	private static String hostName = null;
	
	private State state = State.UNKNOWN;
	
	private final int SECONDS_TO_IDLE = 30;
	
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
	
	
	public boolean isChangedState() {
		
		int idleSec = Win32IdleTime.getIdleTimeMillisWin32() / 1000;
		
		State newState = idleSec < SECONDS_TO_IDLE ? State.ONLINE : State.IDLE;
		
		if (newState != state) {
			state = newState;
			System.out.println(state);
			return true;
		}
		
		return false;
	}
	
	public boolean isOnline(){
		return state.equals(State.ONLINE);
	}
	
	enum State {
		UNKNOWN, ONLINE, IDLE, AWAY
	}
}
