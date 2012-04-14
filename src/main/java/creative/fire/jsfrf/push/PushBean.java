package creative.fire.jsfrf.push;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import org.richfaces.application.push.MessageException;
import org.richfaces.application.push.TopicKey;
import org.richfaces.application.push.TopicsContext;

@ViewScoped
@ManagedBean
public class PushBean {
	private static final Logger log = Logger.getLogger(PushBean.class.getName());
	public void push() throws MessageException {
		TopicKey topicKey = new TopicKey("rfAddress");
		TopicsContext topicsContext = TopicsContext.lookup();
		int testCount = 10;
		while (testCount > 0) {
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String time = format.format(new Date());
			topicsContext.publish(topicKey, time);
			log.info("push event to rfAddress:" + testCount);
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			testCount--;
		}
	}
}