package com.krlsedu.timetracker;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.krlsedu.timetracker.model.AplicationStat;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Core {
	private static final int MAX_TITLE_LENGTH = 1024;
	
	private static final int waitTime = 500;
	
	private static final List<AplicationStat> aplicationStatList = new ArrayList<>();
	
	public static void start() throws Exception {
		WinDef.HWND prevForegroundWindow = null;
		Date now;
		Date ant;
		AplicationStat aplicationStat = null;
		
		ObjectMapper objectMapper = new ObjectMapper();
		while (true) {
			Thread.sleep(waitTime);
			
			WinDef.HWND foregroundWindow = User32.INSTANCE.GetForegroundWindow();
			
			if (foregroundWindow == null) {
				continue;
			}
			String fgImageName = getImageName(foregroundWindow);
			if (fgImageName != null) {
				if (foregroundWindow.equals(prevForegroundWindow)) {
					aplicationStat.setTimeSpentMilis(aplicationStat.getTimeSpentMilis() + waitTime);
				} else {
					if (prevForegroundWindow != null) {
						aplicationStat.setDateEnd(new Date());
						System.out.println(Conector.post("http://127.0.0.1:8080/api/v1/log-aplication", objectMapper.writeValueAsString(aplicationStat)));
						System.out.println(aplicationStat);
						aplicationStatList.add(aplicationStat);
						aplicationStat = new AplicationStat();
						aplicationStat.setName(fgImageName);
						char[] buffer = new char[MAX_TITLE_LENGTH * 2];
						User32DLL.GetWindowTextW(User32DLL.GetForegroundWindow(), buffer, MAX_TITLE_LENGTH);
						System.out.println("Janela ativa: " + Native.toString(buffer));
					} else {
						aplicationStat = new AplicationStat();
					}
					prevForegroundWindow = foregroundWindow;
				}
			}
		}
		
	}
	
	private static String getImageName(WinDef.HWND window) {
		// Get the process ID of the window
		IntByReference procId = new IntByReference();
		User32.INSTANCE.GetWindowThreadProcessId(window, procId);
		
		// Open the process to get permissions to the image name
		WinNT.HANDLE procHandle = Kernel32.INSTANCE.OpenProcess(
				Kernel32.PROCESS_QUERY_LIMITED_INFORMATION,
				false,
				procId.getValue()
		);
		
		// Get the image name
		char[] buffer = new char[4096];
		IntByReference bufferSize = new IntByReference(buffer.length);
		boolean success = Kernel32.INSTANCE.QueryFullProcessImageName(procHandle, 0, buffer, bufferSize);
		
		// Clean up: close the opened process
		Kernel32.INSTANCE.CloseHandle(procHandle);
		
		return success ? new String(buffer, 0, bufferSize.getValue()) : null;
	}
	
	static class Psapi {
		static {
			Native.register("psapi");
		}
		
		public static native int GetModuleBaseNameW(Pointer hProcess, Pointer hmodule, char[] lpBaseName, int size);
	}
	
	static class User32DLL {
		static {
			Native.register("user32");
		}
		
		public static native int GetWindowThreadProcessId(WinDef.HWND hWnd, PointerByReference pref);
		
		public static native WinDef.HWND GetForegroundWindow();
		
		public static native int GetWindowTextW(WinDef.HWND hWnd, char[] lpString, int nMaxCount);
	}
}
