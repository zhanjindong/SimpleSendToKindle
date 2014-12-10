/**
 *  Copyright (c) 2014 zhanjindong. All rights reserved.
 */
package so.zjd.sstk.util;

import java.io.UnsupportedEncodingException;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Address;
import javax.mail.Authenticator;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * Mail send helper class via smpt protocol.
 * 
 * @author jdzhan,2014-12-6
 * 
 */
public class MailSender {

	private static final Logger LOGGER = LoggerFactory.getLogger(MailSender.class);
	private Properties config;

	public MailSender(Properties config) {
		this.config = config;
	}

	public void sendFrom(String subject, String filePath) {
		if (StringUtils.isEmpty(filePath)) {
			throw new IllegalArgumentException("the arg:filePath can not be null or empty");
		}

		LOGGER.debug("sending mail from path: " + filePath);
		MailUtil.send(subject, filePath, config);
	}

	private static class MailUtil {
		public static void send(String subject, String filePath, final Properties config) {
			Session session = Session.getInstance(config, new Authenticator() {
				@Override
				protected PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(config.getProperty("mail.userName"), config
							.getProperty("mail.password"));
				}
			});

			try {
				MimeMessage mimeMessage = new MimeMessage(session);
				mimeMessage.setSubject(subject, "UTF-8");
				mimeMessage.setFrom(new InternetAddress(config.getProperty("mail.from")));
				mimeMessage.setReplyTo(new Address[] { new InternetAddress(config.getProperty("mail.from")) });
				mimeMessage.setRecipients(MimeMessage.RecipientType.TO,
						InternetAddress.parse(config.getProperty("mail.to")));

				MimeMultipart mimeMultipart = new MimeMultipart("mixed");
				MimeBodyPart attch1 = new MimeBodyPart();
				mimeMultipart.addBodyPart(attch1);
				mimeMessage.setContent(mimeMultipart);

				DataSource ds1 = new FileDataSource(filePath);
				DataHandler dataHandler1 = new DataHandler(ds1);
				attch1.setDataHandler(dataHandler1);
				attch1.setFileName(MimeUtility.encodeText(FilenameUtils.getName(filePath)));

				mimeMessage.saveChanges();

				Transport.send(mimeMessage);
			} catch (MessagingException e) {
				LOGGER.error("MessagingException", e);
			} catch (UnsupportedEncodingException e) {
				LOGGER.error("UnsupportedEncodingException", e);
			}
		}
	}
}
