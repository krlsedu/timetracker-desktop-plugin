package com.krlsedu.timetracker.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.krlsedu.timetracker.ConfigFile;
import com.krlsedu.timetracker.model.*;

import java.io.File;
import java.util.*;

import static com.krlsedu.timetracker.service.WakaTimeCli.getCurrentTimestamp;

public class Applications {
	private static List<String> notSendHeartbeat;
	private static List<String> sendHeartbeat;
	private static boolean allApps;
	private static ConfigAppsMonitoring configAppsMonitoring;
	private static ConfigApp defaultConfigApp = null;
	private static Map<String, Map<String, String>> configsMap = new HashMap<>();
	private static String configsCache = "";
	
	private Applications() {
	}
	
	public static void init() {
		try {
			WakaTimeCli.setupDebugging();
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
		configAppsMonitoring = WakaTimeCli.getObjectMapper().readValue(configs, ConfigAppsMonitoring.class);
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
	
	public static boolean checkApplicationDetail(ApplicationDetail applicationDetail) {
		init();
		setProcessName(applicationDetail);
		seConfig(applicationDetail);
		setName(applicationDetail);
		canSendHeartbeat(applicationDetail);
		generateHearbeat(applicationDetail);
		return applicationDetail.isSendHeartbeat();
	}
	
	private static void generateHearbeat(ApplicationDetail applicationDetail) {
		if (applicationDetail.isSendHeartbeat()) {
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
			applicationDetail.setHeartbeat(h);
		}
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
			String ideVersion = getHbpValue(applicationDetail.getProcessName(), "IdeVersion");
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
			switch (getHbpValue(applicationDetail.getProcessName(), "IdeName")) {
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
			String project = getHbpValue(applicationDetail.getProcessName(), "Project");
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
			applicationDetail.setSendHeartbeat(!notSendHeartbeat.contains(applicationDetail.getProcessName()));
		} else {
			applicationDetail.setSendHeartbeat(sendHeartbeat.contains(applicationDetail.getProcessName()));
		}
	}
	
	public static void setProcessName(ApplicationDetail applicationDetail) {
		String name = applicationDetail.getName();
		name = name.substring(name.lastIndexOf("\\") + 1);
		try {
			applicationDetail.setProcessName(name.substring(0, name.lastIndexOf(".")));
		} catch (Exception e) {
			//ignored
		}
	}
	
	public static void setName(ApplicationDetail applicationDetail) {
		if (applicationDetail.getAppConfig().getAppName() != null) {
			applicationDetail.setName(applicationDetail.getAppConfig().getAppName());
		} else {
			applicationDetail.setName(applicationDetail.getProcessName());
		}
	}
	
	private static void seConfig(ApplicationDetail applicationDetail) {
		configAppsMonitoring.getMonitorin().stream().filter(configApp -> configApp.getProcessName().equals(applicationDetail.getProcessName())).findFirst().ifPresent(applicationDetail::setAppConfig);
		if (applicationDetail.getAppConfig() == null) {
			applicationDetail.setAppConfig(defaultConfigApp);
		}
	}
}
