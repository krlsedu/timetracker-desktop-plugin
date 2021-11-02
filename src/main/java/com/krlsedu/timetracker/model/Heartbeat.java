package com.krlsedu.timetracker.model;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class Heartbeat {
	public String entity;
	public String entityType;
	public BigDecimal timestamp;
	public Boolean isWrite;
	public String project;
	public String language;
	public String category;
}