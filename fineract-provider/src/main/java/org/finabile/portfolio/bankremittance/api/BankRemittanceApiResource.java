package org.finabile.portfolio.bankremittance.api;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.UriInfo;

import org.apache.fineract.infrastructure.codes.domain.CodeValue;
import org.apache.fineract.infrastructure.codes.domain.CodeValueRepository;
import org.apache.fineract.infrastructure.core.api.ApiRequestParameterHelper;
import org.apache.fineract.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.apache.fineract.infrastructure.core.serialization.ToApiJsonSerializer;
import org.apache.fineract.infrastructure.core.service.Page;
import org.apache.fineract.infrastructure.core.service.SearchParameters;
import org.apache.fineract.infrastructure.documentmanagement.data.FileData;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.finabile.portfolio.bankremittance.data.BankRemittanceData;
import org.finabile.portfolio.bankremittance.service.BankRemittanceReadPlatformService;
import org.finabile.portfolio.bankremittance.service.BankRemittanceWritePlatformService;
import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;

@Path("bankRemittance")
@Component
@Scope("singleton")
public class BankRemittanceApiResource {

	private final PlatformSecurityContext context;
	private final BankRemittanceReadPlatformService bankRemittanceReadPlatformService;
	private final ApiRequestParameterHelper apiRequestParameterHelper;
	private final ToApiJsonSerializer<BankRemittanceData> toApiJsonSerializer;
	private final BankRemittanceWritePlatformService bankRemittanceWritePlatformService;
	private final CodeValueRepository codeValueRepository;
	zipFiles zip = new zipFiles();

	@Autowired
	public BankRemittanceApiResource(final PlatformSecurityContext context,
			final BankRemittanceReadPlatformService bankRemittanceReadPlatformService,
			final ApiRequestParameterHelper apiRequestParameterHelper,
			final ToApiJsonSerializer<BankRemittanceData> toApiJsonSerializer,
			final BankRemittanceWritePlatformService bankRemittanceWritePlatformService,
			final CodeValueRepository codeValueRepository) {
		this.context = context;
		this.bankRemittanceReadPlatformService = bankRemittanceReadPlatformService;
		this.apiRequestParameterHelper = apiRequestParameterHelper;
		this.toApiJsonSerializer = toApiJsonSerializer;
		this.bankRemittanceWritePlatformService = bankRemittanceWritePlatformService;
		this.codeValueRepository = codeValueRepository;
	}

	@GET
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String retriveData(@Context final UriInfo uriInfo, @QueryParam("offset") final Integer offset,
			@QueryParam("limit") final Integer limit) {
		this.context.authenticatedUser()
				.validateHasReadPermission(BankRemittanceConstants.BANK_REMITTANCE_RESOURCE_NAME);

		final SearchParameters searchParameters = SearchParameters.forClients(null, null, null, null, null, null, null,
				offset, limit, null, null, false, false);
		final Page<BankRemittanceData> approvedLoans = this.bankRemittanceReadPlatformService
				.retrieveAllApprovedLoans(searchParameters);

		final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper
				.process(uriInfo.getQueryParameters());
		return this.toApiJsonSerializer.serialize(settings, approvedLoans,
				BankRemittanceConstants.BANK_REMITTANCE_RESPONSE_DATA_PARAMETERS);

	}

	@GET
	@Path("template")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String retrieveTemplate(@Context final UriInfo uriInfo, @QueryParam("officeId") final Long officeId,
			@QueryParam("commandParam") final String commandParam,
			@DefaultValue("false") @QueryParam("staffInSelectedOfficeOnly") final boolean staffInSelectedOfficeOnly) {

		this.context.authenticatedUser()
				.validateHasReadPermission(BankRemittanceConstants.BANK_REMITTANCE_RESOURCE_NAME);

		BankRemittanceData bankRemittanceData = this.bankRemittanceReadPlatformService.retrieveTemplate();

		final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper
				.process(uriInfo.getQueryParameters());
		return this.toApiJsonSerializer.serialize(settings, bankRemittanceData,
				BankRemittanceConstants.BANK_REMITTANCE_RESPONSE_DATA_PARAMETERS);
	}

	@GET
	@Path("search")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String retriveSearchedData(@Context final UriInfo uriInfo, @QueryParam("bankId") final String bankId,
			@QueryParam("startDate") final String startDate, @QueryParam("endDate") final String endDate) {
		this.context.authenticatedUser()
				.validateHasReadPermission(BankRemittanceConstants.BANK_REMITTANCE_RESOURCE_NAME);

		final List<BankRemittanceData> searchedApprovedLoans = this.bankRemittanceReadPlatformService
				.retrieveSearchedData(bankId, startDate, endDate);

		final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper
				.process(uriInfo.getQueryParameters());
		return this.toApiJsonSerializer.serialize(settings, searchedApprovedLoans,
				BankRemittanceConstants.BANK_REMITTANCE_RESPONSE_DATA_PARAMETERS);
	}

	@GET
	@Path("download")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_OCTET_STREAM })
	public Response fileWriterForApprovedLoans(@Context final UriInfo uriInfo, @QueryParam("bankId") final Long bankId,
			@QueryParam("selectedLoans") final String apiRequestBodyAsJson) {

		this.context.authenticatedUser()
				.validateHasReadPermission(BankRemittanceConstants.BANK_REMITTANCE_RESOURCE_NAME);

		String[] str = apiRequestBodyAsJson.split(",");

		List<String> strData = new ArrayList<String>();
		for (String itr : str) {
			strData.add(itr);
		}

		final File file = this.bankRemittanceWritePlatformService.fileWriterForApprovedLoansDetails(bankId, strData);
		// final FileData
		// fileData=this.bankRemittanceReadPlatformService.retrieveFileData();
		FileInputStream fis = null;
		ZipOutputStream zipOut = null;
		ResponseBuilder response = null;
		String fileName = "";
		try {

			File dir = new File("C:\\vivardhana\\apache-tomcat-7.0.84\\bankRemittanceDetails");
			String zipDirName = "C:\\vivardhana\\apache-tomcat-7.0.84\\zipDir\\bankRemittanceDetails.zip";
			File path = new File("C:\\vivardhana\\apache-tomcat-7.0.84\\zipDir");
			File[] files = path.listFiles();
			if (files != null) {
				for (File fileDelete : files) {
					fileDelete.delete();

				}
			}

			zip.zipDirectory(dir, zipDirName);

			System.out.println("Done... Zipped the files...");
			response = Response
					.ok(new FileInputStream("C:\\vivardhana\\apache-tomcat-7.0.84\\zipDir\\bankRemittanceDetails.zip"));
			Date date = new Date();
			String strDateFormat = "hh:mm:ss_a";
			DateFormat dateFormat = new SimpleDateFormat(strDateFormat);
			String formattedDate = dateFormat.format(date);
			LocalDate currentDate = new LocalDate();
			String fileNameAttach = currentDate + "_" + formattedDate;
			CodeValue banKName = this.codeValueRepository.findOne(bankId);
			if (banKName.getLabel().equals("CUB")) {

				fileName = "CUB" + fileNameAttach;
			} else {
				fileName = "IBNK" + fileNameAttach;
			}
			response.header("Content-Disposition", "attachment; filename=\"" + fileName + ".zip" + "\"");
			response.header("Content-Type", "application/zip");

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		return response.build();

	}

	public class zipFiles {

		private void zipDirectory(File dir, String zipDirName) {
			try {
				List<String> filesListInDir = populateFilesList(dir);
				// now zip files one by one
				// create ZipOutputStream to write to the zip file
				FileOutputStream fos = new FileOutputStream(zipDirName);
				ZipOutputStream zos = new ZipOutputStream(fos);
				for (String filePath : filesListInDir) {
					System.out.println("Zipping " + filePath);
					// for ZipEntry we need to keep only relative file path, so we used substring on
					// absolute path
					ZipEntry ze = new ZipEntry(
							filePath.substring(dir.getAbsolutePath().length() + 1, filePath.length()));
					zos.putNextEntry(ze);
					// read the file and write to ZipOutputStream
					FileInputStream fis = new FileInputStream(filePath);
					byte[] buffer = new byte[1024];
					int len;
					while ((len = fis.read(buffer)) > 0) {
						zos.write(buffer, 0, len);
					}
					zos.closeEntry();
					fis.close();
				}
				zos.close();
				fos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		private List<String> populateFilesList(File dir) throws IOException {
			List<String> filesListInDir = new ArrayList<String>();
			File[] files = dir.listFiles();
			for (File file : files) {
				if (file.isFile())
					filesListInDir.add(file.getAbsolutePath());
				else
					populateFilesList(file);

			}
			return filesListInDir;
		}
	}

	@GET
	@Path("generatedLoans")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String retriveApprovedLoansData(@Context final UriInfo uriInfo) {
		this.context.authenticatedUser()
				.validateHasReadPermission(BankRemittanceConstants.BANK_REMITTANCE_RESOURCE_NAME);

		final List<BankRemittanceData> searchedApprovedLoans = this.bankRemittanceReadPlatformService
				.retrieveApprovedLoansData();

		final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper
				.process(uriInfo.getQueryParameters());
		return this.toApiJsonSerializer.serialize(settings, searchedApprovedLoans,
				BankRemittanceConstants.BANK_REMITTANCE_RESPONSE_DATA_PARAMETERS);
	}

}
