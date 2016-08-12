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

package org.cloudfoundry.reactor;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.handler.codec.http.HttpMethod;
import okhttp3.Headers;
import okhttp3.mockwebserver.RecordedRequest;
import okio.Buffer;
import org.cloudfoundry.util.test.FailingDeserializationProblemHandler;
import org.immutables.value.Value;
import org.junit.Assert;
import org.springframework.core.io.ClassPathResource;
import reactor.core.Exceptions;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.assertNull;

@Value.Immutable
abstract class _TestRequest {

    public static final String EMPTY_HEADER = "EMPTY-HEADER";

    private static final ObjectMapper OBJECT_MAPPER = DefaultConnectionContext.getDefaultObjectMapper()
        .addHandler(new FailingDeserializationProblemHandler());

    private static final Pattern PATH_PATTERN = Pattern.compile("[A-Z]+ (.*) [A-Z0-9\\./]+");

    public void assertEquals(RecordedRequest request) {
        Assert.assertEquals(getMethod().toString(), request.getMethod());
        Assert.assertEquals(getPath(), extractPath(request));

        getHeaders().forEach((key, value) -> {
            if (EMPTY_HEADER == value) {
                assertNull(request.getHeader(key));
            } else {
                Assert.assertEquals(value, request.getHeader(key));
            }
        });

        if (getPayload().isPresent()) {
            assertBodyEquals(getPayload().map(_TestRequest::getBuffer).get(), request.getBody());
        } else if (getContents().isPresent()) {
            getContents().get().accept(Tuples.of(request.getHeaders(), request.getBody()));
        } else {
            Assert.assertEquals("Invalid request body: " + request.getBody().readUtf8(), 0, request.getBodySize());
        }
    }

    abstract Optional<Consumer<Tuple2<Headers, Buffer>>> getContents();

    abstract Map<String, String> getHeaders();

    abstract HttpMethod getMethod();

    abstract String getPath();

    abstract Optional<String> getPayload();

    private static void assertBodyEquals(Buffer expectedBuffer, Buffer actualBuffer) {
        Assert.assertEquals(getValue(expectedBuffer), getValue(actualBuffer));
    }

    private static Buffer getBuffer(String path) {
        try {
            return new Buffer().readFrom(new ClassPathResource(path).getInputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static Object getValue(Buffer buffer) {
        try {
            JsonNode root = OBJECT_MAPPER.readTree(buffer.readByteArray());
            return root.isArray() ? OBJECT_MAPPER.treeToValue(root, List.class) : OBJECT_MAPPER.treeToValue(root, Map.class);
        } catch (IOException e) {
            throw Exceptions.propagate(e);
        }
    }

    private String extractPath(RecordedRequest request) {
        Matcher matcher = PATH_PATTERN.matcher(request.getRequestLine());

        if (matcher.find()) {
            return matcher.group(1);
        } else {
            throw new IllegalArgumentException(String.format("Request Line %s does not contain a valid path", request.getRequestLine()));
        }
    }

}
