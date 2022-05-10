package at.phactum.bp.blueprint.camunda8.adapter.deployment;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface DeployedBpmnRepository extends JpaRepository<DeployedBpmn, Long> {

    @Query("select distinct p.deployedResource from DeployedProcess p where not p.packageId = ?1")
    List<DeployedBpmn> findByPackageIdNot(int packageId);

}
