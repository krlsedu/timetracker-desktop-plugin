/* ==========================================================
File:        Dependencies.java
Description: Manages plugin dependencies.
Maintainer:  WakaTime <support@wakatime.com>
License:     BSD, see LICENSE for more details.
Website:     https://wakatime.com/
===========================================================*/

package com.krlsedu.timetracker.core;

import java.io.File;

public class Dependencies {

    private static String resourcesWakatimeLocation = null;

    public static String getResourcesLocationWakatime() {
        if (Dependencies.resourcesWakatimeLocation != null) return Dependencies.resourcesWakatimeLocation;

        if (isWindows()) {
            File windowsHome = new File(System.getenv("USERPROFILE"));
            File resourcesFolder = new File(windowsHome, ".wakatime");
            Dependencies.resourcesWakatimeLocation = resourcesFolder.getAbsolutePath();
            return Dependencies.resourcesWakatimeLocation;
        }

        File userHomeDir = new File(System.getProperty("user.home"));
        File resourcesFolder = new File(userHomeDir, ".wakatime");
        Dependencies.resourcesWakatimeLocation = resourcesFolder.getAbsolutePath();
        return Dependencies.resourcesWakatimeLocation;
    }

    public static String getCLILocation() {
        if (System.getenv("WAKATIME_CLI_LOCATION") != null && !System.getenv("WAKATIME_CLI_LOCATION").trim().isEmpty()) {
            File wakatimeCLI = new File(System.getenv("WAKATIME_CLI_LOCATION"));
            if (wakatimeCLI.exists()) {
                TimeTrackerCore.log.debug("Using $WAKATIME_CLI_LOCATION as CLI Executable: " + wakatimeCLI);
                return System.getenv("WAKATIME_CLI_LOCATION");
            }
        }

        String ext = isWindows() ? ".exe" : "";
        if (!isStandalone()) {
            return combinePaths(getResourcesLocationWakatime(), "wakatime-cli-" + platform() + "-" + architecture() + ext);
        }
        return combinePaths(getResourcesLocationWakatime(), "wakatime-cli", "wakatime-cli" + ext);
    }

    public static boolean isStandalone() {
        String setting = ConfigFile.get("settings", "standalone", ConfigFile.getConfigFilePathWakaTime());
        return setting == null || !setting.equals("false");
    }

    public static boolean isTimeTrackerToWakatime() {
        String setting = ConfigFile.get("settings", "timeTrackerToWakatime");
        return !(setting == null || setting.equals("false"));
    }

    public static boolean isTimeTrackerOffline() {
        String setting = ConfigFile.get("settings", "timeTrackerOffline");
        return !(setting == null || setting.equals("false"));
    }

    public static String urlTimeTracker() {
        return ConfigFile.get("settings", "urlTimeTracker");
    }

    public static boolean isWindows() {
        return System.getProperty("os.name").contains("Windows");
    }

    public static String platform() {
        if (isWindows()) return "windows";
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("mac") || os.contains("darwin")) return "darwin";
        if (os.contains("linux")) return "linux";
        return os;
    }

    public static String architecture() {
        String arch = System.getProperty("os.arch");
        if (arch.contains("386") || arch.contains("32")) return "386";
        if (arch.equals("aarch64")) return "arm64";
        if (platform().equals("darwin") && arch.contains("arm")) return "arm64";
        if (arch.contains("64")) return "amd64";
        return arch;
    }

    public static String combinePaths(String... args) {
        File path = null;
        for (String arg : args) {
            if (arg != null) {
                if (path == null)
                    path = new File(arg);
                else
                    path = new File(path, arg);
            }
        }
        if (path == null)
            return null;
        return path.toString();
    }
}
