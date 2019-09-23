package org.finabile.portfolio.bankremittance.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BankRemittanceRepository extends JpaRepository<BankRemittanceDetails, Long>, JpaSpecificationExecutor<BankRemittanceDetails>{
	@Query("select brd from BankRemittanceDetails brd left join brd.loan l where l.id = :id")
	BankRemittanceDetails findByloan(@Param("id")Long id);

}
