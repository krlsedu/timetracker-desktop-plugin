package com.krlsedu.timetracker.core.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
	private String process;
	private String completeProcessName;
	private String activityDetail;
	private Long timeSpentMillis;
	private Date dateIni;
	private Date dateEnd;
	private String osName;
	private String hostName;

	@JsonIgnore
	private ConfigApp appConfig;

	@JsonIgnore
	private boolean sendHeartbeat;

	@JsonIgnore
	private Heartbeat heartbeat;
}
