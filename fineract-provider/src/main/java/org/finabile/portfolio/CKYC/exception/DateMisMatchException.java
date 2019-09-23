package org.finabile.portfolio.CKYC.exception;

import org.apache.fineract.infrastructure.core.exception.AbstractPlatformDomainRuleException;

public class DateMisMatchException extends AbstractPlatformDomainRuleException{
	 public DateMisMatchException(final String globalException,final String developerDetails) {
	        super(globalException,developerDetails);
	    }

}
