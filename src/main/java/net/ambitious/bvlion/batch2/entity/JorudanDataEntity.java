package net.ambitious.bvlion.batch2.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.ambitious.bvlion.batch2.util.AccessUtil;
import org.apache.commons.lang3.time.DateUtils;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

@Setter
@Getter
@NoArgsConstructor
public class JorudanDataEntity {
    public JorudanDataEntity(String line) {
        var lines = line.split(" ");

        var date = Calendar.getInstance(AccessUtil.TOKYO).get(Calendar.YEAR) + "/" + lines[1].substring(1)
                + " " + lines[2].substring(0, lines[2].length() - 1) + ":00";

        try {
            this.postedDate = DateUtils.parseDate(date, "yyyy/MM/dd HH:mm:ss");
        } catch (ParseException e) {
            this.postedDate = new Date();
        }
        this.detail = lines[4];

        if (lines[5].startsWith("https://")) {
            this.description = "";
            this.url = lines[5];
        } else {
            this.description = lines[5];
            this.url = lines[6];
        }
    }

    /** 投稿日 */
    private Date postedDate;
    /** 情報 */
    private String detail;
    /** 詳細 */
    private String description;
    /** URL */
    private String url;
}
