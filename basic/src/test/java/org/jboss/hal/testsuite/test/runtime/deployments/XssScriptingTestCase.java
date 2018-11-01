package org.jboss.hal.testsuite.test.runtime.deployments;


import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.category.Standalone;
import org.jboss.hal.testsuite.creaper.ManagementClientProvider;
import org.jboss.hal.testsuite.creaper.command.DeployCommand;
import org.jboss.hal.testsuite.finder.FinderNames;
import org.jboss.hal.testsuite.finder.FinderNavigation;
import org.jboss.hal.testsuite.finder.Row;
import org.jboss.hal.testsuite.page.runtime.StandaloneDeploymentEntryPoint;
import org.jboss.hal.testsuite.util.Console;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.WebDriver;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.operations.OperationException;
import org.wildfly.extras.creaper.core.online.operations.admin.Administration;

import java.io.IOException;
import java.util.concurrent.TimeoutException;


/**
 * @author <a href="padamec@redhat.com">Petr Adamec</a>
 */
@RunWith(Arquillian.class)
@Category(Standalone.class)
public class XssScriptingTestCase {
    private static final String FILE_PATH = "src/test/resources/";
    private static final String FILE_NAME = "mockWar.war";
    private static final String SHORT_NAME = "xss<svg/onload=alert(document.domain)>xss";
    private static final String NAME = SHORT_NAME + " --disabled";

    private static OnlineManagementClient client = ManagementClientProvider.createOnlineManagementClient();
    private static Administration administration = new Administration(client);
    private static DeploymentsOperations ops = new DeploymentsOperations(client);
    private FinderNavigation navigation;

    @Drone
    WebDriver browser;

    @Before
    public void before() {
        navigation = new FinderNavigation(browser, StandaloneDeploymentEntryPoint.class);
    }


    @BeforeClass
    public static void setUp() throws IOException, TimeoutException, InterruptedException, CommandFailedException {
        administration.reloadIfRequired();

        client.apply(new DeployCommand.Builder(FILE_PATH + FILE_NAME)
                .name(NAME)
                .build());
    }

    @AfterClass
    public static void cleanUp() throws IOException, CommandFailedException, OperationException, TimeoutException, InterruptedException {
        try {
            ops.undeployIfExists(SHORT_NAME);
            administration.restartIfRequired();
            administration.reloadIfRequired();
        } finally {
            client.close();
        }
    }

    /**
     * Test if any alert window is displayed.
     */
    @Test
    public void testIfAlertWindowIsDisplayed() {
        FinderNavigation nav = navigation.step(FinderNames.DEPLOYMENT, SHORT_NAME);
        Row row = nav.selectRow(false);
        Console.withBrowser(browser).dismissReloadRequiredWindowIfPresent();
        row.invoke(FinderNames.VIEW);
        try {
            if (browser.switchTo().alert() != null) {
                Assert.fail("Cross-site scripting (XSS) in JBoss Management");
            }
        } catch (NoAlertPresentException e) {
        }
    }


}
