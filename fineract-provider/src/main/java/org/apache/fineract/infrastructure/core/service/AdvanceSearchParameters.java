package org.apache.fineract.infrastructure.core.service;

import org.apache.commons.lang.StringUtils;

public class AdvanceSearchParameters {

	//AdvanceSearch for clients
	
	private final String sqlSearch;
    private final Long officeId;
  	private final Long fund;
  	private final Long loanPurpose;
  	private final Long loanOfficer;
  	private final Long loanProduct;
  	private final String displayName;
  	private final String mobileNo;
  	private final Long status;
  	private final Integer offset;
  	private final Integer limit;
  	
  	//AdvanceSearch for groups
  	
  	private final String groupExternalId;
  	private final String orderBy;
    private final String sortOrder;
    
    //AdvanceSearch for centers
    
    private final String centerExternalId;
  	
  	//AdvanceSearch for clients 
  	public static AdvanceSearchParameters forClientsAdvanceSearch(final String sqlSearch,
    		final Long officeId,final Long fund,final Long loanPurpose,final Long loanOfficer,
    		final Long loanProduct,final String displayName,final String mobileNo,final Long status,
    		final Integer offset, final Integer limit) {
  		
  		return new AdvanceSearchParameters(sqlSearch, officeId, fund, 
  				loanPurpose, loanOfficer, loanProduct, displayName, mobileNo, status,offset,limit,null,null,null,null);
    	//null for the values required in group but not required in clients 
    }
  	
  	//AdvanceSearch for Group 
  	public static AdvanceSearchParameters forGroupAdvanceSearch(final String sqlSearch,
  			final String displayName, final String groupExternalId,final Long officeId,
  			final Long status,final Long loanOfficer,final Integer offset, final Integer limit,
  			final String orderBy,final String sortOrder) {
  		
  		return new AdvanceSearchParameters(sqlSearch, officeId, null, null, loanOfficer, null, displayName,
  				null, status, offset, limit, groupExternalId,orderBy,sortOrder,null);
    	
    }
  	
  //AdvanceSearch for Centers 
  	public static AdvanceSearchParameters forCentersAdvanceSearch(final String sqlSearch,
  			final String displayName, final String centerExternalId,final Long officeId,
  			final Long status,final Long loanOfficer,final Integer offset, final Integer limit,
  			final String orderBy,final String sortOrder) {
  		
  		return new AdvanceSearchParameters(sqlSearch, officeId, null, null, loanOfficer, null, displayName,
  				null, status, offset, limit, null, orderBy, sortOrder, centerExternalId);
  	}
  	
  	 //AdvanceSearch for clients , Groups , centers
    private AdvanceSearchParameters(final String sqlSearch,
    		final Long officeId,final Long fund,final Long loanPurpose,final Long loanOfficer,
    		final Long loanProduct,final String displayName,final String mobileNo,final Long status,
    		final Integer offset, final Integer limit,final String groupExternalId,
    		final String orderBy,final String sortOrder,final String centerExternalId) {
    	this.sqlSearch = sqlSearch;
    	this.officeId = officeId;
    	this.fund = fund;
    	this.loanPurpose = loanPurpose;
    	this.loanOfficer = loanOfficer;
    	this.loanProduct = loanProduct;
    	this.displayName = displayName;
    	this.mobileNo = mobileNo;
    	this.status = status;
    	this.offset = offset;
    	this.limit = limit;
    	this.groupExternalId = groupExternalId;
    	this.orderBy = orderBy;
    	this.sortOrder = sortOrder;
    	this.centerExternalId = centerExternalId;
    }

    
	public String getSqlSearch() {
		return sqlSearch;
	}

	public Long getOfficeId() {
		return officeId;
	}

	public Long getFund() {
		return fund;
	}

	public Long getLoanPurpose() {
		return loanPurpose;
	}

	public Long getLoanOfficer() {
		return loanOfficer;
	}

	public Long getLoanProduct() {
		return loanProduct;
	}

	public String getDisplayName() {
		return displayName;
	}

	public String getMobileNo() {
		return mobileNo;
	}

	public Long getStatus() {
		return status;
	}

	public Integer getOffset() {
		return offset;
	}

	public Integer getLimit() {
		return limit;
	}
    
	public boolean isLimited() {
        return this.limit != null && this.limit.intValue() > 0;
    }

    public boolean isOffset() {
        return this.offset != null;
    }

	public String getGroupExternalId() {
		return groupExternalId;
	}

	public String getOrderBy() {
		return orderBy;
	}

	public String getSortOrder() {
		return sortOrder;
	}

	public String getCenterExternalId() {
		return centerExternalId;
	}
	
}
