package com.krlsedu.timetracker.model;

import lombok.*;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class Error {
	private String url;
	private String json;
}
