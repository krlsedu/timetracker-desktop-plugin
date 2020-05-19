package com.krlsedu.timetracker;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.krlsedu.timetracker.model.Application;
import com.krlsedu.timetracker.model.ApplicationDetail;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Core {
	private static final int MAX_TITLE_LENGTH = 1024;
	
	private static final int waitTime = 500;
	
	private static final List<Application> aplicationList = new ArrayList<>();
	private static final List<ApplicationDetail> aplicationDetailList = new ArrayList<>();
	
	public static void start() throws Exception {
		WinDef.HWND prevForegroundWindow = null;
		String prevForegroundDetail = null;
		Date now;
		Date ant;
		Application aplication = null;
		ApplicationDetail aplicationDetail = null;
		
		ObjectMapper objectMapper = new ObjectMapper();
		String hostName = null;
		String osName = System.getProperty("os.name");
		try {
			InetAddress addr;
			addr = InetAddress.getLocalHost();
			hostName = addr.getHostName();
		} catch (UnknownHostException ex) {
			System.out.println("Hostname can not be resolved");
		}
		while (true) {
			Thread.sleep(waitTime);
			
			WinDef.HWND foregroundWindow = User32.INSTANCE.GetForegroundWindow();
			
			if (foregroundWindow == null) {
				continue;
			}
			String fgImageName = getImageName(foregroundWindow);
			if (fgImageName != null) {
				if (foregroundWindow.equals(prevForegroundWindow)) {
					aplication.setTimeSpentMillis(new Date().getTime() - aplication.getDateIni().getTime());
				} else {
					if (prevForegroundWindow != null) {
						aplication.setDateEnd(new Date());
						aplication.setTimeSpentMillis(aplication.getDateEnd().getTime() - aplication.getDateIni().getTime());
						aplication.setOsName(osName);
						aplication.setHostName(hostName);
						if (!Conector.post("http://192.168.0.8:8080/api/v1/log-application", objectMapper.writeValueAsString(aplication))) {
							aplicationList.add(aplication);
						}
						System.out.println(aplication);
						aplication = new Application();
						aplication.setName(fgImageName);
						aplication.setDateIni(new Date());
					} else {
						aplication = new Application();
						aplication.setName(fgImageName);
						aplication.setDateIni(new Date());
					}
					prevForegroundWindow = foregroundWindow;
				}
				
				
				char[] buffer = new char[MAX_TITLE_LENGTH * 2];
				User32DLL.GetWindowTextW(foregroundWindow, buffer, MAX_TITLE_LENGTH);
				String foregroundDeteail = Native.toString(buffer);
				if (foregroundDeteail.equals(prevForegroundDetail)) {
					if (aplicationDetail == null) {
						aplicationDetail = new ApplicationDetail();
						aplicationDetail.setName(aplication.getName());
						aplicationDetail.setActivityDetail(foregroundDeteail);
						aplicationDetail.setDateIni(new Date());
					} else {
						aplicationDetail.setTimeSpentMillis(new Date().getTime() - aplicationDetail.getDateIni().getTime());
					}
				} else {
					if (aplicationDetail != null) {
						
						aplicationDetail.setDateEnd(new Date());
						aplicationDetail.setTimeSpentMillis(aplicationDetail.getDateEnd().getTime() - aplicationDetail.getDateIni().getTime());
						aplicationDetail.setOsName(osName);
						aplicationDetail.setHostName(hostName);
						if (!Conector.post("http://192.168.0.8:8080/api/v1/log-application-detail", objectMapper.writeValueAsString(aplicationDetail))) {
							aplicationDetailList.add(aplicationDetail);
						}
						System.out.println(aplicationDetail);
						aplicationDetail = new ApplicationDetail();
						aplicationDetail.setName(aplication.getName());
						aplicationDetail.setActivityDetail(foregroundDeteail);
						aplicationDetail.setDateIni(new Date());
					}
				}
				prevForegroundDetail = foregroundDeteail;
			}
			List<Application> applicationListTemp = new ArrayList<>();
			for (Application app :
					aplicationList) {
				if (Conector.post("http://192.168.0.8:8080/api/v1/log-application", objectMapper.writeValueAsString(app))) {
					applicationListTemp.add(app);
				}
			}
			aplicationList.removeAll(applicationListTemp);
			
			List<ApplicationDetail> applicationDetailListTemp = new ArrayList<>();
			for (ApplicationDetail app :
					aplicationDetailList) {
				if (Conector.post("http://192.168.0.8:8080/api/v1/log-application-detail", objectMapper.writeValueAsString(app))) {
					applicationDetailListTemp.add(app);
				}
			}
			aplicationDetailList.removeAll(applicationDetailListTemp);
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
