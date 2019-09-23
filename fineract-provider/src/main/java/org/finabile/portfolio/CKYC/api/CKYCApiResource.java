package org.finabile.portfolio.CKYC.api;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.UriInfo;

import org.apache.fineract.infrastructure.core.api.ApiRequestParameterHelper;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.apache.fineract.infrastructure.core.serialization.ToApiJsonSerializer;
import org.apache.fineract.infrastructure.core.service.Page;
import org.apache.fineract.infrastructure.core.service.SearchParameters;
import org.apache.fineract.infrastructure.documentmanagement.data.FileData;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.finabile.portfolio.CKYC.command.textcommand;
import org.finabile.portfolio.CKYC.data.CKYCData;
import org.finabile.portfolio.CKYC.service.CKYCReadPlatformService;
import org.finabile.portfolio.CKYC.service.CKYCWritePlatformService;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataBodyPart;
import com.sun.jersey.multipart.FormDataParam;

import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;

@Path("ckyc")
@Component
@Scope("singleton")
public class CKYCApiResource {

	private final PlatformSecurityContext context;
	private final ApiRequestParameterHelper apiRequestParameterHelper;
	private final ToApiJsonSerializer<CKYCData> toApiJsonSerializer;
	private final CKYCWritePlatformService cKYCWritePlatformService;
	private final CKYCReadPlatformService cKYCReadPlatformService;
	
	zipFiles zip = new zipFiles();
	

	@Autowired
	public CKYCApiResource(final PlatformSecurityContext context, final CKYCReadPlatformService cKYCReadPlatformService,
			final ApiRequestParameterHelper apiRequestParameterHelper,
			final ToApiJsonSerializer<CKYCData> toApiJsonSerializer,
			final CKYCWritePlatformService cKYCWritePlatformService) {
		this.context = context;
		this.cKYCReadPlatformService = cKYCReadPlatformService;
		this.apiRequestParameterHelper = apiRequestParameterHelper;
		this.toApiJsonSerializer = toApiJsonSerializer;
		this.cKYCWritePlatformService = cKYCWritePlatformService;
	}

	@GET
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String retriveData(@Context final UriInfo uriInfo, @QueryParam("offset") final Integer offset,
			@QueryParam("limit") final Integer limit) {
		this.context.authenticatedUser().validateHasReadPermission(CKYCConstants.CKYC_RESOURCE_NAME);

		final SearchParameters searchParameters = SearchParameters.forClients(null, null, null, null, null, null, null,
				offset, limit, null, null, false, false);
		final Page<CKYCData> activatedClients = this.cKYCReadPlatformService.retrieveAllActivatedClients(searchParameters);

		final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper
				.process(uriInfo.getQueryParameters());
		return this.toApiJsonSerializer.serialize(settings, activatedClients, CKYCConstants.CKYC_RESPONSE_DATA_PARAMETERS);

	}

	@GET
	@Path("pansearch")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String retrivePanSearchedData(@Context final UriInfo uriInfo, @QueryParam("name") final String name,
			@QueryParam("panno") final String panno) {
		this.context.authenticatedUser().validateHasReadPermission(CKYCConstants.CKYC_RESOURCE_NAME);

		final List<CKYCData> searchedActivatedClients = this.cKYCReadPlatformService.retrievePanData(name,
				panno);

		final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper
				.process(uriInfo.getQueryParameters());
		return this.toApiJsonSerializer.serialize(settings, searchedActivatedClients,
				CKYCConstants.CKYC_RESPONSE_DATA_PARAMETERS);
	}
	
	@GET
	@Path("search")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String retriveSearchedData(@Context final UriInfo uriInfo, @QueryParam("startDate") final String startDate,
			@QueryParam("endDate") final String endDate) {
		this.context.authenticatedUser().validateHasReadPermission(CKYCConstants.CKYC_RESOURCE_NAME);

		final List<CKYCData> searchedActivatedClients = this.cKYCReadPlatformService.retrieveSearchedData(startDate,
				endDate);

		final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper
				.process(uriInfo.getQueryParameters());
		return this.toApiJsonSerializer.serialize(settings, searchedActivatedClients,
				CKYCConstants.CKYC_RESPONSE_DATA_PARAMETERS);
	}


	@GET
	@Path("download")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_OCTET_STREAM })
	public Response fileWriterForActivatedClients(@Context final UriInfo uriInfo,@QueryParam("selectedLoans") final String selectedActiveClients) {

		this.context.authenticatedUser().validateHasReadPermission(CKYCConstants.CKYC_RESOURCE_NAME);

		String[] str = selectedActiveClients.split(",");

		List<String> strData = new ArrayList<String>();
		for (String itr : str) {
			strData.add(itr);
		}
		Random rand = new Random(); 
		int value = rand.nextInt(50)+10; 
		final File file = this.cKYCWritePlatformService.fileWriterForApprovedLoansDetails(strData,value);
		// final FileData
		// fileData=this.bankRemittanceReadPlatformService.retrieveFileData();
		FileInputStream fis = null;
		ZipOutputStream zipOut = null;
		ResponseBuilder response = null;
		
//		if(file == null) {
		String currentDateDetails = new SimpleDateFormat("ddMMyyyyhhmmss").format(new Date());
		String fileName = "IN2958_HO_"+currentDateDetails+"_IA004792_U000"+value;
		try {

			/*List<String> files = new ArrayList<String>();
			files.add("/home/habileos2/Desktop/ckyc/");
			if(file!=null) {
			files.add("/home/habileos2/Desktop/ckyc/reUpload.txt");
			}
			FileOutputStream fos = new FileOutputStream(
					"/home/habileos2/Desktop/ckyc.zip");
			zipOut = new ZipOutputStream(new BufferedOutputStream(fos));
			for (String res : files) {
				File input = new File(res);
				fis = new FileInputStream(input);
				ZipEntry ze = new ZipEntry(input.getName());
				System.out.println("Zipping the file: " + input.getName());
				zipOut.putNextEntry(ze);
				byte[] tmp = new byte[4 * 1024];
				int size = 0;
				while ((size = fis.read(tmp)) != -1) {
					zipOut.write(tmp, 0, size);
				}
				zipOut.flush();
				fis.close();
			}
			zipOut.close();*/
//			 File dir = new File("C:\\HedgeFinance\\ckyc");
//		        String zipDirName ="C:\\HedgeFinance\\ckyc.zip";
			String home = System.getProperty("user.home");
//			File path = new File("C:\\HedgeFinance\\ckyc");
				//File path = new File("C:\\Users\\Administrator\\Desktop\\CKYC");
			
				
		        
		        File dir = new File(home+"/CKYC");
		        String zipDirName =home+"/CKYC.zip";
		
		        zip.zipDirectory(dir, zipDirName);
	      /*  zip.close();
	        fW.close();*/
	        
		 
			System.out.println("Done... Zipped the files...");
//			response = Response
//					.ok(new FileInputStream("C:\\HedgeFinance\\ckyc.zip"));
			response = Response
					.ok(new FileInputStream(home+"/CKYC.zip"));
			response.header("Content-Disposition", "attachment; filename=\"" + fileName + ".zip" + "\"");
			response.header("Content-Type", "application/zip");

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		/*}else {
			try {
				response = Response
						.ok(new FileInputStream("/home/habileos2/Desktop/ckyc/reUpload.txt"));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			response.header("Content-Disposition", "attachment; filename=\"" + "reupload"+".txt" + "\"");
			response.header("Content-Type", "text/html");
		}*/
		return response.build();

	}
	public class zipFiles{
		
		private void zipDirectory(File dir, String zipDirName) {
	        try {
	        	List<String> filesListInDir = populateFilesList(dir);
	            //now zip files one by one
	            //create ZipOutputStream to write to the zip file
	            FileOutputStream fos = new FileOutputStream(zipDirName);
	            ZipOutputStream zos = new ZipOutputStream(fos);
	            for(String filePath : filesListInDir){
	                System.out.println("Zipping "+filePath);
	                //for ZipEntry we need to keep only relative file path, so we used substring on absolute path
	                ZipEntry ze = new ZipEntry(filePath.substring(dir.getAbsolutePath().length()+1, filePath.length()));
	                zos.putNextEntry(ze);
	                //read the file and write to ZipOutputStream
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
		        if(files != null) {
		        for(File file : files){
		            if(file.isFile()) filesListInDir.add(file.getAbsolutePath());
		            else populateFilesList(file);
		            
		        }
		        }
		        return filesListInDir;
		    }
	}
	
	



	@GET
	@Path("generatedClients")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String retriveActivatedClientsData(@Context final UriInfo uriInfo) {
		this.context.authenticatedUser().validateHasReadPermission(CKYCConstants.CKYC_RESOURCE_NAME);

		final List<CKYCData> searchedApprovedLoans = this.cKYCReadPlatformService.retriveActivatedClientsData();

		final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper
				.process(uriInfo.getQueryParameters());
		return this.toApiJsonSerializer.serialize(settings, searchedApprovedLoans,
				CKYCConstants.CKYC_RESPONSE_DATA_PARAMETERS);
	}
	
	@POST
    @Consumes({ MediaType.MULTIPART_FORM_DATA })
    @Produces({ MediaType.APPLICATION_JSON })
    public String createDocument(@HeaderParam("Content-Length") final Long fileSize, @FormDataParam("file") final InputStream inputStream,
            @FormDataParam("file") final FormDataContentDisposition fileDetails, @FormDataParam("file") final FormDataBodyPart bodyPart,
            @FormDataParam("name") final String name, @FormDataParam("description") final String description) {

        
        final textcommand textCommand = new textcommand( null, name, fileDetails.getFileName(),
                fileSize, bodyPart.getMediaType().toString(), description, null);

        final Long ckycdata = this.cKYCWritePlatformService.readfile(textCommand, inputStream);

        return this.toApiJsonSerializer.serialize(CommandProcessingResult.resourceResult(ckycdata, null));
    }

}
