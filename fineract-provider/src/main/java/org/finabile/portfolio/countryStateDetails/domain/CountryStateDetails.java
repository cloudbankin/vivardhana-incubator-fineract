package org.finabile.portfolio.countryStateDetails.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;

@Entity
@Table(name = "hab_stateDetails")
public class CountryStateDetails extends AbstractPersistableCustom<Long>{

	@Column(name = "pincode",  nullable = true)
    private Long pincode;
	
	@Column(name = "district", length = 50, nullable = true)
    private String district;
	
	@Column(name = "state_code", length = 50, nullable = true)
    private String stateCode;

	@Column(name = "state", length = 50, nullable = true)
    private String state;

	public Long getPincode() {
		return pincode;
	}

	public void setPincode(Long pincode) {
		this.pincode = pincode;
	}

	public String getDistrict() {
		return district;
	}

	public void setDistrict(String district) {
		this.district = district;
	}

	public String getStateCode() {
		return stateCode;
	}

	public void setStateCode(String stateCode) {
		this.stateCode = stateCode;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}
}
