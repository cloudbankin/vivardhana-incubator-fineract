package org.apache.fineract.portfolio.client.data;

public class FamilyDetailData {
	private final Long client_id;

	private final String name;

	private final String relation;
	
	private final int relationType;

	public FamilyDetailData(long clientId, String name, String relation, int relationType) {
           this.client_id=clientId;
           this.name=name;
           this.relation=relation;
           this.relationType=relationType;
	 }

	public int getRelationType() {
		return relationType;
	}

	public Long getClient_id() {
		return client_id;
	}

	public String getName() {
		return name;
	}

	public String getRelation() {
		return relation;
	}

}