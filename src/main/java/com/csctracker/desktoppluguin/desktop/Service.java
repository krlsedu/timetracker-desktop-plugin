package com.csctracker.desktoppluguin.desktop;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class Service {

    public static final String WIN_ID_CMD = "xprop -root | grep " + "\"_NET_ACTIVE_WINDOW(WINDOW)\"" + "|cut -d ' ' -f 5";
    public static final String WIN_INFO_CMD_PREFIX = "xwininfo -id ";
    public static final String WIN_INFO_CMD_MID = " |awk 'BEGIN {FS=\"\\\"\"}/xwininfo: Window id/{print $2}' | sed 's/-[^-]*$//g'";

    public static String windowTitle() {
        Service windowAndProcessInfo4Linux = new Service();

        String winId = windowAndProcessInfo4Linux.execShellCmd(WIN_ID_CMD);
        String winInfoMcd = windowAndProcessInfo4Linux.windowInfoCmd(winId);
        String windowTitle = windowAndProcessInfo4Linux.execShellCmd(winInfoMcd);
        return windowTitle;
    }

    public String execShellCmd(String cmd) {
        try {

            Runtime runtime = Runtime.getRuntime();
            Process process = runtime.exec(new String[]{"/bin/bash", "-c", cmd});
            int exitValue = process.waitFor();
            System.out.println("exit value: " + exitValue);
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