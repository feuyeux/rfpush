package creative.fire.jsfrf.push.jms;

import java.io.Serializable;
import java.text.DateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jms.JMSException;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.jms.TopicPublisher;
import javax.jms.TopicSession;
import javax.naming.InitialContext;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;

/**
 * Sends message to JMS topic.
 */
public class JMSProducer {
	private static final Logger LOGGER = Logger.getLogger(JMSProducer.class.getName());
	public static final String PUSH_JMS_TOPIC = "pushJms";
	private Topic topic;
	private TopicConnection connection = null;
	private TopicSession session = null;
	private TopicPublisher publisher = null;

	public void sendMessage() throws Exception {
		try {
			initializeMessaging();
			ObjectMessage message = session.createObjectMessage(createMessage());
			publisher.publish(message);
			LOGGER.info("publishing");
		} catch (NameNotFoundException e) {
			LOGGER.fine(e.getMessage());
		} catch (JMSException e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
		}
	}

	private Serializable createMessage() {
		DateFormat dateFormat = DateFormat.getDateTimeInstance();
		dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
		String dateMessage = dateFormat.format(new Date());
		return dateMessage;
	}

	private void initializeMessaging() throws JMSException, NamingException {
		if (connection == null) {
			TopicConnectionFactory tcf = getTopicConnectionFactory();
			connection = tcf.createTopicConnection();
		}
		if (session == null) {
			session = connection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
		}
		if (topic == null) {
			topic = InitialContext.doLookup("topic/" + PUSH_JMS_TOPIC);
		}
		if (publisher == null) {
			publisher = session.createPublisher(topic);
		}
	}

	public int getInterval() {
		return 5000;
	}

	public void finalizeProducer() {
		if (publisher != null) {
			try {
				publisher.close();
			} catch (JMSException e) {
				LOGGER.severe("unable to close publisher");
			}
		}
		if (session != null) {
			try {
				session.close();
			} catch (JMSException e) {
				LOGGER.severe("unable to close session");
			}
		}
		if (connection != null) {
			try {
				connection.close();
			} catch (JMSException e) {
				LOGGER.severe("unable to close connection");
			}
		}
	}

	private TopicConnectionFactory getTopicConnectionFactory() {
		try {
			return (TopicConnectionFactory) InitialContext.doLookup("java:/ConnectionFactory");
		} catch (NamingException e) {
			try {
				return (TopicConnectionFactory) InitialContext.doLookup("ConnectionFactory");
			} catch (NamingException e2) {
				throw new IllegalStateException("Can't find registered ConnectionFactory");
			}
		}
	}
}
