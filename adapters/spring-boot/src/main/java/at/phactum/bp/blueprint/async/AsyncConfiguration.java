package at.phactum.bp.blueprint.async;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.aop.interceptor.SimpleAsyncUncaughtExceptionHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

/**
 * Configuration of task executor(s) for asynchronous processing.
 */
@EnableAsync
@EnableScheduling
@ConditionalOnBean(AsyncPropertiesAware.class)
public class AsyncConfiguration implements AsyncConfigurer, SchedulingConfigurer {

    private final Logger log = LoggerFactory.getLogger(AsyncConfiguration.class);

    @Autowired
    private AsyncPropertiesAware properties;

    /**
     * Producer of the task executor used by annotation @Async.
     */
    @Override
    @Bean(name = "taskExecutor")
    public Executor getAsyncExecutor() {
        
        log.debug("Creating Async Task Executor");
        final var executor = new ExceptionHandlingAsyncTaskExecutor();
        executor.setCorePoolSize(properties.getAsync().getCorePoolSize());
        executor.setMaxPoolSize(properties.getAsync().getMaxPoolSize());
        executor.setQueueCapacity(properties.getAsync().getQueueCapacity());
        executor.setThreadNamePrefix("AsyncExecutor-");
        executor.setThreadGroupName("AsyncExecutor");
        executor.initialize();
        return executor;
        
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        
        return new SimpleAsyncUncaughtExceptionHandler();
        
    }

    @Override
    public void configureTasks(
            final ScheduledTaskRegistrar taskRegistrar) {
        
        taskRegistrar.setScheduler(scheduledTaskExecutor());
        
    }

    /**
     * Producer of the executor used for scheduled tasks.
     */
    @Bean
    public ScheduledExecutorService scheduledTaskExecutor() {
        
        return Executors.newScheduledThreadPool(
                properties.getAsync().getCorePoolSize());
        
    }

    @Bean(name = "taskScheduler")
    public TaskScheduler threadPoolTaskScheduler() {
        
        return new ConcurrentTaskScheduler(
                getAsyncExecutor(), scheduledTaskExecutor());
        
    }

}
