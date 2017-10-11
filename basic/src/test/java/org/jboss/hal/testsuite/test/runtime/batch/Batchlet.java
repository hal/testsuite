package org.jboss.hal.testsuite.test.runtime.batch;

import org.jboss.logging.Logger;

import javax.batch.api.AbstractBatchlet;
import javax.batch.api.BatchProperty;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.concurrent.TimeUnit;

@Named("testBatchlet")
public class Batchlet extends AbstractBatchlet {

    private static final Logger LOGGER = Logger.getLogger(Batchlet.class);
    private Thread currentThread;

    @Inject
    @BatchProperty
    private Long stoppingInterval;

    @Override
    public String process() throws Exception {
        currentThread = Thread.currentThread();
        //Unreachable code error "hack"
        if (true) {
            for (; ; ) {
                try {
                    TimeUnit.MILLISECONDS.sleep(500);
                    LOGGER.debug("Processing");
                } catch (InterruptedException e) {
                    TimeUnit.MILLISECONDS.sleep(stoppingInterval);
                    return "STOPPED";
                }
            }
        }
        return "COMPLETED";
    }

    @Override
    public void stop() {
        currentThread.interrupt();
    }
}
