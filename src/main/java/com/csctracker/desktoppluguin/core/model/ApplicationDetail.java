package com.csctracker.desktoppluguin.core.model;

import lombok.*;

import java.util.Date;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class ApplicationDetail {
	private String name;
	private String activityDetail;
	private Long timeSpentMillis;
	private Date dateIni;
	private Date dateEnd;
	private String osName;
	private String hostName;
	private String pluginName;
	private String processName;
	private String appVersion;
	private String url;
}
