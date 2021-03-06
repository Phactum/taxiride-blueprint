package at.phactum.bp.blueprint.camunda7.adapter;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.spring.application.SpringProcessApplication;
import org.camunda.bpm.spring.boot.starter.annotation.EnableProcessApplication;
import org.springframework.beans.factory.InjectionPoint;
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
import at.phactum.bp.blueprint.camunda7.adapter.deployment.Camunda7DeploymentAdapter;
import at.phactum.bp.blueprint.camunda7.adapter.service.Camunda7ProcessService;
import at.phactum.bp.blueprint.camunda7.adapter.wiring.Camunda7TaskWiring;
import at.phactum.bp.blueprint.camunda7.adapter.wiring.Camunda7TaskWiringPlugin;
import at.phactum.bp.blueprint.camunda7.adapter.wiring.Camunda7UserTaskEventHandler;
import at.phactum.bp.blueprint.camunda7.adapter.wiring.ProcessEntityAwareExpressionManager;
import at.phactum.bp.blueprint.camunda7.adapter.wiring.TaskWiringBpmnParseListener;
import at.phactum.bp.blueprint.utilities.SpringDataTool;

@AutoConfigurationPackage(basePackageClasses = Camunda7AdapterConfiguration.class)
@EnableProcessApplication("org.camunda.bpm.spring.boot.starter.SpringBootProcessApplication")
public class Camunda7AdapterConfiguration extends AdapterConfigurationBase<Camunda7ProcessService<?>> {

    @Value("${workerId}")
    private String workerId;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private RepositoryService repositoryService;

    @Bean
    public SpringDataTool springDataTool(
            final LocalContainerEntityManagerFactoryBean containerEntityManagerFactoryBean) {
        
        return new SpringDataTool(applicationContext, containerEntityManagerFactoryBean);
        
    }
    
    @Bean
    public Camunda7DeploymentAdapter camunda7DeploymentAdapter(
            final SpringProcessApplication processApplication,
            final ProcessEngine processEngine) {

        return new Camunda7DeploymentAdapter(processApplication, processEngine);

    }
    
    @Bean
    public Camunda7UserTaskEventHandler userTaskEventHandler() {
        
        return new Camunda7UserTaskEventHandler();
        
    }
    
    @Bean
    public Camunda7TaskWiring taskWiring(
            final ProcessEntityAwareExpressionManager processEntityAwareExpressionManager,
            final Camunda7UserTaskEventHandler userTaskEventHandler) {
        
        return new Camunda7TaskWiring(
                applicationContext,
                processEntityAwareExpressionManager,
                userTaskEventHandler,
                getConnectableServices());
        
    }
    
    @Bean
    public TaskWiringBpmnParseListener taskWiringBpmnParseListener(
            final Camunda7TaskWiring taskWiring,
            final Camunda7UserTaskEventHandler userTaskEventHandler) {
        
        return new TaskWiringBpmnParseListener(
                taskWiring,
                userTaskEventHandler);
        
    }
    
    @Bean
    public ProcessEntityAwareExpressionManager processEntityAwareExpressionManager() {

        return new ProcessEntityAwareExpressionManager(
                applicationContext,
                getConnectableServices());

    }

    @Bean
    public Camunda7TaskWiringPlugin taskWiringCamundaPlugin(
            final ProcessEntityAwareExpressionManager processEntityAwareExpressionManager,
            final TaskWiringBpmnParseListener taskWiringBpmnParseListener) {
        
        return new Camunda7TaskWiringPlugin(
                processEntityAwareExpressionManager,
                taskWiringBpmnParseListener);
        
    }
    
    @SuppressWarnings("unchecked")
    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public <DE> Camunda7ProcessService<?> camundaProcessService(
            final SpringDataTool springDataTool,
            final InjectionPoint injectionPoint) throws Exception {

        return registerProcessService(
                springDataTool,
                injectionPoint,
                (workflowDomainEntityRepository, workflowDomainEntityClass) ->
                new Camunda7ProcessService<DE>(
                        runtimeService,
                        taskService,
                        repositoryService,
                        domainEntity -> springDataTool.getDomainEntityId(domainEntity),
                        (JpaRepository<DE, String>) workflowDomainEntityRepository,
                        (Class<DE>) workflowDomainEntityClass)
            );

    }

}
