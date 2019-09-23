package org.finabile.portfolio.countryStateDetails.api;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.apache.fineract.infrastructure.core.api.ApiRequestParameterHelper;
import org.apache.fineract.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.apache.fineract.infrastructure.core.serialization.ToApiJsonSerializer;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.finabile.portfolio.countryStateDetails.data.CountryStateDetailsData;
import org.finabile.portfolio.countryStateDetails.service.CountryStateDetailsReadPlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("state")
@Component
@Scope("singleton")
public class CountryStateDetailsApiResource {
	
	private final PlatformSecurityContext context;
	private final ToApiJsonSerializer<CountryStateDetailsData> toApiJsonSerializer;
	private final ApiRequestParameterHelper apiRequestParameterHelper;
	private final CountryStateDetailsReadPlatformService countryStateDetailsReadPlatformService;

	@Autowired
	public CountryStateDetailsApiResource(final PlatformSecurityContext context,final ToApiJsonSerializer<CountryStateDetailsData> toApiJsonSerializer,
			final ApiRequestParameterHelper apiRequestParameterHelper,final CountryStateDetailsReadPlatformService countryStateDetailsReadPlatformService) {
		this.context = context;
		this.toApiJsonSerializer = toApiJsonSerializer;
		this.apiRequestParameterHelper = apiRequestParameterHelper;
		this.countryStateDetailsReadPlatformService = countryStateDetailsReadPlatformService;
	}
	
	
	@GET
	@Path("statedetails")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String retriveSearchedData(@Context final UriInfo uriInfo, @QueryParam("pincode") final Long pincodeMatch ) {
		this.context.authenticatedUser().validateHasReadPermission(CountryStateDetailsConstants.COUNTRYSTATEDETAILS_RESOURCE_NAME);

		final List<CountryStateDetailsData> stateDetails = this.countryStateDetailsReadPlatformService.retrieveStateDetails(pincodeMatch);

		final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper
				.process(uriInfo.getQueryParameters());
		return this.toApiJsonSerializer.serialize(settings, stateDetails,
				CountryStateDetailsConstants.COUNTRYSTATEDETAILS_RESPONSE_DATA_PARAMETERS);
	}
	
	@GET
	@Path("statedetails1")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String retriveSearchedData1(@Context final UriInfo uriInfo, @QueryParam("pincode") final Long pincodeMatch ) {
		this.context.authenticatedUser().validateHasReadPermission(CountryStateDetailsConstants.COUNTRYSTATEDETAILS_RESOURCE_NAME);

		final List<CountryStateDetailsData> stateDetails = this.countryStateDetailsReadPlatformService.retrieveStateDetails1(pincodeMatch);

		final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper
				.process(uriInfo.getQueryParameters());
		return this.toApiJsonSerializer.serialize(settings, stateDetails,
				CountryStateDetailsConstants.COUNTRYSTATEDETAILS_RESPONSE_DATA_PARAMETERS);
	}
	
}
