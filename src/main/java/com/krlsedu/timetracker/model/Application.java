package com.krlsedu.timetracker.model;

import lombok.*;

import java.util.Date;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class Application {
	private String name;
	private Long timeSpentMillis;
	private Date dateIni;
	private Date dateEnd;
}
