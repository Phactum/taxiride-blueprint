package org.blueprint.bp.blueprint.test1;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface Test1DomainEntityRepository extends JpaRepository<Test1DomainEntity, String> {

}
