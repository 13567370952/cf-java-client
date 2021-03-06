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

package org.cloudfoundry.uaa;

import io.netty.util.AsciiString;
import org.cloudfoundry.AbstractIntegrationTest;
import org.cloudfoundry.uaa.clients.Client;
import org.cloudfoundry.uaa.clients.CreateClientRequest;
import org.cloudfoundry.uaa.clients.CreateClientResponse;
import org.cloudfoundry.uaa.clients.DeleteClientRequest;
import org.cloudfoundry.uaa.clients.GetClientRequest;
import org.cloudfoundry.uaa.clients.GetClientResponse;
import org.cloudfoundry.uaa.clients.GetMetadataRequest;
import org.cloudfoundry.uaa.clients.GetMetadataResponse;
import org.cloudfoundry.uaa.clients.ListClientsRequest;
import org.cloudfoundry.uaa.clients.ListClientsResponse;
import org.cloudfoundry.uaa.clients.ListMetadatasRequest;
import org.cloudfoundry.uaa.clients.ListMetadatasResponse;
import org.cloudfoundry.uaa.clients.Metadata;
import org.cloudfoundry.uaa.clients.UpdateClientRequest;
import org.cloudfoundry.uaa.clients.UpdateMetadataRequest;
import org.cloudfoundry.uaa.clients.UpdateMetadataResponse;
import org.cloudfoundry.util.PaginationUtils;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;

import static org.cloudfoundry.uaa.tokens.GrantType.CLIENT_CREDENTIALS;
import static org.cloudfoundry.uaa.tokens.GrantType.PASSWORD;
import static org.cloudfoundry.uaa.tokens.GrantType.REFRESH_TOKEN;
import static org.junit.Assert.assertEquals;

public final class ClientsTest extends AbstractIntegrationTest {

    @Autowired
    private String clientId;

    @Autowired
    private UaaClient uaaClient;

    @Ignore("TODO: Await https://www.pivotaltracker.com/story/show/125574491")
    @Test
    public void batchChangeSecret() {
        //
    }

    @Ignore("TODO: Await https://www.pivotaltracker.com/story/show/125554031")
    @Test
    public void batchCreate() {
        //
    }

    @Ignore("TODO: Await https://www.pivotaltracker.com/story/show/125575011")
    @Test
    public void batchDelete() {
        //
    }

    @Ignore("TODO: Await https://www.pivotaltracker.com/story/show/125572281")
    @Test
    public void batchUpdate() {
        //
    }

    @Ignore("TODO: Await https://www.pivotaltracker.com/story/show/125553341")
    @Test
    public void changeSecret() {
        //
    }

    @Test
    public void create() {
        String clientId = this.nameFactory.getClientId();
        String clientSecret = this.nameFactory.getClientSecret();

        this.uaaClient.clients()
            .create(CreateClientRequest.builder()
                .approvalsDeleted(true)
                .authorizedGrantType(PASSWORD)
                .clientId(clientId)
                .clientSecret(clientSecret)
                .scope("client.read", "client.write")
                .tokenSalt("test-token-salt")
                .build())
            .subscribe(this.<CreateClientResponse>testSubscriber()
                .expectThat(response -> {
                    assertEquals(Arrays.asList(PASSWORD, REFRESH_TOKEN), response.getAuthorizedGrantTypes());
                    assertEquals(clientId, response.getClientId());
                    assertEquals(Arrays.asList("client.read", "client.write"), response.getScopes());
                    assertEquals("test-token-salt", response.getTokenSalt());
                }));
    }

    @Test
    public void delete() {
        String clientId = this.nameFactory.getClientId();
        String clientSecret = this.nameFactory.getClientSecret();

        requestCreateClient(this.uaaClient, clientId, clientSecret)
            .then(this.uaaClient.clients()
                .delete(DeleteClientRequest.builder()
                    .clientId(clientId)
                    .build()))
            .flatMap(ignore -> requestListClients(this.uaaClient))
            .filter(client -> clientId.equals(client.getClientId()))
            .subscribe(this.testSubscriber()
                .expectCount(0));
    }

    @Test
    public void get() {
        String clientId = this.nameFactory.getClientId();
        String clientSecret = this.nameFactory.getClientSecret();

        requestCreateClient(this.uaaClient, clientId, clientSecret)
            .then(this.uaaClient.clients()
                .get(GetClientRequest.builder()
                    .clientId(clientId)
                    .build()))
            .subscribe(this.<GetClientResponse>testSubscriber()
                .expectThat(response -> {
                    assertEquals(Arrays.asList(PASSWORD, REFRESH_TOKEN), response.getAuthorizedGrantTypes());
                    assertEquals(clientId, response.getClientId());
                }));
    }

    @Test
    public void getMetadata() {
        requestUpdateMetadata(this.uaaClient, this.clientId, "http://test.get.url")
            .then(this.uaaClient.clients()
                .getMetadata(GetMetadataRequest.builder()
                    .clientId(this.clientId)
                    .build()))
            .subscribe(this.<GetMetadataResponse>testSubscriber()
                .expectThat(metadata -> {
                    assertEquals("http://test.get.url", metadata.getAppLaunchUrl());
                    assertEquals(this.clientId, metadata.getClientId());
                }));
    }

    @Test
    public void list() {
        String clientId = this.nameFactory.getClientId();
        String clientSecret = this.nameFactory.getClientSecret();

        requestCreateClient(this.uaaClient, clientId, clientSecret)
            .then(this.uaaClient.clients()
                .list(ListClientsRequest.builder()
                    .build()))
            .flatMapIterable(ListClientsResponse::getResources)
            .filter(client -> clientId.equals(client.getClientId()))
            .subscribe(this.testSubscriber()
                .expectCount(1));
    }

    @Test
    public void listMetadatas() {
        requestUpdateMetadata(this.uaaClient, this.clientId, "http://test.list.url")
            .then(this.uaaClient.clients()
                .listMetadatas(ListMetadatasRequest.builder()
                    .build()))
            .flatMapIterable(ListMetadatasResponse::getMetadatas)
            .filter(metadata -> this.clientId.equals(metadata.getClientId()))
            .single()
            .subscribe(this.<Metadata>testSubscriber()
                .expectThat(metadata -> {
                    assertEquals("http://test.list.url", metadata.getAppLaunchUrl());
                    assertEquals(this.clientId, metadata.getClientId());
                }));
    }

    @Ignore("TODO: Await https://www.pivotaltracker.com/story/show/125572641")
    @Test
    public void mixedActions() {
        //
    }

    @Test
    public void update() {
        String clientId = this.nameFactory.getClientId();
        String clientSecret = this.nameFactory.getClientSecret();

        requestCreateClient(this.uaaClient, clientId, clientSecret)
            .then(this.uaaClient.clients()
                .update(UpdateClientRequest.builder()
                    .authorizedGrantType(CLIENT_CREDENTIALS)
                    .clientId(clientId)
                    .name("test-name")
                    .build()))
            .flatMap(ignore -> requestListClients(this.uaaClient))
            .filter(client -> clientId.equals(client.getClientId()))
            .subscribe(this.<Client>testSubscriber()
                .expectThat(client -> {
                    assertEquals(Collections.singletonList(CLIENT_CREDENTIALS), client.getAuthorizedGrantTypes());
                    assertEquals(clientId, client.getClientId());
                    assertEquals("test-name", client.getName());
                }));
    }

    @Test
    public void updateMetadata() {
        String appIcon = Base64.getEncoder().encodeToString(new AsciiString("test-image").toByteArray());

        this.uaaClient.clients()
            .updateMetadata(UpdateMetadataRequest.builder()
                .appIcon(appIcon)
                .appLaunchUrl("http://test.app.launch.url")
                .clientId(this.clientId)
                .showOnHomePage(true)
                .clientName("test-name")
                .build())
            .then(requestGetMetadata(this.uaaClient, this.clientId))
            .subscribe(this.<GetMetadataResponse>testSubscriber()
                .expectThat(metadata -> {
                    assertEquals(appIcon, metadata.getAppIcon());
                    assertEquals("http://test.app.launch.url", metadata.getAppLaunchUrl());
                    assertEquals(this.clientId, metadata.getClientId());
                    assertEquals("test-name", metadata.getClientName());
                    assertEquals(true, metadata.getShowOnHomePage());
                }));
    }

    private static Mono<CreateClientResponse> requestCreateClient(UaaClient uaaClient, String clientId, String clientSecret) {
        return uaaClient.clients()
            .create(CreateClientRequest.builder()
                .authorizedGrantType(PASSWORD)
                .clientId(clientId)
                .clientSecret(clientSecret)
                .build());
    }

    private static Mono<GetMetadataResponse> requestGetMetadata(UaaClient uaaClient, String clientId) {
        return uaaClient.clients()
            .getMetadata(GetMetadataRequest.builder()
                .clientId(clientId)
                .build());
    }

    private static Flux<Client> requestListClients(UaaClient uaaClient) {
        return PaginationUtils
            .requestUaaResources(startIndex -> uaaClient.clients()
                .list(ListClientsRequest.builder()
                    .startIndex(startIndex)
                    .build()));

    }

    private static Mono<UpdateMetadataResponse> requestUpdateMetadata(UaaClient uaaClient, String clientId, String appLaunchUrl) {
        return uaaClient.clients()
            .updateMetadata(UpdateMetadataRequest.builder()
                .appLaunchUrl(appLaunchUrl)
                .clientId(clientId)
                .build());
    }

}
