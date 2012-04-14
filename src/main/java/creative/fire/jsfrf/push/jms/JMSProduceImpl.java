package creative.fire.jsfrf.push.jms;

import static creative.fire.jsfrf.push.jms.JMSProducer.PUSH_JMS_TOPIC;

import java.util.concurrent.atomic.AtomicReference;

import javax.faces.application.Application;
import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.PostConstructApplicationEvent;
import javax.faces.event.PreDestroyApplicationEvent;
import javax.faces.event.SystemEvent;
import javax.faces.event.SystemEventListener;
import javax.naming.InitialContext;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class JMSProduceImpl implements SystemEventListener, ServletContextListener {
	private JMSServer server;
	private Thread producerThread;
	private JMSProducer producer;
	private ProducerRunnable producerRunner;
	private static final AtomicReference<Boolean> JMS_ENABLED = new AtomicReference<Boolean>(null);

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		try {
			init();
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		destroy();
	}

	@Override
	public void processEvent(SystemEvent event) throws AbortProcessingException {
		if (isJmsEnabled()) {
			if (event instanceof PostConstructApplicationEvent) {
				Application application = FacesContext.getCurrentInstance().getApplication();
				application.subscribeToEvent(PreDestroyApplicationEvent.class, this);

				try {
					init();
				} catch (Exception e) {
					throw new RuntimeException("Capability " + this.getClass().getName() + " was not correctly initialized", e);
				}
			} else {
				try {
					destroy();
				} catch (Exception e) {
					throw new RuntimeException("Capability " + this.getClass().getName() + " was not correctly finalized", e);
				}
			}
		}

	}

	@Override
	public boolean isListenerForSource(Object source) {
		return true;
	}

	private void init() {
		server = new JMSServer();
		server.start();
		if (server != null) {
			try {
				server.createTopic(PUSH_JMS_TOPIC, "/topic/" + PUSH_JMS_TOPIC);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		producer = new JMSProducer();
		producerRunner = new ProducerRunnable(producer);
		producerThread = new Thread(producerRunner, "MessageProducerThread");
		producerThread.setDaemon(false);
		producerThread.start();
 
	}

	private void destroy() {
		if (producer != null) {
			producer.disconnect();
		}
		if (producerRunner != null) {
			producerRunner.stop();
		}
		if (producerThread != null) {
			producerThread.interrupt();
		}

		if (server != null) {
			server.stop();
		}
	}

	private static boolean isJmsEnabled() {
		if (null == JMS_ENABLED.get()) {
			boolean isJmsEnabled = isConnectionFactoryRegistered() || isTomcat();
			JMS_ENABLED.compareAndSet(null, isJmsEnabled);
		}
		return JMS_ENABLED.get();
	}

	private static boolean isConnectionFactoryRegistered() {
		try {
			return null != InitialContext.doLookup("java:/ConnectionFactory");
		} catch (NamingException e) {
			if (!(e instanceof NameNotFoundException)) {
				System.out.println("Can't access naming context");
			}
			return false;
		}
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
}
