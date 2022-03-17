package com.krlsedu.timetracker.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.krlsedu.timetracker.core.model.ApplicationDetail;
import com.krlsedu.timetracker.core.model.ConfigApp;
import com.krlsedu.timetracker.core.model.ConfigAppsMonitoring;
import com.krlsedu.timetracker.core.model.ConfigExtra;

import java.io.File;
import java.util.*;

public class Applications {
    private static final Map<String, Map<String, String>> configsMap = new HashMap<>();
    private static List<String> notSendHeartbeat;
    private static List<String> sendHeartbeat;
    private static boolean allApps;
    private static ConfigAppsMonitoring configAppsMonitoring;
    private static ConfigApp defaultConfigApp = null;
    private static String configsCache = "";

    private Applications() {
    }

    public static void init() {
        try {
            TimeTrackerCore.setupDebugging();
            String configs = new Scanner(new File(ConfigFile.getConfigAppsFilePath())).useDelimiter("\\Z").next();
            if (!configs.equals(configsCache)) {
                configsCache = configs;
                config(configs);
            }
        } catch (Exception e) {
            if (configsCache != null) {
                try {
                    config(configsCache);
                } catch (JsonProcessingException ex) {
                    ex.printStackTrace();
                }
            } else {
                e.printStackTrace();
            }
        }

        if (defaultConfigApp == null) {
            defaultConfigApp = new ConfigApp();
            defaultConfigApp.setIde(false);
            defaultConfigApp.setCategory("browsing");
            defaultConfigApp.setEntityType("app");
        }
    }

    private static void config(String configs) throws JsonProcessingException {
        configAppsMonitoring = TimeTrackerCore.getObjectMapper().readValue(configs, ConfigAppsMonitoring.class);
        if (configAppsMonitoring != null) {
            allApps = configAppsMonitoring.isAllApps();
            notSendHeartbeat = new ArrayList<>();
            sendHeartbeat = new ArrayList<>();
            getNotMonitorin();
            getMonitorin();
            setConfExtra();
        }
    }

    private static void getNotMonitorin() {
        if (configAppsMonitoring.getNotMonitorin() != null) {
            configAppsMonitoring.getNotMonitorin().stream().map(ConfigApp::getProcessName).filter(processName -> !notSendHeartbeat.contains(processName)).forEach(notSendHeartbeat::add);
        }
    }

    private static void getMonitorin() {
        if (configAppsMonitoring.getMonitorin() != null) {
            configAppsMonitoring.getMonitorin().stream().filter(configApp -> !sendHeartbeat.contains(configApp.getAppName())).forEach(configApp -> sendHeartbeat.add(configApp.getProcessName()));
        } else {
            configAppsMonitoring.setMonitorin(new ArrayList<>());
        }
    }

    public static void setConfExtra() {
        for (ConfigExtra configsExtra : configAppsMonitoring.getConfigsExtras()) {
            if (configsExtra.getProcessNames() != null) {
                for (String processName : configsExtra.getProcessNames()) {
                    configsMap.computeIfAbsent(processName, k -> new HashMap<>());
                    configsMap.get(processName).put(configsExtra.getHeartbeatParam(), configsExtra.getValue());
                }
            }
        }
    }

    public static void checkApplicationDetail(ApplicationDetail applicationDetail) {
        init();
        setProcessName(applicationDetail);
        setConfig(applicationDetail);
        setName(applicationDetail);
    }

    public static String getHbpValue(String processName, String parameter) {
        if (configsMap.get(processName) == null || configsMap.get(processName).get(parameter) == null) {
            return "none";
        } else {
            return configsMap.get(processName).get(parameter);
        }
    }

    public static void setProcessName(ApplicationDetail applicationDetail) {
        String name = applicationDetail.getName();
        applicationDetail.setCompleteProcessName(name);
        name = name.substring(name.lastIndexOf("\\") + 1);
        try {
            applicationDetail.setProcess(name.substring(0, name.lastIndexOf(".")));
        } catch (Exception e) {
            //ignored
        }
    }

    public static void setName(ApplicationDetail applicationDetail) {
        if (applicationDetail.getAppConfig().getAppName() != null) {
            applicationDetail.setName(applicationDetail.getAppConfig().getAppName());
        } else {
            applicationDetail.setName(applicationDetail.getProcess());
        }
    }

    private static void setConfig(ApplicationDetail applicationDetail) {
        configAppsMonitoring.getMonitorin().stream().filter(configApp -> configApp.getProcessName().equals(applicationDetail.getProcess())).findFirst().ifPresent(applicationDetail::setAppConfig);
        if (applicationDetail.getAppConfig() == null) {
            applicationDetail.setAppConfig(defaultConfigApp);
        }
    }
}
