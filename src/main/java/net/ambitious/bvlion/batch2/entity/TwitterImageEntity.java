package net.ambitious.bvlion.batch2.entity;

import lombok.Getter;
import lombok.Setter;
import twitter4j.Status;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public class TwitterImageEntity {
	public static List<TwitterImageEntity> createTwitterImageEntities(Status tweet) {
		return Arrays.stream(tweet.getMediaEntities())
				.map(media -> new TwitterImageEntity(
						media.getMediaURL(),
						tweet.getCreatedAt(),
						tweet.getText().split(" https")[0])
				)
				.collect(Collectors.toList());
    }

    private TwitterImageEntity(String mediaUrl, Date postedDate, String text) {
		this.mediaUrl = mediaUrl;
		this.postedDate = postedDate;
		this.text = text;
    }

    @Setter
    private int imageType;
    private String mediaUrl;
    private Date postedDate;
    private String text;

	@Override
	public String toString() {
		return "media:[" + this.mediaUrl + "]\n"
				+ "date:[" + this.postedDate + "]\n"
				+ "text:[" + this.text + "]\n\n";
	}
}
