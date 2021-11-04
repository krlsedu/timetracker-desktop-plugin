package com.krlsedu.timetracker.model;

import lombok.*;

import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class ConfigExtra {
	String heartbeatParam;
	String value;
	List<String> processNames;
}
