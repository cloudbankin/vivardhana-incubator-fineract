package org.finabile.portfolio.countryStateDetails.service;

import java.util.List;

import org.finabile.portfolio.countryStateDetails.data.CountryStateDetailsData;

public interface CountryStateDetailsReadPlatformService {

	List<CountryStateDetailsData> retrieveStateDetails(Long pincodeMatch);
	List<CountryStateDetailsData> retrieveStateDetails1(Long pincodeMatch);

}
