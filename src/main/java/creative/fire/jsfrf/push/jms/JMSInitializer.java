package creative.fire.jsfrf.push.jms;

import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.InitialContext;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;

import creative.fire.jsfrf.push.jms.provider.ServerProvider;
import static creative.fire.jsfrf.push.jms.JMSProducer.PUSH_JMS_TOPIC;

/**
 * Initializes JMS server and creates requested topics.
 * 
 */
public class JMSInitializer extends BaseInitializer {

	private static final Logger LOGGER = Logger.getLogger(JMSInitializer.class.getName());
	private static final AtomicReference<Boolean> JMS_ENABLED = new AtomicReference<Boolean>(null);

	private ServerProvider provider;

	public void initializeCapability() {
		initializeJMS();
	}

	@Override
	public boolean isCapabilityEnabled() {
		return isJmsEnabled();
	}

	private void initializeJMS() {
		provider = new ServerProvider();
		provider.initializeProvider();
		if (provider != null) {
			try {
				createTopic(PUSH_JMS_TOPIC, "/topic/" + PUSH_JMS_TOPIC);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void createTopic(String topicName, String jndiName) throws Exception {
		try {
			LOGGER.info("creating topic " + topicName);
			provider.createTopic(topicName, jndiName);
		} catch (Exception e) {
			throw new RuntimeException("Unable to create topic '" + topicName + "' (JNDI: " + jndiName + ")", e);
		}
	}

	protected static boolean isJmsEnabled() {
		if (null == JMS_ENABLED.get()) {
			boolean isJmsEnabled = isConnectionFactoryRegistered() || isTomcat();
			JMS_ENABLED.compareAndSet(null, isJmsEnabled);
		}
		return JMS_ENABLED.get();
	}

	private static boolean isTomcat() {
		try {
			Class<?> clazz = Class.forName("org.apache.catalina.util.ServerInfo");
			String serverInfo = (String) clazz.getMethod("getServerInfo").invoke(null);
			return serverInfo.contains("Tomcat");
		} catch (Exception e) {
			return false;
		}
	}

	public void finalizeCapability() throws Exception {
		if (provider != null) {
			provider.finalizeProvider();
		}
	}
 
	private static boolean isConnectionFactoryRegistered() {
		try {
			return null != InitialContext.doLookup("java:/ConnectionFactory");
		} catch (NamingException e) {
			if (!(e instanceof NameNotFoundException)) {
				LOGGER.log(Level.SEVERE, "Can't access naming context", e);
			}
			return false;
		}
	}

}