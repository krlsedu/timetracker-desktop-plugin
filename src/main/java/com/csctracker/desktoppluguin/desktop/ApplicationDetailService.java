package com.csctracker.desktoppluguin.desktop;

import com.csctracker.desktoppluguin.core.Core;
import com.csctracker.desktoppluguin.core.model.ApplicationDetail;
import com.sun.jna.Native;
import com.sun.jna.platform.win32.WinDef;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;

@Slf4j
public class ApplicationDetailService {
    private static final int MAX_TITLE_LENGTH = 1024;
    private static final SystemInfo systemStat = new SystemInfo();
    private static long lastTimeDetailChange = new Date().getTime();

    private static String prevForegroundDetail = null;

    private static ApplicationDetail aplicationDetail = null;

    private static ApplicationDetail aplicationDetailChrome = null;

    private ApplicationDetailService() {
    }

    public static void generateApplicationDetailInfo(WinDef.HWND foregroundWindow) {
        char[] buffer = new char[MAX_TITLE_LENGTH * 2];
        User32DLL.GetWindowTextW(foregroundWindow, buffer, MAX_TITLE_LENGTH);
        String foregroundDeteail = Native.toString(buffer);
        if (!foregroundDeteail.equals(prevForegroundDetail) || systemStat.isChangedState() || forceAttAppDetails()) {
            if (aplicationDetail != null) {
                aplicationDetail.setDateEnd(new Date());
                aplicationDetail.setTimeSpentMillis(aplicationDetail.getDateEnd().getTime() - aplicationDetail.getDateIni().getTime());
                aplicationDetail.setOsName(SystemInfo.getOsName());
                aplicationDetail.setHostName(SystemInfo.getHostName());
                Core.appendHeartbeat(aplicationDetail);
                if (Core.isDebug()) {
                    log.info(aplicationDetail.toString());
                }
            }
            if (systemStat.isOnline()) {
                aplicationDetail = new ApplicationDetail();
                aplicationDetail.setPluginName("desktop");
                aplicationDetail.setProcessName(User32DLL.getImageName(foregroundWindow));
                try {
                    FileVersion.appDetails(aplicationDetail);
                    aplicationDetail.setName(aplicationDetail.getName().replaceAll("s/\\x00//g", "").replaceAll("\u0000", ""));
                } catch (Exception e) {
                    //
                }
                aplicationDetail.setActivityDetail(foregroundDeteail);
                aplicationDetail.setDateIni(new Date());
                lastTimeDetailChange = new Date().getTime();
            } else {
                aplicationDetail = null;
            }
        }
        setChromeInfos(aplicationDetailChrome);
        prevForegroundDetail = foregroundDeteail;
    }

    private static boolean forceAttAppDetails() {
        long timeSpent = new Date().getTime() - lastTimeDetailChange;
        return timeSpent > (Core.QUEUE_TIMEOUT_SECONDS * 1000);
    }

    public static void clearAplicationDetail() {
        aplicationDetail = null;
    }

    public static void setChromeInfos(ApplicationDetail aplicationDetailFromChrome) {
        if (aplicationDetailFromChrome != null) {
            aplicationDetailChrome = aplicationDetailFromChrome;
            if (aplicationDetail != null) {
                if (aplicationDetail.getActivityDetail() != null) {
                    String activityDetail = aplicationDetail.getActivityDetail();
                    activityDetail = activityDetail.replace(" - " + aplicationDetail.getName(), "");
                    if (activityDetail.equals(aplicationDetailFromChrome.getActivityDetail())) {
                        aplicationDetail.setPluginName(aplicationDetailFromChrome.getPluginName());
                        aplicationDetail.setUrl(aplicationDetailFromChrome.getUrl());
                    }
                }
            }
        }
    }
}
