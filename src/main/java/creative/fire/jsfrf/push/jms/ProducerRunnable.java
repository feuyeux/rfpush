package creative.fire.jsfrf.push.jms;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ProducerRunnable implements Runnable {
	private static final Logger LOGGER = Logger.getLogger(ProducerRunnable.class.getName());

	private AtomicBoolean runFlag = new AtomicBoolean(true);
	private JMSProducer messageProducer;

	public ProducerRunnable(JMSProducer messageProducer) {
		this.messageProducer = messageProducer;
	}

	public void run() {
		while (runFlag.get()) {
			try {
				messageProducer.sendMessage();
			} catch (Exception e) {
				LOGGER.log(Level.SEVERE, e.getMessage(), e);
			}

			try {
				Thread.sleep(messageProducer.getInterval());
			} catch (InterruptedException e) {
				LOGGER.log(Level.INFO, "MessageProducer has been interrupted");
				break;
			}
		}
	}

	public void stop() {
		runFlag.set(false);
	}
}
