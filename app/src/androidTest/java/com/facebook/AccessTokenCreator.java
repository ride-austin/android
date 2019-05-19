package com.facebook;

import java.util.ArrayList;
import java.util.Collection;

/**
 * https://gist.github.com/RomainPiel/e4f64da5b74c23cc27cf
 */

public class AccessTokenCreator {

    public static AccessToken createToken(Collection<String> grantedPermissions) {
        return new AccessToken("token", "appId", "userId", grantedPermissions, new ArrayList<>(), AccessTokenSource.WEB_VIEW, null, null);
    }
}
