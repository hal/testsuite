package org.jboss.hal.testsuite.test.runtime.messaging.deployment;

import javax.annotation.Resource;
import javax.inject.Inject;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.JMSProducer;
import javax.jms.Topic;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/send")
public class MessagingProducerServlet extends HttpServlet {

    @Inject
    JMSContext jmsContext;

    @Resource(mappedName = "java:/jms/topic/testTopic")
    private Topic topic;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        try {
            sendMessagesToTopic();
        } catch (JMSException e) {
            throw new RuntimeException(e);
        }
    }

    private void sendMessagesToTopic() throws JMSException {
        JMSProducer producer = jmsContext.createProducer();
        producer.send(topic, "hello");
    }
}
