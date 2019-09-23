package org.finabile.portfolio.bankremittance.exception;

import org.apache.fineract.infrastructure.core.exception.AbstractPlatformDomainRuleException;

public class BankFormatMissingException extends AbstractPlatformDomainRuleException{

	public BankFormatMissingException(String globalisationMessageCode, String defaultUserMessage) {
		super(globalisationMessageCode, defaultUserMessage);
	}

}
