package org.jboss.hal.testsuite.fragment.config.infinispan;

import org.jboss.hal.testsuite.fragment.shared.modal.WizardWindow;

/**
 * @author mkrajcov <mkrajcov@redhat.com>
 */
public class CacheWizard extends WizardWindow{

    private static final String NAME = "name";
    private static final String CACHE_CONTAINER = "cacheContainer";

    public CacheWizard name(String value){
        getEditor().text(NAME, value);
        return this;
    }

    public CacheWizard cacheContainer(String value){
        getEditor().select(CACHE_CONTAINER, value);
        return this;
    }

}
