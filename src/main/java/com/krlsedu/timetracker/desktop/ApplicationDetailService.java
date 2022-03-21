package com.krlsedu.timetracker.desktop;

import com.krlsedu.timetracker.core.TimeTrackerCore;
import com.krlsedu.timetracker.core.model.ApplicationDetail;
import com.sun.jna.Native;
import com.sun.jna.platform.win32.WinDef;

import java.io.IOException;
import java.util.Date;

public class ApplicationDetailService {
	private ApplicationDetailService() {
	}
	
	private static final int MAX_TITLE_LENGTH = 1024;
	
	private static long lastTimeDetailChange = new Date().getTime();
	
	private static String prevForegroundDetail = null;
	
	private static ApplicationDetail aplicationDetail = null;
	
	private static final SystemInfo systemStat = new SystemInfo();
	
	public static void generateApplicationDetailInfo(WinDef.HWND foregroundWindow)  {
		char[] buffer = new char[MAX_TITLE_LENGTH * 2];
		User32DLL.GetWindowTextW(foregroundWindow, buffer, MAX_TITLE_LENGTH);
		String foregroundDeteail = Native.toString(buffer);
		if (!foregroundDeteail.equals(prevForegroundDetail) || systemStat.isChangedState() || forceAttAppDetails()) {
			if (aplicationDetail != null) {
				aplicationDetail.setDateEnd(new Date());
				aplicationDetail.setTimeSpentMillis(aplicationDetail.getDateEnd().getTime() - aplicationDetail.getDateIni().getTime());
				aplicationDetail.setOsName(SystemInfo.getOsName());
				aplicationDetail.setHostName(SystemInfo.getHostName());
				TimeTrackerCore.appendHeartbeat(aplicationDetail);
				if (TimeTrackerCore.isDebug()) {
					TimeTrackerCore.log.info(aplicationDetail);
				}
			}
			if (systemStat.isOnline()) {
				aplicationDetail = new ApplicationDetail();
				aplicationDetail.setPluginName("desktop");
				aplicationDetail.setProcessName(User32DLL.getImageName(foregroundWindow));
				try {
					FileVersion.appDetails(aplicationDetail);
				} catch (IOException e) {
					//
				}
				aplicationDetail.setActivityDetail(foregroundDeteail);
				aplicationDetail.setDateIni(new Date());
				lastTimeDetailChange = new Date().getTime();
			} else {
				aplicationDetail = null;
			}
		}
		prevForegroundDetail = foregroundDeteail;
	}
	
	private static boolean forceAttAppDetails() {
		long timeSpent = new Date().getTime() - lastTimeDetailChange;
		return timeSpent > (TimeTrackerCore.QUEUE_TIMEOUT_SECONDS * 1000);
	}
	
	public static void clearAplicationDetail() {
		aplicationDetail = null;
	}
}
