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

package org.cloudfoundry.reactor.doppler;

import org.cloudfoundry.doppler.ContainerMetric;
import org.cloudfoundry.doppler.ContainerMetricsRequest;
import org.cloudfoundry.doppler.Envelope;
import org.cloudfoundry.doppler.EventType;
import org.cloudfoundry.doppler.LogMessage;
import org.cloudfoundry.doppler.MessageType;
import org.cloudfoundry.doppler.RecentLogsRequest;
import org.cloudfoundry.reactor.InteractionContext;
import org.cloudfoundry.reactor.TestRequest;
import org.cloudfoundry.reactor.TestResponse;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;

import static io.netty.handler.codec.http.HttpMethod.GET;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;

public final class ReactorDopplerClientTest {

    public static final class ContainerMetrics extends AbstractDopplerApiTest<ContainerMetricsRequest, Envelope> {

        private final ReactorDopplerEndpoints dopplerEndpoints = new ReactorDopplerEndpoints(CONNECTION_CONTEXT, this.root, TOKEN_PROVIDER);

        @Override
        protected InteractionContext getInteractionContext() {
            return InteractionContext.builder()
                .request(TestRequest.builder()
                    .method(GET).path("/apps/test-application-id/containermetrics")
                    .build())
                .response(TestResponse.builder()
                    .status(OK)
                    .contentType("multipart/x-protobuf; boundary=30662872b152b6fbeb87658af504679def2b6680145265ad354761ea7acf")
                    .payload("fixtures/doppler/apps/GET_{id}_containermetrics_response.bin")
                    .build())
                .build();
        }

        @Override
        protected Envelope getResponse() {
            return null;
        }

        @Override
        protected Flux<Envelope> getResponses() {
            return Flux.just(
                Envelope.builder()
                    .containerMetric(ContainerMetric.builder()
                        .applicationId("1a95eadc-95c6-4675-aa07-8c02f80ea8a4")
                        .cpuPercentage(0.09530591690894699)
                        .diskBytes(154005504L)
                        .instanceIndex(2)
                        .memoryBytes(385896448L)
                        .build())
                    .deployment("cf-cfapps-io2-diego")
                    .eventType(EventType.CONTAINER_METRIC)
                    .index("17")
                    .ip("10.10.115.52")
                    .job("cell_z2")
                    .origin("rep")
                    .timestamp(1460991824620929073L)
                    .build(),
                Envelope.builder()
                    .containerMetric(ContainerMetric.builder()
                        .applicationId("1a95eadc-95c6-4675-aa07-8c02f80ea8a4")
                        .cpuPercentage(0.070504789909887)
                        .diskBytes(154005504L)
                        .instanceIndex(0)
                        .memoryBytes(371363840L)
                        .build())
                    .deployment("cf-cfapps-io2-diego")
                    .eventType(EventType.CONTAINER_METRIC)
                    .index("55")
                    .ip("10.10.115.90")
                    .job("cell_z2")
                    .origin("rep")
                    .timestamp(1460991826249611682L)
                    .build());
        }

        @Override
        protected ContainerMetricsRequest getValidRequest() throws Exception {
            return ContainerMetricsRequest.builder()
                .applicationId("test-application-id")
                .build();
        }

        @Override
        protected Flux<Envelope> invoke(ContainerMetricsRequest request) {
            return this.dopplerEndpoints.containerMetrics(request);
        }

    }

    public static final class RecentLogs extends AbstractDopplerApiTest<RecentLogsRequest, Envelope> {

        private final ReactorDopplerEndpoints dopplerEndpoints = new ReactorDopplerEndpoints(CONNECTION_CONTEXT, this.root, TOKEN_PROVIDER);

        @Override
        protected InteractionContext getInteractionContext() {
            return InteractionContext.builder()
                .request(TestRequest.builder()
                    .method(GET).path("/apps/test-application-id/recentlogs")
                    .build())
                .response(TestResponse.builder()
                    .status(OK)
                    .contentType("multipart/x-protobuf; boundary=92d42123ec83c0af6a27ba0de34528b702a53e2e67ba99636286b6a4cafb")
                    .payload("fixtures/doppler/apps/GET_{id}_recentlogs_response.bin")
                    .build())
                .build();
        }

        @Override
        protected Envelope getResponse() {
            return null;
        }

        @Override
        protected Publisher<Envelope> getResponses() {
            return Flux.just(
                Envelope.builder()
                    .deployment("cf-cfapps-io2-diego")
                    .eventType(EventType.LOG_MESSAGE)
                    .index("33")
                    .ip("10.10.115.68")
                    .job("cell_z2")
                    .logMessage(LogMessage.builder()
                        .applicationId("1a95eadc-95c6-4675-aa07-8c02f80ea8a4")
                        .message("2016-04-21 22:36:28.035  INFO 24 --- [           main] o.s.j.e.a.AnnotationMBeanExporter        : Located managed bean 'rabbitConnectionFactory': registering with " +
                            "JMX server as MBean [org.springframework.amqp.rabbit.connection:name=rabbitConnectionFactory,type=CachingConnectionFactory]")
                        .messageType(MessageType.OUT)
                        .sourceInstance("0")
                        .sourceType("APP")
                        .timestamp(1461278188035928339L)
                        .build())
                    .origin("rep")
                    .timestamp(1461278188035930425L)
                    .build(),
                Envelope.builder()
                    .deployment("cf-cfapps-io2-diego")
                    .eventType(EventType.LOG_MESSAGE)
                    .index("33")
                    .ip("10.10.115.68")
                    .job("cell_z2")
                    .logMessage(LogMessage.builder()
                        .applicationId("1a95eadc-95c6-4675-aa07-8c02f80ea8a4")
                        .message("Container became healthy")
                        .messageType(MessageType.OUT)
                        .sourceInstance("0")
                        .sourceType("CELL")
                        .timestamp(1461278188715651492L)
                        .build())
                    .origin("rep")
                    .timestamp(1461278188715653514L)
                    .build());
        }

        @Override
        protected RecentLogsRequest getValidRequest() throws Exception {
            return RecentLogsRequest.builder()
                .applicationId("test-application-id")
                .build();
        }

        @Override
        protected Publisher<Envelope> invoke(RecentLogsRequest request) {
            return this.dopplerEndpoints.recentLogs(request);
        }

    }

}
