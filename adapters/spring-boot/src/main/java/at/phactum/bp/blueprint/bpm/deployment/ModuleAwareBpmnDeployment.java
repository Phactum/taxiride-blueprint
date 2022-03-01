package at.phactum.bp.blueprint.bpm.deployment;

import static java.lang.String.format;

import java.io.IOException;
import java.util.List;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

public abstract class ModuleAwareBpmnDeployment<D> {

    protected abstract Logger getLogger();

    @Value("${spring.application.name}")
    protected String applicationName;

    @Autowired(required = false)
    private List<DeploymentListener<D>> deploymentListeners;

    private final String basePackageName;

    protected final String workflowModuleId;

    protected ModuleAwareBpmnDeployment() {
        
        this(null);
        
    }

    protected ModuleAwareBpmnDeployment(
            final String workflowModuleId) {
        
        this(workflowModuleId, "processes");
        
    }

    protected ModuleAwareBpmnDeployment(
            final String workflowModuleId,
            final String basePackageName) {

        if (workflowModuleId == null) {
            final var simpleName = getClass().getSimpleName();
            if (simpleName.endsWith("Deployment")) {
                this.workflowModuleId = simpleName.substring(0, simpleName.length() - 10);
            } else {
                throw new IllegalArgumentException("Either name deployment class according to "
                        + "'[ProcessArchiveId]Deployment' convention or instantiate with an explicit "
                        + "process archive name.");
            }
        } else {
            this.workflowModuleId = workflowModuleId;
        }

        this.basePackageName = basePackageName;

    }

    protected abstract D doDeployment(Resource[] bpmns, Resource[] dmns, Resource[] cmms) throws Exception;

    @PostConstruct
    public void deployOnStartup() throws Exception {

        try {

            final var bpmns = findResources(workflowModuleId, "*.bpmn");
            final var cmms = findResources(workflowModuleId, "*.cmmn");
            final var dmns = findResources(workflowModuleId, "*.dmn");

            final var deploymentDefinitions = doDeployment(bpmns, dmns, cmms);

            if (deploymentListeners != null) {
                deploymentListeners.forEach(listener -> listener.notify(workflowModuleId, deploymentDefinitions));
            }

            getLogger()
                    .info("Deployed resources for process archive <{}>: {}", workflowModuleId, deploymentDefinitions);

        } catch (Exception e) {

            getLogger().error(format("Could not deploy resources for process archive <%s>", workflowModuleId), e);
            throw e;

        }

    }

    private Resource[] findResources(
            final String processArchiveId,
            final String fileNamePattern) throws IOException {

        final var resourcesPath = format("%s%s/*/%s/**/%s",
                ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX,
                basePackageName.replace('.', '/'),
                processArchiveId.toLowerCase().replace("-", ""),
                fileNamePattern);

        getLogger()
                .debug("Scanning process archive <{}> for {}", processArchiveId, resourcesPath);

        return new PathMatchingResourcePatternResolver().getResources(resourcesPath);

    }

}
