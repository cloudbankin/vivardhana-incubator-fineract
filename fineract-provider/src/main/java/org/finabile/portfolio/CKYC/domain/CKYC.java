package org.finabile.portfolio.CKYC.domain;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;
import org.apache.fineract.portfolio.client.domain.Client;

@Entity
@Table(name = "hab_ckyc_details")
public class CKYC extends AbstractPersistableCustom<Long>{

	 @ManyToOne
	    @JoinColumn(name = "client_id", nullable = true)
	    private Client client;

	 
	 public CKYC(Client client) {
		 this.client = client;
	}

	public Client getClient() {
			return client;
		}

		public void setClient(Client client) {
			this.client = client;
		}

		public static CKYC addingRefference(Client client) {
			return new CKYC(client);
		}
	 
}
