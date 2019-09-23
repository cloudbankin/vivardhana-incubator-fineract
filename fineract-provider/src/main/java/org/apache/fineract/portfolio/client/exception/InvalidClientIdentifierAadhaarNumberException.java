package org.apache.fineract.portfolio.client.exception;

import org.apache.fineract.infrastructure.core.exception.AbstractPlatformDomainRuleException;

public class InvalidClientIdentifierAadhaarNumberException extends AbstractPlatformDomainRuleException
{
	
	private final String identifierType;

	public InvalidClientIdentifierAadhaarNumberException(final String identifierType)
	{
		super("Invalid Aadhaar number", "Invalid " + identifierType
                + " Number", identifierType);
        this.identifierType = identifierType;
	}
	
	public String getIdentifierType() {
		return identifierType;
	}
	
	

}
