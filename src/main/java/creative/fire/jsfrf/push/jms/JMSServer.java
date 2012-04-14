package creative.fire.jsfrf.push.jms;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.InitialContext;

import org.hornetq.api.core.TransportConfiguration;
import org.hornetq.core.config.Configuration;
import org.hornetq.core.config.impl.ConfigurationImpl;
import org.hornetq.core.remoting.impl.netty.NettyAcceptorFactory;
import org.hornetq.core.remoting.impl.netty.NettyConnectorFactory;
import org.hornetq.core.server.HornetQServer;
import org.hornetq.core.server.HornetQServers;
import org.hornetq.jms.server.JMSServerManager;
import org.hornetq.jms.server.config.ConnectionFactoryConfiguration;
import org.hornetq.jms.server.config.impl.ConnectionFactoryConfigurationImpl;
import org.hornetq.jms.server.impl.JMSServerManagerImpl;

public class JMSServer {
	private static final Logger logger = Logger.getLogger(JMSServer.class.getName());
	private HornetQServer jmsServer;
	private JMSServerManager jmsServerManager;

	public void start() {
		try {
			jmsServer = HornetQServers.newHornetQServer(createHornetQConfiguration());
			jmsServerManager = new JMSServerManagerImpl(jmsServer);
			jmsServerManager.setContext(new InitialContext());
			jmsServerManager.start();
			createJMSConnectionFactory();
		} catch (Exception e) {
			logger.log(Level.SEVERE, e.getLocalizedMessage());
		}
	}

	public void stop() {
		try {
			jmsServerManager.stop();
			jmsServerManager = null;
			jmsServer.stop();
			jmsServer = null;
		} catch (Exception e) {
			logger.log(Level.SEVERE, "wasn't able to finalize custom messaging");
		}
	}

	public void createTopic(String topicName, String jndiName) throws Exception {
		jmsServerManager.createTopic(false, topicName, jndiName);
	}

	private void createJMSConnectionFactory() throws Exception {
		List<String> connectors = Arrays.asList(new String[] { "netty" });

		ConnectionFactoryConfiguration connectionFactoryConfiguration = new ConnectionFactoryConfigurationImpl("ConnectionFactory", false, connectors,
				(String) null);
		connectionFactoryConfiguration.setUseGlobalPools(false);

		jmsServerManager.createConnectionFactory(false, connectionFactoryConfiguration, "ConnectionFactory");
	}

	private Configuration createHornetQConfiguration() {
		Configuration configuration = new ConfigurationImpl();
		configuration.setPersistenceEnabled(false);
		configuration.setSecurityEnabled(false);

		TransportConfiguration transportationConfiguration = new TransportConfiguration(NettyAcceptorFactory.class.getName());
		HashSet<TransportConfiguration> setTransp = new HashSet<TransportConfiguration>();
		setTransp.add(transportationConfiguration);
		configuration.setAcceptorConfigurations(setTransp);
		configuration.getConnectorConfigurations().put("netty", new TransportConfiguration(NettyConnectorFactory.class.getName()));

		return configuration;
	}
}