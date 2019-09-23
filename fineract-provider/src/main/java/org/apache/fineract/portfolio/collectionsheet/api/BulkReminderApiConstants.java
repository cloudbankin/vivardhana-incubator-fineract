package org.apache.fineract.portfolio.collectionsheet.api;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class BulkReminderApiConstants {
	  private static final String LOANACCOUNTNUMBERPARAM = "loanAccountNumber";
	    private static final String INSTALLMENTNUMBERPARAM = "installmentNumber";
	    private static final String AMOUNTPARAM = "amount";
	    private static final String MOBILENUMBERPARAM = "mobileno";
	    private static final String DUEDATEPARAM = "duedate";
	    private static final String NAMEPARAM = "name";
	    
	    protected static final Set<String> BULK_REMINDER_RESPONSE_DATA_PARAMETERS = new HashSet<>(Arrays.asList(LOANACCOUNTNUMBERPARAM,INSTALLMENTNUMBERPARAM
	    		,AMOUNTPARAM,MOBILENUMBERPARAM,DUEDATEPARAM,NAMEPARAM));
	
}
