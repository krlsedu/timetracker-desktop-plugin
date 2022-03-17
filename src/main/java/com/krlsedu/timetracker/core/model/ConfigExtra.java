package com.krlsedu.timetracker.core.model;

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
