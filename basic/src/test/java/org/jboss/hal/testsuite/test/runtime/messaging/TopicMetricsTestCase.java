package org.jboss.hal.testsuite.test.runtime.messaging;

import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.dmr.ModelNode;
import org.jboss.hal.testsuite.category.Standalone;
import org.jboss.hal.testsuite.creaper.ManagementClientProvider;
import org.jboss.hal.testsuite.creaper.ResourceVerifier;
import org.jboss.hal.testsuite.dmr.ModelNodeGenerator;
import org.jboss.hal.testsuite.fragment.MetricsAreaFragment;
import org.jboss.hal.testsuite.page.runtime.MessagingStatisticsPage;
import org.jboss.hal.testsuite.test.runtime.deployments.DeploymentsOperations;
import org.jboss.hal.testsuite.test.runtime.messaging.deployment.MessagingProducerServlet;
import org.jboss.hal.testsuite.test.runtime.messaging.deployment.TestTopicMDB;
import org.jboss.hal.testsuite.util.ConfigUtils;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.Values;

import java.io.IOException;

import static java.net.HttpURLConnection.HTTP_OK;

@RunWith(Arquillian.class)
@Category(Standalone.class)
public class TopicMetricsTestCase {

    private static final Logger log = LoggerFactory.getLogger(TopicMetricsTestCase.class);
    private static final OnlineManagementClient client = ManagementClientProvider.createOnlineManagementClient();
    private static final Operations ops = new Operations(client);
    private static final DeploymentsOperations deployOps = new DeploymentsOperations(client);

    private static final String
        SUBSYSTEM_NAME = "messaging-activemq",
        SERVER = "server",
        DEFAULT = "default",
        TOPIC_NAME = "testTopic",
        TOPIC_JNDI = "java:/jms/topic/" + TOPIC_NAME,
        DEPLOYMENT_NAME = "producerServlet",
        DEPLOYMENT_EXTENSION = ".war",
        DEPLOYMENT_URL = "http://" + ConfigUtils.getUrl().getHost() + ":8080/" + DEPLOYMENT_NAME;

    private static final Address
        defaultJmsServerAddress = Address.subsystem(SUBSYSTEM_NAME).and(SERVER, DEFAULT),
        topicAddress = defaultJmsServerAddress.and("jms-topic", TOPIC_NAME);

    @Drone
    private WebDriver browser;

    @Page
    private MessagingStatisticsPage page;

    @BeforeClass
    public static void setUp() throws Exception {
        ModelNode entriesNode = new ModelNodeGenerator.ModelNodeListBuilder(new ModelNode(TOPIC_JNDI)).build();
        ops.add(topicAddress, Values.of("entries", entriesNode)).assertSuccess();
        deployOps.deploy(getDeployment());
    }

    @AfterClass
    public static void cleanUp() throws Exception {
        try {
            deployOps.undeploy(DEPLOYMENT_NAME + DEPLOYMENT_EXTENSION);
            ops.removeIfExists(topicAddress);
        } finally {
            IOUtils.closeQuietly(client);
        }
    }

    /**
     * Creates a deployment containing:
     * - a servlet sending messages to a topic
     * - a MDB consuming from the topic, to make sure there is an active subscription
     * - beans.xml to enable CDI
     */
    public static Archive<?> getDeployment() {
        WebArchive war = ShrinkWrap.create(WebArchive.class, DEPLOYMENT_NAME + DEPLOYMENT_EXTENSION);
        war.addClass(MessagingProducerServlet.class);
        war.addClass(TestTopicMDB.class);
        war.addAsWebInfResource("beans.xml", "beans.xml");
        return war;
    }

    /**
     * @tpTestDetails Checks that all the topic metrics are displayed correctly
     */
    @Test
    public void testTopicMetrics() throws Exception {
        page.navigateToDefaultProviderStats().switchToTopics().selectTopic(TOPIC_NAME);
        verifyMetrics();
        sendMessage();
        page.refreshStats();
        verifyMetrics();
    }

    private void sendMessage() throws IOException {
        log.info("Sending request to message producer servlet");
        HttpGet get = new HttpGet(DEPLOYMENT_URL + "/send");
        try (CloseableHttpClient client = HttpClientBuilder.create().build()) {
            CloseableHttpResponse response = client.execute(get);
            Assert.assertEquals(HTTP_OK, response.getStatusLine().getStatusCode());
        }
    }

    private void verifyMetrics() throws Exception {
        ResourceVerifier verifier = new ResourceVerifier(topicAddress, client);
        MetricsAreaFragment topicMetrics = page.getMetricsArea("Topic Metrics");

        String[] intAttrs = {"Delivering Count", "Durable Message Count", "Durable Subscription Count", "Subscription Count"};
        String[] longAttrs = {"Message Count", "Messages Added"};

        for (String attr : intAttrs) {
            String attrName = attr.toLowerCase().replace(" ", "-");
            verifier.verifyAttribute(attrName, Integer.parseInt(topicMetrics.getMetric(attr)));
        }

        for (String attr : longAttrs) {
            String attrName = attr.toLowerCase().replace(" ", "-");
            verifier.verifyAttribute(attrName, Long.parseLong(topicMetrics.getMetric(attr)));
        }

    }
}
