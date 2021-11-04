package com.krlsedu.timetracker.model;

import lombok.*;

import java.math.BigDecimal;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class Heartbeat {
	private String entity;
	private String entityType;
	private BigDecimal timestamp;
	private boolean write;
	private String project;
	private String language;
	private String category;
	private String ideName;
	private String ideVersion;
	private String hostName;
}