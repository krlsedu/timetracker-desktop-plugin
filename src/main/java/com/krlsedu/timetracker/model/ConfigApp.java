package com.krlsedu.timetracker.model;

import lombok.*;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class ConfigApp {
	private String appName;
	private String processName;
	private String appDescription;
	private String category;
	private String entityType;
	private String project;
	private boolean ide;
}
