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

package org.cloudfoundry.client.v3;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

import java.util.Optional;

@JsonDeserialize
@Value.Immutable
abstract class _Pagination {

    /**
     * The first
     */
    @JsonProperty("first")
    abstract Optional<Link> getFirst();

    /**
     * The last
     */
    @JsonProperty("last")
    abstract Optional<Link> getLast();

    /**
     * The next
     */
    @JsonProperty("next")
    abstract Optional<Link> getNext();

    /**
     * The previous
     */
    @JsonProperty("previous")
    abstract Optional<Link> getPrevious();

    /**
     * The total pages
     */
    @JsonProperty("total_pages")
    abstract Optional<Integer> getTotalPages();

    /**
     * The total results
     */
    @JsonProperty("total_results")
    abstract Optional<Integer> getTotalResults();

}
