package at.phactum.bp.blueprint.camunda7.adapter;

import at.phactum.bp.blueprint.bpm.deployment.AdapterConfigurationBase;
import at.phactum.bp.blueprint.camunda7.adapter.cockpit.WakeupFilter;
import at.phactum.bp.blueprint.camunda7.adapter.deployment.Camunda7DeploymentAdapter;
import at.phactum.bp.blueprint.camunda7.adapter.jobexecutor.BlueprintJobExecutor;
import at.phactum.bp.blueprint.camunda7.adapter.service.Camunda7ProcessService;
import at.phactum.bp.blueprint.camunda7.adapter.service.WakupJobExecutorService;
import at.phactum.bp.blueprint.camunda7.adapter.wiring.Camunda7TaskWiring;
import at.phactum.bp.blueprint.camunda7.adapter.wiring.Camunda7TaskWiringPlugin;
import at.phactum.bp.blueprint.camunda7.adapter.wiring.Camunda7UserTaskEventHandler;
import at.phactum.bp.blueprint.camunda7.adapter.wiring.ProcessEntityAwareExpressionManager;
import at.phactum.bp.blueprint.camunda7.adapter.wiring.TaskWiringBpmnParseListener;
import at.phactum.bp.blueprint.utilities.SpringDataTool;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.impl.jobexecutor.JobExecutor;
import org.camunda.bpm.engine.impl.jobexecutor.NotifyAcquisitionRejectedJobsHandler;
import org.camunda.bpm.engine.spring.application.SpringProcessApplication;
import org.camunda.bpm.engine.spring.components.jobexecutor.SpringJobExecutor;
import org.camunda.bpm.spring.boot.starter.annotation.EnableProcessApplication;
import org.camunda.bpm.spring.boot.starter.configuration.impl.DefaultJobConfiguration.JobConfiguration;
import org.camunda.bpm.spring.boot.starter.property.CamundaBpmProperties;
import org.camunda.bpm.spring.boot.starter.property.JobExecutionProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InjectionPoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.autoconfigure.AutoConfigurationPackage;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.core.annotation.Order;
import org.springframework.core.task.TaskExecutor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.scheduling.TaskScheduler;

import java.util.Optional;

@AutoConfigurationPackage(basePackageClasses = Camunda7AdapterConfiguration.class)
@EnableProcessApplication("org.camunda.bpm.spring.boot.starter.SpringBootProcessApplication")
public class Camunda7AdapterConfiguration extends AdapterConfigurationBase<Camunda7ProcessService<?>> {

    private static final Logger logger = LoggerFactory.getLogger(Camunda7AdapterConfiguration.class);
    
    @Value("${workerId}")
    private String workerId;

    @Value("${camunda.bpm.webapp.application-path:/camunda}")
    private String camundaWebAppBaseUrl;

    @Autowired
    private ApplicationContext applicationContext;

    @Bean
    @Order(-1)
    @ConditionalOnProperty(
            prefix = "camunda.bpm.job-execution",
            name = "blueprint",
            havingValue = "true",
            matchIfMissing = true)
    public static JobExecutor jobExecutor(
            @Qualifier(JobConfiguration.CAMUNDA_TASK_EXECUTOR_QUALIFIER) final TaskExecutor taskExecutor,
            CamundaBpmProperties properties) {
        
        logger.info("Blueprint's job-executor is using jobExecutorPreferTimerJobs=true and jobExecutorAcquireByDueDate=true. Please add DB-index according to https://docs.camunda.org/manual/7.6/user-guide/process-engine/the-job-executor/#the-job-order-of-job-acquisition");
        
        final SpringJobExecutor springJobExecutor = new BlueprintJobExecutor();
        springJobExecutor.setTaskExecutor(taskExecutor);
        springJobExecutor.setRejectedJobsHandler(new NotifyAcquisitionRejectedJobsHandler());

        JobExecutionProperty jobExecution = properties.getJobExecution();
        Optional.ofNullable(jobExecution.getLockTimeInMillis()).ifPresent(springJobExecutor::setLockTimeInMillis);
        Optional.ofNullable(jobExecution.getMaxJobsPerAcquisition()).ifPresent(springJobExecutor::setMaxJobsPerAcquisition);
        Optional.ofNullable(jobExecution.getWaitTimeInMillis()).ifPresent(springJobExecutor::setWaitTimeInMillis);
        Optional.ofNullable(jobExecution.getMaxWait()).ifPresent(springJobExecutor::setMaxWait);
        Optional.ofNullable(jobExecution.getBackoffTimeInMillis()).ifPresent(springJobExecutor::setBackoffTimeInMillis);
        Optional.ofNullable(jobExecution.getMaxBackoff()).ifPresent(springJobExecutor::setMaxBackoff);
        Optional.ofNullable(jobExecution.getBackoffDecreaseThreshold()).ifPresent(springJobExecutor::setBackoffDecreaseThreshold);
        Optional.ofNullable(jobExecution.getWaitIncreaseFactor()).ifPresent(springJobExecutor::setWaitIncreaseFactor);

        return springJobExecutor;
        
    }
    
    @Bean
    public SpringDataTool springDataTool(
            final LocalContainerEntityManagerFactoryBean containerEntityManagerFactoryBean) {
        
        return new SpringDataTool(applicationContext, containerEntityManagerFactoryBean);
        
    }
    
    @Bean
    public Camunda7DeploymentAdapter camunda7DeploymentAdapter(
            final SpringProcessApplication processApplication,
            final ProcessEngine processEngine,
            final Camunda7TaskWiring taskWiring) {

        return new Camunda7DeploymentAdapter(
                processApplication,
                taskWiring,
                processEngine);

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
            final ApplicationEventPublisher applicationEventPublisher,
            final ProcessEngine processEngine,
            final SpringDataTool springDataTool,
            final InjectionPoint injectionPoint) throws Exception {

        return registerProcessService(
                springDataTool,
                injectionPoint,
                (workflowDomainEntityRepository, workflowDomainEntityClass) ->
                new Camunda7ProcessService<DE>(
                        applicationEventPublisher,
                        processEngine,
                        domainEntity -> springDataTool.getDomainEntityId(domainEntity),
                        (JpaRepository<DE, String>) workflowDomainEntityRepository,
                        (Class<DE>) workflowDomainEntityClass)
            );

    }
    
    @Bean
    public WakupJobExecutorService wakupJobExecutorService(
            final ProcessEngine processEngine) {
        
        return new WakupJobExecutorService(processEngine);
        
    }

    @Bean
    @ConditionalOnProperty(
            prefix = "camunda.bpm.job-execution",
            name = "blueprint",
            havingValue = "true",
            matchIfMissing = true)
    public FilterRegistrationBean<WakeupFilter> wakeupFilterForCockpit(
            final ApplicationEventPublisher applicationEventPublisher,
            final TaskScheduler taskScheduler) {

        final var registrationBean = new FilterRegistrationBean<WakeupFilter>();

        registrationBean.setFilter(
                new WakeupFilter(
                        applicationEventPublisher,
                        taskScheduler));
        registrationBean.addUrlPatterns(camundaWebAppBaseUrl + "/api/*");
        registrationBean.setOrder(-1);

        return registrationBean;

    }

    @Bean
    @ConditionalOnProperty(
            prefix = "camunda.bpm.job-execution",
            name = "blueprint",
            havingValue = "true",
            matchIfMissing = true)
    public FilterRegistrationBean<WakeupFilter> wakeupFilterForRestApi(
            final ApplicationEventPublisher applicationEventPublisher,
            final TaskScheduler taskScheduler) {

        final var registrationBean = new FilterRegistrationBean<WakeupFilter>();

        registrationBean.setFilter(
                new WakeupFilter(
                        applicationEventPublisher,
                        taskScheduler));
        registrationBean.addUrlPatterns("/engine-rest/*");
        registrationBean.setOrder(-1);

        return registrationBean;

    }
    
}
