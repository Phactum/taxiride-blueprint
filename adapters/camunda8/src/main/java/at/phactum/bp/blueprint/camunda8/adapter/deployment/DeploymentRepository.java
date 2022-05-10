package at.phactum.bp.blueprint.camunda8.adapter.deployment;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DeploymentRepository extends JpaRepository<Deployment, Long> {

    List<DeployedProcess> findByPackageIdNot(int packageId);

}
