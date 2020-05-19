package com.krlsedu.timetracker.service;

import com.krlsedu.timetracker.model.ApplicationDetail;
import com.sun.jna.Native;
import com.sun.jna.platform.win32.WinDef;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ApplicationDetailService {
	private static final int MAX_TITLE_LENGTH = 1024;
	
	private static String prevForegroundDetail = null;
	
	private static ApplicationDetail aplicationDetail = null;
	
	public static void generateApplicationDetail(WinDef.HWND foregroundWindow) throws Exception{
		char[] buffer = new char[MAX_TITLE_LENGTH * 2];
		User32DLL.GetWindowTextW(foregroundWindow, buffer, MAX_TITLE_LENGTH);
		String foregroundDeteail = Native.toString(buffer);
		if (!foregroundDeteail.equals(prevForegroundDetail)) {
			if (aplicationDetail != null) {
				
				aplicationDetail.setDateEnd(new Date());
				aplicationDetail.setTimeSpentMillis(aplicationDetail.getDateEnd().getTime() - aplicationDetail.getDateIni().getTime());
				aplicationDetail.setOsName(SystenInfo.getOsName());
				aplicationDetail.setHostName(SystenInfo.getHostName());
				Sender.post("http://192.168.0.8:8080/api/v1/log-application-detail", Sender.getObjectMapper().writeValueAsString(aplicationDetail));
				System.out.println(aplicationDetail);
				aplicationDetail = new ApplicationDetail();
				aplicationDetail.setName(User32DLL.getImageName(foregroundWindow));
				aplicationDetail.setActivityDetail(foregroundDeteail);
				aplicationDetail.setDateIni(new Date());
				
			} else {
				
				aplicationDetail = new ApplicationDetail();
				aplicationDetail.setName(User32DLL.getImageName(foregroundWindow));
				aplicationDetail.setActivityDetail(foregroundDeteail);
				aplicationDetail.setDateIni(new Date());
				
			}
		}
		prevForegroundDetail = foregroundDeteail;
	}
}
