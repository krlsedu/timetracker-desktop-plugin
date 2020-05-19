package com.krlsedu.timetracker;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;

import java.util.Date;
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