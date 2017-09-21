package org.jboss.hal.testsuite.test.configuration.JCA;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import org.apache.commons.io.IOUtils;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.dmr.ModelNode;
import org.jboss.hal.testsuite.category.Standalone;
import org.jboss.hal.testsuite.creaper.ManagementClientProvider;
import org.jboss.hal.testsuite.creaper.ResourceVerifier;
import org.jboss.hal.testsuite.dmr.ModelNodeGenerator;
import org.jboss.hal.testsuite.page.config.ResourceAdaptersPage;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.OperationException;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.Values;
import org.wildfly.extras.creaper.core.online.operations.admin.Administration;
import static org.junit.Assert.assertTrue;

/**
 * Created by pcyprian on 24.8.15.
 */
@RunWith(Arquillian.class)
@Category(Standalone.class)
public class ResourceAdaptersPolicyTestCase {

    private static final String
        RESOURCE_ADAPTER_NAME = "RA_ResourceAdaptersPolicyTestCase",
        CONNECTION_DEFINITIONS_NAME = "CD_test",
        JNDINAME = "java:/" + CONNECTION_DEFINITIONS_NAME;
    private static final OnlineManagementClient client = ManagementClientProvider.createOnlineManagementClient();
    private static final Administration adminOps = new Administration(client);
    private static final Operations ops = new Operations(client);
    private static final Address
        resourceAdapterAddress = Address.subsystem("resource-adapters").and("resource-adapter", RESOURCE_ADAPTER_NAME),
        connectionDefinitionsAddress = resourceAdapterAddress.and("connection-definitions", CONNECTION_DEFINITIONS_NAME);

    private final ResourceVerifier verifier = new ResourceVerifier(connectionDefinitionsAddress, client);
    private final ModelNodeGenerator nodeGenerator = new ModelNodeGenerator();
    private static final PoolOperations poolOperations = new PoolOperations(client, connectionDefinitionsAddress);

    @BeforeClass
    public static void setUp() throws IOException {
        ops.add(resourceAdapterAddress, Values.of("archive", "archive_test").and("transaction-support", "NoTransaction"));
        ops.add(connectionDefinitionsAddress,
                Values.of("jndi-name", JNDINAME).and("class-name", "test-class").and("enabled", false));
    }

    @AfterClass
    public static void tearDown() throws IOException, InterruptedException, TimeoutException, OperationException {
        try {
            ops.removeIfExists(resourceAdapterAddress);
            adminOps.reloadIfRequired();
        } finally {
            IOUtils.closeQuietly(client);
        }
    }

    @Drone
    private WebDriver browser;
    @Page
    private ResourceAdaptersPage resourceAdaptersPage;

    private void navigateToResourceAdapter() {
        resourceAdaptersPage.navigateToResourceAdapter(RESOURCE_ADAPTER_NAME);
        resourceAdaptersPage.switchToConnectionDefinitions();
        resourceAdaptersPage.getPoolConfig().edit();
    }


    @Test
    public void setDecrementerClass() throws Exception {
        navigateToResourceAdapter();
        final String decrementerClass = "org.jboss.jca.core.connectionmanager.pool.capacity.WatermarkDecrementer";
        resourceAdaptersPage.setDecrementerClass(decrementerClass);
        boolean finished = resourceAdaptersPage.getConfigFragment().save();
        assertTrue("Config should be saved and closed.", finished);
        verifier.verifyAttribute(PoolConfigurationConstants.CAPACITY_DECREMENTER_CLASS, "org.jboss.jca.core.connectionmanager.pool.capacity.WatermarkDecrementer");
    }

    @Test
    public void setDecrementerProperty() throws Exception {
        final String decrementerClass = "org.jboss.jca.core.connectionmanager.pool.capacity.WatermarkDecrementer";
        final String propertyKey = "Watermark";
        final String propertyValue = "9";
        poolOperations.setCapacityDecrementerClassInModel(decrementerClass);
        navigateToResourceAdapter();
        resourceAdaptersPage.setDecrementerProperty(propertyKey, propertyValue);
        boolean finished = resourceAdaptersPage.getConfigFragment().save();
        assertTrue("Config should be saved and closed.", finished);
        adminOps.reloadIfRequired();
        ModelNode expectedPropertiesNode = nodeGenerator.createObjectNodeWithPropertyChild(propertyKey, propertyValue);
        verifier.verifyAttribute(PoolConfigurationConstants.CAPACITY_DECREMENTER_PROPERTIES, expectedPropertiesNode);

    }

    @Test
    public void unsetDecrementerProperty() throws Exception {
        final String decrementerClass = "org.jboss.jca.core.connectionmanager.pool.capacity.WatermarkDecrementer";
        final String propertyKey = "key";
        final String propertyValue = "value";
        poolOperations.setCapacityDecrementerClassInModel(decrementerClass);
        poolOperations.setCapacityDecrementerPropertyInModel(propertyKey, propertyValue);
        navigateToResourceAdapter();
        resourceAdaptersPage.unsetDecrementerProperty();
        boolean finished = resourceAdaptersPage.getConfigFragment().save();
        assertTrue("Config should be saved and closed.", finished);
        adminOps.reloadIfRequired();
        verifier.verifyAttributeIsUndefined(PoolConfigurationConstants.CAPACITY_DECREMENTER_PROPERTIES, "See https://issues.jboss.org/browse/HAL-1112 ");
    }

    @Test
    public void unsetDecrementerClass() throws Exception {
        final String decrementerClass = "org.jboss.jca.core.connectionmanager.pool.capacity.WatermarkDecrementer";
        poolOperations.setCapacityDecrementerClassInModel(decrementerClass);
        navigateToResourceAdapter();
        resourceAdaptersPage.unsetDecrementerClass();
        boolean finished = resourceAdaptersPage.getConfigFragment().save();
        assertTrue("Config should be saved and closed.", finished);
        verifier.verifyAttributeIsUndefined(PoolConfigurationConstants.CAPACITY_DECREMENTER_CLASS);
    }

    @Test
    public void setIncrementerClass() throws Exception {
        navigateToResourceAdapter();
        final String incrementerClass = "org.jboss.jca.core.connectionmanager.pool.capacity.SizeIncrementer";
        resourceAdaptersPage.setIncrementerClass(incrementerClass);
        boolean finished = resourceAdaptersPage.getConfigFragment().save();
        assertTrue("Config should be saved and closed.", finished);
        verifier.verifyAttribute(PoolConfigurationConstants.CAPACITY_INCREMENTER_CLASS, "org.jboss.jca.core.connectionmanager.pool.capacity.SizeIncrementer");
    }

    @Test
    public void setIncrementerProperty() throws Exception {
        final String incrementerClass = "org.jboss.jca.core.connectionmanager.pool.capacity.SizeIncrementer";
        final String propertyKey = "Size";
        final String propertyValue = "7";
        poolOperations.setCapacityIncrementerClassInModel(incrementerClass);
        navigateToResourceAdapter();
        resourceAdaptersPage.setIncrementerProperty(propertyKey, propertyValue);
        boolean finished = resourceAdaptersPage.getConfigFragment().save();
        assertTrue("Config should be saved and closed.", finished);
        adminOps.reloadIfRequired();
        ModelNode expectedPropertiesNode = nodeGenerator.createObjectNodeWithPropertyChild(propertyKey, propertyValue);
        verifier.verifyAttribute(PoolConfigurationConstants.CAPACITY_INCREMENTER_PROPERTIES, expectedPropertiesNode);
    }

    @Test
    public void unsetIncrementerProperty() throws Exception {
        final String incrementerClass = "org.jboss.jca.core.connectionmanager.pool.capacity.SizeIncrementer";
        final String propertyKey = "key";
        final String propertyValue = "value";
        poolOperations.setCapacityIncrementerClassInModel(incrementerClass);
        poolOperations.setCapacityIncrementerPropertyInModel(propertyKey, propertyValue);
        navigateToResourceAdapter();
        resourceAdaptersPage.unsetIncrementerProperty();
        boolean finished = resourceAdaptersPage.getConfigFragment().save();
        assertTrue("Config should be saved and closed.", finished);
        adminOps.reloadIfRequired();
        verifier.verifyAttributeIsUndefined(PoolConfigurationConstants.CAPACITY_INCREMENTER_PROPERTIES, "See https://issues.jboss.org/browse/HAL-1112 ");
    }

    @Test
    public void unsetIncrementerClass() throws Exception {
        final String incrementerClass = "org.jboss.jca.core.connectionmanager.pool.capacity.SizeIncrementer";
        poolOperations.setCapacityIncrementerClassInModel(incrementerClass);
        navigateToResourceAdapter();
        resourceAdaptersPage.unsetIncrementerClass();
        boolean finished = resourceAdaptersPage.getConfigFragment().save();
        assertTrue("Config should be saved and closed.", finished);
        verifier.verifyAttributeIsUndefined(PoolConfigurationConstants.CAPACITY_INCREMENTER_CLASS);
    }
}
