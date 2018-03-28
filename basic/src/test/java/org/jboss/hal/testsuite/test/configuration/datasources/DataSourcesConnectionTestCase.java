package org.jboss.hal.testsuite.test.configuration.datasources;

import org.apache.commons.io.IOUtils;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.creaper.ManagementClientProvider;
import org.jboss.hal.testsuite.fragment.config.datasource.ConnectionConfig;
import org.jboss.hal.testsuite.page.config.DatasourcesPage;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;
import org.wildfly.extras.creaper.core.online.ModelNodeResult;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.admin.Administration;

@RunWith(Arquillian.class)
@RunAsClient
public class DataSourcesConnectionTestCase {

    private static final String VALID_URL = "jdbc:h2:mem:test2;DB_CLOSE_DELAY=-1";
    private static final String USE_CCM = "use-ccm";
    private static final String USE_CCM_CONSOLE = "ccm";

    private static OnlineManagementClient client;
    private static DataSourcesOperations dsOps;
    private static Administration administration;
    private static Operations operations;

    private static String dsName;

    @Drone
    protected WebDriver browser;

    @Page
    protected DatasourcesPage page;

    @BeforeClass
    public static void setup() throws Exception {
        client = ManagementClientProvider.createOnlineManagementClient();
        dsOps = new DataSourcesOperations(client);
        operations = new Operations(client);
        administration = new Administration(client);
        dsName = dsOps.createDataSource(VALID_URL);
        administration.reloadIfRequired();
    }

    @AfterClass
    public static void tearDown() throws Exception {
        try {
            dsOps.removeDataSource(dsName);
        } finally {
            IOUtils.closeQuietly(client);
        }
    }

    /**
     * Test that the use ccm value is correctly displayed.
     * https://issues.jboss.org/browse/HAL-1389
     */
    @Test
    public void testCorrectUseCcmValueDisplayed() throws Exception {
        // Check the default value is displayed correctly
        Address dsAddress = DataSourcesOperations.getDsAddress(dsName);
        ModelNodeResult modelNodeResult = operations.readAttribute(dsAddress, USE_CCM);
        modelNodeResult.assertSuccess();
        boolean defaultValue = modelNodeResult.booleanValue();
        assertExpectedValueDisplayed(dsName, USE_CCM_CONSOLE, modelNodeResult.stringValue());

        // Check that even after changing the value it is still displayed correctly
        modelNodeResult = operations.writeAttribute(dsAddress, USE_CCM, !defaultValue);
        modelNodeResult.assertSuccess();
        assertExpectedValueDisplayed(dsName, USE_CCM_CONSOLE, String.valueOf(!defaultValue));
    }

    private void assertExpectedValueDisplayed(String dsName, String attribute, String expectedValue) {
        page.invokeViewDatasource(dsName);
        ConnectionConfig connection = page.getConnectionConfig();
        String actualValue = connection.getAttributeValueBySubstring(attribute);
        Assert.assertEquals("Incorrect value of " + attribute  + " displayed. See https://issues.jboss.org/browse/HAL-1389", expectedValue, actualValue);
    }

}
