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

package org.cloudfoundry.uaa.identityzones;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.cloudfoundry.Nullable;
import org.immutables.value.Value;

import java.util.List;
import java.util.Optional;

/**
 * The payload for the identity zone configuration
 */
@JsonDeserialize
@Value.Immutable
abstract class _IdentityZoneConfiguration {

    /**
     * IDP Discovery should be set to true if you have configured more than one identity provider for UAA. The discovery relies on email domain being set for each additional provider.
     */
    @JsonProperty("idpDiscoveryEnabled")
    abstract Optional<Boolean> getLdapDiscoveryEnabled();

    /**
     * Array The links
     */
    @JsonProperty("links")
    abstract Optional<Links> getLinks();

    /**
     * The prompts
     */
    @JsonProperty("prompts")
    @Nullable
    abstract List<Prompt> getPrompts();

    /**
     * The saml configuration
     */
    @JsonProperty("samlConfig")
    abstract Optional<SamlConfiguration> getSamlConfiguration();

    /**
     * The token policy
     */
    @JsonProperty("tokenPolicy")
    abstract Optional<TokenPolicy> getTokenPolicy();

}
