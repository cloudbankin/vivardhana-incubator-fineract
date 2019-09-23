package org.apache.fineract.infrastructure.codes.service;

import java.util.Collection;

import org.apache.fineract.infrastructure.codes.data.AdvanceSearchData;

public interface AdvanceSearchPlatformService 
{
	Collection<AdvanceSearchData> retrieveLoanProducts();
	Collection<AdvanceSearchData> retrieveFunds();
}
