package at.phactum.bp.blueprint.bpm.deployment;

import org.springframework.data.jpa.repository.JpaRepository;

import at.phactum.bp.blueprint.process.ProcessService;

public interface ProcessServiceImplementation<DE> extends ProcessService<DE> {

    Class<DE> getWorkflowDomainEntityClass();

    JpaRepository<DE, String> getWorkflowDomainEntityRepository();

}
