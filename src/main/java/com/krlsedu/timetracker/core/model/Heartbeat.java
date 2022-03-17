package com.krlsedu.timetracker.core.model;

import lombok.*;

import java.math.BigDecimal;
import java.util.Date;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class Heartbeat {
	private String entity;
	private String process;
	private String applicationName;
	private String entityType;
	private BigDecimal timestamp;
	private boolean write;
	private String project;
	private String language;
	private String category;
	private String ideName;
	private String ideVersion;
	private String hostName;
	private Long timeSpentMillis;
	private boolean sent;
	private Date dateTime;
}