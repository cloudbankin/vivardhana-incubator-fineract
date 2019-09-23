package org.apache.fineract.portfolio.client.data;


public class ClientAddressData {

	private final Long client_id;

	private final String addressType;

	private final Long addressId;

	private final String street;

	private final String addressLine1;

	private final String addressLine2;

	private final String addressLine3;

	private final String townVillage;

	private final String city;

	private final String countyDistrict;

	private final String countryName;

	private final String stateName;

	private final String postalCode;

	private final String clientExternal;
	
	private final String clientAadhaarNo;



	public ClientAddressData(final String addressType, final Long client_id, final Long addressId,final String street, final String addressLine1, final String addressLine2,
			final String addressLine3, final String townVillage, final String city, final String countyDistrict,
		 final String stateName, final String countryName,
			final String postalCode, final String clientExternal, final String clientAadhaarNo) {
		this.addressType = addressType;
		this.client_id = client_id;
		this.addressId = addressId;
		this.street = street;
		this.addressLine1 = addressLine1;
		this.addressLine2 = addressLine2;
		this.addressLine3 = addressLine3;
		this.townVillage = townVillage;
		this.city = city;
		this.countyDistrict = countyDistrict;
		this.stateName = stateName;
		this.countryName = countryName;
		this.postalCode = postalCode;
		this.clientExternal = clientExternal;
		this.clientAadhaarNo = clientAadhaarNo;
	}



	public Long getClient_id() {
		return client_id;
	}



	public String getAddressType() {
		return addressType;
	}



	public Long getAddressId() {
		return addressId;
	}



	public String getStreet() {
		return street;
	}



	public String getAddressLine1() {
		return addressLine1;
	}



	public String getAddressLine2() {
		return addressLine2;
	}



	public String getAddressLine3() {
		return addressLine3;
	}



	public String getTownVillage() {
		return townVillage;
	}



	public String getCity() {
		return city;
	}



	public String getCountyDistrict() {
		return countyDistrict;
	}



	public String getCountryName() {
		return countryName;
	}



	public String getStateName() {
		return stateName;
	}



	public String getPostalCode() {
		return postalCode;
	}



	public String getClientExternal() {
		return clientExternal;
	}


	public String getClientAadhaarNo() {
		return clientAadhaarNo;
	}	
	
	
}