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
import org.jboss.hal.testsuite.fragment.config.infinispan.CacheFragment;
import org.jboss.hal.testsuite.fragment.config.infinispan.CacheWizard;
import org.jboss.hal.testsuite.fragment.formeditor.Editor;
import org.jboss.hal.testsuite.page.config.InvalidationCachePage;
import org.jboss.hal.testsuite.test.category.Shared;
import org.jboss.hal.testsuite.test.util.ConfigAreaChecker;
import org.jboss.hal.testsuite.util.ConfigUtils;
import org.jboss.hal.testsuite.util.Console;
import org.jboss.hal.testsuite.util.ResourceVerifier;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
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
public class InvalidationCacheTestCase {

    private static final String CACHE_CONTAINER = "hibernate";
    private static final String CACHE_NAME = "cn_" + RandomStringUtils.randomAlphanumeric(5);
    private static final String JNDI_NAME = "java:/" + CACHE_NAME;
    private static final String DMR_BASE = (ConfigUtils.isDomain() ? DOMAIN_CACHE_CONTAINER_ADDRESS : CACHE_CONTAINER_ADDRESS) + "=" + CACHE_CONTAINER + "/invalidation-cache=";

    private final String cacheName = "cache_" + RandomStringUtils.randomAlphanumeric(5);
    private final String cacheDmr = DMR_BASE + cacheName;
    private final String lockingDmr = cacheDmr + "/locking=LOCKING";
    private final String transactionDmr = cacheDmr + "/transaction=TRANSACTION";
    private final String storeDmr = cacheDmr + "/store=STORE";

    private CliClient client = CliClientFactory.getClient();
    private ResourceVerifier verifier = new ResourceVerifier(cacheDmr, client);
    private ResourceVerifier lockingVerifier = new ResourceVerifier(lockingDmr, client);
    private ResourceVerifier transactionVerifier = new ResourceVerifier(transactionDmr, client);
    private ResourceVerifier storeVerifier = new ResourceVerifier(storeDmr, client);
    private ConfigAreaChecker checker = new ConfigAreaChecker(verifier);

    @Drone
    public WebDriver browser;

    @Page
    public InvalidationCachePage page;

    @Before
    public void before() {
        addCache();
        browser.navigate().refresh();
        Graphene.goTo(InvalidationCachePage.class);
        Console.withBrowser(browser).waitUntilLoaded().maximizeWindow();
    }

    @After
    public void after(){
        deleteCache();
    }

    @Test
    @InSequence(0)
    public void createCache() {
        CacheWizard wizard = page.content().addCache();

        boolean result = wizard.name(CACHE_NAME)
                .cacheContainer(CACHE_CONTAINER)
                .finish();

        Assert.assertTrue("Window should be closed", result);
        verifier.verifyResource(DMR_BASE + CACHE_NAME, true);
    }


    @Test
    @InSequence(1)
    public void removeCache() {
        page.content().getResourceManager().removeResourceAndConfirm(CACHE_NAME);
        verifier.verifyResource(DMR_BASE + CACHE_NAME, false);
    }

    //ATTRIBUTES
    @Test
    public void editDefaultForCacheContainer() {
        CacheFragment content = page.content();
        content.getResourceManager().selectByName(cacheName);
        Editor edit = content.edit();
        edit.checkbox("default", true);

        Assert.assertTrue("Config was supposed to be saved successfully, read view should be active.", content.save());
        new ResourceVerifier(CACHE_CONTAINER_ADDRESS + "=" + CACHE_CONTAINER, client).verifyAttribute("default-cache", cacheName);
    }

    @Test
    public void editStartMode() {
        checker.editSelectAndAssert(page, "start", "LAZY").rowName(cacheName).invoke();
    }

    @Test
    public void editJndiName() {
        checker.editTextAndAssert(page, "jndiName", JNDI_NAME).rowName(cacheName).invoke();
    }

    @Test
    public void editBatching() {
        checker.editCheckboxAndAssert(page, "batching", true).rowName(cacheName).invoke();
        checker.editCheckboxAndAssert(page, "batching", false).rowName(cacheName).invoke();
    }

    @Test
    public void editIndexing() {
        checker.editSelectAndAssert(page, "indexing", "LOCAL").rowName(cacheName).invoke();
        checker.editSelectAndAssert(page, "indexing", "ALL").rowName(cacheName).invoke();
        checker.editSelectAndAssert(page, "indexing", "NONE").rowName(cacheName).invoke();
    }

    //STORE
    @Test
    public void editShared() {
        checker.editCheckboxAndAssert(page, "storeShared", true).withVerifier(storeVerifier)
                .tab("Store").rowName(cacheName).dmrAttribute("shared").invoke();
        checker.editCheckboxAndAssert(page, "storeShared", false).withVerifier(storeVerifier)
                .tab("Store").rowName(cacheName).dmrAttribute("shared").invoke();
    }

    @Test
    public void editPassivation() {
        checker.editCheckboxAndAssert(page, "storePassivation", true).withVerifier(storeVerifier)
                .tab("Store").rowName(cacheName).dmrAttribute("passivation").invoke();
        checker.editCheckboxAndAssert(page, "storePassivation", false).withVerifier(storeVerifier)
                .tab("Store").rowName(cacheName).dmrAttribute("passivation").invoke();
    }

    @Test
    public void editPreload() {
        checker.editCheckboxAndAssert(page, "storePreload", true).withVerifier(storeVerifier)
                .tab("Store").rowName(cacheName).dmrAttribute("preload").invoke();
        checker.editCheckboxAndAssert(page, "storePreload", false).withVerifier(storeVerifier)
                .tab("Store").rowName(cacheName).dmrAttribute("preload").invoke();
    }

    @Test
    public void editFetchState() {
        checker.editCheckboxAndAssert(page, "storeFetchState", true).withVerifier(storeVerifier)
                .tab("Store").rowName(cacheName).dmrAttribute("fetch-state").invoke();
        checker.editCheckboxAndAssert(page, "storeFetchState", false).withVerifier(storeVerifier)
                .tab("Store").rowName(cacheName).dmrAttribute("fetch-state").invoke();
    }

    @Test
    public void editPurge() {
        checker.editCheckboxAndAssert(page, "storePurge", true).withVerifier(storeVerifier)
                .tab("Store").rowName(cacheName).dmrAttribute("purge").invoke();
        checker.editCheckboxAndAssert(page, "storePurge", false).withVerifier(storeVerifier)
                .tab("Store").rowName(cacheName).dmrAttribute("purge").invoke();
    }

    @Test
    public void editSingleton() {
        checker.editCheckboxAndAssert(page, "storeSingleton", true).withVerifier(storeVerifier)
                .tab("Store").rowName(cacheName).dmrAttribute("singleton").invoke();
        checker.editCheckboxAndAssert(page, "storeSingleton", false).withVerifier(storeVerifier)
                .tab("Store").rowName(cacheName).dmrAttribute("singleton").invoke();
    }

    //LOCKING
    @Test
    public void editConcurrencyLevel() {
        checker.editTextAndAssert(page, "concurrencyLevel", "1").withVerifier(lockingVerifier)
                .tab("Locking").rowName(cacheName).invoke();
        checker.editTextAndAssert(page, "concurrencyLevel", "-1").expectError()
                .tab("Locking").rowName(cacheName).invoke();
    }

    @Test
    public void editIsolation() {
        checker.editSelectAndAssert(page, "isolation", "REPEATABLE_READ").withVerifier(lockingVerifier)
                .tab("Locking").rowName(cacheName).invoke();
        checker.editSelectAndAssert(page, "isolation", "NONE").withVerifier(lockingVerifier)
                .tab("Locking").rowName(cacheName).invoke();
        checker.editSelectAndAssert(page, "isolation", "SERIALIZABLE").withVerifier(lockingVerifier)
                .tab("Locking").rowName(cacheName).invoke();
    }

    @Test
    public void editStriping() {
        checker.editCheckboxAndAssert(page, "striping", true).withVerifier(lockingVerifier)
                .tab("Locking").rowName(cacheName).invoke();
        checker.editCheckboxAndAssert(page, "striping", false).withVerifier(lockingVerifier)
                .tab("Locking").rowName(cacheName).invoke();
    }

    @Test
    public void editAcquireTimeout() {
        checker.editTextAndAssert(page, "acquireTimeout", "5000").withVerifier(lockingVerifier)
                .tab("Locking").rowName(cacheName).invoke();
        checker.editTextAndAssert(page, "acquireTimeout", "-1").expectError()
                .tab("Locking").rowName(cacheName).invoke();
    }

    //TRANSACTION
    @Test
    public void editMode() {
        checker.editSelectAndAssert(page, "transactionMode", "NONE").withVerifier(transactionVerifier)
                .tab("Trans").rowName(cacheName).dmrAttribute("mode").invoke();
        checker.editSelectAndAssert(page, "transactionMode", "NON_XA").withVerifier(transactionVerifier)
                .tab("Trans").rowName(cacheName).dmrAttribute("mode").invoke();
    }

    @Test
    public void editLocking() {
        checker.editSelectAndAssert(page, "locking", "OPTIMISTIC").withVerifier(transactionVerifier)
                .tab("Trans").rowName(cacheName).invoke();
        checker.editSelectAndAssert(page, "locking", "PESSIMISTIC").withVerifier(transactionVerifier)
                .tab("Trans").rowName(cacheName).invoke();
    }

    @Test
    public void editStopTimeout() {
        checker.editTextAndAssert(page, "stopTimeout", "13400").withVerifier(transactionVerifier)
                .tab("Trans").rowName(cacheName).invoke();
        checker.editTextAndAssert(page, "stopTimeout", "150asdf50").expectError()
                .tab("Trans").rowName(cacheName).invoke();
        checker.editTextAndAssert(page, "stopTimeout", "-15000").expectError()
                .tab("Trans").rowName(cacheName).invoke();
    }

    public void addCache(){
        String addCache = CliUtils.buildCommand(cacheDmr, ":add", new String[]{"mode=SYNC"});
        String addTransaction = CliUtils.buildCommand(transactionDmr, ":add");
        String addLocking = CliUtils.buildCommand(lockingDmr, ":add");
        String addStore = CliUtils.buildCommand(storeDmr, ":add", new String[] {"class=clazz"});
        client.executeCommand(addCache);
        client.executeCommand(addTransaction);
        client.executeCommand(addLocking);
        client.executeCommand(addStore);
    }

    public void deleteCache(){
        String cmd = CliUtils.buildCommand(cacheDmr, ":remove");
        client.executeCommand(cmd);
    }
}

