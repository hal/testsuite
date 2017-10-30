package org.jboss.hal.testsuite.test.configuration.elytron.principal.decoder;

import org.apache.commons.lang3.RandomStringUtils;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.category.Elytron;
import org.jboss.hal.testsuite.creaper.ResourceVerifier;
import org.jboss.hal.testsuite.fragment.config.AddResourceWizard;
import org.jboss.hal.testsuite.page.config.elytron.MapperDecoderPage;
import org.jboss.hal.testsuite.test.configuration.elytron.AbstractElytronTestCase;
import org.jboss.hal.testsuite.util.ConfigChecker;
import org.jboss.hal.testsuite.util.ModuleUtils;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Values;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeoutException;

@RunWith(Arquillian.class)
@RunAsClient
@Category(Elytron.class)
public class CustomRoleDecoderTestCase extends AbstractElytronTestCase {

    private static final String CUSTOM_ROLE_DECODER_LABEL = "Custom Role Decoder";
    private static final String CUSTOM_DOLE_DECODER = "custom-role-decoder";

    private static ModuleUtils moduleUtils;
    private static String customRoleDecoderModule;
    private static final String ARCHIVE_NAME = String.format("custom_role_decoder_%s.jar",
            RandomStringUtils.randomAlphanumeric(7));
    private static final Path CUSTOM_ROLE_DECODER_PATH = Paths.get("test", "configuration"
            , "elytron", "custom", "role", "decoder_" + RandomStringUtils.randomAlphanumeric(7));
    private static final String CLASS_NAME = "class-name";
    private static final String MODULE = "module";

    @Page
    private MapperDecoderPage page;

    @BeforeClass
    public static void setUp() throws IOException {
        moduleUtils = new ModuleUtils(client);
        customRoleDecoderModule = moduleUtils.createModule(CUSTOM_ROLE_DECODER_PATH,
                ShrinkWrap.create(JavaArchive.class, ARCHIVE_NAME)
                        .addClasses(RandomizedRoleDecoder.class),
                "org.wildfly.extension.elytron", "org.wildfly.security.elytron-private");
    }

    @AfterClass
    public static void tearDown() throws IOException {
        moduleUtils.removeModule(CUSTOM_ROLE_DECODER_PATH);
    }

    @Test
    public void addCustomRoleDecoderTest() throws Exception {

        final String customRoleDecoderName = "custom_role_decoder_" + RandomStringUtils.randomAlphanumeric(7);
        final String className = RandomizedRoleDecoder.class.getCanonicalName();
        final Address customRoleDecoderAddress = elyOps.getElytronAddress(CUSTOM_DOLE_DECODER, customRoleDecoderName);
        try {
            page.navigateToDecoder()
                    .selectResource(CUSTOM_ROLE_DECODER_LABEL)
                    .getResourceManager()
                    .addResource(AddResourceWizard.class)
                    .name(customRoleDecoderName)
                    .text("class-name", className)
                    .text("module", customRoleDecoderModule)
                    .saveAndDismissReloadRequiredWindowWithState()
                    .assertWindowClosed();
            Assert.assertTrue("Newly created custom role decoder should be present in the table",
                    page.getResourceManager().isResourcePresent(customRoleDecoderName));
            new ResourceVerifier(customRoleDecoderAddress, client)
                    .verifyExists()
                    .verifyAttribute("class-name", className)
                    .verifyAttribute("module", customRoleDecoderModule);
        } finally {
            ops.removeIfExists(customRoleDecoderAddress);
            adminOps.reloadIfRequired();
        }
    }

    @Test
    public void removeCustomRoleDecoderTest() throws Exception {
        final String customRoleDecoderName = "custom_role_decoder_" + RandomStringUtils.randomAlphanumeric(7);
        final Address customRoleDecoderAddress = elyOps.getElytronAddress(CUSTOM_DOLE_DECODER, customRoleDecoderName);
        final ResourceVerifier verifier = new ResourceVerifier(customRoleDecoderAddress, client);
        try {
            createCustomRoleDecoderInModel(customRoleDecoderAddress);
            verifier.verifyExists();
            page.navigateToDecoder()
                    .selectResource(CUSTOM_ROLE_DECODER_LABEL)
                    .getResourceManager()
                    .removeResource(customRoleDecoderName)
                    .confirmAndDismissReloadRequiredMessage()
                    .assertClosed();
            Assert.assertFalse("Newly removed custom role decoder should not be present in the table",
                    page.getResourceManager().isResourcePresent(customRoleDecoderName));
            verifier.verifyDoesNotExist();

        } finally {
            ops.removeIfExists(customRoleDecoderAddress);
            adminOps.reloadIfRequired();
        }
    }

    private void createCustomRoleDecoderInModel(Address customRoleDecoderAddress) throws IOException, TimeoutException, InterruptedException {
        final String className = RandomizedRoleDecoder.class.getCanonicalName();
        ops.add(customRoleDecoderAddress, Values.of(CLASS_NAME, className).and(MODULE, customRoleDecoderModule))
                .assertSuccess();
        adminOps.reloadIfRequired();
    }

    @Test
    public void editAttributesTest() throws Exception {
        final String customRoleDecoderName = "custom_role_decoder_" + RandomStringUtils.randomAlphanumeric(7);
        final Path editingModulePath = Paths.get("test", "elytron", "custom", "role", "decoder",
                "custom_role_decoder_to_be_edited_" + RandomStringUtils.randomAlphanumeric(7));
        final String editingModule = moduleUtils.createModule(editingModulePath,
                ShrinkWrap.create(JavaArchive.class).addClass(ThorRoleDecoder.class),
                "org.wildfly.extension.elytron", "org.wildfly.security.elytron-private");
        final String className = ThorRoleDecoder.class.getCanonicalName();
        final Address customRoleDecoderAddress = elyOps.getElytronAddress(CUSTOM_DOLE_DECODER, customRoleDecoderName);
        try {
            createCustomRoleDecoderInModel(customRoleDecoderAddress);
            page.navigateToDecoder()
                    .selectResource(CUSTOM_ROLE_DECODER_LABEL)
                    .getResourceManager()
                    .selectByName(customRoleDecoderName);
            new ConfigChecker.Builder(client, customRoleDecoderAddress)
                    .configFragment(page.getConfigFragment())
                    .edit(ConfigChecker.InputType.TEXT, CLASS_NAME, className)
                    .edit(ConfigChecker.InputType.TEXT, MODULE, editingModule)
                    .andSave()
                    .verifyFormSaved()
                    .verifyAttribute(CLASS_NAME, className)
                    .verifyAttribute(MODULE, editingModule);
        } finally {
            ops.removeIfExists(customRoleDecoderAddress);
            moduleUtils.removeModule(editingModulePath);
            adminOps.reloadIfRequired();
        }


    }
}
