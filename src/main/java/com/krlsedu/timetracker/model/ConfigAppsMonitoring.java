package com.krlsedu.timetracker.model;

import lombok.*;

import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class ConfigAppsMonitoring {
	private boolean allApps;
	private List<ConfigApp> monitorin;
	private List<ConfigApp> notMonitorin;
	private List<ConfigExtra> configsExtras;
}
