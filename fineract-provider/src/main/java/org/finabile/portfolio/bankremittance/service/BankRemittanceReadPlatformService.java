package org.finabile.portfolio.bankremittance.service;

import java.util.List;

import org.apache.fineract.infrastructure.core.service.Page;
import org.apache.fineract.infrastructure.core.service.SearchParameters;
import org.apache.fineract.infrastructure.documentmanagement.data.FileData;
import org.finabile.portfolio.bankremittance.data.BankRemittanceData;

import net.minidev.json.JSONObject;

public interface BankRemittanceReadPlatformService {
	BankRemittanceData retrieveTemplate();

	Page<BankRemittanceData> retrieveAllApprovedLoans(SearchParameters searchParameters);

	List<BankRemittanceData> retrieveSearchedData(String bankId, String startDate, String endDate);

	BankRemittanceData readBankDetails(Long clientId);

	FileData retrieveFileData();

	List<BankRemittanceData> retrieveApprovedLoansData();


	
}
