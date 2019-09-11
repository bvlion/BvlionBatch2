package net.ambitious.bvlion.batch2.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class WeatherSearchesEntity {
	private String areaName;
	private String pcUrl;
	private String mobileUrl;
	private String userAgent;
}
