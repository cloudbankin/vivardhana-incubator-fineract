/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.fineract.portfolio.client.service;

//High mark added import 
import java.util.Collection;
import java.util.Date;

import org.apache.fineract.infrastructure.core.service.AdvanceSearchParameters;
import org.apache.fineract.infrastructure.core.service.Page;
import org.apache.fineract.infrastructure.core.service.SearchParameters;
import org.apache.fineract.portfolio.client.data.AdvanceClientData;
import org.apache.fineract.portfolio.client.data.ClientData;
import org.apache.poi.ss.usermodel.Workbook;

public interface ClientReadPlatformService {

    ClientData retrieveTemplate(Long officeId, boolean staffInSelectedOfficeOnly);

    Page<ClientData> retrieveAll(SearchParameters searchParameters);

    ClientData retrieveOne(Long clientId);

    Collection<ClientData> retrieveAllForLookup(String extraCriteria);

    Collection<ClientData> retrieveAllForLookupByOfficeId(Long officeId);

    ClientData retrieveClientByIdentifier(Long identifierTypeId, String identifierKey);

    Collection<ClientData> retrieveClientMembersOfGroup(Long groupId);

    Collection<ClientData> retrieveActiveClientMembersOfGroup(Long groupId);

    Collection<ClientData> retrieveActiveClientMembersOfCenter(final Long centerId);

    ClientData retrieveAllNarrations(String clientNarrations);

	Page<ClientData> advanceRetrieveAll(AdvanceSearchParameters advancesearchParameters);
	
	//High Mark added 
	Workbook retrieveHighMarkData(String asOnDate, String closedFrom);
	
	//High Mark Weekly added
	Workbook retrieveWeeklyHighMarkData(String asOnDate, String closedFrom);
	
	//High Mark added 
	Workbook retrieveEquifaxData(String asOnDate, String closedFrom);
		
	//Advance Search 
	AdvanceClientData advanceRetrieveTemplate(Long officeId, boolean staffInSelectedOfficeOnly);
}