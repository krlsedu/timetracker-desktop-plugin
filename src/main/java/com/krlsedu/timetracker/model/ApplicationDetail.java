package com.krlsedu.timetracker.model;

import com.krlsedu.timetracker.core.model.Heartbeat;
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
	private ConfigApp appConfig;
	private boolean sendHeartbeat;
	private Heartbeat heartbeat;
}
