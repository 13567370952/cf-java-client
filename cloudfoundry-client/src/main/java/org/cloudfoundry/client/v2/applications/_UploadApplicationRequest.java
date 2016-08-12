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

package org.cloudfoundry.client.v2.applications;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.cloudfoundry.QueryParameter;
import org.immutables.value.Value;

import java.io.InputStream;
import java.util.List;
import java.util.Optional;

/**
 * Request payload for the Upload Application request.
 */
@Value.Immutable
abstract class _UploadApplicationRequest {

    /**
     * A binary zip file containing the application bits
     */
    @JsonIgnore
    abstract InputStream getApplication();

    /**
     * The application id
     */
    @JsonIgnore
    abstract String getApplicationId();

    /**
     * If true, a new asynchronous job is submitted to persist the bits and the job id is included in the response
     */
    @QueryParameter("async")
    abstract Optional<Boolean> getAsync();

    /**
     * Fingerprints of the application bits that have previously been pushed to Cloud Foundry
     */
    @JsonIgnore
    abstract List<Resource> getResources();

}
