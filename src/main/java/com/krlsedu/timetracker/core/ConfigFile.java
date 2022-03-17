/* ==========================================================
File:        ConfigFile.java
Description: Read and write settings from the INI config file.
Maintainer:  WakaTime <support@wakatime.com>
License:     BSD, see LICENSE for more details.
Website:     https://wakatime.com/
===========================================================*/

package com.krlsedu.timetracker.core;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class ConfigFile {
    private static final String WAKATIME_CFG = ".wakatime.cfg";
    private static final String TIMETRACKER_CFG = ".timeTracker.cfg";
    private static final String WAKATIME_APPS_MONITORIN_JSON = ".wakatime-desktop-plugin-config.json";
    private static String resourcesLocation = null;
    private static String wakaTimeCachedConfigFile = null;
    private static String timeTrackerCachedConfigFile = null;
    private static String cachedConfigFileConfigApps = null;
    private static String apiKey = "";

    private ConfigFile() {
    }

    private static String getConfigFilePath() {
        ConfigFile.timeTrackerCachedConfigFile = new File(getResourcesLocation(), ConfigFile.TIMETRACKER_CFG).getAbsolutePath();
        if (TimeTrackerCore.isDebug()) {
            TimeTrackerCore.log.debug("Using $HOME for config folder: " + ConfigFile.timeTrackerCachedConfigFile);
        }
        return ConfigFile.timeTrackerCachedConfigFile;
    }


    public static String getResourcesLocation() {
        if (resourcesLocation != null) return resourcesLocation;

        if (Dependencies.isWindows()) {
            File windowsHome = new File(System.getenv("USERPROFILE"));
            File resourcesFolder = new File(windowsHome, ".timeTracker");
            resourcesLocation = resourcesFolder.getAbsolutePath();
            return resourcesLocation;
        }

        File userHomeDir = new File(System.getProperty("user.home"));
        File resourcesFolder = new File(userHomeDir, ".timeTracker");
        resourcesLocation = resourcesFolder.getAbsolutePath();
        return resourcesLocation;
    }

    public static String getConfigFilePathWakaTime() {
        ConfigFile.wakaTimeCachedConfigFile = new File(System.getProperty("user.home"), ConfigFile.WAKATIME_CFG).getAbsolutePath();
        if (TimeTrackerCore.isDebug()) {
            TimeTrackerCore.log.debug("Using $HOME for config folder: " + ConfigFile.wakaTimeCachedConfigFile);
        }
        return ConfigFile.wakaTimeCachedConfigFile;
    }

    public static String getConfigAppsFilePath() {
        ConfigFile.cachedConfigFileConfigApps = new File(System.getProperty("user.home"), ConfigFile.WAKATIME_APPS_MONITORIN_JSON).getAbsolutePath();
        if (TimeTrackerCore.isDebug()) {
            TimeTrackerCore.log.debug("Using $HOME for config folder: " + ConfigFile.cachedConfigFileConfigApps);
        }
        return ConfigFile.cachedConfigFileConfigApps;
    }


    public static String get(String section, String key) {
        return get(section, key, ConfigFile.getConfigFilePath());
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
        String file = ConfigFile.getConfigFilePath();
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

    public static String getApiKey() {
        if (!ConfigFile.apiKey.equals("")) {
            return ConfigFile.apiKey;
        }

        String apiKey = get("settings", "api_key", ConfigFile.getConfigFilePathWakaTime());
        if (apiKey == null) apiKey = "";

        ConfigFile.apiKey = apiKey;
        return apiKey;
    }

}
