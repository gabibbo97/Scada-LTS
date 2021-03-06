package com.serotonin.mango.util;

import com.serotonin.mango.Common;
import com.serotonin.mango.rt.event.EventInstance;
import com.serotonin.mango.rt.event.handlers.NotificationType;
import com.serotonin.mango.rt.event.type.SystemEventType;
import com.serotonin.mango.rt.maint.work.EmailWorkItem;
import com.serotonin.mango.web.email.MangoEmailContent;
import com.serotonin.util.StringUtils;
import com.serotonin.web.email.EmailSender;
import com.serotonin.web.i18n.LocalizableMessage;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.scada_lts.dao.SystemSettingsDAO;

import javax.mail.internet.InternetAddress;
import java.text.MessageFormat;
import java.util.ResourceBundle;
import java.util.Set;

public final class SendMsgUtils {

    private SendMsgUtils() {}

    private static final Log LOG = LogFactory.getLog(SendMsgUtils.class);

    public static boolean sendEmailWithoutQueue(EventInstance evt, NotificationType notificationType,
                                                Set<String> addresses, String alias) {
        try {

            validateEmail(evt, notificationType, addresses, alias);

            if (evt.getEventType().isSystemMessage()) {
                if (((SystemEventType) evt.getEventType()).getSystemEventTypeId() == SystemEventType.TYPE_EMAIL_SEND_FAILURE) {
                    // Don't send email notifications about email send failures.
                    LOG.info("Not sending email for event raised due to email failure");
                    return false;
                }
            }
            String[] toAddrs = addresses.toArray(new String[0]);
            MangoEmailContent content = EmailContentUtils.createContent(evt, notificationType, alias);

            // Send the email.
            sendEmailWithoutQueue(toAddrs, content);
            return true;

        } catch (Exception e) {
            LOG.error(MessageFormat.format("Info about email: {0}, StackTrace: {1}",
                    getInfoEmail(evt,notificationType,alias),
                    ExceptionUtils.getStackTrace(e)));
            return false;
        }
    }

    public static boolean sendSmsWithoutQueue(EventInstance evt, NotificationType notificationType, Set<String> addresses,
                                              String alias) {
        try {

            validateEmail(evt, notificationType, addresses, alias);

            if (evt.getEventType().isSystemMessage()) {
                if (((SystemEventType) evt.getEventType()).getSystemEventTypeId() == SystemEventType.TYPE_EMAIL_SEND_FAILURE) {
                    // Don't send email notifications about email send failures.
                    LOG.info("Not sending email for event raised due to email failure");
                    return false;
                }
            }
            String[] toAddrs = addresses.toArray(new String[0]);
            MangoEmailContent content = EmailContentUtils.createTextContent(evt, notificationType, alias);

            // Send the email.
            sendEmailWithoutQueue(toAddrs, content);
            return true;

        } catch (Exception e) {
            LOG.error(MessageFormat.format("Info about email: {0}, StackTrace: {1}",
                    getInfoEmail(evt,notificationType,alias),
                    ExceptionUtils.getStackTrace(e)));
            return false;
        }
    }

    public static boolean sendEmail(EventInstance evt, NotificationType notificationType, Set<String> addresses,
                                 String alias) {
        try {

            validateEmail(evt, notificationType, addresses, alias);

            if (evt.getEventType().isSystemMessage()) {
                if (((SystemEventType) evt.getEventType()).getSystemEventTypeId() == SystemEventType.TYPE_EMAIL_SEND_FAILURE) {
                    // Don't send email notifications about email send failures.
                    LOG.info("Not sending email for event raised due to email failure");
                    return false;
                }
            }
            String[] toAddrs = addresses.toArray(new String[0]);
            MangoEmailContent content = EmailContentUtils.createContent(evt, notificationType, alias);

            // Send the email.
            EmailWorkItem.queueEmail(toAddrs, content);
            return true;

        } catch (Exception e) {
            LOG.error(MessageFormat.format("Info about email: {0}, StackTrace: {1}",
                    getInfoEmail(evt,notificationType,alias),
                    ExceptionUtils.getStackTrace(e)));
            return false;
        }
    }

    public static boolean sendSms(EventInstance evt, NotificationType notificationType, Set<String> addresses,
                               String alias) {
        try {

            validateEmail(evt, notificationType, addresses, alias);

            if (evt.getEventType().isSystemMessage()) {
                if (((SystemEventType) evt.getEventType()).getSystemEventTypeId() == SystemEventType.TYPE_EMAIL_SEND_FAILURE) {
                    // Don't send email notifications about email send failures.
                    LOG.info("Not sending email for event raised due to email failure");
                    return false;
                }
            }

            MangoEmailContent content = EmailContentUtils.createTextContent(evt, notificationType, alias);
            String[] toAddrs = addresses.toArray(new String[0]);

            EmailWorkItem.queueEmail(toAddrs, content);
            return true;

        } catch (Exception e) {
            LOG.error(MessageFormat.format("Info about email: {0}, StackTrace: {1}",
                    getInfoEmail(evt,notificationType,alias),
                    ExceptionUtils.getStackTrace(e)));
            return false;
        }
    }

    private static void sendEmailWithoutQueue(String[] toAddrs, MangoEmailContent content)
            throws Exception {
        InternetAddress[] toAddresses = new InternetAddress[toAddrs.length];
        for (int i = 0; i < toAddrs.length; i++)
            toAddresses[i] = new InternetAddress(toAddrs[i]);

        String addr = SystemSettingsDAO.getValue(SystemSettingsDAO.EMAIL_FROM_ADDRESS);
        String pretty = SystemSettingsDAO.getValue(SystemSettingsDAO.EMAIL_FROM_NAME);
        String host = SystemSettingsDAO.getValue(SystemSettingsDAO.EMAIL_SMTP_HOST);
        int port = SystemSettingsDAO.getIntValue(SystemSettingsDAO.EMAIL_SMTP_PORT);
        boolean authorization = SystemSettingsDAO.getBooleanValue(SystemSettingsDAO.EMAIL_AUTHORIZATION);
        String username = SystemSettingsDAO.getValue(SystemSettingsDAO.EMAIL_SMTP_USERNAME);
        String password = SystemSettingsDAO.getValue(SystemSettingsDAO.EMAIL_SMTP_PASSWORD);
        boolean tls = SystemSettingsDAO.getBooleanValue(SystemSettingsDAO.EMAIL_TLS);

        validateConfig(SystemSettingsDAO.EMAIL_FROM_ADDRESS, addr);
        validateConfig(SystemSettingsDAO.EMAIL_SMTP_HOST, host);
        if(authorization) {
            validateConfig(SystemSettingsDAO.EMAIL_SMTP_USERNAME, username);
            validateConfig(SystemSettingsDAO.EMAIL_SMTP_PASSWORD, password);
        }

        InternetAddress fromAddress = new InternetAddress(addr, pretty);
        EmailSender emailSender = new EmailSender(host, port, authorization, username, password, tls);

        emailSender.send(fromAddress, toAddresses, content.getSubject(), content);
    }

    private static String getInfoEmail(EventInstance evt, NotificationType notificationType, String alias) {

        String messageInfoAlias = MessageFormat.format("Alias: {0} \n", alias);
        String messageInfoEmail = MessageFormat.format("Event: {0} \n", evt.getId());
        String messageInfoNotyfication = MessageFormat.format("Notyfication: {0} \n", notificationType.getKey());
        String subject = "";
        String messageExceptionWhenGetSubjectEmail = "";
        try {
            LocalizableMessage subjectMsg;
            LocalizableMessage notifTypeMsg = new LocalizableMessage(notificationType.getKey());
            if (StringUtils.isEmpty(alias)) {
                if (evt.getId() == Common.NEW_ID)
                    subjectMsg = new LocalizableMessage("ftl.subject.default", notifTypeMsg);
                else
                    subjectMsg = new LocalizableMessage("ftl.subject.default.id", notifTypeMsg, evt.getId());
            } else {
                if (evt.getId() == Common.NEW_ID)
                    subjectMsg = new LocalizableMessage("ftl.subject.alias", alias, notifTypeMsg);
                else
                    subjectMsg = new LocalizableMessage("ftl.subject.alias.id", alias, notifTypeMsg, evt.getId());
            }

            ResourceBundle bundle = Common.getBundle();
            subject = subjectMsg.getLocalizedMessage(bundle);
        } catch (Exception e) {
            messageExceptionWhenGetSubjectEmail =  MessageFormat.format("StackTrace for subjectMsg {0}",ExceptionUtils.getStackTrace(e));
        }

        String messages = new StringBuilder()
                .append(messageInfoEmail)
                .append(messageInfoNotyfication)
                .append(messageInfoAlias)
                .append(subject)
                .append(messageExceptionWhenGetSubjectEmail).toString();

        return messages;
    }

    private static void validateEmail(EventInstance evt, NotificationType notificationType, Set<String> addresses, String alias) throws Exception {

        String messageErrorEventInstance = "Event Instance null \n";
        String messageErrorNotyficationType = "Notification type is null \n";
        String messageErrorEmails = "Don't have e-mail \n";
        String messageErrorAlias = "Don't have alias\n";
        String messages = "";
        if (evt == null || evt.getEventType() == null) messages += messageErrorEventInstance;
        if (notificationType == null) messages += messageErrorNotyficationType;
        if (addresses == null || addresses.size() == 0) messages += messageErrorEmails;
        if (alias == null) messages += messageErrorAlias;

        if (messages.length() > 0) {
            throw new Exception(getInfoEmail(evt, notificationType, alias) + messages );
        }

    }

    private static void validateConfig(String name, Object object) throws Exception {
        if(object == null)
            throw new IllegalStateException("Email config properties: " + name + " is null!");
        if((object instanceof String) && ((String)object).isEmpty())
            throw new IllegalStateException("Email config properties: " + name + " is empty!");
    }

}
