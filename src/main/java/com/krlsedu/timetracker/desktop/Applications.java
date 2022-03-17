package com.krlsedu.timetracker.desktop;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.krlsedu.timetracker.core.ConfigFile;
import com.krlsedu.timetracker.core.TimeTrackerCore;
import com.krlsedu.timetracker.core.model.Heartbeat;
import com.krlsedu.timetracker.model.ApplicationDetail;
import com.krlsedu.timetracker.model.ConfigApp;
import com.krlsedu.timetracker.model.ConfigAppsMonitoring;
import com.krlsedu.timetracker.model.ConfigExtra;
import org.apache.tika.config.TikaConfig;
import org.apache.tika.mime.MimeType;
import org.apache.tika.mime.MimeTypes;

import java.io.File;
import java.util.*;

import static com.krlsedu.timetracker.core.TimeTrackerCore.getCurrentTimestamp;

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
        seConfig(applicationDetail);
        setName(applicationDetail);
        canSendHeartbeat(applicationDetail);
        generateHearbeat(applicationDetail);
    }

    private static void generateHearbeat(ApplicationDetail applicationDetail) {
        Heartbeat h = new Heartbeat();
        h.setEntityType(getEntityType(applicationDetail));
        h.setTimestamp(getCurrentTimestamp(applicationDetail));
        h.setWrite(true);
        h.setCategory(getCategory(applicationDetail));
        h.setHostName(applicationDetail.getHostName());
        if (applicationDetail.getAppConfig().isIde()) {
            h.setIdeName(applicationDetail.getName());
            h.setIdeVersion("");
        } else {
            h.setIdeName(getIdeName(applicationDetail));
            h.setIdeVersion(getIdeVersion(applicationDetail));
        }
        h.setProject(getProject(applicationDetail));
        h.setEntity(getEntity(applicationDetail));
        h.setProcess(applicationDetail.getCompleteProcessName());
        h.setApplicationName(applicationDetail.getName());
        h.setLanguage(getLanguage(applicationDetail));
        h.setSent(applicationDetail.isSendHeartbeat());
        h.setTimeSpentMillis(applicationDetail.getTimeSpentMillis());
        h.setDateTime(applicationDetail.getDateEnd());
        applicationDetail.setHeartbeat(h);
    }

    private static String getLanguage(ApplicationDetail applicationDetail) {
        String activityDetail = applicationDetail.getActivityDetail();
        if (activityDetail != null) {
            try {
                int fim = activityDetail.lastIndexOf(".");
                for (int i = fim; i < activityDetail.length(); i++) {
                    String st = String.valueOf(activityDetail.charAt(i));
                    if (st.equals(" ")) {
                        fim = i;
                        break;
                    }
                }
                String file = activityDetail.substring(0, fim);
                MimeTypes mimeTypes = TikaConfig.getDefaultConfig().getMimeRepository();
                MimeType mimeType = mimeTypes.getMimeType(file);
                String lang = mimeType.getDescription().replaceAll(" source code", "");
                if (lang.trim().isEmpty()) {
                    lang = mimeType.getType().toString().replace("application/", "");
                }
                if (!lang.trim().isEmpty()) {
                    return lang;
                }
            } catch (Exception e) {
                //ignored
            }
        }
        return null;
    }


    private static String getEntity(ApplicationDetail applicationDetail) {
        if (applicationDetail.getAppConfig().getProject() != null) {
            return applicationDetail.getName() + " - " + applicationDetail.getActivityDetail();
        }
        if (applicationDetail.getActivityDetail().trim().isEmpty()) {
            return applicationDetail.getName();
        }
        return applicationDetail.getActivityDetail();
    }

    private static String getIdeVersion(ApplicationDetail applicationDetail) {
        if (applicationDetail.getAppConfig().getProject() == null) {
            String ideVersion = getHbpValue(applicationDetail.getProcess(), "IdeVersion");
            switch (ideVersion) {
                case "blank":
                    return applicationDetail.getName();
                case "none":
                    return applicationDetail.getOsName().replace(" ", "_");
                default:
                    return ideVersion;
            }
        }
        return applicationDetail.getAppConfig().getProject();
    }

    public static String getHbpValue(String processName, String parameter) {
        if (configsMap.get(processName) == null || configsMap.get(processName).get(parameter) == null) {
            return "none";
        } else {
            return configsMap.get(processName).get(parameter);
        }
    }

    private static String getIdeName(ApplicationDetail applicationDetail) {
        if (applicationDetail.getAppConfig().getProject() == null) {
            switch (getHbpValue(applicationDetail.getProcess(), "IdeName")) {
                case "appName":
                    return applicationDetail.getName();
                case "ActivityDetail":
                    return applicationDetail.getActivityDetail();
                default:
                    return "Desktop";
            }
        }
        return applicationDetail.getAppConfig().getProject();
    }

    private static String getProject(ApplicationDetail applicationDetail) {
        if (applicationDetail.getAppConfig().getProject() == null) {
            String project = getHbpValue(applicationDetail.getProcess(), "Project");
            if ("none".equals(project)) {
                return "Using Desktop";
            }
            return project;
        }
        return applicationDetail.getAppConfig().getProject();
    }

    private static String getCategory(ApplicationDetail applicationDetail) {
        if (applicationDetail.getAppConfig().getCategory() == null) {
            return defaultConfigApp.getCategory();
        }
        return applicationDetail.getAppConfig().getCategory();
    }

    private static String getEntityType(ApplicationDetail applicationDetail) {
        if (applicationDetail.getAppConfig().getEntityType() == null) {
            return defaultConfigApp.getEntityType();
        }
        return applicationDetail.getAppConfig().getEntityType();
    }

    private static void canSendHeartbeat(ApplicationDetail applicationDetail) {
        if (allApps) {
            applicationDetail.setSendHeartbeat(!notSendHeartbeat.contains(applicationDetail.getProcess()));
        } else {
            applicationDetail.setSendHeartbeat(sendHeartbeat.contains(applicationDetail.getProcess()));
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

    private static void seConfig(ApplicationDetail applicationDetail) {
        configAppsMonitoring.getMonitorin().stream().filter(configApp -> configApp.getProcessName().equals(applicationDetail.getProcess())).findFirst().ifPresent(applicationDetail::setAppConfig);
        if (applicationDetail.getAppConfig() == null) {
            applicationDetail.setAppConfig(defaultConfigApp);
        }
    }
}
