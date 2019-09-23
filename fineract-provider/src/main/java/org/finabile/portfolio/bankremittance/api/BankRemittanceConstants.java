package org.finabile.portfolio.bankremittance.api;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class BankRemittanceConstants {
	
    public static final String BANK_REMITTANCE_RESOURCE_NAME = "bankremittance";

	
	public static final String BANK_DETAILS = "BANK_DETAILS";
	public static final String BANK_REMITTANCE_DETAILS = "bankDetails";
	
	   protected static final Set<String> BANK_REMITTANCE_RESPONSE_DATA_PARAMETERS = new HashSet<>(Arrays.asList(BANK_REMITTANCE_DETAILS));
}
