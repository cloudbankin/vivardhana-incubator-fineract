package org.finabile.portfolio.bankremittance.data;

import java.math.BigDecimal;
import java.util.Collection;

import org.apache.fineract.infrastructure.codes.data.CodeValueData;

public class BankRemittanceData {

	private final Collection<CodeValueData> bankOptions;

	//
	private final String accountNumber;
	private final String clientFullName;
	private final String accountHolderName;
	private final BigDecimal principal;
	private final String bankAccountnumber;
	private final String groupDisplayName;
	private final String groupId;
	private final String iFSCCode;
	private final String bankName;
	private final String address;
	private final String officeName;
	private final long id;
	private final long bankId;
	private final String bankCodeName;

	public BankRemittanceData(Collection<CodeValueData> bankOptions) {
		this.bankOptions = bankOptions;
		this.accountNumber = null;
		this.clientFullName = null;
		this.accountHolderName = null;
		this.principal = null;
		this.bankAccountnumber = null;
		this.groupDisplayName = null;
		this.groupId = null;
		this.iFSCCode = null;
		this.bankName = null;
		this.address = null;
		this.officeName = null;
		this.bankId = 0L;
		this.id = 0L;
		this.bankCodeName = null;
	}

	public long getId() {
		return id;
	}

	public BankRemittanceData(final long id, final String accountNumber, final String clientFullName,
			final String accountHolderName, final BigDecimal principal, final String bankAccountnumber,
			final String groupDisplayName, final String groupId, final String iFSCCode, final String bankName,
			final String address,final String officeName,final long bankId,final String bankCodeName) {
		this.id = id;
		this.accountNumber = accountNumber;
		this.clientFullName = clientFullName;
		this.accountHolderName = accountHolderName;
		this.principal = principal;
		this.bankAccountnumber = bankAccountnumber;
		this.groupDisplayName = groupDisplayName;
		this.groupId = groupId;
		this.iFSCCode = iFSCCode;
		this.bankName = bankName;
		this.address = address;
		this.officeName = officeName;
		this.bankId = bankId;
		this.bankCodeName = bankCodeName;
		
		this.bankOptions = null;
		

	}

	public Collection<CodeValueData> getBankOptions() {
		return bankOptions;
	}

	public String getAccountNumber() {
		return accountNumber;
	}

	public String getClientFullName() {
		return clientFullName;
	}

	public String getAccountHolderName() {
		return accountHolderName;
	}

	public BigDecimal getPrincipal() {
		return principal;
	}

	public String getBankAccountnumber() {
		return bankAccountnumber;
	}

	public String getGroupDisplayName() {
		return groupDisplayName;
	}

	public String getGroupId() {
		return groupId;
	}

	public String getiFSCCode() {
		return iFSCCode;
	}

	public String getBankName() {
		return bankName;
	}

	public String getAddress() {
		return address;
	}

	public static BankRemittanceData template(final Collection<CodeValueData> bankOptions) {
		return new BankRemittanceData(bankOptions);
	}

	public String getOfficeName() {
		return officeName;
	}

	public long getBankId() {
		return bankId;
	}

}
