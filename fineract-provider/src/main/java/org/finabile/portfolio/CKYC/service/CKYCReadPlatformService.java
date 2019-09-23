package org.finabile.portfolio.CKYC.service;

import java.util.List;

import org.apache.fineract.infrastructure.core.service.Page;
import org.apache.fineract.infrastructure.core.service.SearchParameters;
import org.apache.fineract.infrastructure.documentmanagement.data.DocumentData;
import org.apache.fineract.infrastructure.documentmanagement.domain.Image;
import org.apache.fineract.portfolio.client.domain.Client;
import org.finabile.portfolio.CKYC.data.CKYCData;

public interface CKYCReadPlatformService {

	Page<CKYCData> retrieveAllActivatedClients(SearchParameters searchParameters);

	List<CKYCData> retrieveSearchedData(String startDate, String endDate);

	List<CKYCData> retriveActivatedClientsData();

	List<CKYCData> getClientFamilyDetails(Client client);

	DocumentData readDocumentDetails(String string, Long id);
	
	List<CKYCData> retrievePanData(String name, String panno);


}
