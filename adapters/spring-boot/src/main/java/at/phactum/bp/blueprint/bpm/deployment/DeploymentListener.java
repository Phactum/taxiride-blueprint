package at.phactum.bp.blueprint.bpm.deployment;

/**
 * @param <D> BPMS-specific deployment information
 */
public interface DeploymentListener<D> {

    void notify(String moduleId, D deployment);

}
