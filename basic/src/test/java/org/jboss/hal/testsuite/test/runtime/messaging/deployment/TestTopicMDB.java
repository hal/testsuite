package org.jboss.hal.testsuite.test.runtime.messaging.deployment;

import org.jboss.logging.Logger;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

@MessageDriven(name = "TestTopicMDB", activationConfig = {
    @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "jms/topic/testTopic"),
    @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Topic"),
    @ActivationConfigProperty(propertyName = "acknowledgeMode", propertyValue = "Auto-acknowledge")
})
public class TestTopicMDB implements MessageListener {

    private static final Logger log = Logger.getLogger(TestTopicMDB.class.toString());

    @Override
    public void onMessage(Message message) {
        TextMessage msg = null;
        try {
            if (message instanceof TextMessage) {
                msg = (TextMessage) message;
                log.info("Message received: " + msg.getText());
            } else {
                log.info("Received a message, but it is not a TextMessage.");
            }
        } catch (JMSException e) {
            throw new RuntimeException(e);
        }
    }
}
