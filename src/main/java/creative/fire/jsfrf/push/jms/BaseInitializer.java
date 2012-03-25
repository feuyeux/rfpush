package creative.fire.jsfrf.push.jms;

import javax.faces.application.Application;
import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.PostConstructApplicationEvent;
import javax.faces.event.PreDestroyApplicationEvent;
import javax.faces.event.SystemEvent;
import javax.faces.event.SystemEventListener;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public abstract class BaseInitializer implements SystemEventListener,
        ServletContextListener {

    private boolean correctlyInitialized = false;

    public void processEvent(SystemEvent event) throws AbortProcessingException {
        if (isCapabilityEnabled()) {
            if (event instanceof PostConstructApplicationEvent) {
                Application application = FacesContext.getCurrentInstance().getApplication();
                application.subscribeToEvent(PreDestroyApplicationEvent.class, this);

                try {
                    initializeCapability();
                    correctlyInitialized = true;
                } catch (Exception e) {
                    throw new RuntimeException("Capability " + this.getClass().getName() + " was not correctly initialized", e);
                }
            } else {
                try {
                    finalizeCapability();
                } catch (Exception e) {
                    throw new RuntimeException("Capability " + this.getClass().getName() + " was not correctly finalized", e);
                }
            }
        }
    }

    public abstract void initializeCapability();

	public void contextInitialized(ServletContextEvent sce) {
        try {
            initializeCapability();
            correctlyInitialized = true;
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    public void contextDestroyed(ServletContextEvent sce) {
        try {
            finalizeCapability();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    protected boolean isCorrentlyInitialized() {
        return correctlyInitialized;
    }

    public boolean isListenerForSource(Object source) {
        return true;
    }

    public void finalizeCapability() throws Exception {
    }

    public boolean isCapabilityEnabled() {
        return true;
    }
}
