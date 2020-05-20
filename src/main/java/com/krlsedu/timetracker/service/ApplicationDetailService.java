package com.krlsedu.timetracker.service;

import com.krlsedu.timetracker.model.ApplicationDetail;
import com.sun.jna.Native;
import com.sun.jna.platform.win32.WinDef;

import java.util.Date;

public class ApplicationDetailService {
	private static final int MAX_TITLE_LENGTH = 1024;
	
	private static String prevForegroundDetail = null;
	
	private static ApplicationDetail aplicationDetail = null;
	
	private static final String URL = "http://192.168.0.8:8080/api/v1/log-application-detail";
	
	private static final SystemInfo systemStat = new SystemInfo();
	
	public static void generateApplicationDetailInfo(WinDef.HWND foregroundWindow) throws Exception {
		char[] buffer = new char[MAX_TITLE_LENGTH * 2];
		User32DLL.GetWindowTextW(foregroundWindow, buffer, MAX_TITLE_LENGTH);
		String foregroundDeteail = Native.toString(buffer);
		if (!foregroundDeteail.equals(prevForegroundDetail) || systemStat.isChangedState()) {
			if (aplicationDetail != null) {
				
				aplicationDetail.setDateEnd(new Date());
				aplicationDetail.setTimeSpentMillis(aplicationDetail.getDateEnd().getTime() - aplicationDetail.getDateIni().getTime());
				aplicationDetail.setOsName(SystemInfo.getOsName());
				aplicationDetail.setHostName(SystemInfo.getHostName());
				Sender.post(URL, aplicationDetail);
				System.out.println(aplicationDetail);
			}
			if (systemStat.isOnline()) {
				aplicationDetail = new ApplicationDetail();
				aplicationDetail.setName(User32DLL.getImageName(foregroundWindow));
				aplicationDetail.setActivityDetail(foregroundDeteail);
				aplicationDetail.setDateIni(new Date());
			}else{
				aplicationDetail = null;
			}
		}
		prevForegroundDetail = foregroundDeteail;
	}
}
