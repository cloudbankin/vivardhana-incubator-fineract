package org.apache.fineract.infrastructure.core.exception;
import org.apache.fineract.infrastructure.core.exception.AbstractPlatformDomainRuleException;



	public class endDateException extends AbstractPlatformDomainRuleException
	{

		public endDateException()
		{
			super("error.validation.collectionsheet1","Date end date should not be before the current Date");
		}

		
		
	}



