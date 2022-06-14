package at.phactum.bp.blueprint.camunda8.adapter;

import java.lang.reflect.Method;
import java.util.List;

import org.springframework.beans.factory.InjectionPoint;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.autoconfigure.AutoConfigurationPackage;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;

import at.phactum.bp.blueprint.bpm.deployment.AdapterConfigurationBase;
import at.phactum.bp.blueprint.bpm.deployment.parameters.MethodParameter;
import at.phactum.bp.blueprint.camunda8.adapter.deployment.Camunda8DeploymentAdapter;
import at.phactum.bp.blueprint.camunda8.adapter.deployment.DeployedBpmnRepository;
import at.phactum.bp.blueprint.camunda8.adapter.deployment.DeploymentRepository;
import at.phactum.bp.blueprint.camunda8.adapter.deployment.DeploymentResourceRepository;
import at.phactum.bp.blueprint.camunda8.adapter.deployment.DeploymentService;
import at.phactum.bp.blueprint.camunda8.adapter.service.Camunda8ProcessService;
import at.phactum.bp.blueprint.camunda8.adapter.wiring.Camunda8TaskHandler;
import at.phactum.bp.blueprint.camunda8.adapter.wiring.Camunda8TaskWiring;
import at.phactum.bp.blueprint.camunda8.adapter.wiring.Camunda8UserTaskHandler;
import at.phactum.bp.blueprint.utilities.SpringDataTool;
import io.camunda.zeebe.spring.client.EnableZeebeClient;
import io.camunda.zeebe.spring.client.ZeebeClientLifecycle;
import io.camunda.zeebe.spring.client.jobhandling.DefaultCommandExceptionHandlingStrategy;

@AutoConfigurationPackage(basePackageClasses = Camunda8AdapterConfiguration.class)
@EnableZeebeClient
public class Camunda8AdapterConfiguration extends AdapterConfigurationBase<Camunda8ProcessService<?>> {

    @Value("${workerId}")
    private String workerId;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private ZeebeClientLifecycle clientLifecycle;
    
    @Autowired
    private DefaultCommandExceptionHandlingStrategy commandExceptionHandlingStrategy;

    @Autowired
    private DeploymentRepository deploymentRepository;

    @Autowired
    private DeployedBpmnRepository deployedBpmnRepository;

    @Autowired
    private DeploymentResourceRepository deploymentResourceRepository;

    @Autowired
    private LocalContainerEntityManagerFactoryBean containerEntityManagerFactoryBean;

    @Bean
    public SpringDataTool springDataTool() {

        return new SpringDataTool(applicationContext, containerEntityManagerFactoryBean);

    }

    @Bean
    public Camunda8DeploymentAdapter camunda8Adapter(
            final DeploymentService deploymentService,
            final Camunda8TaskWiring camunda8TaskWiring) {

        return new Camunda8DeploymentAdapter(
                deploymentService,
                clientLifecycle,
                camunda8TaskWiring);

    }

    @Bean
    public Camunda8TaskWiring camunda8TaskWiring(
            final Camunda8UserTaskHandler userTaskHandler,
            final ObjectProvider<Camunda8TaskHandler> taskHandlers) {

        return new Camunda8TaskWiring(
                applicationContext,
                workerId,
                userTaskHandler,
                taskHandlers,
                getConnectableServices());

    }

    @Bean
    public DeploymentService deploymentService() {

        return new DeploymentService(
                deploymentRepository,
                deploymentResourceRepository,
                deployedBpmnRepository);

    }

    @Bean
    public Camunda8UserTaskHandler userTaskHandler() {

        return new Camunda8UserTaskHandler();
    }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public Camunda8TaskHandler camunda8TaskHandler(
            final JpaRepository<Object, String> repository,
            final String taskDefinition,
            final Object bean,
            final Method method,
            final List<MethodParameter> parameters) {
        
        return new Camunda8TaskHandler(
                deploymentService(),
                commandExceptionHandlingStrategy,
                repository,
                bean,
                method,
                parameters);
        
    }
    
    @SuppressWarnings("unchecked")
    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public <DE> Camunda8ProcessService<?> camundaProcessService(
            final SpringDataTool springDataTool,
            final InjectionPoint injectionPoint) throws Exception {

        return registerProcessService(
                springDataTool,
                injectionPoint,
                (workflowDomainEntityRepository, workflowDomainEntityClass) ->
                new Camunda8ProcessService<DE>(
                        (JpaRepository<DE, String>) workflowDomainEntityRepository,
                        domainEntity -> springDataTool.getDomainEntityId(domainEntity),
                        (Class<DE>) workflowDomainEntityClass)
            );

    }
    
}
