package com.facebook.login;


import com.facebook.AccessTokenCreator;
import com.facebook.login.LoginClient.Request;
import com.facebook.login.LoginClient.Result;
import com.rideaustin.utils.Constants;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * https://gist.github.com/RomainPiel/e4f64da5b74c23cc27cf
 */

public class LoginClientCreator {

    public static LoginClient.Request createRequest() {
        final Set<String> permissions = new HashSet<>(Arrays.asList(Constants.FacebookFields.PUBLIC_PROFILE, Constants.FacebookFields.EMAIL));
        return new Request(LoginBehavior.NATIVE_WITH_FALLBACK, permissions, DefaultAudience.EVERYONE, "appId", "authId");
    }

    public static Result createSuccess() {
        final Request request = createRequest();
        return new Result(request, LoginClient.Result.Code.SUCCESS,  AccessTokenCreator.createToken(request.getPermissions()), null, null);
    }

    public static Result createCancel() {
        return Result.createCancelResult(createRequest(), "User cancelled");
    }
}
