package at.phactum.bp.blueprint.camunda8.adapter.test.springboot;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;

import at.phactum.bp.blueprint.camunda8.adapter.Camunda8AdapterConfiguration;
import at.phactum.bp.blueprint.camunda8.adapter.test.testcase.TestWorkflowDomainEntity;
import at.phactum.bp.blueprint.camunda8.adapter.test.testcase.TestWorkflowDomainEntityRepository;
import at.phactum.bp.blueprint.utilities.SpringDataTool;
import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.ZeebeClientBuilder;
import io.camunda.zeebe.client.api.ZeebeFuture;
import io.camunda.zeebe.client.api.command.DeployProcessCommandStep1;
import io.camunda.zeebe.client.api.command.DeployProcessCommandStep1.DeployProcessCommandBuilderStep2;
import io.camunda.zeebe.client.api.response.DeploymentEvent;
import io.camunda.zeebe.client.api.worker.JobHandler;
import io.camunda.zeebe.client.api.worker.JobWorker;
import io.camunda.zeebe.client.api.worker.JobWorkerBuilderStep1;
import io.camunda.zeebe.client.api.worker.JobWorkerBuilderStep1.JobWorkerBuilderStep2;
import io.camunda.zeebe.client.api.worker.JobWorkerBuilderStep1.JobWorkerBuilderStep3;
import io.camunda.zeebe.spring.client.ZeebeClientLifecycle;

@Configuration
@ComponentScan(basePackageClasses = { TestConfiguration.class })
@Import(Camunda8AdapterConfiguration.class)
public class TestConfiguration {

    @Bean
    @Primary
    public SpringDataTool mockedSpringDataTool(
            final TestWorkflowDomainEntityRepository testWorkflowDomainEntityRepository) {
        
        final var result = mock(SpringDataTool.class);
        
        when(result.getJpaRepository(eq(TestWorkflowDomainEntity.class)))
                .thenReturn(testWorkflowDomainEntityRepository);

        return result;
        
    }
    
    @Bean
    public TestWorkflowDomainEntityRepository testWorkflowDomainEntityRepository() {

        return mock(TestWorkflowDomainEntityRepository.class);

    }

    @Bean
    @Primary
    @SuppressWarnings("unchecked")
    public ZeebeClientLifecycle mockedZeebeClientLifecycle() {

        final var client = mock(ZeebeClientLifecycle.class);

        // mock "newDeployCommand"

        final var deploymentEvent = mock(DeploymentEvent.class);

        final var deploymentFuture = (ZeebeFuture<DeploymentEvent>) mock(ZeebeFuture.class);
        when(deploymentFuture.join()).thenReturn(deploymentEvent);

        final var command2 = mock(DeployProcessCommandBuilderStep2.class);
        when(command2.send()).thenReturn(deploymentFuture);

        final var command1 = mock(DeployProcessCommandStep1.class);
        when(command1.addProcessModel(any(), any())).thenReturn(command2);

        when(client.newDeployCommand()).thenReturn(command1);

        // mock "newWorker"

        final var jobWorker = mock(JobWorker.class);

        final var jobWorkerBuilder3 = mock(JobWorkerBuilderStep3.class);
        when(jobWorkerBuilder3.name(anyString())).thenReturn(jobWorkerBuilder3);
        when(jobWorkerBuilder3.fetchVariables(any(List.class))).thenReturn(jobWorkerBuilder3);
        when(jobWorkerBuilder3.open()).thenReturn(jobWorker);

        final var jobWorkerBuilder2 = mock(JobWorkerBuilderStep2.class);
        when(jobWorkerBuilder2.handler(any(JobHandler.class))).thenReturn(jobWorkerBuilder3);

        final var jobWorkerBuilder1 = mock(JobWorkerBuilderStep1.class);
        when(jobWorkerBuilder1.jobType(anyString())).thenReturn(jobWorkerBuilder2);

        when(client.newWorker()).thenReturn(jobWorkerBuilder1);

        return client;

    }

    @Bean
    public ZeebeClientBuilder zeebeClientBuilder(
            final ZeebeClient client) {

        final var result = mock(ZeebeClientBuilder.class);
        
        when(result.build()).thenReturn(client);
        
        return result;

    }

}
