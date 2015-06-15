package org.jboss.hal.testsuite.test.configuration.infinispan;

import org.apache.commons.lang3.RandomStringUtils;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.Graphene;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.hal.testsuite.cli.CliClient;
import org.jboss.hal.testsuite.cli.CliClientFactory;
import org.jboss.hal.testsuite.cli.CliUtils;
import org.jboss.hal.testsuite.page.config.CacheContainersPage;
import org.jboss.hal.testsuite.test.category.Shared;
import org.jboss.hal.testsuite.test.util.ConfigAreaChecker;
import org.jboss.hal.testsuite.util.ConfigUtils;
import org.jboss.hal.testsuite.util.Console;
import org.jboss.hal.testsuite.util.ResourceVerifier;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;

import static org.jboss.hal.testsuite.cli.CliConstants.CACHE_CONTAINER_ADDRESS;
import static org.jboss.hal.testsuite.cli.CliConstants.DOMAIN_CACHE_CONTAINER_ADDRESS;

/**
 * @author mkrajcov <mkrajcov@redhat.com>
 */
@RunWith(Arquillian.class)
@Category(Shared.class)
public class CacheContainersTestCase {

    private static final String CACHE_CONTAINER_NAME = "cc_" + RandomStringUtils.randomAlphabetic(5);
    public static final String DMR_BASE = (ConfigUtils.isDomain() ? DOMAIN_CACHE_CONTAINER_ADDRESS : CACHE_CONTAINER_ADDRESS) + "=";
    private static final String CACHE_DMR = DMR_BASE + CACHE_CONTAINER_NAME;
    private static final String JNDI_NAME = "java:/" + CACHE_CONTAINER_NAME;
    private static final String START = "EAGER";
    private static final String EVICTION_EXECUTOR = "ee_" + RandomStringUtils.randomAlphanumeric(5);
    private static final String LISTENER_EXECUTOR = "le_" + RandomStringUtils.randomAlphanumeric(5);
    private static final String REPLICATION_QUEUE_EXECUTOR = "rqe_" + RandomStringUtils.randomAlphanumeric(5);
    private static final String TRANSPORT_VALUE = "1000";

    private final String cacheContainerName = "container_" + RandomStringUtils.randomAlphanumeric(5);
    private final String cacheContainerDmr = DMR_BASE + cacheContainerName;
    private final String transportDmr = cacheContainerDmr +"/transport=TRANSPORT";
    
    private CliClient client = CliClientFactory.getClient();
    private ResourceVerifier attrVerifier = new ResourceVerifier(cacheContainerDmr, client);
    private ConfigAreaChecker attrChecker = new ConfigAreaChecker(attrVerifier);
    private ResourceVerifier transportVerifier = new ResourceVerifier(transportDmr, client);
    private ConfigAreaChecker transportChecker = new ConfigAreaChecker(transportVerifier);

    @Drone
    public WebDriver browser;

    @Page
    public CacheContainersPage page;

    @Before
    public void before() {
        addCacheContainer();
        browser.navigate().refresh();
        Graphene.goTo(CacheContainersPage.class);
        Console.withBrowser(browser).waitUntilLoaded();
        Console.withBrowser(browser).maximizeWindow();
    }

    @After
    public void after(){
        deleteCacheContainer();
    }

    @Test
    @InSequence(0)
    public void createCacheContainer(){
        page.content().addCacheContainer(CACHE_CONTAINER_NAME);

        attrVerifier.verifyResource(DMR_BASE + CACHE_CONTAINER_NAME,true);
    }
    
    @Test
    @InSequence(1)
    public void removeCacheContainer(){
        page.getResourceManager().removeResourceAndConfirm(CACHE_CONTAINER_NAME);

        attrVerifier.verifyResource(DMR_BASE + CACHE_CONTAINER_NAME,false);
    }

    @Ignore
    @Test
    public void editStartMode() {
        attrChecker.editSelectAndAssert(page, "start", START)
                .rowName(cacheContainerName).invoke();
    }
    
    @Test
    public void editJndiName() {
        attrChecker.editTextAndAssert(page, "jndiName", JNDI_NAME)
                .rowName(cacheContainerName).invoke();
    }
    
    @Test
    public void editEvictionExecutor(){
        attrChecker.editTextAndAssert(page, "evictionExecutor", EVICTION_EXECUTOR)
                .rowName(cacheContainerName).invoke();
    }

    @Test
    public void editListenerExecutor(){
        attrChecker.editTextAndAssert(page, "listenerExecutor", LISTENER_EXECUTOR)
                .rowName(cacheContainerName).invoke();
    }
    
    @Test
    public void editReplicationQueueExecutor(){
        attrChecker.editTextAndAssert(page, "replicationQueueExecutor", REPLICATION_QUEUE_EXECUTOR)
                .rowName(cacheContainerName).invoke();
    }

    @Test
    public void editStack() {
        transportChecker.editTextAndAssert(page, "stack", TRANSPORT_VALUE)
                .rowName(cacheContainerName)
                .tab("Transport")
                .invoke();
    }

    @Test
    public void editExecutor(){
        transportChecker.editTextAndAssert(page, "executor", TRANSPORT_VALUE)
                .rowName(cacheContainerName)
                .tab("Transport")
                .invoke();
    }

    @Test
    public void editLockTimeout(){
        transportChecker.editTextAndAssert(page, "lockTimeout", TRANSPORT_VALUE)
                .rowName(cacheContainerName)
                .tab("Transport")
                .invoke();
    }

    private void addCacheContainer() {
        String addContainer = CliUtils.buildCommand(cacheContainerDmr, ":add");
        String addTransport = CliUtils.buildCommand(transportDmr, ":add", new String[] {"stack=500"});
        client.executeCommand(addContainer);
        client.executeCommand(addTransport);
    }

    private void deleteCacheContainer() {
        String cmd = CliUtils.buildCommand(cacheContainerDmr, ":remove");
        client.executeCommand(cmd);
    }

}
