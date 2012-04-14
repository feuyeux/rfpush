package creative.fire.jsfrf.push;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.enterprise.context.SessionScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.inject.Named;

import org.richfaces.application.push.MessageException;
import org.richfaces.cdi.push.Push;

@Named("cdiPushBean")
@SessionScoped
public class CDIPushBean implements Serializable {
	private static final long serialVersionUID = 9012104591335228707L;
	@Inject
	@Push(topic = "rfAddress")
	Event<String> pushEvent;

	public void push() throws MessageException {
		int testCount = 10;
		while (testCount > 0) {
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String time = format.format(new Date());
			//topicsContext.publish(topicKey, time);
			pushEvent.fire(time);
			System.out.println("push event " + testCount);
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			testCount--;
		}
	}
}