package net.ambitious.bvlion.batch2.util;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.ambitious.bvlion.batch2.entity.MailApiEntity;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.FastDateFormat;

import javax.mail.*;
import javax.mail.internet.MimeUtility;
import javax.mail.search.AddressStringTerm;
import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.Comparator;

@Slf4j
public class MailUtil {

    private MailUtil() { }

    public static String getNotSetMessage(Message message) {
        return "Message # " + message + " not deleted";
    }

    public static boolean isNotSet(Message message) {
        try {
            return !message.isSet(Flags.Flag.DELETED);
        } catch (MessagingException e) {
            return false;
        }
    }

    public static String getSubject(Message msg) throws MessagingException, UnsupportedEncodingException {
        if (StringUtils.isNoneBlank(msg.getSubject())) {
            return MimeUtility.decodeText(msg.getSubject());
        }
        return StringUtils.EMPTY;
    }

    public static String getBody(Message message) throws MessagingException, IOException {
        if (message.isMimeType("text/plain")) {
            return message.getContent().toString();
        } else if (message.isMimeType("multipart/*")) {
            var mp = (Multipart) message.getContent();
            return mp.getBodyPart(0).getContent().toString();
        }
        return StringUtils.EMPTY;
    }

    public static String getPostMessage(Message msg, Folder folder) {
        try {
            String subject = getSubject(msg);

            long messageId = ((UIDFolder) folder).getUID(msg);
            var message = ((UIDFolder) folder).getMessageByUID(messageId);

            String body = getBody(message);

            return "件名：" + subject + "\n----------\n" + body + "\n----------";
        } catch (MessagingException | IOException e) {
            log.warn("Can't get subject & body", e);
            return null;
        }
    }

    public static String getSlackUserName(MailApiEntity entity, Message message) {
        if (StringUtils.isNoneBlank(entity.getPrefixFormat())) {
            try {
                return entity.getUserName()
                        + FastDateFormat.getInstance(entity.getPrefixFormat(), AccessUtil.TOKYO)
                                .format(message.getReceivedDate());
            } catch (MessagingException e) {
                log.warn("Can't get ReceivedDate", e);
            }
        }
        return entity.getUserName();
    }

    public static void slackPost(SlackPostEntity entity, AppParams appParams) {
        if (StringUtils.isBlank(entity.getText()) || StringUtils.isBlank(entity.getChannel())) {
            return;
        }
        try {
            new SlackHttpPost(
                    entity.getChannel(),
                    entity.getUserName(),
                    entity.getText(),
                    entity.getIconUrl()
            ).send(appParams);
        } catch (IOException e) {
            log.warn("MailUtil#slackPost Error", e);
        }
    }

    @Getter
    @AllArgsConstructor
    public static class SlackPostEntity {
        private String channel;
        private String userName;
        private String text;
        private String iconUrl;
    }

    public static class MailComparator implements Comparator<Message>, Serializable {
        @Override
        public int compare(Message message1, Message message2) {
            try {
                return message1.getReceivedDate().compareTo(message2.getReceivedDate());
            } catch (MessagingException e) {
                return 0;
            }
        }
    }

    @Slf4j
    public static class MailAddressTerm extends AddressStringTerm {
        protected MailAddressTerm(String pattern) {
            super(pattern);
        }

        @Override
        public boolean match(Message msg) {
            String addressText = "";
            String subject = "";
            try {
                var address = msg.getFrom();
                if (address != null) {
                    addressText = MimeUtility.decodeText(address[0].toString());
                }
                subject = getSubject(msg);
            } catch (MessagingException | UnsupportedEncodingException e) {
                log.warn("MailAddressTerm Error", e);
            }
            return addressText.contains(getPattern()) || subject.contains(getPattern());
        }
    }
}