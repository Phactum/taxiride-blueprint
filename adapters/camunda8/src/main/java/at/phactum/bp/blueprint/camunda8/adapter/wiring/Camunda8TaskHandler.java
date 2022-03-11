package at.phactum.bp.blueprint.camunda8.adapter.wiring;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.Map;

import org.springframework.data.jpa.repository.JpaRepository;

import io.camunda.zeebe.client.api.command.CompleteJobCommandStep1;
import io.camunda.zeebe.client.api.command.FinalCommandStep;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.client.api.worker.JobHandler;
import io.camunda.zeebe.spring.client.exception.ZeebeBpmnError;
import io.camunda.zeebe.spring.client.jobhandling.CommandWrapper;
import io.camunda.zeebe.spring.client.jobhandling.DefaultCommandExceptionHandlingStrategy;

public class Camunda8TaskHandler implements JobHandler {

    private final DefaultCommandExceptionHandlingStrategy commandExceptionHandlingStrategy;

    private final JpaRepository<?, String> workflowDomainEntityRepository;

    private final Object bean;

    private final Method method;

    public Camunda8TaskHandler(
            final DefaultCommandExceptionHandlingStrategy commandExceptionHandlingStrategy,
            final JpaRepository<?, String> workflowDomainEntityRepository,
            final Object bean,
            final Method method) {

        this.commandExceptionHandlingStrategy = commandExceptionHandlingStrategy;
        this.workflowDomainEntityRepository = workflowDomainEntityRepository;
        this.bean = bean;
        this.method = method;

    }

    @Override
    public void handle(final JobClient client, final ActivatedJob job) throws Exception {

        CommandWrapper command;
        try {
            Object result = method.invoke(bean);

            command = new CommandWrapper(createCompleteCommand(client, job, result), job,
                    commandExceptionHandlingStrategy);
        } catch (ZeebeBpmnError bpmnError) {
            command = new CommandWrapper(createThrowErrorCommand(client, job, bpmnError), job,
                    commandExceptionHandlingStrategy);
        }
        command.executeAsync();

    }

    public FinalCommandStep createCompleteCommand(JobClient jobClient, ActivatedJob job, Object result) {

        CompleteJobCommandStep1 completeCommand = jobClient.newCompleteCommand(job.getKey());
        if (result != null) {
            if (result.getClass().isAssignableFrom(Map.class)) {
                completeCommand = completeCommand.variables((Map) result);
            } else if (result.getClass().isAssignableFrom(String.class)) {
                completeCommand = completeCommand.variables((String) result);
            } else if (result.getClass().isAssignableFrom(InputStream.class)) {
                completeCommand = completeCommand.variables((InputStream) result);
            } else {
                completeCommand = completeCommand.variables(result);
            }
        }
        return completeCommand;

    }

    private FinalCommandStep<Void> createThrowErrorCommand(JobClient jobClient, ActivatedJob job,
            ZeebeBpmnError bpmnError) {

        FinalCommandStep<Void> command = jobClient.newThrowErrorCommand(job.getKey()) // TODO: PR for taking a job only
                                                                                      // in command chain
                .errorCode(bpmnError.getErrorCode()).errorMessage(bpmnError.getErrorMessage());
        return command;

    }

}
