package org.apache.fineract.portfolio.client.data;

import java.math.BigDecimal;
import java.util.Date;

public class LoanDetailData {
	
	private final long clientID;

	private final String loanAcc;

	private final String officeExtrenal;

	private final String loanType;
	
	private final long loanCycle;

	private final String loanExternal;

	private final String loanPurpose;

	private final String groupExternal;

	private final Date submittedDate;

	private final Date disbursedDate;

	private Date closedDate;
	
	private Date transactionDate;

	private final BigDecimal appliedAmount;

	private final BigDecimal approvedAmount;

	private final BigDecimal disbAmount;

	private final int installmentNumber;

	private final String termfrequency;
	
	private final BigDecimal outBalance;
	
	private final String clientExternal;
	
	private final String clientAadhaarNo;

	private final BigDecimal installmentAmount;
	private final BigDecimal overDueAmount;
	private final int daysOverDUe;
	
	private final String loanOfficername;

	public LoanDetailData(long clientID, String loanAcc, String officeExtrenal, String loanType, String loanExternal,
			String loanPurpose, String groupExternal, Date submittedDate, Date disbursedDate, Date closedDate,
			BigDecimal appliedAmount, BigDecimal approvedAmount, BigDecimal disbAmount, int installmentNumber,
			String termfrequency, BigDecimal outBalance, String clientExternal,String clientAadhaarNo, 
			BigDecimal installmentAmount, BigDecimal overDueAmount, int daysOverDUe, long loanCycle, Date transactionDate,String loanOfficername) {
		
		this.clientID = clientID;
		this.appliedAmount=appliedAmount;
		this.approvedAmount=approvedAmount;
		this.clientExternal=clientExternal;
		this.clientAadhaarNo = clientAadhaarNo;
		this.closedDate=closedDate;
		this.disbAmount=disbAmount;
		this.disbursedDate=disbursedDate;
		this.groupExternal=groupExternal;
		this.installmentAmount=installmentAmount;
		this.installmentNumber=installmentNumber;
		this.loanAcc=loanAcc;
		this.loanExternal=loanExternal;
		this.loanPurpose=loanPurpose;
		this.loanType=loanType;
		this.officeExtrenal=officeExtrenal;
		this.outBalance=outBalance;
		this.submittedDate=submittedDate;
		this.termfrequency=termfrequency;
		this.daysOverDUe=daysOverDUe;	
	    this.overDueAmount=overDueAmount;
	    this.loanCycle=loanCycle;
	    this.transactionDate = transactionDate;
	    this.loanOfficername = loanOfficername;
	}

	public BigDecimal getOverDueAmount() {
		return overDueAmount;
	}

	public int getDaysOverDUe() {
		return daysOverDUe;
	}

	public String getLoanAcc() {
		return loanAcc;
	}

	public String getOfficeExtrenal() {
		return officeExtrenal;
	}

	public String getLoanType() {
		return loanType;
	}

	public String getLoanExternal() {
		return loanExternal;
	}

	public String getLoanPurpose() {
		return loanPurpose;
	}

	public String getGroupExternal() {
		return groupExternal;
	}

	public Date getSubmittedDate() {
		return submittedDate;
	}

	public Date getDisbursedDate() {
		return disbursedDate;
	}

	public Date getClosedDate() {
		return closedDate;
	}

	public Date setClosedDate(Date closedDate) {
		return this.closedDate=closedDate;
	}
	
	public BigDecimal getAppliedAmount() {
		return appliedAmount;
	}

	public BigDecimal getApprovedAmount() {
		return approvedAmount;
	}

	public BigDecimal getDisbAmount() {
		return disbAmount;
	}

	public int getInstallmentNumber() {
		return installmentNumber;
	}

	public String getTermfrequency() {
		return termfrequency;
	}

	public BigDecimal getOutBalance() {
		return outBalance;
	}

	public String getClientExternal() {
		return clientExternal;
	}

	public String getClientAadhaarNo() {
		return clientAadhaarNo;
	}

	public BigDecimal getInstallmentAmount() {
		return installmentAmount;
	}

	public long getClientID() {
		return clientID;
	}

	public long getLoanCycle() {
		return loanCycle;
	}

	public Date getTransactionDate() {
		return transactionDate;
	}

	public void setTransactionDate(Date transactionDate) {
		this.transactionDate = transactionDate;
	}

	public String getLoanOfficername() {
		return loanOfficername;
	}

	
}