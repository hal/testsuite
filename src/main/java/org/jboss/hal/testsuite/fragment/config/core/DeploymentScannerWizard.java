package org.jboss.hal.testsuite.fragment.config.core;

import org.jboss.hal.testsuite.fragment.shared.modal.WizardWindow;

/**
 * @author mkrajcov <mkrajcov@redhat.com>
 */
public class DeploymentScannerWizard extends WizardWindow {

    public DeploymentScannerWizard name(String value){
        getEditor().text("name", value);
        return this;
    }

    public DeploymentScannerWizard path(String value){
        getEditor().text("path", value);
        return this;
    }

    public DeploymentScannerWizard relativeTo(String value){
        getEditor().text("relativeTo", value);
        return this;
    }

    public DeploymentScannerWizard scanInterval(String value){
        getEditor().text("scanInterval", value);
        return this;
    }

    public DeploymentScannerWizard deploymentTimeout(String value){
        getEditor().text("deploymentTimeout", value);
        return this;
    }

    public DeploymentScannerWizard enabled(boolean value){
        getEditor().checkbox("enabled", value);
        return this;
    }
}
