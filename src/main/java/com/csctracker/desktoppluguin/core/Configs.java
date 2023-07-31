

package com.csctracker.desktoppluguin.core;

import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.charset.StandardCharsets;

@Slf4j
public class Configs {
    private static final String TIMETRACKER_CFG = "cscTracker.cfg";
    private static String resourcesLocation = null;
    private static String cscTrackerCachedConfigFile = null;

    private Configs() {
    }

    private static String getConfigFilePath() {
        Configs.cscTrackerCachedConfigFile = new File(getResourcesLocation(), Configs.TIMETRACKER_CFG).getAbsolutePath();
        if (Configs.isDebug()) {
            log.debug("Using $HOME for config folder: " + Configs.cscTrackerCachedConfigFile);
        }
        return Configs.cscTrackerCachedConfigFile;
    }

    public static String getUserFolderLocation() {

        if (Configs.isWindows()) {
            File windowsHome = new File(System.getenv("USERPROFILE"));
            return windowsHome.getAbsolutePath();
        }
        File userHomeDir = new File(System.getProperty("user.home"));
        return userHomeDir.getAbsolutePath();

    }

    public static String getResourcesLocation() {
        if (resourcesLocation != null) return resourcesLocation;

        if (Configs.isWindows()) {
            File windowsHome = new File(System.getenv("USERPROFILE"));
            File resourcesFolder = new File(windowsHome, ".cscTracker");
            resourcesLocation = resourcesFolder.getAbsolutePath();
            return resourcesLocation;
        }

        File userHomeDir = new File(System.getProperty("user.home"));
        File resourcesFolder = new File(userHomeDir, ".cscTracker");
        resourcesLocation = resourcesFolder.getAbsolutePath();
        return resourcesLocation;
    }


    public static String get(String section, String key) {
        return get(section, key, Configs.getConfigFilePath());
    }

    public static String get(String section, String key, String file) {
        String val = null;
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String currentSection = "";
            try {
                String line = br.readLine();
                while (line != null) {
                    if (line.trim().startsWith("[") && line.trim().endsWith("]")) {
                        currentSection = line.trim().substring(1, line.trim().length() - 1).toLowerCase();
                    } else {
                        if (section.toLowerCase().equals(currentSection)) {
                            String[] parts = line.split("=");
                            if (parts.length == 2 && parts[0].trim().equals(key)) {
                                val = parts[1].trim();
                                br.close();
                                return val;
                            }
                        }
                    }
                    line = br.readLine();
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (FileNotFoundException e1) { /* ignored */ }
        return val;
    }

    public static void set(String section, String key, String val) {
        String file = Configs.getConfigFilePath();
        StringBuilder contents = new StringBuilder();
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            try {
                String currentSection = "";
                String line = br.readLine();
                boolean found = false;
                while (line != null) {
                    if (line.trim().startsWith("[") && line.trim().endsWith("]")) {
                        if (section.toLowerCase().equals(currentSection) && !found) {
                            contents.append(key).append(" = ").append(val).append("\n");
                            found = true;
                        }
                        currentSection = line.trim().substring(1, line.trim().length() - 1).toLowerCase();
                        contents.append(line).append("\n");
                    } else {
                        if (section.toLowerCase().equals(currentSection)) {
                            String[] parts = line.split("=");
                            String currentKey = parts[0].trim();
                            if (currentKey.equals(key)) {
                                if (!found) {
                                    contents.append(key).append(" = ").append(val).append("\n");
                                    found = true;
                                }
                            } else {
                                contents.append(line).append("\n");
                            }
                        } else {
                            contents.append(line).append("\n");
                        }
                    }
                    line = br.readLine();
                }
                if (!found) {
                    if (!section.toLowerCase().equals(currentSection)) {
                        contents.append("[").append(section.toLowerCase()).append("]\n");
                    }
                    contents.append(key).append(" = ").append(val).append("\n");
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (FileNotFoundException e1) {

            // cannot read config file, so create it
            contents = new StringBuilder();
            contents.append("[").append(section.toLowerCase()).append("]\n");
            contents.append(key).append(" = ").append(val).append("\n");
        }

        PrintWriter writer = null;
        try {
            writer = new PrintWriter(file, StandardCharsets.UTF_8);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (writer != null) {
            writer.print(contents);
            writer.close();
        }
    }

    public static boolean isCscTrackerOffline() {
        String setting = Configs.get("settings", "cscTrackerOffline");
        return !(setting == null || setting.equals("false"));
    }

    public static String urlProxy() {
        return Configs.get("settings", "urlProxy");
    }

    public static Integer portProxy() {
        try {
            return Integer.parseInt(Configs.get("settings", "portProxy"));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static String userProxy() {
        return Configs.get("settings", "userProxy");
    }

    public static String passwordProxy() {
        return Configs.get("settings", "passwordProxy");
    }


    public static Integer heartbeatMaxTimeSeconds() {
        try {
            var heartbeatMaxTimeSeconds = SqlLitle.getConfig("heartbeatMaxTimeSeconds");
            if (heartbeatMaxTimeSeconds == null) {
                return 60;
            }
            return Integer.parseInt(heartbeatMaxTimeSeconds);
        } catch (NumberFormatException e) {
            return 60;
        }
    }

    public static String urlCscTracker() {
        var urlCscTracker = SqlLitle.getConfig("urlCscTracker");
        if (urlCscTracker == null) {
            urlCscTracker = SqlLitle.DEFAULT_URL_USAGE_INFO;
        }
        return getDomain() + urlCscTracker;
    }

    public static String getDomain() {
        var domain = SqlLitle.getConfig("domain");
        if (domain == null) {
            domain = SqlLitle.DEFAULT_URL_DOMAIN;
        }
        domain = domain.replace("subdomain", getSubDomain());
        return domain;
    }

    public static void changeSubDomain() {
        var posicao = SqlLitle.getConfig("subdomainActive");
        if (posicao == null) {
            posicao = "1";
        }
        int pos = Integer.parseInt(posicao);
        var subDomainsSt = SqlLitle.getConfig("subdomains");
        if (subDomainsSt == null) {
            subDomainsSt = SqlLitle.DEFAULT_SUBDOMAINS;
        }
        var subDomains = subDomainsSt.split(",");
        if (pos >= subDomains.length) {
            pos = 1;
        } else {
            pos++;
        }
        SqlLitle.saveConfig("subdomainActive", String.valueOf(pos));
    }

    public static String getSubDomain() {
        var posicao = SqlLitle.getConfig("subdomainActive");
        if (posicao == null) {
            posicao = "1";
        }
        int pos = Integer.parseInt(posicao);
        var subDomainsSt = SqlLitle.getConfig("subdomains");
        if (subDomainsSt == null) {
            subDomainsSt = SqlLitle.DEFAULT_SUBDOMAINS;
        }
        var subDomains = subDomainsSt.split(",");
        if (pos > subDomains.length) {
            pos = 1;
            SqlLitle.saveConfig("subdomainActive", String.valueOf(pos));
        }
        return subDomains[pos - 1];
    }

    public static String urlNotifySync() {
        var urlNotifySync = SqlLitle.getConfig("urlNotifySync");
        if (urlNotifySync == null) {
            urlNotifySync = SqlLitle.DEFAULT_URL_NOTIFY_SYNC;
        }
        return getDomain() + urlNotifySync;
    }

    public static String tokenCscTracker() {
        var tokenCscTracker = SqlLitle.getConfig("tokenCscTracker");
        if (tokenCscTracker == null) {
            tokenCscTracker = "";
        }
        return tokenCscTracker;
    }

    public static String dbNotificationsName() {
        return getUserFolderLocation() + "\\AppData\\Local\\Microsoft\\Windows\\Notifications\\wpndatabase.db";
    }

    public static Long lastArrivalTime() {
        try {
            var lastArrivalTime = SqlLitle.getConfig("lastArrivalTime");
            if (lastArrivalTime == null) {
                return 0L;
            }
            return Long.parseLong(lastArrivalTime);
        } catch (NumberFormatException e) {
            return 0L;
        }
    }

    public static void lastArrivalTime(Long lastArrivalTime) {
        SqlLitle.saveConfig("lastArrivalTime", lastArrivalTime.toString());
    }

    public static boolean isWindows() {
        return System.getProperty("os.name").contains("Windows");
    }

    public static boolean isDebug() {
        return "true".equalsIgnoreCase(SqlLitle.getConfig("debug"));
    }
}
