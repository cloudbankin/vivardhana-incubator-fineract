package org.finabile.portfolio.bankremittance.domain;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.apache.fineract.infrastructure.codes.domain.CodeValue;
import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.springframework.stereotype.Component;

@Entity
@Table(name = "hab_bank_remittance_generator_details")
public class BankRemittanceDetails extends AbstractPersistableCustom<Long> {

	 @ManyToOne
	    @JoinColumn(name = "loan_id", nullable = true)
	    private Loan loan;
	 
	 @ManyToOne
	    @JoinColumn(name = "bank_id", nullable = true)
	    private CodeValue bankDetails;

	public BankRemittanceDetails(Loan loan, CodeValue codeValueBAnkDetails) {

	this.loan = loan;
	this.bankDetails = codeValueBAnkDetails;
	}

	public CodeValue getBankDetails() {
		return bankDetails;
	}

	public void setBankDetails(CodeValue bankDetails) {
		this.bankDetails = bankDetails;
	}

	public static BankRemittanceDetails addingRefference(Loan loan, CodeValue codeValueBAnkDetails) {

		
		return new BankRemittanceDetails(loan,codeValueBAnkDetails);
	}

	public Loan getLoan() {
		return loan;
	}

	public void setLoan(Loan loan) {
		this.loan = loan;
	}
}
