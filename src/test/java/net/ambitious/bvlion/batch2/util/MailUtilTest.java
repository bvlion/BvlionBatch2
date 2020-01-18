package net.ambitious.bvlion.batch2.util;

import org.junit.Test;
import org.mockito.stubbing.OngoingStubbing;

import javax.activation.DataHandler;
import javax.mail.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.Enumeration;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MailUtilTest {

    @Test
    public void getNotSetMessageTest() {
        Message message = mock(Message.class);
        when(message.toString()).thenReturn("message");
        assertEquals(MailUtil.getNotSetMessage(message), "Message # message not deleted");
    }

    @Test
    public void isNotSetTest() throws MessagingException {
        Message message = mock(Message.class);
        when(message.isSet(Flags.Flag.DELETED)).thenReturn(false);
        assertTrue(MailUtil.isNotSet(message));

        when(message.isSet(Flags.Flag.DELETED)).thenReturn(true);
        assertFalse(MailUtil.isNotSet(message));

        when(message.getFlags()).thenThrow(new MessagingException());
        assertFalse(MailUtil.isNotSet(message));
    }
}
