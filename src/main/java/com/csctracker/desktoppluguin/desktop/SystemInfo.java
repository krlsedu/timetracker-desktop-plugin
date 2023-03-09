package com.csctracker.desktoppluguin.desktop;

import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.net.UnknownHostException;


@Slf4j
public class SystemInfo {
	private static final int SECONDS_TO_IDLE = 30;

	private static String osName = null;

	private static String hostName = null;

	private State state = State.UNKNOWN;


	public static String getOsName() {
		if (osName == null) {
			osName = System.getProperty("os.name");
		}
		return osName;
	}

	public static boolean isWindows() {
		return getOsName().contains("Windows");
	}

	public static String getHostName() {
		if (hostName == null) {
			try {
				InetAddress addr;
				addr = InetAddress.getLocalHost();
				hostName = addr.getHostName();
			} catch (UnknownHostException ex) {
				Core.error(ex);
			}
		}
		return hostName;
	}

	private State getState(){

		int idleSec = Win32IdleTime.getIdleTimeMillisWin32() / 1000;
		return idleSec < SECONDS_TO_IDLE ? State.ONLINE : State.IDLE;

	}

	public boolean isChangedState() {

		State newState = getState();

		if (newState != state) {
			state = newState;
			log.info(state.toString());
			return true;
		}

		return false;

	}

	public boolean isOnline(){
		return getState().equals(State.ONLINE);
	}

	enum State {
		UNKNOWN, ONLINE, IDLE, AWAY
	}
}
