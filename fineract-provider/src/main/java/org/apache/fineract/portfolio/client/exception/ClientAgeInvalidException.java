package org.apache.fineract.portfolio.client.exception;

import java.util.Date;

import org.apache.fineract.infrastructure.core.exception.AbstractPlatformDomainRuleException;

public class ClientAgeInvalidException extends AbstractPlatformDomainRuleException
{
	private final Date identifierType;

	public ClientAgeInvalidException(final Date identifierType)
	{
		super("Invalid Age. Age must be between 18 to 57", "Invalid " + identifierType
                + " Number", identifierType);
        this.identifierType = identifierType;
	}

	public Date getIdentifierType() {
		return identifierType;
	}
	
}
