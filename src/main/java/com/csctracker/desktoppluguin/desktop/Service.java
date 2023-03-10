package com.csctracker.desktoppluguin.desktop;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class Service {

    public static final String WIN_ID_CMD = "xprop -root | grep " + "\"_NET_ACTIVE_WINDOW(WINDOW)\"" + "|cut -d ' ' -f 5";
    public static final String WIN_INFO_CMD_PREFIX = "xwininfo -id ";
    public static final String WIN_INFO_CMD_MID = " |awk 'BEGIN {FS=\"\\\"\"}/xwininfo: Window id/{print $2}'";

    public static String windowTitle() {
        Service windowAndProcessInfo4Linux = new Service();

        String winId = windowAndProcessInfo4Linux.execShellCmd(WIN_ID_CMD);
        String winInfoMcd = windowAndProcessInfo4Linux.windowInfoCmd(winId);
        String windowTitle = windowAndProcessInfo4Linux.execShellCmd(winInfoMcd);
        return windowTitle;
    }

    public static String procName() {
        Service windowAndProcessInfo4Linux = new Service();
        try {

            var winId = windowAndProcessInfo4Linux.execShellCmd("xprop -id $(xprop -root 32x '\t$0' _NET_ACTIVE_WINDOW | cut -f 2) _NET_WM_PID");
            winId = winId.split("=")[1].trim();
            var windowTitle = windowAndProcessInfo4Linux.execShellCmd("ls -l /proc/" + winId + "/exe");
            windowTitle = windowTitle.split("->")[1];

            return windowTitle;
        } catch (Exception e) {
            return "Not found";
        }

    }


    public static String appName() {
        Service windowAndProcessInfo4Linux = new Service();

        String winId = windowAndProcessInfo4Linux.execShellCmd("xprop -id $(xprop -root 32x '\t$0' _NET_ACTIVE_WINDOW | cut -f 2) WM_CLASS | awk '{print $4}'");
        try {
            return winId.substring(1, winId.length() - 1);
        } catch (Exception e) {
            winId = windowAndProcessInfo4Linux.execShellCmd("xprop -id $(xprop -root 32x '\t$0' _NET_ACTIVE_WINDOW | cut -f 2) WM_CLASS | awk '{print $3}'");
            try {
                return winId.substring(1, winId.length() - 1).split(",")[0].trim().substring(1, winId.length() - 1);
            } catch (Exception e1) {
                winId = windowAndProcessInfo4Linux.execShellCmd("xprop -id $(xprop -root 32x '\t$0' _NET_ACTIVE_WINDOW | cut -f 2) WM_CLASS");
                if (winId.trim().isEmpty()) {
                    return "Not found";
                }
                return winId.replace("WM_CLASS(STRING) = ", "");
            }
        }
    }

    public static String distroName() {
        Service windowAndProcessInfo4Linux = new Service();
        String winId = windowAndProcessInfo4Linux.execShellCmd("lsb_release -d");
        winId = winId.replace("Description:", "").trim();
        return winId;
    }

    public String execShellCmd(String cmd) {
        try {

            Runtime runtime = Runtime.getRuntime();
            Process process = runtime.exec(new String[]{"/bin/bash", "-c", cmd});
            int exitValue = process.waitFor();
            BufferedReader buf = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line = "";
            String output = "";
            while ((line = buf.readLine()) != null) {
                output = line;
            }
            return output;
        } catch (Exception e) {
            System.out.println(e);
            return null;
        }
    }

    public String windowInfoCmd(String winId) {
        if (null != winId && !"".equalsIgnoreCase(winId)) {
            return WIN_INFO_CMD_PREFIX + winId + WIN_INFO_CMD_MID;
        }
        return null;
    }
}