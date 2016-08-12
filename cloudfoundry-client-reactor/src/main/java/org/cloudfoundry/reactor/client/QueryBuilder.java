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

package org.cloudfoundry.reactor.client;

import org.cloudfoundry.QueryParameter;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.util.UriComponentsBuilder;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * A builder for Cloud Foundry queries
 */
public final class QueryBuilder {

    private QueryBuilder() {
    }

    /**
     * Augments a {@link UriComponentsBuilder} with queries based on the methods annotated with {@link QueryParameter}
     *
     * @param builder  the builder to augment
     * @param instance the instance to inspect and invoke
     */
    public static void augment(UriComponentsBuilder builder, Object instance) {
        Method[] methods = ReflectionUtils.getAllDeclaredMethods(instance.getClass());
        Arrays.sort(methods, MethodNameComparator.INSTANCE);

        for (Method method : methods) {
            QueryParameter queryParameter = AnnotationUtils.getAnnotation(method, QueryParameter.class);
            if (!hasQueryParameter(queryParameter)) {
                continue;
            }

            getValue(method, instance)
                .ifPresent(value -> {
                    if (value instanceof Collection) {
                        builder.queryParam(queryParameter.value(), ((Collection<?>) value).stream()
                            .map(Object::toString)
                            .collect(Collectors.joining(queryParameter.delimiter())));
                    } else {
                        builder.queryParam(queryParameter.value(), value);
                    }
                });
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static Optional<Object> getValue(Method method, Object instance) {
        ReflectionUtils.makeAccessible(method);
        Object value = ReflectionUtils.invokeMethod(method, instance);
        return value instanceof Optional ? (Optional<Object>) value : Optional.ofNullable(value);
    }

    private static boolean hasQueryParameter(QueryParameter queryParameter) {
        return queryParameter == null;
    }

}
