package com.krlsedu.timetracker.service;

import com.krlsedu.timetracker.model.Application;
import com.sun.jna.platform.win32.WinDef;

import java.util.Date;

public class ApplicationService {
	private static WinDef.HWND prevForegroundWindow = null;
	
	private static Application aplication = null;
	
	private static final String URL = "http://192.168.0.8:8080/api/v1/log-application";
	
	private static final SystemInfo systemStat = new SystemInfo();
	
	public static void generateApplicationInfo(WinDef.HWND foregroundWindow) throws Exception {
		if (!foregroundWindow.equals(prevForegroundWindow) || systemStat.isChangedToIdle()) {
			if (prevForegroundWindow != null) {
				
				aplication.setDateEnd(new Date());
				aplication.setTimeSpentMillis(aplication.getDateEnd().getTime() - aplication.getDateIni().getTime());
				aplication.setOsName(SystemInfo.getOsName());
				aplication.setHostName(SystemInfo.getHostName());
				Sender.post(URL, aplication);
				System.out.println(aplication);
				
			}
			
			aplication = new Application();
			aplication.setName(User32DLL.getImageName(foregroundWindow));
			aplication.setDateIni(new Date());
			prevForegroundWindow = foregroundWindow;
		}
	}
}
