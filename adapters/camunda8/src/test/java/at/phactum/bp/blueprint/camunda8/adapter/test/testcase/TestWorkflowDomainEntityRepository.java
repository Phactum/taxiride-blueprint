package at.phactum.bp.blueprint.camunda8.adapter.test.testcase;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TestWorkflowDomainEntityRepository extends JpaRepository<TestWorkflowDomainEntity, String> {

}
