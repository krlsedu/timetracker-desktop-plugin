package com.krlsedu.timetracker.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

@Getter
@Setter
@ToString
public class AplicationStat {
	public AplicationStat() {
		dateIni = new Date();
		timeSpentMilis = 0L;
	}
	
	private String name;
	private Long timeSpentMilis;
	private Date dateIni;
	private Date dateEnd;
}
