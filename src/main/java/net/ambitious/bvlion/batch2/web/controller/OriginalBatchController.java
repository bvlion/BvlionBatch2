package net.ambitious.bvlion.batch2.web.controller;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.ambitious.bvlion.batch2.mapper.*;
import net.ambitious.bvlion.batch2.util.AccessUtil;
import net.ambitious.bvlion.batch2.util.AppParams;
import net.ambitious.bvlion.batch2.util.SlackBinaryPost;
import net.ambitious.bvlion.batch2.util.SlackHttpPost;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.commons.lang3.time.FastDateFormat;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/batch/${url.ifttt}")
public class OriginalBatchController {

    private static final String JORUDAN_ICON = "https://pbs.twimg.com/profile_images/753471803/JorudanLive-Icon.png";

    @NonNull
    private AppParams appParams;

    @NonNull
    private DatingMapper datingMapper;

    @NonNull
    private final HolidayMapper holidayMapper;

    @NonNull
    private final ExecTimeMapper execTimeMapper;

    @RequestMapping(value = "/dating-notification", method = RequestMethod.PUT) // cron = "0 0 6 * * *"
    public void datingNotification() {
        var now = Calendar.getInstance(AccessUtil.TOKYO);

        var message = this.datingMapper.allDatings().stream()
                .map(value -> {
                    if (value.getTargetDate().length() == 8) {
                        try {
                            var anniversary = DateUtils.parseDate(value.getTargetDate(), "yyyyMMdd");
                            var totalDays = TimeUnit.DAYS.convert(
                                    now.getTimeInMillis() - anniversary.getTime(), TimeUnit.MILLISECONDS
                            ) + 1;
                            if (totalDays % 100 == 0) {
                                return String.format(value.getMessage(), NumberFormat.getNumberInstance().format(totalDays));
                            }
                        } catch (ParseException e) {
                            log.debug(value.getTargetDate(), e);
                        }
                    } else {
                        if (FastDateFormat.getInstance("MMdd", AccessUtil.TOKYO).format(Calendar.getInstance(AccessUtil.TOKYO)).equals(value.getTargetDate())) {
                            return value.getMessage();
                        }
                    }
                    return "";
                }).collect(Collectors.joining("\n"));

        if (message.trim().length() > 0) {
            log.debug("DatingBatch:" + message);
            AccessUtil.sendFcm(
                    AccessUtil.createTopicMessage("記念日通知", message + "おめでとう(*･ω･)ﾉ", "dating"),
                    appParams,
                    log
            );
        }
    }

    @RequestMapping(value = "/horoscope", method = RequestMethod.PUT) // cron = "25 30 7 * * *"
    public void horoscope() throws IOException {
        new SlackHttpPost(
                "horoscope-api",
                "horoscope-api-" + AccessUtil.getNow("yyyyMMdd"),
                getHoroscopeMessage(),
                "https://4s.ambitious-i.net/icon/1434076.png"
        ).send(appParams);
    }

    private static String getHoroscopeMessage() {
        var today = AccessUtil.getNow("yyyy/MM/dd");
        var message = new StringBuilder();

        var request = Request.Get("http://api.jugemkey.jp/api/horoscope/free/" + today);
        Response res = null;
        try {
            res = request.execute();
            var json = new JSONObject(new String(res.returnContent().asBytes(), StandardCharsets.UTF_8));
            var horoscope = json.getJSONObject("horoscope");
            var todayData = horoscope.getJSONArray(today);
            for (var i = 0; i < todayData.length(); i++) {
                var object = todayData.getJSONObject(i);
                if ("双子座".equals(object.getString("sign"))) {
                    message.append(today);
                    message.append("の双子座の運勢は第");
                    message.append(object.getInt("rank"));
                    message.append("位！\\n");
                    message.append(object.getString("content"));
                    message.append("\\n");
                    message.append("ラッキーカラーは「");
                    message.append(object.getString("color"));
                    message.append("」、");
                    message.append("ラッキーアイテムは「");
                    message.append(object.getString("item"));
                    message.append("」だよ。\\n\\n");
                    message.append("金運：");
                    message.append(object.getInt("money"));
                    message.append("\\n");
                    message.append("仕事運：");
                    message.append(object.getInt("job"));
                    message.append("\\n");
                    message.append("恋愛運：");
                    message.append(object.getInt("love"));
                    message.append("\\n");
                    message.append("総合評価：");
                    message.append(object.getInt("total"));
                    break;
                }
            }
        } catch (IOException | JSONException e) {
            log.error("Horoscope Get Error", e);
        } finally {
            if (res != null) {
                res.discardContent();
            }
        }

        if (message.length() == 0) {
            message.append(today);
            message.append("の双子座の運勢は取得できませんでした(´･ω･`)");
        }

        return message.toString();
    }

    @RequestMapping(value = "/jorudan", method = RequestMethod.PUT)
    public void jorudan(
            @RequestParam("detail") String detail,
            @RequestParam("url") String url,
            @RequestParam("description") String description,
            @RequestParam("slackChannel") String slackChannel,
            @RequestParam("searchValue") String searchValue,
            @RequestParam("date") String date
    ) throws IOException {
        if (AccessUtil.isExecTime(holidayMapper.isHoliday(), execTimeMapper.selectExecTimes())) {
            var details = detail.split("〕");
            var section = details[0]
                    .substring(1)
                    .replaceAll("（.*）", "")
                    .replace("〜", "から")
                    + "の区間";
            var state = details[1].split("／")[0];
            var googleHomeMessage = searchValue + "の" + section + "で"
                    + (state.equals("止まってる") ? state : state + "の") + "ようです。";
            AccessUtil.postGoogleHome(googleHomeMessage, log, appParams);
        }

        var message = new StringBuilder();
        message.append(detail);
        if (StringUtils.isNoneBlank(description)) {
            message.append("\n");
            message.append(description);
        }
        message.append("\n");
        message.append("\n");
        message.append(url);
        new SlackHttpPost(
                slackChannel,
                searchValue + "-" + date,
                message.toString(),
                JORUDAN_ICON
        ).send(appParams);
        
    }

    @RequestMapping(value = "/twitter-images", method = RequestMethod.PUT)
    public void twitterImages(
            @RequestParam("url") String url,
            @RequestParam("slackChannel") String slackChannel,
            @RequestParam("text") String text
    ) throws IOException {
        if (appParams.isProduction()) {
            new SlackBinaryPost.Builder()
                    .channels(slackChannel)
                    .title(text)
                    .fileName(FastDateFormat.getInstance("yyyyMMddHHmmss", AccessUtil.TOKYO).format(Calendar.getInstance(AccessUtil.TOKYO)) + ".png")
                    .fileData(AccessUtil.getBinaryBytes(url))
                    .build(appParams.getSlackToken()).post(appParams);
        }
    }
}
