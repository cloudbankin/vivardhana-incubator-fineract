package org.finabile.portfolio.bankremittance.exception;

import org.apache.fineract.infrastructure.core.exception.AbstractPlatformResourceNotFoundException;

public class BankRemittanceNotFoundException extends AbstractPlatformResourceNotFoundException{
	 public BankRemittanceNotFoundException(final Long id) {
	        super("error.msg.bankRemittance.id.invalid", "Bank Remittance with identifier " + id + " does not exist", id);
	    }

}

