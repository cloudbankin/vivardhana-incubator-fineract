package org.finabile.portfolio.bankremittance.domain;

import org.finabile.portfolio.bankremittance.exception.BankRemittanceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BankRemittanceRepositoryWrapper {
	@Autowired
	private BankRemittanceRepository bankRemittanceRepository;

	/*
	 * @Autowired public BankRemittanceRepositoryWrapper(final
	 * BankRemittanceRepository bankRemittanceRepository) {
	 * this.bankRemittanceRepository = bankRemittanceRepository; }
	 */

	public void save(final BankRemittanceDetails entity) {
		this.bankRemittanceRepository.save(entity);
	}

	public BankRemittanceDetails findOneWithNotFoundDetection(final Long id) {
		final BankRemittanceDetails entity = this.bankRemittanceRepository.findByloan(id);
		return entity;
	}
}
