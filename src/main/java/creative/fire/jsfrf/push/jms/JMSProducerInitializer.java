package creative.fire.jsfrf.push.jms;

public class JMSProducerInitializer extends BaseProducerInitializer {
	@Override
	public JMSProducer createMessageProducer() {
		return new JMSProducer();
	}

	@Override
	public boolean isCapabilityEnabled() {
		return JMSInitializer.isJmsEnabled();
	}
}
