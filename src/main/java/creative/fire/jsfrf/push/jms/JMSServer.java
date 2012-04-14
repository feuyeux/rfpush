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

	private Configuration createHornetQConfiguration() {
		String naf=NettyAcceptorFactory.class.getName();
		String ncf=NettyConnectorFactory.class.getName();
		HashSet<TransportConfiguration> tSet = new HashSet<TransportConfiguration>();
		TransportConfiguration t1 = new TransportConfiguration(naf);
		TransportConfiguration t2 = new TransportConfiguration(ncf);
		tSet.add(t1);
		Configuration conf = new ConfigurationImpl();
		conf.setPersistenceEnabled(false);
		conf.setSecurityEnabled(false);
		conf.setAcceptorConfigurations(tSet);
		conf.getConnectorConfigurations().put("netty", t2);
		return conf;
	}

	private String f = "ConnectionFactory";

	private void createJMSConnectionFactory() throws Exception {
		List<String> connectors = Arrays.asList(new String[] { "netty" });

		ConnectionFactoryConfiguration config = new ConnectionFactoryConfigurationImpl(f, false, connectors, (String) null);
		config.setUseGlobalPools(false);

		jmsServerManager.createConnectionFactory(false, config, f);
	}
}