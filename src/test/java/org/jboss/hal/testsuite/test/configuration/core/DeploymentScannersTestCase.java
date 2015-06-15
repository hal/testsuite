package org.jboss.hal.testsuite.test.configuration.core;

import org.apache.commons.lang3.RandomStringUtils;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.Graphene;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.cli.CliClient;
import org.jboss.hal.testsuite.cli.CliClientFactory;
import org.jboss.hal.testsuite.cli.CliConstants;
import org.jboss.hal.testsuite.fragment.config.core.DeploymentScannerWizard;
import org.jboss.hal.testsuite.page.config.DeploymentScannersPage;
import org.jboss.hal.testsuite.test.category.Standalone;
import org.jboss.hal.testsuite.util.Console;
import org.jboss.hal.testsuite.util.ResourceVerifier;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;

/**
 * @author mkrajcov <mkrajcov@redhat.com>
 */
@RunWith(Arquillian.class)
@Category(Standalone.class)
public class DeploymentScannersTestCase {

    private static final String DEPLOYMENT_SCANNER = "ds_" + RandomStringUtils.randomAlphanumeric(5);
    private CliClient client = CliClientFactory.getClient();
    ResourceVerifier verifier = new ResourceVerifier(CliConstants.DEPLOYMENT_SCANNER_ADDRESS + "=" + DEPLOYMENT_SCANNER, client);

    @Drone
    public WebDriver browser;

    @Page
    public DeploymentScannersPage page;

    @Before
    public void before(){
        browser.navigate().refresh();
        Graphene.goTo(DeploymentScannersPage.class);
        Console.withBrowser(browser).waitUntilLoaded();
        Console.withBrowser(browser).maximizeWindow();
    }

    @Test
    public void createDeploymentScanner() {
        DeploymentScannerWizard wizard = page.addDeploymentScanner();

        boolean result = wizard.name(DEPLOYMENT_SCANNER)
                .path("deployments")
                .relativeTo("jboss.server.base.dir")
                .scanInterval("45")
                .deploymentTimeout("60")
                .enabled(true)
                .finish();

        Assert.assertTrue("Window should be closed", result);
        verifier.verifyResource(true);

        page.removeDeploymentScanner(DEPLOYMENT_SCANNER);
        verifier.verifyResource(false);
    }
}
