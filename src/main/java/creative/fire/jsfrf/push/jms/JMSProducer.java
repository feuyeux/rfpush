package creative.fire.jsfrf.push.jms;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jms.JMSException;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.jms.TopicPublisher;
import javax.jms.TopicSession;
import javax.naming.InitialContext;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;

public class JMSProducer {
	private static final Logger log = Logger.getLogger(JMSProducer.class.getName());
	public static final String PUSH_JMS_TOPIC = "jmsAddress";
	private Topic topic;
	private TopicConnection connection = null;
	private TopicSession session = null;
	private TopicPublisher publisher = null;

	public void sendMessage() throws Exception {
		try {
			connect();
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String time = format.format(new Date());
			TextMessage message = session.createTextMessage(time);
			publisher.publish(message);
			log.info("push event to " + PUSH_JMS_TOPIC);
		} catch (NameNotFoundException e) {
			log.fine(e.getMessage());
		} catch (JMSException e) {
			log.log(Level.SEVERE, e.getMessage(), e);
		}
	}

	public int getInterval() {
		return 5000;
	}
	
	private void connect() throws JMSException, NamingException {
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

	public void disconnect() {
		if (publisher != null) {
			try {
				publisher.close();
			} catch (JMSException e) {
				log.severe("unable to close publisher");
			}
		}
		if (session != null) {
			try {
				session.close();
			} catch (JMSException e) {
				log.severe("unable to close session");
			}
		}
		if (connection != null) {
			try {
				connection.close();
			} catch (JMSException e) {
				log.severe("unable to close connection");
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
