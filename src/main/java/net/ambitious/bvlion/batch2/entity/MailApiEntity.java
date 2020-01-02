package net.ambitious.bvlion.batch2.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class MailApiEntity {
    private String targetFrom;
    private String toFolder;
    private String channel;
    private String userName;
    private String iconUrl;
    private String prefixFormat;
}
