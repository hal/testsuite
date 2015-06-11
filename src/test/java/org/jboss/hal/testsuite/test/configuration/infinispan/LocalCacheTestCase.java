package org.jboss.hal.testsuite.test.configuration.infinispan;

import org.apache.commons.lang3.RandomStringUtils;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.Graphene;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.hal.testsuite.cli.CliClient;
import org.jboss.hal.testsuite.cli.CliClientFactory;
import org.jboss.hal.testsuite.fragment.config.infinispan.CacheFragment;
import org.jboss.hal.testsuite.fragment.config.infinispan.CacheWizard;
import org.jboss.hal.testsuite.fragment.formeditor.Editor;
import org.jboss.hal.testsuite.page.config.LocalCachePage;
import org.jboss.hal.testsuite.test.category.Shared;
import org.jboss.hal.testsuite.test.util.ConfigAreaChecker;
import org.jboss.hal.testsuite.util.ConfigUtils;
import org.jboss.hal.testsuite.util.Console;
import org.jboss.hal.testsuite.util.ResourceVerifier;
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
public class LocalCacheTestCase {

    private static final String CACHE_CONTAINER = "hibernate";
    private static final String CACHE_NAME = "cn_" + RandomStringUtils.randomAlphanumeric(5);
    private static final String JNDI_NAME = "java:/" + CACHE_NAME;

    private static final String CACHE_DMR = (ConfigUtils.isDomain() ? DOMAIN_CACHE_CONTAINER_ADDRESS : CACHE_CONTAINER_ADDRESS) + "=" + CACHE_CONTAINER + "/local-cache=" + CACHE_NAME;

    private CliClient client = CliClientFactory.getClient();
    private ResourceVerifier verifier = new ResourceVerifier(CACHE_DMR, client);
    private ResourceVerifier lockingVerifier = new ResourceVerifier(CACHE_DMR + "/locking=LOCKING", client);
    private ResourceVerifier transactionVerifier = new ResourceVerifier(CACHE_DMR + "/transaction=TRANSACTION", client);
    private ResourceVerifier storeVerifier = new ResourceVerifier(CACHE_DMR + "/store=STORE", client);
    private ConfigAreaChecker checker = new ConfigAreaChecker(verifier);

    @Drone
    public WebDriver browser;

    @Page
    public LocalCachePage page;

    @Before
    public void before() {
        browser.navigate().refresh();
        Graphene.goTo(LocalCachePage.class);
        Console.withBrowser(browser).waitUntilLoaded();
        Console.withBrowser(browser).maximizeWindow();
    }

    @Test
    @InSequence(0)
    public void createCache() {
        CacheWizard wizard = page.content().addCache();

        boolean result = wizard.name(CACHE_NAME)
                .cacheContainer(CACHE_CONTAINER)
                .finish();

        Assert.assertTrue("Window should be closed", result);
        verifier.verifyResource(true);
    }

    //ATTRIBUTES
    @Test
    @InSequence(1)
    public void editDefaultForCacheContainer() {
        CacheFragment content = page.content();
        content.getResourceManager().selectByName(CACHE_NAME);
        Editor edit = content.edit();
        edit.checkbox("default", true);

        content.saveAndAssert(true);
        new ResourceVerifier(CACHE_CONTAINER_ADDRESS + "=" + CACHE_CONTAINER, client).verifyAttribute("default-cache", CACHE_NAME);
    }

    @Test
    @InSequence(2)
    public void editStartMode() {
        checker.editSelectAndAssert(page, "start", "LAZY").rowName(CACHE_NAME).invoke();
    }

    @Test
    @InSequence(3)
    public void editJndiName() {
        checker.editTextAndAssert(page, "jndiName", JNDI_NAME).rowName(CACHE_NAME).invoke();
    }

    @Test
    @InSequence(4)
    public void editBatching() {
        checker.editCheckboxAndAssert(page, "batching", true).rowName(CACHE_NAME).invoke();
        checker.editCheckboxAndAssert(page, "batching", false).rowName(CACHE_NAME).invoke();
    }

    @Test
    @InSequence(5)
    public void editIndexing() {
        checker.editSelectAndAssert(page, "indexing", "LOCAL").rowName(CACHE_NAME).invoke();
        checker.editSelectAndAssert(page, "indexing", "ALL").rowName(CACHE_NAME).invoke();
        checker.editSelectAndAssert(page, "indexing", "NONE").rowName(CACHE_NAME).invoke();
    }

    //STORE
    @Test
    @InSequence(6)
    public void editShared() {
        checker.editCheckboxAndAssert(page, "storeShared", true).withVerifier(storeVerifier)
                .tab("Store").rowName(CACHE_NAME).defineFirst("hasStore", "storeClass").dmrAttribute("shared").invoke();
        checker.editCheckboxAndAssert(page, "storeShared", false).withVerifier(storeVerifier)
                .tab("Store").rowName(CACHE_NAME).defineFirst("hasStore", "storeClass").dmrAttribute("shared").invoke();
    }

    @Test
    @InSequence(7)
    public void editPassivation() {
        checker.editCheckboxAndAssert(page, "storePassivation", true).withVerifier(storeVerifier)
                .tab("Store").rowName(CACHE_NAME).defineFirst("hasStore", "storeClass").dmrAttribute("passivation").invoke();
        checker.editCheckboxAndAssert(page, "storePassivation", false).withVerifier(storeVerifier)
                .tab("Store").rowName(CACHE_NAME).defineFirst("hasStore", "storeClass").dmrAttribute("passivation").invoke();
    }

    @Test
    @InSequence(8)
    public void editPreload() {
        checker.editCheckboxAndAssert(page, "storePreload", true).withVerifier(storeVerifier)
                .tab("Store").rowName(CACHE_NAME).defineFirst("hasStore", "storeClass").dmrAttribute("preload").invoke();
        checker.editCheckboxAndAssert(page, "storePreload", false).withVerifier(storeVerifier)
                .tab("Store").rowName(CACHE_NAME).defineFirst("hasStore", "storeClass").dmrAttribute("preload").invoke();
    }

    @Test
    @InSequence(9)
    public void editFetchState() {
        checker.editCheckboxAndAssert(page, "storeFetchState", true).withVerifier(storeVerifier)
                .tab("Store").rowName(CACHE_NAME).defineFirst("hasStore", "storeClass").dmrAttribute("fetch-state").invoke();
        checker.editCheckboxAndAssert(page, "storeFetchState", false).withVerifier(storeVerifier)
                .tab("Store").rowName(CACHE_NAME).defineFirst("hasStore", "storeClass").dmrAttribute("fetch-state").invoke();
    }

    @Test
    @InSequence(10)
    public void editPurge() {
        checker.editCheckboxAndAssert(page, "storePurge", true).withVerifier(storeVerifier)
                .tab("Store").rowName(CACHE_NAME).defineFirst("hasStore", "storeClass").dmrAttribute("purge").invoke();
        checker.editCheckboxAndAssert(page, "storePurge", false).withVerifier(storeVerifier)
                .tab("Store").rowName(CACHE_NAME).defineFirst("hasStore", "storeClass").dmrAttribute("purge").invoke();
    }

    @Test
    @InSequence(11)
    public void editSingleton() {
        checker.editCheckboxAndAssert(page, "storeSingleton", true).withVerifier(storeVerifier)
                .tab("Store").rowName(CACHE_NAME).defineFirst("hasStore", "storeClass").dmrAttribute("singleton").invoke();
        checker.editCheckboxAndAssert(page, "storeSingleton", false).withVerifier(storeVerifier)
                .tab("Store").rowName(CACHE_NAME).defineFirst("hasStore", "storeClass").dmrAttribute("singleton").invoke();
    }

    //LOCKING
    @Test
    @InSequence(12)
    public void editConcurrencyLevel() {
        checker.editTextAndAssert(page, "concurrencyLevel", "1").withVerifier(lockingVerifier)
                .tab("Locking").rowName(CACHE_NAME).defineFirst("hasLocking").invoke();
        checker.editTextAndAssert(page, "concurrencyLevel", "-1").expectError()
                .tab("Locking").rowName(CACHE_NAME).defineFirst("hasLocking").invoke();
    }

    @Test
    @InSequence(13)
    public void editIsolation() {
        checker.editSelectAndAssert(page, "isolation", "REPEATABLE_READ").withVerifier(lockingVerifier)
                .tab("Locking").rowName(CACHE_NAME).defineFirst("hasLocking").invoke();
        checker.editSelectAndAssert(page, "isolation", "NONE").withVerifier(lockingVerifier)
                .tab("Locking").rowName(CACHE_NAME).defineFirst("hasLocking").invoke();
        checker.editSelectAndAssert(page, "isolation", "SERIALIZABLE").withVerifier(lockingVerifier)
                .tab("Locking").rowName(CACHE_NAME).defineFirst("hasLocking").invoke();
    }

    @Test
    @InSequence(14)
    public void editStriping() {
        checker.editCheckboxAndAssert(page, "striping", true).withVerifier(lockingVerifier)
                .tab("Locking").rowName(CACHE_NAME).defineFirst("hasLocking").invoke();
        checker.editCheckboxAndAssert(page, "striping", false).withVerifier(lockingVerifier)
                .tab("Locking").rowName(CACHE_NAME).defineFirst("hasLocking").invoke();
    }

    @Test
    @InSequence(15)
    public void editAcquireTimeout() {
        checker.editTextAndAssert(page, "acquireTimeout", "5000").withVerifier(lockingVerifier)
                .tab("Locking").rowName(CACHE_NAME).defineFirst("hasLocking").invoke();
        checker.editTextAndAssert(page, "acquireTimeout", "-1").expectError()
                .tab("Locking").rowName(CACHE_NAME).defineFirst("hasLocking").invoke();
    }

    //TRANSACTION
    @Test
    @InSequence(16)
    public void editMode() {
        checker.editSelectAndAssert(page, "transactionMode", "NONE").withVerifier(transactionVerifier)
                .tab("Trans").rowName(CACHE_NAME).defineFirst("hasTransaction").dmrAttribute("mode").invoke();
        checker.editSelectAndAssert(page, "transactionMode", "NON_XA").withVerifier(transactionVerifier)
                .tab("Trans").rowName(CACHE_NAME).defineFirst("hasTransaction").dmrAttribute("mode").invoke();
    }

    @Test
    @InSequence(17)
    public void editLocking() {
        checker.editSelectAndAssert(page, "locking", "OPTIMISTIC").withVerifier(transactionVerifier)
                .tab("Trans").rowName(CACHE_NAME).defineFirst("hasTransaction").invoke();
        checker.editSelectAndAssert(page, "locking", "PESSIMISTIC").withVerifier(transactionVerifier)
                .tab("Trans").rowName(CACHE_NAME).defineFirst("hasTransaction").invoke();
    }

    @Test
    @InSequence(18)
    public void editStopTimeout() {
        checker.editTextAndAssert(page, "stopTimeout", "13400").withVerifier(transactionVerifier)
                .tab("Trans").rowName(CACHE_NAME).defineFirst("hasTransaction").invoke();
        checker.editTextAndAssert(page, "stopTimeout", "150asdf50").expectError()
                .tab("Trans").rowName(CACHE_NAME).defineFirst("hasTransaction").invoke();
        checker.editTextAndAssert(page, "stopTimeout", "-15000").expectError()
                .tab("Trans").rowName(CACHE_NAME).defineFirst("hasTransaction").invoke();
    }

    @Test
    @InSequence(19)
    public void removeCache() {
        page.content().getResourceManager().removeResourceAndConfirm(CACHE_NAME);
    }
}

