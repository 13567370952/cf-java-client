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

package org.cloudfoundry.client.v2;

import org.cloudfoundry.AbstractIntegrationTest;
import org.cloudfoundry.client.CloudFoundryClient;
import org.cloudfoundry.client.v2.applications.ApplicationEntity;
import org.cloudfoundry.client.v2.applications.ApplicationResource;
import org.cloudfoundry.client.v2.applications.CreateApplicationRequest;
import org.cloudfoundry.client.v2.applications.CreateApplicationResponse;
import org.cloudfoundry.client.v2.domains.CreateDomainRequest;
import org.cloudfoundry.client.v2.domains.DomainEntity;
import org.cloudfoundry.client.v2.domains.DomainResource;
import org.cloudfoundry.client.v2.events.EventEntity;
import org.cloudfoundry.client.v2.events.EventResource;
import org.cloudfoundry.client.v2.organizations.AssociateOrganizationAuditorRequest;
import org.cloudfoundry.client.v2.organizations.AssociateOrganizationBillingManagerRequest;
import org.cloudfoundry.client.v2.organizations.AssociateOrganizationManagerRequest;
import org.cloudfoundry.client.v2.organizations.AssociateOrganizationUserByUsernameRequest;
import org.cloudfoundry.client.v2.organizations.CreateOrganizationRequest;
import org.cloudfoundry.client.v2.organizations.CreateOrganizationResponse;
import org.cloudfoundry.client.v2.routes.CreateRouteRequest;
import org.cloudfoundry.client.v2.routes.CreateRouteResponse;
import org.cloudfoundry.client.v2.routes.RouteResource;
import org.cloudfoundry.client.v2.spaces.AssociateSpaceAuditorByUsernameRequest;
import org.cloudfoundry.client.v2.spaces.AssociateSpaceAuditorRequest;
import org.cloudfoundry.client.v2.spaces.AssociateSpaceAuditorResponse;
import org.cloudfoundry.client.v2.spaces.AssociateSpaceDeveloperByUsernameRequest;
import org.cloudfoundry.client.v2.spaces.AssociateSpaceDeveloperRequest;
import org.cloudfoundry.client.v2.spaces.AssociateSpaceManagerByUsernameRequest;
import org.cloudfoundry.client.v2.spaces.AssociateSpaceManagerRequest;
import org.cloudfoundry.client.v2.spaces.AssociateSpaceManagerResponse;
import org.cloudfoundry.client.v2.spaces.CreateSpaceRequest;
import org.cloudfoundry.client.v2.spaces.DeleteSpaceRequest;
import org.cloudfoundry.client.v2.spaces.GetSpaceRequest;
import org.cloudfoundry.client.v2.spaces.GetSpaceSummaryRequest;
import org.cloudfoundry.client.v2.spaces.GetSpaceSummaryResponse;
import org.cloudfoundry.client.v2.spaces.ListSpaceApplicationsRequest;
import org.cloudfoundry.client.v2.spaces.ListSpaceAuditorsRequest;
import org.cloudfoundry.client.v2.spaces.ListSpaceDevelopersRequest;
import org.cloudfoundry.client.v2.spaces.ListSpaceDomainsRequest;
import org.cloudfoundry.client.v2.spaces.ListSpaceEventsRequest;
import org.cloudfoundry.client.v2.spaces.ListSpaceManagersRequest;
import org.cloudfoundry.client.v2.spaces.ListSpaceRoutesRequest;
import org.cloudfoundry.client.v2.spaces.ListSpaceUserRolesRequest;
import org.cloudfoundry.client.v2.spaces.ListSpacesRequest;
import org.cloudfoundry.client.v2.spaces.RemoveSpaceAuditorByUsernameRequest;
import org.cloudfoundry.client.v2.spaces.RemoveSpaceAuditorRequest;
import org.cloudfoundry.client.v2.spaces.RemoveSpaceDeveloperByUsernameRequest;
import org.cloudfoundry.client.v2.spaces.RemoveSpaceDeveloperRequest;
import org.cloudfoundry.client.v2.spaces.RemoveSpaceManagerByUsernameRequest;
import org.cloudfoundry.client.v2.spaces.RemoveSpaceManagerRequest;
import org.cloudfoundry.client.v2.spaces.SpaceEntity;
import org.cloudfoundry.client.v2.spaces.SpaceResource;
import org.cloudfoundry.client.v2.spaces.UpdateSpaceRequest;
import org.cloudfoundry.client.v2.spaces.UserSpaceRoleEntity;
import org.cloudfoundry.client.v2.stacks.ListStacksRequest;
import org.cloudfoundry.client.v2.users.ListUsersRequest;
import org.cloudfoundry.client.v2.users.UserEntity;
import org.cloudfoundry.client.v2.users.UserResource;
import org.cloudfoundry.util.DateUtils;
import org.cloudfoundry.util.JobUtils;
import org.cloudfoundry.util.PaginationUtils;
import org.cloudfoundry.util.ResourceUtils;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

import static java.time.temporal.ChronoUnit.HOURS;
import static org.cloudfoundry.util.OperationUtils.thenKeep;
import static org.cloudfoundry.util.tuple.TupleUtils.function;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public final class SpacesTest extends AbstractIntegrationTest {

    @Autowired
    private CloudFoundryClient cloudFoundryClient;

    @Autowired
    private Mono<String> organizationId;

    @Autowired
    private String stackName;

    @Autowired
    private String username;

    @Test
    public void associateAuditor() {
        String organizationName = this.nameFactory.getOrganizationName();
        String spaceName = this.nameFactory.getSpaceName();

        createUserIdAndSpaceId(this.cloudFoundryClient, organizationName, spaceName, this.username)
            .then(function((userId, spaceId) -> Mono.when(
                Mono.just(spaceId),
                this.cloudFoundryClient.spaces()
                    .associateAuditor(AssociateSpaceAuditorRequest.builder()
                        .spaceId(spaceId)
                        .auditorId(userId)
                        .build())
                    .map(ResourceUtils::getId)
            )))
            .subscribe(this.<Tuple2<String, String>>testSubscriber()
                .expectThat(this::assertTupleEquality));
    }

    @Test
    public void associateAuditorByUsername() {
        String organizationName = this.nameFactory.getOrganizationName();
        String spaceName = this.nameFactory.getSpaceName();

        createUserIdAndSpaceId(this.cloudFoundryClient, organizationName, spaceName, this.username)
            .as(thenKeep(function((userId, spaceId) -> this.cloudFoundryClient.spaces()
                .associateAuditorByUsername(AssociateSpaceAuditorByUsernameRequest.builder()
                    .spaceId(spaceId)
                    .username(this.username)
                    .build()))))
            .flatMap(function((userId, spaceId) -> PaginationUtils
                .requestClientV2Resources(page -> this.cloudFoundryClient.spaces()
                    .listAuditors(ListSpaceAuditorsRequest.builder()
                        .page(page)
                        .spaceId(spaceId)
                        .build()))
                .map(ResourceUtils::getEntity)
                .map(UserEntity::getUsername)))
            .subscribe(this.testSubscriber()
                .expectEquals(this.username));
    }

    @Test
    public void associateDeveloper() {
        String organizationName = this.nameFactory.getOrganizationName();
        String spaceName = this.nameFactory.getSpaceName();

        createUserIdAndSpaceId(this.cloudFoundryClient, organizationName, spaceName, this.username)
            .then(function((userId, spaceId) -> Mono.when(
                Mono.just(spaceId),
                this.cloudFoundryClient.spaces()
                    .associateDeveloper(AssociateSpaceDeveloperRequest.builder()
                        .spaceId(spaceId)
                        .developerId(userId)
                        .build())
                    .map(ResourceUtils::getId)
            )))
            .subscribe(this.<Tuple2<String, String>>testSubscriber()
                .expectThat(this::assertTupleEquality));
    }

    @Test
    public void associateDeveloperByUsername() {
        String organizationName = this.nameFactory.getOrganizationName();
        String spaceName = this.nameFactory.getSpaceName();

        createUserIdAndSpaceId(this.cloudFoundryClient, organizationName, spaceName, this.username)
            .as(thenKeep(function((userId, spaceId) -> this.cloudFoundryClient.spaces()
                .associateDeveloperByUsername(AssociateSpaceDeveloperByUsernameRequest.builder()
                    .spaceId(spaceId)
                    .username(this.username)
                    .build()))))
            .flatMap(function((userId, spaceId) -> PaginationUtils
                .requestClientV2Resources(page -> this.cloudFoundryClient.spaces()
                    .listDevelopers(ListSpaceDevelopersRequest.builder()
                        .page(page)
                        .spaceId(spaceId)
                        .build()))
                .map(ResourceUtils::getEntity)
                .map(UserEntity::getUsername)))
            .subscribe(this.testSubscriber()
                .expectEquals(this.username));
    }

    @Test
    public void associateManager() {
        String organizationName = this.nameFactory.getOrganizationName();
        String spaceName = this.nameFactory.getSpaceName();

        createUserIdAndSpaceId(this.cloudFoundryClient, organizationName, spaceName, this.username)
            .then(function((userId, spaceId) -> Mono.when(
                Mono.just(spaceId),
                this.cloudFoundryClient.spaces()
                    .associateManager(AssociateSpaceManagerRequest.builder()
                        .spaceId(spaceId)
                        .managerId(userId)
                        .build())
                    .map(ResourceUtils::getId)
            )))
            .subscribe(this.<Tuple2<String, String>>testSubscriber()
                .expectThat(this::assertTupleEquality));
    }

    @Test
    public void associateManagerByUsername() {
        String organizationName = this.nameFactory.getOrganizationName();
        String spaceName = this.nameFactory.getSpaceName();

        createUserIdAndSpaceId(this.cloudFoundryClient, organizationName, spaceName, this.username)
            .as(thenKeep(function((userId, spaceId) -> this.cloudFoundryClient.spaces()
                .associateManagerByUsername(AssociateSpaceManagerByUsernameRequest.builder()
                    .spaceId(spaceId)
                    .username(this.username)
                    .build()))))
            .flatMap(function((userId, spaceId) -> requestListSpaceManagers(this.cloudFoundryClient, spaceId)
                .map(ResourceUtils::getEntity)
                .map(UserEntity::getUsername)))
            .subscribe(this.testSubscriber()
                .expectEquals(this.username));
    }

    @Ignore("TODO: awaiting https://www.pivotaltracker.com/story/show/101522656")
    @Test
    public void associateSecurityGroup() {
        fail("TODO: awaiting https://www.pivotaltracker.com/story/show/101522656");
    }

    @Test
    public void create() {
        String spaceName = this.nameFactory.getSpaceName();

        this.organizationId
            .then(organizationId -> this.cloudFoundryClient.spaces()
                .create(CreateSpaceRequest.builder()
                    .organizationId(organizationId)
                    .name(spaceName)
                    .build()))
            .map(ResourceUtils::getEntity)
            .map(SpaceEntity::getName)
            .subscribe(this.<String>testSubscriber()
                .expectEquals(spaceName));
    }

    @Test
    public void delete() {
        String spaceName = this.nameFactory.getSpaceName();

        this.organizationId
            .then(organizationId -> createSpaceId(this.cloudFoundryClient, organizationId, spaceName))
            .flatMap(spaceId -> this.cloudFoundryClient.spaces()
                .delete(DeleteSpaceRequest.builder()
                    .spaceId(spaceId)
                    .async(true)
                    .build())
                .then(job -> JobUtils.waitForCompletion(this.cloudFoundryClient, job))
                .then(Mono.just(spaceId)))
            .flatMap(spaceId -> requestListSpaces(this.cloudFoundryClient)
                .map(ResourceUtils::getId)
                .filter(spaceId::equals))
            .subscribe(testSubscriber());
    }

    @Test
    public void deleteAsyncFalse() {
        String spaceName = this.nameFactory.getSpaceName();

        this.organizationId
            .then(organizationId -> createSpaceId(this.cloudFoundryClient, organizationId, spaceName))
            .as(thenKeep(spaceId -> this.cloudFoundryClient.spaces()
                .delete(DeleteSpaceRequest.builder()
                    .spaceId(spaceId)
                    .async(false)
                    .build())))
            .flatMap(spaceId -> requestListSpaces(this.cloudFoundryClient)
                .map(ResourceUtils::getId)
                .filter(spaceId::equals))
            .subscribe(testSubscriber());
    }

    @Test
    public void get() {
        String spaceName = this.nameFactory.getSpaceName();

        this.organizationId
            .then(organizationId -> createSpaceId(this.cloudFoundryClient, organizationId, spaceName))
            .then(spaceId -> this.cloudFoundryClient.spaces()
                .get(GetSpaceRequest.builder()
                    .spaceId(spaceId)
                    .build()))
            .map(ResourceUtils::getEntity)
            .map(SpaceEntity::getName)
            .subscribe(this.<String>testSubscriber()
                .expectEquals(spaceName));
    }

    @Test
    public void getSummary() {
        String spaceName = this.nameFactory.getSpaceName();

        this.organizationId
            .then(organizationId -> createSpaceId(this.cloudFoundryClient, organizationId, spaceName))
            .then(spaceId -> this.cloudFoundryClient.spaces()
                .getSummary(GetSpaceSummaryRequest.builder()
                    .spaceId(spaceId)
                    .build()))
            .map(GetSpaceSummaryResponse::getName)
            .subscribe(this.<String>testSubscriber()
                .expectEquals(spaceName));
    }

    @Test
    public void list() {
        String organizationName = this.nameFactory.getOrganizationName();
        String spaceName = this.nameFactory.getSpaceName();

        createOrganizationIdAndSpaceId(this.cloudFoundryClient, organizationName, spaceName)
            .then(function((organizationId, spaceId) -> Mono.when(
                Mono.just(spaceId),
                requestListSpaces(this.cloudFoundryClient)
                    .filter(hasOrganizationId(organizationId))
                    .single()
                    .map(ResourceUtils::getId)
            )))
            .subscribe(this.<Tuple2<String, String>>testSubscriber()
                .expectThat(this::assertTupleEquality));
    }

    @Test
    public void listApplications() {
        String applicationName = this.nameFactory.getApplicationName();
        String spaceName = this.nameFactory.getSpaceName();

        this.organizationId
            .then(organizationId -> createSpaceId(this.cloudFoundryClient, organizationId, spaceName))
            .as(thenKeep(spaceId -> createApplicationId(this.cloudFoundryClient, spaceId, applicationName)))
            .then(spaceId -> requestListSpaceApplications(this.cloudFoundryClient, spaceId)
                .single())
            .map(ResourceUtils::getEntity)
            .map(ApplicationEntity::getName)
            .subscribe(this.<String>testSubscriber()
                .expectEquals(applicationName));
    }

    @Test
    public void listApplicationsFilterByDiego() {
        String applicationName = this.nameFactory.getApplicationName();
        String spaceName = this.nameFactory.getSpaceName();

        this.organizationId
            .then(organizationId -> createSpaceId(this.cloudFoundryClient, organizationId, spaceName))
            .as(thenKeep(spaceId -> createApplicationId(this.cloudFoundryClient, spaceId, applicationName)))
            .then(spaceId -> requestListSpaceApplications(this.cloudFoundryClient, spaceId, builder -> builder.diego(true))
                .single())
            .map(ResourceUtils::getEntity)
            .map(ApplicationEntity::getName)
            .subscribe(this.<String>testSubscriber()
                .expectEquals(applicationName));
    }

    @Test
    public void listApplicationsFilterByName() {
        String applicationName = this.nameFactory.getApplicationName();
        String spaceName = this.nameFactory.getSpaceName();

        this.organizationId
            .then(organizationId -> createSpaceId(this.cloudFoundryClient, organizationId, spaceName))
            .then(spaceId -> Mono.when(
                Mono.just(spaceId),
                createApplicationId(this.cloudFoundryClient, spaceId, applicationName)
            ))
            .flatMap(function((spaceId, applicationId) -> Mono.when(
                Mono.just(applicationId),
                requestListSpaceApplications(this.cloudFoundryClient, spaceId, builder -> builder.name(applicationName))
                    .single()
                    .map(ResourceUtils::getId)
            )))
            .subscribe(this.<Tuple2<String, String>>testSubscriber()
                .expectThat(this::assertTupleEquality));
    }

    @Test
    public void listApplicationsFilterByOrganizationId() {
        String applicationName = this.nameFactory.getApplicationName();
        String spaceName = this.nameFactory.getSpaceName();

        this.organizationId
            .then(organizationId -> Mono.when(
                Mono.just(organizationId),
                createSpaceId(this.cloudFoundryClient, organizationId, spaceName)
            ))
            .as(thenKeep(function((organizationId, spaceId) -> createApplicationId(this.cloudFoundryClient, spaceId, applicationName))))
            .flatMap(function((organizationId, spaceId) -> requestListSpaceApplications(this.cloudFoundryClient, spaceId, builder -> builder.organizationId(organizationId))))
            .map(ResourceUtils::getEntity)
            .map(ApplicationEntity::getName)
            .subscribe(this.<String>testSubscriber()
                .expectEquals(applicationName));
    }

    @Test
    public void listApplicationsFilterByStackId() {
        String applicationName = this.nameFactory.getApplicationName();
        String spaceName = this.nameFactory.getSpaceName();

        this.organizationId
            .then(organizationId -> createSpaceId(this.cloudFoundryClient, organizationId, spaceName))
            .as(thenKeep(spaceId -> createApplicationId(this.cloudFoundryClient, spaceId, applicationName)))
            .then(spaceId -> Mono.when(
                Mono.just(spaceId),
                PaginationUtils
                    .requestClientV2Resources(page -> this.cloudFoundryClient.stacks()
                        .list(ListStacksRequest.builder()
                            .name(this.stackName)
                            .page(page)
                            .build()))
                    .map(ResourceUtils::getId)
                    .single()
            ))
            .flatMap(function((spaceId, stackId) -> requestListSpaceApplications(this.cloudFoundryClient, spaceId, builder -> builder.stackId(stackId))))
            .map(ResourceUtils::getEntity)
            .map(ApplicationEntity::getName)
            .subscribe(this.<String>testSubscriber()
                .expectEquals(applicationName));
    }

    @Test
    public void listAuditors() {
        String organizationName = this.nameFactory.getOrganizationName();
        String spaceName = this.nameFactory.getSpaceName();

        createUserIdAndSpaceId(this.cloudFoundryClient, organizationName, spaceName, this.username)
            .as(thenKeep(function((userId, spaceId) -> this.cloudFoundryClient.spaces()
                .associateAuditor(AssociateSpaceAuditorRequest.builder()
                    .spaceId(spaceId)
                    .auditorId(userId)
                    .build()))))
            .flatMap(function((userId, spaceId) -> PaginationUtils
                .requestClientV2Resources(page -> this.cloudFoundryClient.spaces()
                    .listAuditors(ListSpaceAuditorsRequest.builder()
                        .page(page)
                        .spaceId(spaceId)
                        .build()))))
            .map(ResourceUtils::getEntity)
            .map(UserEntity::getUsername)
            .subscribe(this.testSubscriber()
                .expectEquals(this.username));
    }

    @Test
    public void listDevelopers() {
        String organizationName = this.nameFactory.getOrganizationName();
        String spaceName = this.nameFactory.getSpaceName();

        createUserIdAndSpaceId(this.cloudFoundryClient, organizationName, spaceName, this.username)
            .as(thenKeep(function((userId, spaceId) -> this.cloudFoundryClient.spaces()
                .associateDeveloper(AssociateSpaceDeveloperRequest.builder()
                    .spaceId(spaceId)
                    .developerId(userId)
                    .build()))))
            .flatMap(function((userId, spaceId) -> PaginationUtils
                .requestClientV2Resources(page -> this.cloudFoundryClient.spaces()
                    .listDevelopers(ListSpaceDevelopersRequest.builder()
                        .page(page)
                        .spaceId(spaceId)
                        .build()))))
            .map(ResourceUtils::getEntity)
            .map(UserEntity::getUsername)
            .subscribe(this.testSubscriber()
                .expectEquals(this.username));
    }

    @Test
    public void listDomains() {
        String domainName = this.nameFactory.getDomainName();
        String spaceName = this.nameFactory.getSpaceName();

        this.organizationId
            .then(organizationId -> createSpaceIdWithDomain(this.cloudFoundryClient, organizationId, spaceName, domainName))
            .then(spaceId -> Mono.when(
                Mono.just(spaceId),
                getSpaceDomainId(this.cloudFoundryClient, spaceId, domainName)
            ))
            .flatMap(function((spaceId, domainId) -> requestListSpaceDomains(this.cloudFoundryClient, spaceId)))
            .filter(domainResource -> domainName.equals(ResourceUtils.getEntity(domainResource).getName()))
            .subscribe(testSubscriber()
                .expectCount(1));
    }

    @Test
    public void listDomainsFilterByName() {
        String domainName = this.nameFactory.getDomainName();
        String spaceName = this.nameFactory.getSpaceName();

        this.organizationId
            .then(organizationId -> createSpaceIdWithDomain(this.cloudFoundryClient, organizationId, spaceName, domainName))
            .flatMap(spaceId -> requestListSpaceDomains(this.cloudFoundryClient, spaceId, builder -> builder.name(domainName)))
            .map(ResourceUtils::getEntity)
            .map(DomainEntity::getName)
            .subscribe(this.<String>testSubscriber()
                .expectEquals(domainName));
    }

    @Ignore("Filter parameter not honoured: see https://github.com/cloudfoundry/cloud_controller_ng/issues/584")
    @Test
    public void listDomainsFilterByOwningOrganizationId() {
        String domainName = this.nameFactory.getDomainName();
        String spaceOrganizationName = this.nameFactory.getOrganizationName();
        String domainOrganizationName = this.nameFactory.getOrganizationName();
        String spaceName = this.nameFactory.getSpaceName();

        Mono
            .when(
                createOrganizationId(this.cloudFoundryClient, spaceOrganizationName),
                createOrganizationId(this.cloudFoundryClient, domainOrganizationName)
            )
            .then(function((spaceOrganizationId, domainOrganizationId) -> Mono.when(
                Mono.just(domainOrganizationId),
                this.cloudFoundryClient.domains()
                    .create(CreateDomainRequest.builder()
                        .name(domainName)
                        .owningOrganizationId(domainOrganizationId)
                        .wildcard(true)
                        .build())
                    .map(ResourceUtils::getId)
                    .then(domainId -> this.cloudFoundryClient.spaces()
                        .create(CreateSpaceRequest.builder()
                            .domainId(domainId)
                            .organizationId(spaceOrganizationId)
                            .name(spaceName)
                            .build()))
                    .map(ResourceUtils::getId)
            )))
            .flatMap(function((domainOrganizationId, spaceId) -> requestListSpaceDomains(this.cloudFoundryClient, spaceId, builder -> builder.owningOrganizationId(domainOrganizationId))))
            .map(ResourceUtils::getEntity)
            .map(DomainEntity::getName)
            .subscribe(this.<String>testSubscriber()
                .expectEquals(domainName));
    }

    @Test
    public void listEvents() {
        String spaceName = this.nameFactory.getSpaceName();

        this.organizationId
            .then(organizationId -> createSpaceId(this.cloudFoundryClient, organizationId, spaceName))
            .flatMap(spaceId -> requestListSpaceEvents(this.cloudFoundryClient, spaceId))
            .map(ResourceUtils::getEntity)
            .map(EventEntity::getType)
            .subscribe(this.<String>testSubscriber()
                .expectEquals("audit.space.create"));
    }

    @Test
    public void listEventsFilterByActee() {
        String spaceName = this.nameFactory.getSpaceName();

        this.organizationId
            .then(organizationId -> createSpaceId(this.cloudFoundryClient, organizationId, spaceName))
            .flatMap(spaceId -> requestListSpaceEvents(this.cloudFoundryClient, spaceId, builder -> builder.actee(spaceId)))
            .map(ResourceUtils::getEntity)
            .map(EventEntity::getType)
            .subscribe(this.<String>testSubscriber()
                .expectEquals("audit.space.create"));
    }

    @Test
    public void listEventsFilterByTimestamp() {
        String spaceName = this.nameFactory.getSpaceName();
        String timestamp = getPastTimestamp();

        this.organizationId
            .then(organizationId -> createSpaceId(this.cloudFoundryClient, organizationId, spaceName))
            .flatMap(spaceId -> requestListSpaceEvents(this.cloudFoundryClient, spaceId, builder -> builder.timestamp(timestamp)))
            .map(ResourceUtils::getEntity)
            .map(EventEntity::getType)
            .subscribe(this.<String>testSubscriber()
                .expectEquals("audit.space.create"));
    }

    @Test
    public void listEventsFilterByType() {
        String spaceName = this.nameFactory.getSpaceName();

        this.organizationId
            .then(organizationId -> createSpaceId(this.cloudFoundryClient, organizationId, spaceName))
            .then(spaceId -> Mono.when(
                Mono.just(spaceId),
                requestListSpaceEvents(this.cloudFoundryClient, spaceId, builder -> builder.type("audit.space.create"))
                    .single()
                    .map(ResourceUtils::getEntity)
                    .map(EventEntity::getSpaceId)
            ))
            .subscribe(this.<Tuple2<String, String>>testSubscriber()
                .expectThat(this::assertTupleEquality));
    }

    @Test
    public void listFilterByApplicationId() {
        String applicationName = this.nameFactory.getApplicationName();
        String spaceName = this.nameFactory.getSpaceName();

        this.organizationId
            .then(organizationId -> createSpaceId(this.cloudFoundryClient, organizationId, spaceName))
            .then(spaceId -> Mono.when(
                Mono.just(spaceId),
                createApplicationId(this.cloudFoundryClient, spaceId, applicationName)
            ))
            .then(function((spaceId, applicationId) -> Mono.when(
                Mono.just(spaceId),
                requestListSpaces(this.cloudFoundryClient, builder -> builder.applicationId(applicationId))
                    .map(ResourceUtils::getId)
                    .single()
            )))
            .subscribe(this.<Tuple2<String, String>>testSubscriber()
                .expectThat(this::assertTupleEquality));
    }

    @Ignore("TODO: awaiting https://www.pivotaltracker.com/story/show/101522686 really create a new user")
    @Test
    public void listFilterByDeveloperId() {
        fail("TODO: awaiting https://www.pivotaltracker.com/story/show/101522686 really create a new user");
    }

    @Test
    public void listFilterByName() {
        String spaceName = this.nameFactory.getSpaceName();

        this.organizationId
            .then(organizationId -> createSpaceId(this.cloudFoundryClient, organizationId, spaceName))
            .then(spaceId -> Mono.when(
                Mono.just(spaceId),
                requestListSpaces(this.cloudFoundryClient, builder -> builder.name(spaceName))
                    .map(ResourceUtils::getId)
                    .single()
            ))
            .subscribe(this.<Tuple2<String, String>>testSubscriber()
                .expectThat(this::assertTupleEquality));
    }

    @Test
    public void listFilterByOrganizationId() {
        String organizationName = this.nameFactory.getOrganizationName();
        String spaceName = this.nameFactory.getSpaceName();

        createOrganizationIdAndSpaceId(this.cloudFoundryClient, organizationName, spaceName)
            .then(function((organizationId, spaceId) -> Mono.when(
                Mono.just(spaceId),
                requestListSpaces(this.cloudFoundryClient, builder -> builder.organizationId(organizationId))
                    .single()
                    .map(ResourceUtils::getId)
            )))
            .subscribe(this.<Tuple2<String, String>>testSubscriber()
                .expectThat(this::assertTupleEquality));
    }

    @Test
    public void listManagers() {
        String organizationName = this.nameFactory.getOrganizationName();
        String spaceName = this.nameFactory.getSpaceName();

        createUserIdAndSpaceId(this.cloudFoundryClient, organizationName, spaceName, this.username)
            .as(thenKeep(function((userId, spaceId) -> requestAssociateSpaceManager(this.cloudFoundryClient, spaceId, userId))))
            .flatMap(function((userId, spaceId) -> requestListSpaceManagers(this.cloudFoundryClient, spaceId)
                .map(ResourceUtils::getEntity)
                .map(UserEntity::getUsername)))
            .subscribe(this.testSubscriber()
                .expectEquals(this.username));
    }

    @Test
    public void listManagersFilterByAuditedOrganizationId() {
        String organizationName = this.nameFactory.getOrganizationName();
        String spaceName = this.nameFactory.getSpaceName();

        createOrganizationId(this.cloudFoundryClient, organizationName)
            .then(organizationId -> Mono.when(
                createUserId(this.cloudFoundryClient, organizationId, this.username),
                createSpaceId(this.cloudFoundryClient, organizationId, spaceName),
                Mono.just(organizationId)
            ))
            .as(thenKeep(function((userId, spaceId, organizationId) -> Mono.when(
                requestAssociateSpaceManager(this.cloudFoundryClient, spaceId, userId),
                this.cloudFoundryClient.organizations()
                    .associateAuditor(AssociateOrganizationAuditorRequest.builder()
                        .organizationId(organizationId)
                        .auditorId(userId)
                        .build())))))
            .flatMap(function((userId, spaceId, organizationId) -> requestListSpaceManagers(this.cloudFoundryClient, spaceId, builder -> builder.auditedOrganizationId(organizationId))
                .map(ResourceUtils::getEntity)
                .map(UserEntity::getUsername)))
            .subscribe(this.testSubscriber()
                .expectEquals(this.username));
    }

    @Test
    public void listManagersFilterByAuditedSpaceId() {
        String organizationName = this.nameFactory.getOrganizationName();
        String spaceName = this.nameFactory.getSpaceName();

        createUserIdAndSpaceId(this.cloudFoundryClient, organizationName, spaceName, this.username)
            .as(thenKeep(function((userId, spaceId) -> Mono.when(
                requestAssociateSpaceManager(this.cloudFoundryClient, spaceId, userId),
                requestAssociateSpaceAuditor(this.cloudFoundryClient, spaceId, userId)
            ))))
            .flatMap(function((userId, spaceId) -> requestListSpaceManagers(this.cloudFoundryClient, spaceId, builder -> builder.auditedSpaceId(spaceId))
                .map(ResourceUtils::getEntity)
                .map(UserEntity::getUsername)))
            .subscribe(this.testSubscriber()
                .expectEquals(this.username));
    }

    @Test
    public void listManagersFilterByBillingManagedOrganizationId() {
        String organizationName = this.nameFactory.getOrganizationName();
        String spaceName = this.nameFactory.getSpaceName();

        createOrganizationId(this.cloudFoundryClient, organizationName)
            .then(organizationId -> Mono.when(
                createUserId(this.cloudFoundryClient, organizationId, this.username),
                createSpaceId(this.cloudFoundryClient, organizationId, spaceName),
                Mono.just(organizationId)
            ))
            .as(thenKeep(function((userId, spaceId, organizationId) -> Mono.when(
                requestAssociateSpaceManager(this.cloudFoundryClient, spaceId, userId),
                this.cloudFoundryClient.organizations()
                    .associateBillingManager(AssociateOrganizationBillingManagerRequest.builder()
                        .organizationId(organizationId)
                        .billingManagerId(userId)
                        .build())
            ))))
            .then(function((userId, spaceId, organizationId) -> Mono.when(
                Mono.just(userId),
                requestListSpaceManagers(this.cloudFoundryClient, spaceId, builder -> builder.billingManagedOrganizationId(organizationId))
                    .single()
                    .map(ResourceUtils::getId)
            )))
            .subscribe(this.<Tuple2<String, String>>testSubscriber()
                .expectThat(this::assertTupleEquality));
    }

    @Test
    public void listManagersFilterByManagedOrganizationId() {
        String organizationName = this.nameFactory.getOrganizationName();
        String spaceName = this.nameFactory.getSpaceName();

        createOrganizationId(this.cloudFoundryClient, organizationName)
            .then(organizationId -> Mono.when(
                createUserId(this.cloudFoundryClient, organizationId, this.username),
                createSpaceId(this.cloudFoundryClient, organizationId, spaceName),
                Mono.just(organizationId)
            ))
            .as(thenKeep(function((userId, spaceId, organizationId) -> Mono.when(
                requestAssociateSpaceManager(this.cloudFoundryClient, spaceId, userId),
                this.cloudFoundryClient.organizations()
                    .associateManager(AssociateOrganizationManagerRequest.builder()
                        .organizationId(organizationId)
                        .managerId(userId)
                        .build())
            ))))
            .then(function((userId, spaceId, organizationId) -> Mono.when(
                Mono.just(userId),
                requestListSpaceManagers(this.cloudFoundryClient, spaceId, builder -> builder.managedOrganizationId(organizationId))
                    .single()
                    .map(ResourceUtils::getId)
            )))
            .subscribe(this.<Tuple2<String, String>>testSubscriber()
                .expectThat(this::assertTupleEquality));
    }

    @Test
    public void listManagersFilterByManagedSpaceId() {
        String organizationName = this.nameFactory.getOrganizationName();
        String spaceName = this.nameFactory.getSpaceName();

        createUserIdAndSpaceId(this.cloudFoundryClient, organizationName, spaceName, this.username)
            .as(thenKeep(function((userId, spaceId) -> requestAssociateSpaceManager(this.cloudFoundryClient, spaceId, userId))))
            .flatMap(function((userId, spaceId) -> requestListSpaceManagers(this.cloudFoundryClient, spaceId, builder -> builder.managedSpaceId(spaceId))
                .map(ResourceUtils::getEntity)
                .map(UserEntity::getUsername)))
            .subscribe(this.testSubscriber()
                .expectEquals(this.username));
    }

    @Test
    public void listManagersFilterByOrganizationId() {
        String organizationName = this.nameFactory.getOrganizationName();
        String spaceName = this.nameFactory.getSpaceName();

        createOrganizationId(this.cloudFoundryClient, organizationName)
            .then(organizationId -> Mono.when(
                createUserId(this.cloudFoundryClient, organizationId, this.username),
                createSpaceId(this.cloudFoundryClient, organizationId, spaceName),
                Mono.just(organizationId)
            ))
            .as(thenKeep(function((userId, spaceId, organizationId) -> requestAssociateSpaceManager(this.cloudFoundryClient, spaceId, userId))))
            .then(function((userId, spaceId, organizationId) -> Mono.when(
                Mono.just(userId),
                requestListSpaceManagers(this.cloudFoundryClient, spaceId, builder -> builder.organizationId(organizationId))
                    .single()
                    .map(ResourceUtils::getId)
            )))
            .subscribe(this.<Tuple2<String, String>>testSubscriber()
                .expectThat(this::assertTupleEquality));
    }

    @Test
    public void listRoutes() {
        String domainName = this.nameFactory.getDomainName();
        String hostName = this.nameFactory.getHostName();
        String spaceName = this.nameFactory.getSpaceName();

        this.organizationId
            .then(organizationId -> createSpaceIdWithDomain(this.cloudFoundryClient, organizationId, spaceName, domainName))
            .then(spaceId -> Mono.when(
                Mono.just(spaceId),
                createRouteId(this.cloudFoundryClient, spaceId, domainName, hostName, "/test-path")
            ))
            .flatMap(function((spaceId, routeId) -> Mono.when(
                Mono.just(routeId),
                requestListSpaceRoutes(this.cloudFoundryClient, spaceId)
                    .map(ResourceUtils::getId)
                    .single()
            )))
            .subscribe(this.<Tuple2<String, String>>testSubscriber()
                .expectThat(this::assertTupleEquality));
    }

    @Test
    public void listRoutesFilterByDomainId() {
        String domainName = this.nameFactory.getDomainName();
        String hostName = this.nameFactory.getHostName();
        String spaceName = this.nameFactory.getSpaceName();

        this.organizationId
            .then(organizationId -> createSpaceIdWithDomain(this.cloudFoundryClient, organizationId, spaceName, domainName))
            .then(spaceId -> Mono.when(
                Mono.just(spaceId),
                getSpaceDomainId(this.cloudFoundryClient, spaceId, domainName),
                createRouteId(this.cloudFoundryClient, spaceId, domainName, hostName, "/test-path")
            ))
            .flatMap(function((spaceId, domainId, routeId) -> Mono.when(
                Mono.just(routeId),
                requestListSpaceRoutes(this.cloudFoundryClient, spaceId, builder -> builder.domainId(domainId))
                    .map(ResourceUtils::getId)
                    .single()
            )))
            .subscribe(this.<Tuple2<String, String>>testSubscriber()
                .expectThat(this::assertTupleEquality));
    }

    @Test
    public void listRoutesFilterByHost() {
        String domainName = this.nameFactory.getDomainName();
        String hostName = this.nameFactory.getHostName();
        String spaceName = this.nameFactory.getSpaceName();

        this.organizationId
            .then(organizationId -> createSpaceIdWithDomain(this.cloudFoundryClient, organizationId, spaceName, domainName))
            .then(spaceId -> Mono.when(
                Mono.just(spaceId),
                createRouteId(this.cloudFoundryClient, spaceId, domainName, hostName, "/test-path")
            ))
            .flatMap(function((spaceId, routeId) -> Mono.when(
                Mono.just(routeId),
                requestListSpaceRoutes(this.cloudFoundryClient, spaceId, builder -> builder.host(hostName))
                    .map(ResourceUtils::getId)
                    .single()
            )))
            .subscribe(this.<Tuple2<String, String>>testSubscriber()
                .expectThat(this::assertTupleEquality));
    }

    @Test
    public void listRoutesFilterByPath() {
        String domainName = this.nameFactory.getDomainName();
        String hostName = this.nameFactory.getHostName();
        String spaceName = this.nameFactory.getSpaceName();

        this.organizationId
            .then(organizationId -> createSpaceIdWithDomain(this.cloudFoundryClient, organizationId, spaceName, domainName))
            .then(spaceId -> Mono.when(
                Mono.just(spaceId),
                createRouteId(this.cloudFoundryClient, spaceId, domainName, hostName, "/test-path")
            ))
            .flatMap(function((spaceId, routeId) -> Mono.when(
                Mono.just(routeId),
                requestListSpaceRoutes(this.cloudFoundryClient, spaceId, builder -> builder.path("/test-path"))
                    .map(ResourceUtils::getId)
                    .single()
            )))
            .subscribe(this.<Tuple2<String, String>>testSubscriber()
                .expectThat(this::assertTupleEquality));
    }

    @Ignore("TODO: awaiting https://www.pivotaltracker.com/story/show/101522656")
    @Test
    public void listSecurityGroups() {
        fail("TODO: awaiting https://www.pivotaltracker.com/story/show/101522656");
    }

    @Ignore("TODO: awaiting https://www.pivotaltracker.com/story/show/101522656")
    @Test
    public void listSecurityGroupsFilterByName() {
        fail("TODO: awaiting https://www.pivotaltracker.com/story/show/101522656");
    }

    @Ignore("TODO: awaiting https://www.pivotaltracker.com/story/show/118387501")
    @Test
    public void listServiceInstances() {
        fail("TODO: awaiting https://www.pivotaltracker.com/story/show/118387501");
    }

    @Ignore("TODO: awaiting https://www.pivotaltracker.com/story/show/118387501")
    @Test
    public void listServiceInstancesFilterByGatewayName() {
        fail("TODO: awaiting https://www.pivotaltracker.com/story/show/118387501");
    }

    @Ignore("TODO: awaiting https://www.pivotaltracker.com/story/show/118387501")
    @Test
    public void listServiceInstancesFilterByName() {
        fail("TODO: awaiting https://www.pivotaltracker.com/story/show/118387501");
    }

    @Ignore("TODO: awaiting https://www.pivotaltracker.com/story/show/118387501")
    @Test
    public void listServiceInstancesFilterByOrganizationId() {
        fail("TODO: awaiting https://www.pivotaltracker.com/story/show/118387501");
    }

    @Ignore("TODO: awaiting https://www.pivotaltracker.com/story/show/118387501")
    @Test
    public void listServiceInstancesFilterByServiceBindingId() {
        fail("TODO: awaiting https://www.pivotaltracker.com/story/show/118387501");
    }

    @Ignore("TODO: awaiting https://www.pivotaltracker.com/story/show/118387501")
    @Test
    public void listServiceInstancesFilterByServiceKeyId() {
        fail("TODO: awaiting https://www.pivotaltracker.com/story/show/118387501");
    }

    @Ignore("TODO: awaiting https://www.pivotaltracker.com/story/show/118387501")
    @Test
    public void listServiceInstancesFilterByServicePlanId() {
        fail("TODO: awaiting https://www.pivotaltracker.com/story/show/118387501");
    }

    @Ignore("TODO: awaiting https://www.pivotaltracker.com/story/show/118387501")
    @Test
    public void listServices() {
        fail("TODO: awaiting https://www.pivotaltracker.com/story/show/118387501");
    }

    @Ignore("TODO: awaiting https://www.pivotaltracker.com/story/show/118387501")
    @Test
    public void listServicesFilterByActive() {
        fail("TODO: awaiting https://www.pivotaltracker.com/story/show/118387501");
    }

    @Ignore("TODO: awaiting https://www.pivotaltracker.com/story/show/118387501")
    @Test
    public void listServicesFilterByLabel() {
        fail("TODO: awaiting https://www.pivotaltracker.com/story/show/118387501");
    }

    @Ignore("TODO: awaiting https://www.pivotaltracker.com/story/show/118387501")
    @Test
    public void listServicesFilterByServiceBrokerId() {
        fail("TODO: awaiting https://www.pivotaltracker.com/story/show/118387501");
    }

    @Test
    public void listUserRoles() {
        String organizationName = this.nameFactory.getOrganizationName();
        String spaceName = this.nameFactory.getSpaceName();

        createUserIdAndSpaceId(this.cloudFoundryClient, organizationName, spaceName, this.username)
            .as(thenKeep(function((userId, spaceId) -> requestAssociateSpaceManager(this.cloudFoundryClient, spaceId, userId))))
            .flatMap(function((userId, spaceId) -> PaginationUtils
                .requestClientV2Resources(page -> this.cloudFoundryClient.spaces()
                    .listUserRoles(ListSpaceUserRolesRequest.builder()
                        .page(page)
                        .spaceId(spaceId)
                        .build()))
                .map(ResourceUtils::getEntity)))
            .subscribe(this.<UserSpaceRoleEntity>testSubscriber()
                .expectThat(userSpaceRoleEntity -> {
                    assertEquals(this.username, userSpaceRoleEntity.getUsername());
                    assertEquals(Collections.singletonList("space_manager"), userSpaceRoleEntity.getSpaceRoles());
                }));
    }

    @Test
    public void removeAuditor() {
        String organizationName = this.nameFactory.getOrganizationName();
        String spaceName = this.nameFactory.getSpaceName();

        createUserIdAndSpaceId(this.cloudFoundryClient, organizationName, spaceName, this.username)
            .as(thenKeep(function((userId, spaceId) -> requestAssociateSpaceAuditor(this.cloudFoundryClient, spaceId, userId))))
            .as(thenKeep(function((userId, spaceId) -> this.cloudFoundryClient.spaces()
                .removeAuditor(RemoveSpaceAuditorRequest.builder()
                    .spaceId(spaceId)
                    .auditorId(userId)
                    .build()))))
            .flatMap(function((userId, spaceId) -> PaginationUtils
                .requestClientV2Resources(page -> this.cloudFoundryClient.spaces()
                    .listAuditors(ListSpaceAuditorsRequest.builder()
                        .page(page)
                        .spaceId(spaceId)
                        .build()))))
            .subscribe(this.testSubscriber());
    }

    @Test
    public void removeAuditorByUsername() {
        String organizationName = this.nameFactory.getOrganizationName();
        String spaceName = this.nameFactory.getSpaceName();

        createUserIdAndSpaceId(this.cloudFoundryClient, organizationName, spaceName, this.username)
            .as(thenKeep(function((userId, spaceId) -> requestAssociateSpaceAuditor(this.cloudFoundryClient, spaceId, userId))))
            .as(thenKeep(function((userId, spaceId) -> this.cloudFoundryClient.spaces()
                .removeAuditorByUsername(RemoveSpaceAuditorByUsernameRequest.builder()
                    .spaceId(spaceId)
                    .username(this.username)
                    .build()))))
            .flatMap(function((userId, spaceId) -> PaginationUtils
                .requestClientV2Resources(page -> this.cloudFoundryClient.spaces()
                    .listAuditors(ListSpaceAuditorsRequest.builder()
                        .page(page)
                        .spaceId(spaceId)
                        .build()))))
            .subscribe(this.testSubscriber());
    }

    @Test
    public void removeDeveloper() {
        String organizationName = this.nameFactory.getOrganizationName();
        String spaceName = this.nameFactory.getSpaceName();

        createUserIdAndSpaceId(this.cloudFoundryClient, organizationName, spaceName, this.username)
            .as(thenKeep(function((userId, spaceId) -> this.cloudFoundryClient.spaces()
                .associateDeveloper(AssociateSpaceDeveloperRequest.builder()
                    .spaceId(spaceId)
                    .developerId(userId)
                    .build()))))
            .as(thenKeep(function((userId, spaceId) -> this.cloudFoundryClient.spaces()
                .removeDeveloper(RemoveSpaceDeveloperRequest.builder()
                    .spaceId(spaceId)
                    .developerId(userId)
                    .build()))))
            .flatMap(function((userId, spaceId) -> PaginationUtils
                .requestClientV2Resources(page -> this.cloudFoundryClient.spaces()
                    .listDevelopers(ListSpaceDevelopersRequest.builder()
                        .page(page)
                        .spaceId(spaceId)
                        .build()))))
            .subscribe(this.testSubscriber());
    }

    @Test
    public void removeDeveloperByUsername() {
        String organizationName = this.nameFactory.getOrganizationName();
        String spaceName = this.nameFactory.getSpaceName();

        createUserIdAndSpaceId(this.cloudFoundryClient, organizationName, spaceName, this.username)
            .as(thenKeep(function((userId, spaceId) -> this.cloudFoundryClient.spaces()
                .associateDeveloper(AssociateSpaceDeveloperRequest.builder()
                    .spaceId(spaceId)
                    .developerId(userId)
                    .build()))))
            .as(thenKeep(function((userId, spaceId) -> this.cloudFoundryClient.spaces()
                .removeDeveloperByUsername(RemoveSpaceDeveloperByUsernameRequest.builder()
                    .spaceId(spaceId)
                    .username(this.username)
                    .build()))))
            .flatMap(function((userId, spaceId) -> PaginationUtils
                .requestClientV2Resources(page -> this.cloudFoundryClient.spaces()
                    .listDevelopers(ListSpaceDevelopersRequest.builder()
                        .page(page)
                        .spaceId(spaceId)
                        .build()))))
            .subscribe(this.testSubscriber());
    }

    @Test
    public void removeManager() {
        String organizationName = this.nameFactory.getOrganizationName();
        String spaceName = this.nameFactory.getSpaceName();

        createUserIdAndSpaceId(this.cloudFoundryClient, organizationName, spaceName, this.username)
            .as(thenKeep(function((userId, spaceId) -> requestAssociateSpaceManager(this.cloudFoundryClient, spaceId, userId))))
            .as(thenKeep(function((userId, spaceId) -> this.cloudFoundryClient.spaces()
                .removeManager(RemoveSpaceManagerRequest.builder()
                    .spaceId(spaceId)
                    .managerId(userId)
                    .build()))))
            .flatMap(function((userId, spaceId) -> requestListSpaceManagers(this.cloudFoundryClient, spaceId)
                .map(ResourceUtils::getEntity)
                .map(UserEntity::getUsername)
            ))
            .subscribe(this.testSubscriber());
    }

    @Test
    public void removeManagerByUsername() {
        String organizationName = this.nameFactory.getOrganizationName();
        String spaceName = this.nameFactory.getSpaceName();

        createUserIdAndSpaceId(this.cloudFoundryClient, organizationName, spaceName, this.username)
            .as(thenKeep(function((userId, spaceId) -> requestAssociateSpaceManager(this.cloudFoundryClient, spaceId, userId))))
            .as(thenKeep(function((userId, spaceId) -> this.cloudFoundryClient.spaces()
                .removeManagerByUsername(RemoveSpaceManagerByUsernameRequest.builder()
                    .spaceId(spaceId)
                    .username(this.username)
                    .build()))))
            .flatMap(function((userId, spaceId) -> requestListSpaceManagers(this.cloudFoundryClient, spaceId)
                .map(ResourceUtils::getEntity)
                .map(UserEntity::getUsername)))
            .subscribe(this.testSubscriber());
    }

    @Ignore("TODO: awaiting https://www.pivotaltracker.com/story/show/101522658")
    @Test
    public void removeSecurityGroup() {
        fail("TODO: awaiting https://www.pivotaltracker.com/story/show/101522658");
    }

    @Test
    public void update() {
        String spaceName = this.nameFactory.getSpaceName();
        String spaceName2 = this.nameFactory.getSpaceName();

        this.organizationId
            .then(organizationId -> createSpaceId(this.cloudFoundryClient, organizationId, spaceName))
            .as(thenKeep(spaceId -> this.cloudFoundryClient.spaces()
                .update(UpdateSpaceRequest.builder()
                    .spaceId(spaceId)
                    .name(spaceName2)
                    .build())))
            .then(spaceId -> this.cloudFoundryClient.spaces()
                .getSummary(GetSpaceSummaryRequest.builder()
                    .spaceId(spaceId)
                    .build()))
            .map(GetSpaceSummaryResponse::getName)
            .subscribe(this.<String>testSubscriber()
                .expectEquals(spaceName2));
    }

    @Test
    public void updateEmptyManagers() {
        String organizationName = this.nameFactory.getOrganizationName();
        String spaceName = this.nameFactory.getSpaceName();

        createUserIdAndSpaceId(this.cloudFoundryClient, organizationName, spaceName, this.username)
            .as(thenKeep(function((userId, spaceId) -> requestAssociateSpaceManager(this.cloudFoundryClient, spaceId, userId))))
            .as(thenKeep(function((userId, spaceId) -> this.cloudFoundryClient.spaces()
                .update(UpdateSpaceRequest.builder()
                    .spaceId(spaceId)
                    .managerIds(Collections.emptyList())
                    .build()))))
            .flatMap(function((userId, spaceId) -> requestListSpaceManagers(this.cloudFoundryClient, spaceId)
                .map(ResourceUtils::getEntity)
                .map(UserEntity::getUsername)
            ))
            .subscribe(this.testSubscriber());
    }

    private static Mono<String> createApplicationId(CloudFoundryClient cloudFoundryClient, String spaceId, String applicationName) {
        return requestCreateApplication(cloudFoundryClient, spaceId, applicationName)
            .map(ResourceUtils::getId);
    }

    private static Mono<String> createOrganizationId(CloudFoundryClient cloudFoundryClient, String organization) {
        return requestCreateOrganization(cloudFoundryClient, organization)
            .map(ResourceUtils::getId);
    }

    private static Mono<Tuple2<String, String>> createOrganizationIdAndSpaceId(CloudFoundryClient cloudFoundryClient, String organizationName, String spaceName) {
        return createOrganizationId(cloudFoundryClient, organizationName)
            .then(organizationId -> Mono.when(
                Mono.just(organizationId),
                createSpaceId(cloudFoundryClient, organizationId, spaceName)
            ));
    }

    private static Mono<String> createRouteId(CloudFoundryClient cloudFoundryClient, String spaceId, String domainName, String host, String path) {
        return getSpaceDomainId(cloudFoundryClient, spaceId, domainName)
            .then(domainId -> requestCreateRoute(cloudFoundryClient, spaceId, domainId, path, host))
            .map(ResourceUtils::getId);
    }

    private static Mono<String> createSpaceId(CloudFoundryClient cloudFoundryClient, String organizationId, String spaceName) {
        return cloudFoundryClient.spaces()
            .create(CreateSpaceRequest.builder()
                .organizationId(organizationId)
                .name(spaceName)
                .build())
            .map(ResourceUtils::getId);
    }

    private static Mono<String> createSpaceIdWithDomain(CloudFoundryClient cloudFoundryClient, String organizationId, String spaceName, String domainName) {
        return cloudFoundryClient.domains()
            .create(CreateDomainRequest.builder()
                .name(domainName)
                .wildcard(true)
                .build())
            .map(ResourceUtils::getId)
            .then(domainId -> cloudFoundryClient.spaces()
                .create(CreateSpaceRequest.builder()
                    .domainId(domainId)
                    .organizationId(organizationId)
                    .name(spaceName)
                    .build()))
            .map(ResourceUtils::getId);
    }

    // TODO: after: https://www.pivotaltracker.com/story/show/118387501 really create a new user
    private static Mono<String> createUserId(CloudFoundryClient cloudFoundryClient, String organizationId, String username) {
        return cloudFoundryClient.organizations()
            .associateUserByUsername(AssociateOrganizationUserByUsernameRequest.builder()
                .organizationId(organizationId)
                .username(username)
                .build())
            .then(PaginationUtils
                .requestClientV2Resources(page -> cloudFoundryClient.users()
                    .list(ListUsersRequest.builder()
                        .organizationId(organizationId)
                        .page(page)
                        .build()))
                .filter(resource -> ResourceUtils.getEntity(resource).getUsername().equals(username))
                .single()
                .map(ResourceUtils::getId)
            );
    }

    private static Mono<Tuple2<String, String>> createUserIdAndSpaceId(CloudFoundryClient cloudFoundryClient, String organizationName, String spaceName, String userName) {
        return createOrganizationId(cloudFoundryClient, organizationName)
            .then(organizationId -> Mono.when(
                createUserId(cloudFoundryClient, organizationId, userName),
                createSpaceId(cloudFoundryClient, organizationId, spaceName)
            ));
    }

    private static String getPastTimestamp() {
        Date past = Date.from(Instant.now().minus(1, HOURS));
        return DateUtils.formatToIso8601(past);
    }

    private static Mono<String> getSpaceDomainId(CloudFoundryClient cloudFoundryClient, String spaceId, String domainName) {
        return requestListSpaceDomains(cloudFoundryClient, spaceId, builder -> builder.name(domainName))
            .single()
            .map(ResourceUtils::getId);
    }

    private static Predicate<SpaceResource> hasOrganizationId(String organizationId) {
        return spaceResource -> ResourceUtils.getEntity(spaceResource).getOrganizationId().equals(organizationId);
    }

    private static Mono<AssociateSpaceAuditorResponse> requestAssociateSpaceAuditor(CloudFoundryClient cloudFoundryClient, String spaceId, String userId) {
        return cloudFoundryClient.spaces()
            .associateAuditor(AssociateSpaceAuditorRequest.builder()
                .spaceId(spaceId)
                .auditorId(userId)
                .build());
    }

    private static Mono<AssociateSpaceManagerResponse> requestAssociateSpaceManager(CloudFoundryClient cloudFoundryClient, String spaceId, String userId) {
        return cloudFoundryClient.spaces()
            .associateManager(AssociateSpaceManagerRequest.builder()
                .spaceId(spaceId)
                .managerId(userId)
                .build());
    }

    private static Mono<CreateApplicationResponse> requestCreateApplication(CloudFoundryClient cloudFoundryClient, String spaceId, String applicationName) {
        return cloudFoundryClient.applicationsV2()
            .create(CreateApplicationRequest.builder()
                .diego(true)
                .name(applicationName)
                .spaceId(spaceId)
                .build());
    }

    private static Mono<CreateOrganizationResponse> requestCreateOrganization(CloudFoundryClient cloudFoundryClient, String organization) {
        return cloudFoundryClient.organizations()
            .create(CreateOrganizationRequest.builder()
                .name(organization)
                .build());
    }

    private static Mono<CreateRouteResponse> requestCreateRoute(CloudFoundryClient cloudFoundryClient, String spaceId, String domainId, String path, String host) {
        return cloudFoundryClient.routes()
            .create(CreateRouteRequest.builder()
                .spaceId(spaceId)
                .domainId(domainId)
                .host(host)
                .path(path)
                .build());
    }

    private static Flux<ApplicationResource> requestListSpaceApplications(CloudFoundryClient cloudFoundryClient, String spaceId) {
        return requestListSpaceApplications(cloudFoundryClient, spaceId, UnaryOperator.identity());
    }

    private static Flux<ApplicationResource> requestListSpaceApplications(CloudFoundryClient cloudFoundryClient, String spaceId, UnaryOperator<ListSpaceApplicationsRequest.Builder> transformer) {
        return PaginationUtils
            .requestClientV2Resources(page -> cloudFoundryClient.spaces()
                .listApplications(transformer.apply(ListSpaceApplicationsRequest.builder())
                    .page(page)
                    .spaceId(spaceId)
                    .build()));
    }

    private static Flux<DomainResource> requestListSpaceDomains(CloudFoundryClient cloudFoundryClient, String spaceId) {
        return requestListSpaceDomains(cloudFoundryClient, spaceId, UnaryOperator.identity());
    }

    private static Flux<DomainResource> requestListSpaceDomains(CloudFoundryClient cloudFoundryClient, String spaceId, UnaryOperator<ListSpaceDomainsRequest.Builder> transformer) {
        return PaginationUtils
            .requestClientV2Resources(page -> cloudFoundryClient.spaces()
                .listDomains(transformer.apply(ListSpaceDomainsRequest.builder())
                    .spaceId(spaceId)
                    .page(page)
                    .build()));
    }

    private static Flux<EventResource> requestListSpaceEvents(CloudFoundryClient cloudFoundryClient, String spaceId) {
        return requestListSpaceEvents(cloudFoundryClient, spaceId, UnaryOperator.identity());
    }

    private static Flux<EventResource> requestListSpaceEvents(CloudFoundryClient cloudFoundryClient, String spaceId, UnaryOperator<ListSpaceEventsRequest.Builder> transformer) {
        return PaginationUtils
            .requestClientV2Resources(page -> cloudFoundryClient.spaces()
                .listEvents(transformer.apply(ListSpaceEventsRequest.builder())
                    .page(page)
                    .spaceId(spaceId)
                    .build()));
    }

    private static Flux<UserResource> requestListSpaceManagers(CloudFoundryClient cloudFoundryClient, String spaceId) {
        return requestListSpaceManagers(cloudFoundryClient, spaceId, Function.identity());
    }

    private static Flux<UserResource> requestListSpaceManagers(CloudFoundryClient cloudFoundryClient, String spaceId, Function<ListSpaceManagersRequest.Builder, ListSpaceManagersRequest.Builder>
        transformer) {
        return PaginationUtils
            .requestClientV2Resources(page -> cloudFoundryClient.spaces()
                .listManagers(transformer.apply(ListSpaceManagersRequest.builder())
                    .page(page)
                    .spaceId(spaceId)
                    .build()));
    }

    private static Flux<RouteResource> requestListSpaceRoutes(CloudFoundryClient cloudFoundryClient, String spaceId) {
        return requestListSpaceRoutes(cloudFoundryClient, spaceId, UnaryOperator.identity());
    }

    private static Flux<RouteResource> requestListSpaceRoutes(CloudFoundryClient cloudFoundryClient, String spaceId, UnaryOperator<ListSpaceRoutesRequest.Builder> transformer) {
        return PaginationUtils
            .requestClientV2Resources(page -> cloudFoundryClient.spaces()
                .listRoutes(transformer.apply(ListSpaceRoutesRequest.builder())
                    .spaceId(spaceId)
                    .build()));
    }

    private static Flux<SpaceResource> requestListSpaces(CloudFoundryClient cloudFoundryClient) {
        return requestListSpaces(cloudFoundryClient, UnaryOperator.identity());
    }

    private static Flux<SpaceResource> requestListSpaces(CloudFoundryClient cloudFoundryClient, UnaryOperator<ListSpacesRequest.Builder> transformer) {
        return PaginationUtils
            .requestClientV2Resources(page -> cloudFoundryClient.spaces()
                .list(transformer.apply(ListSpacesRequest.builder())
                    .page(page)
                    .build()));
    }

}
