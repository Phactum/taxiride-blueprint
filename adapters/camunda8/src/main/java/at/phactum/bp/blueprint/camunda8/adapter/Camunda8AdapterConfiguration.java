package at.phactum.bp.blueprint.camunda8.adapter;

import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

import org.springframework.beans.factory.InjectionPoint;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.core.ResolvableType;
import org.springframework.data.jpa.repository.JpaRepository;

import at.phactum.bp.blueprint.camunda8.adapter.deployment.Camunda8DeploymentAdapter;
import at.phactum.bp.blueprint.camunda8.adapter.service.Camunda8ProcessService;
import at.phactum.bp.blueprint.camunda8.adapter.wiring.Camunda8TaskHandler;
import at.phactum.bp.blueprint.camunda8.adapter.wiring.Camunda8TaskWiring;
import at.phactum.bp.blueprint.domain.WorkflowDomainEntity;
import at.phactum.bp.blueprint.utilities.SpringDataTool;
import io.camunda.zeebe.spring.client.EnableZeebeClient;
import io.camunda.zeebe.spring.client.ZeebeClientLifecycle;
import io.camunda.zeebe.spring.client.jobhandling.DefaultCommandExceptionHandlingStrategy;

@Configuration
@EnableZeebeClient
public class Camunda8AdapterConfiguration {

    private List<Camunda8ProcessService<?>> connectableServices = new LinkedList<>();
    
    @Value("${workerId}")
    private String workerId;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private ZeebeClientLifecycle clientLifecycle;
    
    @Autowired
    private DefaultCommandExceptionHandlingStrategy commandExceptionHandlingStrategy;

    @Bean
    public SpringDataTool springDataTool() {

        return new SpringDataTool(applicationContext);

    }

    @Bean
    public Camunda8DeploymentAdapter camunda8Adapter(
            final Camunda8TaskWiring camunda8TaskWiring) {

        return new Camunda8DeploymentAdapter(
                clientLifecycle,
                camunda8TaskWiring);

    }

    @Bean
    public Camunda8TaskWiring camunda8TaskWiring(
            final ObjectProvider<Camunda8TaskHandler> taskHandlers) {

        return new Camunda8TaskWiring(
                applicationContext,
                workerId,
                taskHandlers,
                connectableServices);

    }

    @SuppressWarnings("unchecked")
    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public <DE extends WorkflowDomainEntity> Camunda8ProcessService<DE> camunda8ProcessService(
            final SpringDataTool springDataTool,
            final InjectionPoint injectionPoint) throws Exception {

        final var resolvableType = ResolvableType.forField(injectionPoint.getField());

        final var workflowDomainEntityClass = (Class<DE>) resolvableType
                .getGeneric(0)
                .resolve();
        
        final var workflowDomainEntityRepository = springDataTool
                .getJpaRepository(workflowDomainEntityClass);
        
        
        final var result = new Camunda8ProcessService<DE>(
                (JpaRepository<DE, String>) workflowDomainEntityRepository,
                workflowDomainEntityClass);

        connectableServices.add(result);

        return result;
        
    }
    
    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public Camunda8TaskHandler camunda8TaskHandler(
            final String taskDefinition,
            final Object bean,
            final Method method) {
        
        return new Camunda8TaskHandler(
                commandExceptionHandlingStrategy,
                bean,
                method);
        
    }

}
