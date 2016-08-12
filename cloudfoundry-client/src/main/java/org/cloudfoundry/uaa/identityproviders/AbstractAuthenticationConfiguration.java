/*
 * Copyright 2013-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.cloudfoundry.uaa.identityproviders;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.cloudfoundry.Nullable;

import java.util.List;
import java.util.Optional;

/**
 * The payload for the oauth/oidc identity provider configuration
 */
abstract class AbstractAuthenticationConfiguration extends AbstractExternalIdentityProviderConfiguration {

    /**
     * Determines whether or not shadow users must be created before login by an administrator.
     */
    @JsonProperty("addShadowUserOnLogin")
    abstract Optional<Boolean> getAddShadowUserOnLogin();

    /**
     * The OAuth 2.0 authorization endpoint URL
     */
    @JsonProperty("authUrl")
    abstract String getAuthUrl();

    /**
     * Text to use for the login link to the provider
     */
    @JsonProperty("linkText")
    abstract Optional<Boolean> getLinkText();

    /**
     * The client ID which is registered with the external OAuth provider for use by the UAA
     */
    @JsonProperty("relyingPartyId")
    abstract String getRelyingPartyId();

    /**
     * The client secret of the relying party at the external OAuth provider
     */
    @JsonProperty("relyingPartySecret")
    abstract String getRelyingPartySecret();

    /**
     * What scopes to request on a call to the external OAuth/OpenID provider. For example, can provide openid, roles, or profile to request ID token, scopes populated in the ID token external groups
     * attribute mappings, or the user profile information, respectively.
     */
    @JsonProperty("scopes")
    @Nullable
    abstract List<String> getScopes();

    /**
     * A flag controlling whether a link to this provider’s login will be shown on the UAA login page
     */
    @JsonProperty("showLinkText")
    abstract Optional<Boolean> getShowLinkText();

    /**
     * Skips validation of the LDAP cert if set to true.
     */
    @JsonProperty("skipSslValidation")
    abstract Optional<Boolean> getSkipSslVerification();

    /**
     * A verification key for validating token signatures
     */
    @JsonProperty("tokenKey")
    abstract Optional<String> getTokenKey();

    /**
     * The URL of the token key endpoint which renders a verification key for validating token signatures
     */
    @JsonProperty("tokenKeyUrl")
    abstract Optional<String> getTokenKeyUrl();

    /**
     * The OAuth 2.0 authorization endpoint URL
     */
    @JsonProperty("tokenUrl")
    abstract String getTokenUrl();

}
