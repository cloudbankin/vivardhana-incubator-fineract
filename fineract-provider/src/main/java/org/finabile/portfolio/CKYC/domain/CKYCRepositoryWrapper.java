package org.finabile.portfolio.CKYC.domain;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CKYCRepositoryWrapper {
	@Autowired
	private CKYCRepository cKYCRepository;
	
	public void save(final CKYC entity) {
		this.cKYCRepository.save(entity);
	}

	public CKYC findOneWithNotFoundDetection(final Long id) {
		final CKYC entity = this.cKYCRepository.findByloan(id);
		return entity;
	}
	

}
