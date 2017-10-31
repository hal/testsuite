package org.jboss.hal.testsuite.test.configuration.elytron.decoder;

import java.security.Principal;

import org.wildfly.security.auth.server.PrincipalDecoder;

/**
 * Custom implementation of {@link PrincipalDecoder} for test purposes
 */
public class LowercaseCustomPrincipalDecoder implements PrincipalDecoder {

    @Override
    public String getName(Principal principal) {
        return principal.getName().toLowerCase();
    }

}
