package org.jboss.hal.testsuite.page.config;

import org.jboss.arquillian.graphene.page.Location;
import org.jboss.hal.testsuite.fragment.config.core.DeploymentScannerWizard;
import org.jboss.hal.testsuite.page.ConfigPage;

/**
 * @author mkrajcov <mkrajcov@redhat.com>
 */
@Location("#deployment-scanner")
public class DeploymentScannersPage extends ConfigPage {

    public DeploymentScannerWizard addDeploymentScanner(){
        return getResourceManager().addResource(DeploymentScannerWizard.class);
    }

    public void removeDeploymentScanner(String name){
        getResourceManager().removeResourceAndConfirm(name);
    }
}
