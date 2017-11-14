package edu.gatech.dynodroid.reporting;

import java.net.InetAddress;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import edu.gatech.dynodroid.master.PropertyParser;
import edu.gatech.dynodroid.utilities.Logger;

public class TestProfileReporting {
	private static String userName = PropertyParser.reportEmailUserName;
	private static String password = PropertyParser.reportEmailPassword;

	public static boolean sendCompletionMail(String logLocation,String receipents) {
		boolean retVal = false;
		try {
			// setup the mail server properties
			Properties props = new Properties();
			props.put("mail.smtp.auth", "true");
			props.put("mail.smtp.starttls.enable", "true");

			// set up the message
			Session session = Session.getInstance(props);

			Message message = new MimeMessage(session);

			// add a TO address
			message.setRecipients(Message.RecipientType.TO,
					InternetAddress.parse(receipents));
			
			String computername=InetAddress.getLocalHost().getHostName();

			message.setSubject("M3 Tests Completed on Machine "+computername);
			message.setContent(
					"hi,\n\nTests Completed on the Machine:"+computername +"\n\n Log Location on the machine:"+logLocation+" \n \nHave a Nice Day..\n\n Thanks,\nM3 Reporting Services",
					"text/plain");

			Transport transport = session.getTransport("smtp");
			transport.connect("smtp.gmail.com", 587, userName,
					password);
			transport.sendMessage(message, message.getAllRecipients());
			retVal = true;
		} catch (Exception e) {
			Logger.logException(e);
		}
		return retVal;
	}
	
	public static boolean sendMail(String subject,String receipents,String body) {
		boolean retVal = false;
		try {
			// setup the mail server properties
			Properties props = new Properties();
			props.put("mail.smtp.auth", "true");
			props.put("mail.smtp.starttls.enable", "true");

			// set up the message
			Session session = Session.getInstance(props);

			Message message = new MimeMessage(session);

			// add a TO address
			message.setRecipients(Message.RecipientType.TO,
					InternetAddress.parse(receipents));
			message.setSubject(subject);
			message.setContent(body,"text/plain");

			Transport transport = session.getTransport("smtp");
			transport.connect("smtp.gmail.com", 587, userName,
					password);
			transport.sendMessage(message, message.getAllRecipients());
			retVal = true;
		} catch (Exception e) {
			Logger.logException(e);
		}
		return retVal;
	}

}
