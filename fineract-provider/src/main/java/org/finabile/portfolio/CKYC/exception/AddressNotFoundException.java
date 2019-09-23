package org.finabile.portfolio.CKYC.exception;

import org.apache.fineract.infrastructure.core.exception.AbstractPlatformDomainRuleException;

public class AddressNotFoundException extends AbstractPlatformDomainRuleException{

	public AddressNotFoundException(String globalisationMessageCode, String defaultUserMessage,
			Object[] defaultUserMessageArgs) {
		super(globalisationMessageCode, defaultUserMessage, defaultUserMessageArgs);
	}
	

}
