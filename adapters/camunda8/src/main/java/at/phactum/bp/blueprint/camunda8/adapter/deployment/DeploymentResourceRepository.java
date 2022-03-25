package at.phactum.bp.blueprint.camunda8.adapter.deployment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DeploymentResourceRepository extends JpaRepository<DeploymentResource, Integer> {

}
