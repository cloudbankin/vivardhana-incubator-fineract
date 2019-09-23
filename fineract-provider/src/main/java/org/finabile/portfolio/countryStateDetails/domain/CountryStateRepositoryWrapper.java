package org.finabile.portfolio.countryStateDetails.domain;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CountryStateRepositoryWrapper {
	@Autowired
	private CountryStateRepository countryStateRepository;
	
	public void save(final CountryStateDetails entity) {
		this.countryStateRepository.save(entity);
	}

	public CountryStateDetails findOneWithNotFoundDetection(final Long id) {
		final CountryStateDetails entity = this.countryStateRepository.findBypincode(id);
		return entity;
	}
	

}
