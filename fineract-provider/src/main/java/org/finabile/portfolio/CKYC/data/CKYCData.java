package org.finabile.portfolio.CKYC.data;

import java.util.Date;

public class CKYCData {
	
	private final long id;
	private final String officeName;
	private final String clientName;
	private final String clientId;
	private final Date clientActivationDate;
	
	//familyDetails
	private final String fatherName;
	private final String motherName;
	private final String spouseName;
	private final String maritalStatus;
	private final String occupation;
	
	public CKYCData(long id, String officeName, String clientName, String clientId, Date clientActivationDate) {

		this.id = id;
		this.officeName = officeName;
		this.clientName = clientName;
		this.clientId = clientId;
		this.clientActivationDate = clientActivationDate;
		
		this.fatherName = null;
		this.motherName = null;
		this.spouseName = null;
		this.maritalStatus = null;
		this.occupation = null;
		
	}

	public CKYCData(String fatherName, String motherName, String spouseName, String maritalStatus, String occupation) {
		
		this.id = 0L;
		this.officeName = null;
		this.clientName = null;
		this.clientId = null;
		this.clientActivationDate = null;
		
		this.fatherName = fatherName;
		this.motherName = motherName;
		this.spouseName = spouseName;
		this.maritalStatus = maritalStatus;
		this.occupation = occupation;
		
	}

	public long getId() {
		return id;
	}

	public String getOfficeName() {
		return officeName;
	}

	public String getClientName() {
		return clientName;
	}

	public String getClientId() {
		return clientId;
	}

	public Date getClientActivationDate() {
		return clientActivationDate;
	}

	public String getFatherName() {
		return fatherName;
	}

	public String getMotherName() {
		return motherName;
	}

	public String getSpouseName() {
		return spouseName;
	}

	public String getMaritalStatus() {
		return maritalStatus;
	}

	public String getOccupation() {
		return occupation;
	}

}
