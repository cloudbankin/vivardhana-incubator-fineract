package org.apache.fineract.portfolio.client.exception;

import org.apache.fineract.infrastructure.core.exception.AbstractPlatformDomainRuleException;

public class AadhaarNumberInvalidException extends AbstractPlatformDomainRuleException
{
	private final String AadhaarNo;
	
	public AadhaarNumberInvalidException(final String AadhaarNo)
	{
		super("Invalid aadhaar number ", "Invalid " + AadhaarNo
                + " Number", AadhaarNo);
        this.AadhaarNo = AadhaarNo;
	}

	public String getAadhaarNo() {
		return AadhaarNo;
	}
	
	
}
