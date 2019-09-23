package org.finabile.portfolio.countryStateDetails.domain;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CountryStateRepository extends JpaRepository<CountryStateDetails, Long>, JpaSpecificationExecutor<CountryStateDetails>{
	@Query("select p from CountryStateDetails p where p.pincode = :id")
	CountryStateDetails findBypincode(@Param("id")Long id);
}
