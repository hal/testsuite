package org.jboss.hal.testsuite.page.config;

import org.jboss.arquillian.graphene.Graphene;
import org.jboss.arquillian.graphene.page.Location;
import org.jboss.hal.testsuite.fragment.config.infinispan.CacheFragment;
import org.jboss.hal.testsuite.page.ConfigPage;
import org.jboss.hal.testsuite.util.PropUtils;
import org.openqa.selenium.By;

/**
 * @author mkrajcov <mkrajcov@redhat.com>
 */
@Location("#replicated-cache")
public class ReplicatedCachePage extends ConfigPage {

    private static final By CONTENT = By.id(PropUtils.get("page.content.id"));

    public CacheFragment content(){
        return Graphene.createPageFragment(CacheFragment.class, getContentRoot().findElement(CONTENT));
    }
}
