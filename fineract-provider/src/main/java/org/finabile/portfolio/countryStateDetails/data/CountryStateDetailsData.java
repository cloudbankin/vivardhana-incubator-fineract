package org.finabile.portfolio.countryStateDetails.data;


public class CountryStateDetailsData {
	
	 private final Long id;
	 private final Long pincode;
	 private final String district;
	 private final String stateCode;
	 private final String state;

	public CountryStateDetailsData(long id, Long pincode, String district, String stateCode, String state) {
		
		this.id = id;
		this.pincode = pincode;
		this.district = district;
		this.stateCode = stateCode;
		this.state = state;
				
		
	}

	public Long getId() {
		return id;
	}

	public Long getPincode() {
		return pincode;
	}

	public String getDistrict() {
		return district;
	}

	public String getStateCode() {
		return stateCode;
	}

	public String getState() {
		return state;
	}

}
