package net.ambitious.bvlion.batch2.util;

import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.IMAPStore;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.extern.slf4j.Slf4j;
import net.ambitious.bvlion.batch2.entity.MailApiEntity;
import javax.mail.*;
import java.util.*;

@Slf4j
public class Mail {

    private Session session;

    private Mail() {
        var props = new Properties();
        props.setProperty("mail.store.protocol", "imap");
        session = Session.getDefaultInstance(props);
    }

    private static Mail instance = new Mail();

    public static Mail getInstance() {
        return instance;
    }

    @SuppressFBWarnings(
            value = "RCN_REDUNDANT_NULLCHECK_WOULD_HAVE_BEEN_A_NPE",
            justification = "because null checked by try-with-resources")
    public void moveAndSlack(AppParams appParams, List<MailApiEntity> mailApiEntities) {
        long start = System.currentTimeMillis();
        try (var store = new IMAPStore(session, null)) {
            store.connect(appParams.getMailHost(), 143, appParams.getMailUser(), appParams.getMailPassword());
            try (var folder = store.getFolder("INBOX")) {
                folder.open(IMAPFolder.READ_WRITE);
                mailApiEntities.forEach(entity -> {
                    try {
                        // 対象メッセージ一覧取得
                        Message[] messages = folder.search(new MailUtil.MailAddressTerm(entity.getTargetFrom()));
                        if (messages == null) {
                            return;
                        }

                        // 受信日でソート
                        Arrays.sort(messages, new MailUtil.MailComparator());

                        // 適切にフォーマットしてSlackにPost
                        Arrays.stream(messages).map(msg -> new MailUtil.SlackPostEntity(
                                entity.getChannel(),
                                MailUtil.getSlackUserName(entity, msg),
                                MailUtil.getPostMessage(msg, folder),
                                entity.getIconUrl()
                        )).forEach(model -> MailUtil.slackPost(model, appParams));

                        // INBOX から削除
                        folder.copyMessages(messages, store.getFolder("INBOX." + entity.getToFolder()));
                        folder.setFlags(messages, new Flags(Flags.Flag.DELETED), true);

                        // 削除できていないメールをログ出力
                        Arrays.stream(messages)
                                .filter(MailUtil::isNotSet)
                                .map(MailUtil::getNotSetMessage)
                                .forEach(log::warn);
                    } catch (MessagingException e) {
                        log.warn("JavaMail Each Error", e);
                    }
                });
            } catch (MessagingException e) {
                log.warn("JavaMail Folder Error", e);
            }
        } catch (MessagingException e) {
            log.warn("JavaMail Connect Error", e);
        }
        log.info("moveAndSlack done: " + (System.currentTimeMillis() - start) + "ms");
    }
}
