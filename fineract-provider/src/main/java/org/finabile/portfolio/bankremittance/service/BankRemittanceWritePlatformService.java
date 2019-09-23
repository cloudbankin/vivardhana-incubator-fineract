package org.finabile.portfolio.bankremittance.service;

import java.io.File;
import java.util.List;

import net.minidev.json.JSONObject;

public interface BankRemittanceWritePlatformService {

	File fileWriterForApprovedLoansDetails(Long bankId, List<String> apiRequestBodyAsJson);
}
