package org.jboss.hal.testsuite.test.configuration.undertow;

import org.apache.commons.lang.RandomStringUtils;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.category.Shared;
import org.jboss.hal.testsuite.creaper.ResourceVerifier;
import org.jboss.hal.testsuite.fragment.formeditor.Editor;
import org.jboss.hal.testsuite.fragment.shared.modal.WizardWindow;
import org.jboss.hal.testsuite.fragment.shared.util.ResourceManager;
import org.jboss.hal.testsuite.page.config.UndertowHostPage;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Batch;
import org.wildfly.extras.creaper.core.online.operations.OperationException;
import org.wildfly.extras.creaper.core.online.operations.Values;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

@RunWith(Arquillian.class)
@Category(Shared.class)
public class HostTestCase extends UndertowTestCaseAbstract {

    @Page
    private UndertowHostPage page;

    private static final String ALIAS = "alias";
    private static final String DEFAULT_RESPONSE_CODE = "default-response-code";
    private static final String DEFAULT_WEB_MODULE = "default-web-module";
    private static final String DISABLE_CONSOLE_REDIRECT = "disable-console-redirect";

    //values
    private static final String[] ALIAS_VALUES = new String[]{"localhost", "test", "example"};
    private static final int DEFAULT_RESPONSE_CODE_VALUE = 500;

    private static final Address
            HTTP_SERVER_ADDRESS = UndertowConstants.UNDERTOW_ADDRESS.and("server", "undertow-http-server-host_" + RandomStringUtils.randomAlphanumeric(5)),
            HOST_ADDRESS = HTTP_SERVER_ADDRESS.and("host", "host_" + RandomStringUtils.randomAlphanumeric(5)),
            HOST_TBR_ADDRESS = HTTP_SERVER_ADDRESS.and("host", "host-btr_" + RandomStringUtils.randomAlphanumeric(5)),
            HOST_TBA_ADDRESS = HTTP_SERVER_ADDRESS.and("host", "host-tba_" + RandomStringUtils.randomAlphanumeric(5));

    private static final Address
            REWRITE_FILTER_ADDRESS = UndertowConstants.UNDERTOW_FILTERS_ADDRESS.and("rewrite",
                    "RewriteFilterReferenceREMOVE_" + RandomStringUtils.randomAlphanumeric(5)),
            REWRITE_FILTER_REFERENCE_ADDRESS = HOST_ADDRESS.and("filter-ref", REWRITE_FILTER_ADDRESS.getLastPairValue()),
            ERROR_PAGE_FILTER_ADDRESS = UndertowConstants.UNDERTOW_FILTERS_ADDRESS.and("error-page",
                    "ErrorFilterReference_" + RandomStringUtils.randomAlphanumeric(5)),
            ERROR_PAGE_FILTER_REFERENCE_ADDRESS = HOST_ADDRESS.and("filter-ref", ERROR_PAGE_FILTER_ADDRESS.getLastPairValue()),
            GZIP_PAGE_FILTER_ADDRESS = UndertowConstants.UNDERTOW_FILTERS_ADDRESS.and("gzip",
                    "GzipFilterReferenceADD_" + RandomStringUtils.randomAlphanumeric(5)),
            GZIP_FILTER_REFERENCE_ADDRESS = HOST_ADDRESS.and("filter-ref", GZIP_PAGE_FILTER_ADDRESS.getLastPairValue()),
            REQUEST_LIMIT_FILTER_ADDRESS = UndertowConstants.UNDERTOW_FILTERS_ADDRESS.and("request-limit",
                    "ReqLimitFilterReference_" + RandomStringUtils.randomAlphanumeric(5)),
            REQUEST_LIMIT_FILTER_REFERENCE_ADDRESS = HOST_ADDRESS.and("filter-ref", REQUEST_LIMIT_FILTER_ADDRESS.getLastPairValue());

    @BeforeClass
    public static void setUp() throws InterruptedException, TimeoutException, IOException {
        final String defaultWebModule = "default-web-module";
        operations.add(HTTP_SERVER_ADDRESS).assertSuccess();
        operations.add(HOST_ADDRESS, Values.of(defaultWebModule, "defaultWebModule-" + RandomStringUtils
                .randomAlphanumeric(6))).assertSuccess();
        operations.add(HOST_TBR_ADDRESS, Values.of(defaultWebModule, "defaultWebModule-tbr-" + RandomStringUtils
                .randomAlphanumeric(6))).assertSuccess();

        administration.reloadIfRequired();

        //host filter references
        operations.add(GZIP_PAGE_FILTER_ADDRESS).assertSuccess();

        operations.batch(new Batch()
                .add(ERROR_PAGE_FILTER_ADDRESS, Values.of("code", 404))
                .add(HOST_ADDRESS.and("filter-ref", ERROR_PAGE_FILTER_ADDRESS.getLastPairValue()))).assertSuccess();

        operations.batch(new Batch()
                .add(REWRITE_FILTER_ADDRESS, Values.of("target", "foo"))
                .add(HOST_ADDRESS.and("filter-ref", REWRITE_FILTER_ADDRESS.getLastPairValue()))).assertSuccess();


        operations.batch(new Batch()
                .add(REQUEST_LIMIT_FILTER_ADDRESS, Values.of("max-concurrent-requests", 42))
                .add(HOST_ADDRESS.and("filter-ref", REQUEST_LIMIT_FILTER_ADDRESS.getLastPairValue()))).assertSuccess();

        administration.reloadIfRequired();
    }

    @Before
    public void before() {
        page.navigate();
        page.viewHTTPServer(HTTP_SERVER_ADDRESS.getLastPairValue())
                .switchToHosts()
                .selectItemInTableByText(HOST_ADDRESS.getLastPairValue());
    }

    @AfterClass
    public static void tearDown() throws InterruptedException, IOException, TimeoutException, OperationException {
        //remove references to filters first
        operations.removeIfExists(REWRITE_FILTER_REFERENCE_ADDRESS);
        operations.removeIfExists(GZIP_FILTER_REFERENCE_ADDRESS);
        operations.removeIfExists(ERROR_PAGE_FILTER_REFERENCE_ADDRESS);
        operations.removeIfExists(REQUEST_LIMIT_FILTER_REFERENCE_ADDRESS);

        administration.reloadIfRequired();

        operations.removeIfExists(GZIP_PAGE_FILTER_ADDRESS);
        operations.removeIfExists(REWRITE_FILTER_ADDRESS);
        operations.removeIfExists(ERROR_PAGE_FILTER_ADDRESS);
        operations.removeIfExists(REQUEST_LIMIT_FILTER_ADDRESS);
        operations.removeIfExists(HOST_ADDRESS);
        operations.removeIfExists(HOST_TBA_ADDRESS);
        operations.removeIfExists(HOST_TBR_ADDRESS);
        operations.removeIfExists(HTTP_SERVER_ADDRESS);
    }

    @Test
    public void editAliases() throws Exception {
        editTextAreaAndVerify(HOST_ADDRESS, ALIAS, ALIAS_VALUES);
    }

    @Test
    public void editDefaultResponseCode() throws Exception {
        editTextAndVerify(HOST_ADDRESS, DEFAULT_RESPONSE_CODE, DEFAULT_RESPONSE_CODE_VALUE);
    }

    @Test
    public void editDefaultWebModule() throws Exception {
        editTextAndVerify(HOST_ADDRESS, DEFAULT_WEB_MODULE);
    }

    @Test
    public void setDisableConsoleRedirectToTrue() throws Exception {
        editCheckboxAndVerify(HOST_ADDRESS, DISABLE_CONSOLE_REDIRECT, true);
    }

    @Test
    public void setDisableConsoleRedirectToFalse() throws Exception {
        editCheckboxAndVerify(HOST_ADDRESS, DISABLE_CONSOLE_REDIRECT, false);
    }

    @Test
    public void addHTTPServerHostInGUI() throws Exception {
        String webModule = "webModule_" + RandomStringUtils.randomAlphanumeric(6);
        WizardWindow wizard = page.getResourceManager().addResource();

        Editor editor = wizard.getEditor();
        editor.text("name", HOST_TBA_ADDRESS.getLastPairValue());
        editor.text(ALIAS, String.join("\n", ALIAS_VALUES));
        editor.text(DEFAULT_RESPONSE_CODE, String.valueOf(DEFAULT_RESPONSE_CODE_VALUE));
        editor.text(DEFAULT_WEB_MODULE, webModule);
        editor.checkbox(DISABLE_CONSOLE_REDIRECT, true);
        boolean result = wizard.finish();

        Assert.assertTrue("Window should be closed", result);
        Assert.assertTrue("HTTP server host should be present in table",
                page.getResourceManager().isResourcePresent(HOST_TBA_ADDRESS.getLastPairValue()));
        ResourceVerifier verifier = new ResourceVerifier(HOST_TBA_ADDRESS, client);
        verifier.verifyExists();
        verifier.verifyAttribute(DEFAULT_RESPONSE_CODE, DEFAULT_RESPONSE_CODE_VALUE);
        verifier.verifyAttribute(DEFAULT_WEB_MODULE, webModule);
        verifier.verifyAttribute(DISABLE_CONSOLE_REDIRECT, true);
    }

    @Test
    public void removeHTTPServerHostInGUI() throws Exception {
        page.getResourceManager()
                .removeResource(HOST_TBR_ADDRESS.getLastPairValue())
                .confirm();

        Assert.assertFalse("Host server host should not be present in table",
                page.getResourceManager().isResourcePresent(HOST_TBR_ADDRESS.getLastPairValue()));
        new ResourceVerifier(HOST_TBR_ADDRESS, client).verifyDoesNotExist(); //HTTP server host should not be present on the server
    }

    @Test
    public void checkReferencesToFilters() throws IOException {
        page.switchToFilterReferencesSubTab();

        Assert.assertTrue("Filter reference " + ERROR_PAGE_FILTER_ADDRESS.getLastPairValue() + " is not present in table!",
                page.getConfigFragment().getResourceManager().isResourcePresent(ERROR_PAGE_FILTER_ADDRESS.getLastPairValue()));

        Assert.assertTrue("Filter reference  " + REQUEST_LIMIT_FILTER_ADDRESS.getLastPairValue() + " is not present in table!",
                page.getConfigFragment().getResourceManager().isResourcePresent(REQUEST_LIMIT_FILTER_ADDRESS.getLastPairValue()));
    }

    @Test
    public void addReferenceToFilter() throws Exception {
        page.switchToFilterReferencesSubTab();
        page.addReferenceToFilter(GZIP_PAGE_FILTER_ADDRESS.getLastPairValue());

        Assert.assertTrue("Filter reference  " + GZIP_PAGE_FILTER_ADDRESS.getLastPairValue() + " is not present in table!",
                page.getConfigFragment().getResourceManager().isResourcePresent(GZIP_PAGE_FILTER_ADDRESS.getLastPairValue()));

        new ResourceVerifier(GZIP_FILTER_REFERENCE_ADDRESS, client).verifyExists();
    }

    @Test
    public void removeReferenceToFilter() throws Exception {
        page.switchToFilterReferencesSubTab();
        ResourceManager manager = page.getConfigFragment().getResourceManager();

        manager.removeResource(REWRITE_FILTER_ADDRESS.getLastPairValue()).confirmAndDismissReloadRequiredMessage();

        Assert.assertFalse("Filter reference  " + REWRITE_FILTER_ADDRESS.getLastPairValue() + " is present in table!",
                manager.isResourcePresent(REWRITE_FILTER_ADDRESS.getLastPairValue()));

        new ResourceVerifier(REWRITE_FILTER_REFERENCE_ADDRESS, client).verifyDoesNotExist();
    }

}
