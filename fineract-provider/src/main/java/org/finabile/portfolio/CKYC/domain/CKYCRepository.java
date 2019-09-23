package org.finabile.portfolio.CKYC.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CKYCRepository extends JpaRepository<CKYC, Long>, JpaSpecificationExecutor<CKYC>{
	@Query("select brd from CKYC brd left join brd.client c where c.id = :id")
	CKYC findByloan(@Param("id")Long id);
}
