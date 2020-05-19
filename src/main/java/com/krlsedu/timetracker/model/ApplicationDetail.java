package com.krlsedu.timetracker.model;

import lombok.*;

import java.util.Date;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class ApplicationDetail {
	private Long id;
	private String name;
	private String activityDetail;
	private Long timeSpentMillis;
	private Date dateIni;
	private Date dateEnd;
}
