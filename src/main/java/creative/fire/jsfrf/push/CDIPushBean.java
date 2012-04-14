package creative.fire.jsfrf.push;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;

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
	private static final Logger log = Logger.getLogger(CDIPushBean.class.getName());
	@Inject
	@Push(topic = "cdiAddress")
	Event<String> pushEvent;

	public void push() throws MessageException {
		int testCount = 10;
		while (testCount > 0) {
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String time = format.format(new Date());
			pushEvent.fire(time);
			log.info("push event to cdiAddress:" + testCount);
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			testCount--;
		}
	}
}