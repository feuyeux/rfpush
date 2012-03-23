package creative.fire.jsfrf.push.jms.provider;

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

public class ServerProvider {

	private static final Logger logger = Logger.getLogger(ServerProvider.class.getName());

	private HornetQServer jmsServer;
	private JMSServerManager jmsServerManager;

	public void initializeProvider() {
		try {
			startJMSServer();
			startJMSServerManager();
			createJMSConnectionFactory();
		} catch (Exception e) {
			logger.log(Level.SEVERE, e.getLocalizedMessage());
		}
	}

	public void createTopic(String topicName, String jndiName) throws Exception {
		jmsServerManager.createTopic(false, topicName, jndiName);
	}

	public void finalizeProvider() {
		try {
			stopJMSServerManager();
			stopJMSServer();
		} catch (Exception e) {
			logger.log(Level.SEVERE, "wasn't able to finalize custom messaging");
		}
	}

	private void startJMSServer() throws Exception {
		jmsServer = HornetQServers.newHornetQServer(createHornetQConfiguration());
	}

	private void startJMSServerManager() throws Exception {
		jmsServerManager = new JMSServerManagerImpl(jmsServer);
		InitialContext context = new InitialContext();
		jmsServerManager.setContext(context);
		jmsServerManager.start();
	}

	private void createJMSConnectionFactory() throws Exception {
		List<String> connectors = Arrays.asList(new String[] { "netty" });

		ConnectionFactoryConfiguration connectionFactoryConfiguration = new ConnectionFactoryConfigurationImpl("ConnectionFactory", false, connectors,
				(String) null);
		connectionFactoryConfiguration.setUseGlobalPools(false);

		jmsServerManager.createConnectionFactory(false, connectionFactoryConfiguration, "ConnectionFactory");
	}

	private void stopJMSServer() throws Exception {
		jmsServer.stop();
		jmsServer = null;
	}

	private void stopJMSServerManager() throws Exception {
		jmsServerManager.stop();
		jmsServerManager = null;
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