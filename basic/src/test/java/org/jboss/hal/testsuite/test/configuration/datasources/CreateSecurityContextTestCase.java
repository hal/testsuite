package org.jboss.hal.testsuite.test.configuration.datasources;


import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.category.Domain;
import org.jboss.hal.testsuite.creaper.ManagementClientProvider;
import org.jboss.hal.testsuite.page.runtime.DataSourcesMetricsPage;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;
import org.junit.experimental.categories.Category;
import org.wildfly.extras.creaper.commands.foundation.online.SnapshotBackup;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.operations.admin.Administration;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import java.util.concurrent.TimeoutException;

import java.io.IOException;

/**
 * @author <a href="padamec@redhat.com">Petr Adamec</a>
 */
@RunWith(Arquillian.class)
@Category(Domain.class)
public class CreateSecurityContextTestCase {

    private static final String DATASOURCE_ORIGINAL_NAME = "ExampleDS";
    private static final String DATASOURCE_ORIGINAL_URL = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE";
    private static final String DATASOURCE_NEW_NAME = "H2XADS";
    private static final String DATASOURCE_NEW_URL = "jdbc:h2:mem:test";
    private static final String USE_JAVA_CONTEXT = "use-java-context";

    private static OnlineManagementClient client;
    private static DataSourcesOperations dsOps;
    private static Administration administration;
    private static Operations operations;
    private static final SnapshotBackup snapshotBackup = new SnapshotBackup();

    @Drone
    private WebDriver browser;

    @Page
    private DataSourcesMetricsPage dsPage;

    @BeforeClass
    public static void setup() throws Exception {
        client = ManagementClientProvider.createOnlineManagementClient();
        client.apply(snapshotBackup.backup());
        dsOps = new DataSourcesOperations(client);
        dsOps.removeDataSource(DATASOURCE_ORIGINAL_NAME);
        dsOps.createXADataSource(DATASOURCE_NEW_NAME, DATASOURCE_NEW_URL);
        operations = new Operations(client);
        administration = new Administration(client);
        administration.reloadIfRequired();
    }

    @AfterClass
    public static void tearDown() throws InterruptedException, TimeoutException, IOException, CommandFailedException {
        client.apply(snapshotBackup.restore());
        administration.reloadIfRequired();
        client.close();
    }

    /**
     * Try to navigate web console to Runtime / Server Group / main-server-group / server-one / Subsystems / Datasources /
     * and then gets resources. </br>
     * Its test for https://issues.jboss.org/browse/HAL-1457
     */
    @Test
    public void connectionPoolMetrics() {
        try {
            dsPage.navigate();
            Assert.assertNotNull(dsPage.getResourceManager().listResources());
        } catch (Exception e) {
            Assert.fail("Test failed with the exception. It is probably due to https://issues.jboss.org/browse/HAL-1457 " + e);
        }
    }

}
