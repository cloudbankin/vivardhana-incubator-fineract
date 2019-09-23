package org.finabile.portfolio.bankremittance.exception;

import org.apache.fineract.infrastructure.core.exception.AbstractPlatformDomainRuleException;

public class loanAlreadyMarkAnotherBankRemittanceException extends AbstractPlatformDomainRuleException{
	 public loanAlreadyMarkAnotherBankRemittanceException(final String bankName,final String loan) {
	        super("error.msg.bankRemittance.bank.already.added", "Bank Remittance with identifier " + bankName + "  exist"+"and the loan accountNumber is "+loan, bankName);
	    }

}
