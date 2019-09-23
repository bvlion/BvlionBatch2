package net.ambitious.bvlion.batch2.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class TwitterChannelsDataEntity {
	private int imageType;
	private String slackChannel;
	private String searchValue;
}
