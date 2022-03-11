package at.phactum.bp.blueprint.camunda7.adapter;

import java.util.LinkedList;
import java.util.List;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.spring.boot.starter.annotation.EnableProcessApplication;
import org.springframework.beans.factory.InjectionPoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.core.ResolvableType;
import org.springframework.data.jpa.repository.JpaRepository;

import at.phactum.bp.blueprint.camunda7.adapter.deployment.Camunda7DeploymentAdapter;
import at.phactum.bp.blueprint.camunda7.adapter.service.Camunda7ProcessService;
import at.phactum.bp.blueprint.camunda7.adapter.wiring.Camunda7TaskWiring;
import at.phactum.bp.blueprint.camunda7.adapter.wiring.Camunda7TaskWiringPlugin;
import at.phactum.bp.blueprint.camunda7.adapter.wiring.ProcessEntityAwareExpressionManager;
import at.phactum.bp.blueprint.camunda7.adapter.wiring.TaskWiringBpmnParseListener;
import at.phactum.bp.blueprint.domain.WorkflowDomainEntity;
import at.phactum.bp.blueprint.utilities.SpringDataTool;

@Configuration
@EnableProcessApplication
public class Camunda7AdapterConfiguration {

    private List<Camunda7ProcessService<?>> connectableServices = new LinkedList<>();

    @Value("${workerId}")
    private String workerId;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private ProcessEngine processEngine;
    
    @Autowired
    private RuntimeService runtimeService;

    @Bean
    public SpringDataTool springDataTool() {
        
        return new SpringDataTool(applicationContext);
        
    }
    
    @Bean
    public Camunda7DeploymentAdapter camunda7DeploymentAdapter() {

        return new Camunda7DeploymentAdapter(processEngine);

    }
    
    @Bean
    public Camunda7TaskWiring taskWiring(
            final ProcessEntityAwareExpressionManager processEntityAwareExpressionManager) {
        
        return new Camunda7TaskWiring(
                applicationContext,
                processEntityAwareExpressionManager,
                connectableServices);
        
    }
    
    @Bean
    public TaskWiringBpmnParseListener taskWiringBpmnParseListener(
            final Camunda7TaskWiring taskWiring) {
        
        return new TaskWiringBpmnParseListener(taskWiring);
        
    }
    
    @Bean
    public ProcessEntityAwareExpressionManager processEntityAwareExpressionManager() {

        return new ProcessEntityAwareExpressionManager(applicationContext);

    }

    @Bean
    public Camunda7TaskWiringPlugin taskWiringCamundaPlugin(
            final ProcessEntityAwareExpressionManager processEntityAwareExpressionManager,
            final TaskWiringBpmnParseListener taskWiringBpmnParseListener) {
        
        return new Camunda7TaskWiringPlugin(
                processEntityAwareExpressionManager,
                taskWiringBpmnParseListener);
        
    }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public <DE extends WorkflowDomainEntity> Camunda7ProcessService<DE> camunda7ProcessService(
            final SpringDataTool springDataTool,
            final InjectionPoint injectionPoint) throws Exception {

        final var resolvableType = ResolvableType.forField(injectionPoint.getField());

        @SuppressWarnings("unchecked")
        final var workflowDomainEntityClass = (Class<DE>) resolvableType
                .getGeneric(0)
                .resolve();

        final var workflowDomainEntityRepository = springDataTool
                .getJpaRepository(workflowDomainEntityClass);

        @SuppressWarnings("unchecked")
        final var result = new Camunda7ProcessService<DE>(
                runtimeService,
                (JpaRepository<DE, String>) workflowDomainEntityRepository,
                workflowDomainEntityClass);

        connectableServices.add(result);

        return result;

    }

}
