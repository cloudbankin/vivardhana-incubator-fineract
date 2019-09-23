package org.apache.fineract.portfolio.client.data;

import java.util.Collection;

import org.apache.fineract.infrastructure.codes.data.AdvanceSearchData;
import org.apache.fineract.infrastructure.codes.data.CodeValueData;
import org.apache.fineract.organisation.office.data.OfficeData;
import org.apache.fineract.organisation.staff.data.StaffData;

public class AdvanceClientData 
{
	 private final Collection<OfficeData> officeOptions;
	 private final Collection<StaffData> staffOptions;
	 private final Collection<CodeValueData> loanPurposeOptions;
	 private final Collection<AdvanceSearchData> loanProductsOptions;
	 private final Collection<AdvanceSearchData> funds;
	
	 public AdvanceClientData(final Collection<OfficeData> officeOptions,
			 final Collection<StaffData> staffOptions,final Collection<CodeValueData> loanPurposeOptions,
			 final Collection<AdvanceSearchData> loanProductsOptions,final Collection<AdvanceSearchData> funds) 
	 {
		 this.officeOptions = officeOptions;
		 this.staffOptions = staffOptions;
		 this.loanPurposeOptions = loanPurposeOptions;
		 this.loanProductsOptions = loanProductsOptions;
		 this.funds = funds;
	 }
	 
	 
	public static AdvanceClientData advanceTemplate(final Collection<OfficeData> officeOptions,
			final Collection<StaffData> staffOptions,final Collection<CodeValueData> loanPurposeOptions,
			final Collection<AdvanceSearchData> loanProductsOptions,final Collection<AdvanceSearchData> funds) 
	{
		
		return new AdvanceClientData(officeOptions, staffOptions,loanPurposeOptions,loanProductsOptions,funds);
	}
	
}
