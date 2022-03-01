package at.phactum.bp.blueprint.camunda8.adapter;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import org.springframework.core.io.Resource;

import at.phactum.bp.blueprint.bpm.deployment.ModuleAwareBpmnDeployment;
import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.Process;

public abstract class Camunda8DeploymentAdapter extends ModuleAwareBpmnDeployment<List<Process>>
        implements Consumer<ZeebeClient> {

    private ZeebeClient client;

    @Override
    public void accept(final ZeebeClient client) {

        this.client = client;

    }

    @Override
    protected List<Process> doDeployment(
            final Resource[] bpmns,
            final Resource[] dmns,
            final Resource[] cmms) throws Exception {

        final var deployProcessCommand = client.newDeployCommand();

        final var deploymentResult = Arrays
                .stream(bpmns)
                .map(resource -> {
                    try (InputStream inputStream = resource.getInputStream()) {
                        return deployProcessCommand.addResourceStream(inputStream, resource.getFilename());
                    } catch (IOException e) {
                        throw new RuntimeException(e.getMessage());
                    }
                })
                .filter(Objects::nonNull)
                .reduce((first, second) -> second)
                .map(command -> command.send().join().getProcesses())
                .orElseGet(() -> List.of());

        return deploymentResult;

    }

}
