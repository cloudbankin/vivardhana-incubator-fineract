package org.finabile.portfolio.CKYC.service;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.imageio.ImageIO;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.apache.fineract.infrastructure.codes.domain.CodeValue;
import org.apache.fineract.infrastructure.codes.domain.CodeValueRepository;
import org.apache.fineract.infrastructure.documentmanagement.data.DocumentData;
import org.apache.fineract.infrastructure.documentmanagement.domain.Image;
import org.apache.fineract.infrastructure.documentmanagement.service.DocumentReadPlatformService;
import org.apache.fineract.portfolio.client.data.ClientIdentifierData;
import org.apache.fineract.portfolio.client.domain.Client;
import org.apache.fineract.portfolio.client.domain.ClientAddress;
import org.apache.fineract.portfolio.client.domain.ClientAddressRepository;
import org.apache.fineract.portfolio.client.domain.ClientAddressRepositoryWrapper;
import org.apache.fineract.portfolio.client.domain.ClientIdentifier;
import org.apache.fineract.portfolio.client.domain.ClientIdentifierRepository;
import org.apache.fineract.portfolio.client.domain.ClientRepositoryWrapper;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.finabile.portfolio.CKYC.command.textcommand;
import org.finabile.portfolio.CKYC.data.CKYCData;
import org.finabile.portfolio.CKYC.domain.CKYCRepositoryWrapper;
import org.finabile.portfolio.CKYC.domain.CKYC;
import org.finabile.portfolio.CKYC.exception.AddressNotFoundException;
import org.finabile.portfolio.countryStateDetails.domain.CountryStateDetails;
import org.finabile.portfolio.countryStateDetails.domain.CountryStateRepository;
import org.finabile.portfolio.countryStateDetails.domain.CountryStateRepositoryWrapper;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CKYCWritePlatformServiceImpl implements CKYCWritePlatformService {

	private final ClientRepositoryWrapper clientRepositoryWrapper;
	private final CKYCRepositoryWrapper cKYCRepositoryWrapper;
	private final CKYCReadPlatformService cKYCReadPlatformService;
	private final ClientAddressRepositoryWrapper clientAddressRepositoryWrapper;
	private final ClientAddressRepository clientAddressRepository;
	private final ClientIdentifierRepository clientIdentifierRepository;
    private final DocumentReadPlatformService documentReadPlatformService;
    private final CodeValueRepository codeValueRepository;
    private final CountryStateRepositoryWrapper countryStateRepositoryWrapper;
	private final CountryStateRepository countryStateRepository;


	@Autowired
	public CKYCWritePlatformServiceImpl(final ClientRepositoryWrapper clientRepositoryWrapper,
			final CKYCRepositoryWrapper cKYCRepositoryWrapper, final CKYCReadPlatformService cKYCReadPlatformService,
			final ClientAddressRepositoryWrapper clientAddressRepositoryWrapper,
			final ClientIdentifierRepository clientIdentifierRepository,
			final DocumentReadPlatformService documentReadPlatformService,
			final CodeValueRepository codeValueRepository,
			final CountryStateRepositoryWrapper countryStateRepositoryWrapper,
			final CountryStateRepository countryStateRepository,
			final ClientAddressRepository clientAddressRepository) {
		this.clientRepositoryWrapper = clientRepositoryWrapper;
		this.cKYCRepositoryWrapper = cKYCRepositoryWrapper;
		this.cKYCReadPlatformService = cKYCReadPlatformService;
		this.clientAddressRepositoryWrapper = clientAddressRepositoryWrapper;
		this.clientIdentifierRepository = clientIdentifierRepository;
		this.documentReadPlatformService = documentReadPlatformService;
		this.codeValueRepository = codeValueRepository;
		this.countryStateRepository=countryStateRepository;
		this.countryStateRepositoryWrapper=countryStateRepositoryWrapper;
		this.clientAddressRepository=clientAddressRepository;
	}

	@Override
	public File fileWriterForApprovedLoansDetails(List<String> strData,int fvalue) {
	
		File errorFile = null;
		File fileDirectorySameTemplate = null;
		File fileDirectoryDiffTemplate =null;
		File fileDirectoryzip = null;
		
		List<Client> clientDetails = new ArrayList();
		List<Client> errorClientDetailsForAdress = new ArrayList();
		List<Client> errorClientDetailsForFamily = new ArrayList();
		List<Client> errorClientDetailsForAdressGender = new ArrayList();
		
		try {
			String currentDateDetials = new SimpleDateFormat("ddMMyyyyhhmmss").format(new Date());
			String currentDateDetials1 = new SimpleDateFormat("dd-MM-yyyy").format(new Date());
			String home = System.getProperty("user.home");
//		File path = new File("C:\\HedgeFinance\\ckyc");
			//File path = new File("C:\\Users\\Administrator\\Desktop\\CKYC");
			File path = new File(home, "CKYC");
		
			File[] files = path.listFiles();
	      if(files != null) {
			for(File file : files){
	            file.delete();
	            
	        }
	      }
		//	FileUtils.cleanDirectory(path); 
			String fileNameForFinalVerification="IN2958_HO_"+currentDateDetials+"_IA004792_U000"+fvalue;
//			FileWriter fw = new FileWriter("C:\\HedgeFinance\\ckyc\\"+fileNameForFinalVerification+".txt");
//			FileWriter fwError = new FileWriter("C:\\HedgeFinance\\Error_File.txt");
			
			
			path.mkdir();
		      fileDirectorySameTemplate = new File(path, fileNameForFinalVerification+".txt");
		      fileDirectoryDiffTemplate =new File(path,"Error_File.txt");
		      try {
		      fileDirectorySameTemplate.createNewFile();
		      fileDirectoryDiffTemplate.createNewFile();

		      } catch (IOException e) {
		      e.printStackTrace();
		      }
			
			FileWriter fw = new FileWriter(fileDirectorySameTemplate);
			
			Iterator itr = strData.iterator();
			
			while (itr.hasNext()) {
				Long longId = Long.parseLong(itr.next().toString());

				Client clientDetail = this.clientRepositoryWrapper.getClientDetails(longId);
				clientDetails.add(clientDetail);
			}
			if (!clientDetails.isEmpty()) {
				
				String addresstypeid="";
				String NameIntial = "";
				String gender = "";
				String martialStatus = "";
				String adddressCode = "";
				String StateDetails = "";
				String occupationData = "";
				int i = 1;
				String batchNumber = new SimpleDateFormat("YYYYMMdd").format(new Date());
				int totalClientDetails = clientDetails.size();
				
				Random r = new Random( System.currentTimeMillis() );
			    int batchno= ((1 + r.nextInt(2)) * 1000 + r.nextInt(1000));
			    String regionCode ="TN";
			    int sizeDetail = clientDetails.size();
			   while(sizeDetail < 0) {
				   for(Client clientData : clientDetails) {
					   if(clientData.getOffice().getName().equals("Puducherry")) {
						   regionCode = "PY";
					   }
				   }
				   sizeDetail--;
			   }
				
			 // ckyc file details
				fw.write(10 + "|" +batchNumber+ batchno + "|" + "IN2828|"+regionCode+"|" + totalClientDetails + "|" + currentDateDetials1
						+ "|V1.1|||||\n");
				
		
				for (Client client : clientDetails) {
					try {
					// bank remittance validations
					List<ClientIdentifier> identifiersData = new ArrayList<ClientIdentifier>();
					List<String> clientFileLocation = new ArrayList<String>();
					if (client != null) {
						try {
							CKYC ckycdetails = this.cKYCRepositoryWrapper.findOneWithNotFoundDetection(client.getId());
							 identifiersData = this.clientIdentifierRepository
									.getIdentifierDetails(client.getId());
							if (ckycdetails == null) {
								CKYC ckycDataDetails = CKYC.addingRefference(client);
								this.cKYCRepositoryWrapper.save(ckycDataDetails);
							}
						} catch (NullPointerException e) {
							e.printStackTrace();
						}
					}
					//String[] str = {client.getId().toString()};
					CodeValue genderDetails = client.getGender();
					
					if(genderDetails == null) {
						//throw new AddressNotFoundException("error.ckyc.gender.not.found", " Given client doesn't has a gender details ",str);
						errorClientDetailsForAdressGender.add(client);
					}
					
					

					if (genderDetails.getlabel().equalsIgnoreCase("male")) {
						NameIntial = "Mr";
						gender = "M";

					} else {
						NameIntial = "Ms";
						gender = "F";
					}
					
					//Family Details Checking
					List<CKYCData> familyDetailsList = this.cKYCReadPlatformService.getClientFamilyDetails(client);
					CKYCData familyDetails = null;
					if(familyDetailsList.isEmpty()) {
						errorClientDetailsForFamily.add(client);
					}else if(!familyDetailsList.isEmpty()) {
					for(CKYCData familyDetailsData : familyDetailsList) {
						familyDetails = familyDetailsData;
					}
					}
					
					
//					ClientAddress address = this.clientAddressRepositoryWrapper
//							.findOneByClientIdAddress(client.getId());
					ClientAddress address=null;
					
					List<ClientAddress>  address1 = this.clientAddressRepository
							.findAddressByClient(client.getId());
					for(ClientAddress clientAddress:address1)
					 {
						if(clientAddress.getAddressType().getId()==14l)
						{
						 address = clientAddress;
						 break;
						}
						if(clientAddress.getAddressType().getId()==15l)
						{
						 address = clientAddress;
						 break;
						}
						if(clientAddress.getAddressType().getId()==16l)
						{
						 address = clientAddress;
						 break;
						}
						if(clientAddress.getAddressType().getId()==17l)
						{
						 address = clientAddress;
						 break;
						}
					 }
					
					
					//Client Address checking whether it has a address or not 
					if(address == null) {
						//throw new AddressNotFoundException("error.ckyc.address.not.found","given client doesn't has a address",str) ;
						errorClientDetailsForAdress.add(client);
						
					}
					

					if (familyDetails.getMaritalStatus().equals("1")) {
						martialStatus = "01";
						if(genderDetails.getlabel().equalsIgnoreCase("male")) {
							NameIntial = "MR";
						}else {
							NameIntial = "MRS";
						}
					} else {
						martialStatus = "02";
					}
					
					CodeValue addressType = address.getAddressType();
//					String StateDetailsMatch = address.getAddress().getStateProvince();
					String StateDetailsMatch = "Tamil Nadu";
					if(StateDetailsMatch.equals("Tamil Nadu")) {
						StateDetails = "TN";
					}else if(StateDetailsMatch.equals("Kerala")) {
						StateDetails = "KL";
					}
					if (addressType.getlabel().equalsIgnoreCase("Permanent address")||(addressType.getlabel().equalsIgnoreCase("Temporary address with period / reason"))) {
						adddressCode = "01";
					} else if (addressType.getlabel().equalsIgnoreCase("Residential address")) {
						adddressCode = "02";
					} else if (addressType.getlabel().equalsIgnoreCase("Office address")) {
						adddressCode = "04";
					}
					int documentCount =0;
					for(ClientIdentifier clientIdentifierData : identifiersData) {
						documentCount++;
					}
					

					String occID = familyDetails.getOccupation();
					Long occupationId = Long.parseLong(occID);
					CodeValue occupationDetials = this.codeValueRepository.findOne(occupationId);
					if(occupationDetials.getlabel().equals("Bussiness")) {
						occupationData = "B-01";
					}else if(occupationDetials.getlabel().equals("Others - Professional")) {
						occupationData = "O-01";
					}else if(occupationDetials.getlabel().equals("Others - Self Employed")) {
						occupationData = "O-02";
					}else if(occupationDetials.getlabel().equals("Others - Retired")) {
						occupationData = "O-03";
					}else if(occupationDetials.getlabel().equals("Others - Housewife")) {
						occupationData = "O-04";
					}else if(occupationDetials.getlabel().equals("Service - Public Sector")) {
						occupationData = "S-01";
					}else if(occupationDetials.getlabel().equals("Service - Private Sector ")) {
						occupationData = "S-02";
					}else if(occupationDetials.getlabel().equals("Service - Government Sector")) {
						occupationData = "S-03";
					}else if(occupationDetials.getlabel().equals("Not Categorised")) {
						occupationData = "X-01";
					}	
					
					String dateOfBirth = new SimpleDateFormat("dd-MM-yyyy").format(client.getDateOfBirth());
					
					
					List<DocumentData> clientDocumentDetailLists1=new ArrayList<DocumentData>();
					for(ClientIdentifier clientDocumentDetails1 : identifiersData) {
						DocumentData documentData = this.cKYCReadPlatformService.readDocumentDetails("client_identifiers",clientDocumentDetails1.getId());
						clientDocumentDetailLists1.add(documentData);
						//clientFileLocation.add(documentData.getLocation());
						switch (clientDocumentDetails1.getDocumentType().getlabel()) {
						case "Voter ID":
							addresstypeid="04";
							break;	
						case "Passport":
							addresstypeid="02";
							break;
						case "Aadhar":
							addresstypeid="01";
							break;
						case "Drivers License":
							addresstypeid="03";
							break;
													
						default:
							break;
						}
						
					}
					//Client branch code and region code
					String officeName = client.getOffice().getName();
					String branchName = "";
					 regionCode = "";
					switch(officeName) {
					case "Head Office":
						branchName = "HO";
						regionCode = "TN";
						break;
					case "Tirunelveli":
						branchName = "TIRUNELVEL";
						regionCode = "TN";
						break;
					case "Puducherry":
						branchName = "PUDUCHERRY";
						regionCode = "PY";
						break;
					case "Tenkasi":
						branchName = "TENKASI";
						regionCode = "TN";
						break;
					case "Tiruchendur":
						branchName = "TIRUCHENDU";
						regionCode = "TN";
						break;
					case "Madurai":
						branchName = "MADURAI";
						regionCode = "TN";
						break;
					case "Tuticorin":
						branchName = "TUTICORIN";
						regionCode = "TN";
						break;
					case "Nagercoil":
						branchName = "NAGERCOIL";
						regionCode = "TN";
						break;
					case "Srivanjiyam":
						branchName = "SRIVANJIYA";
						regionCode = "TN";
						break;
					case "Mylapore-Chennai":
						branchName = "HO";
						regionCode = "TN";
						break;
						default:
							branchName="";
							regionCode="";
						
					}
					
					String statecode=address.getAddress().getPostalCode();
					CountryStateDetails csd = this.countryStateRepository.findBypincode(Long.parseLong(statecode));
//					String middname="";
//					if(!client.getMiddlename().equals(null))
//					{
//					 middname=client.getMiddlename();
//					}
					
					
					
					
					
					// client Details
					fw.write(20 + "|" + i + "|01|"+branchName+"|||||||||||01|||01|"+client.getId()+"|" + NameIntial + "|"
							+ client.getFirstname() + "||" + client.getLastname() + "|||||||01|MR|"
							+ familyDetails.getFatherName() + "||||MRS|"+ familyDetails.getMotherName() + "||||" + gender + "|" + martialStatus + "|IN|"
							+ occupationData + "|" + dateOfBirth + "|||||||||01|02|||||"
							+ adddressCode + "|" + address.getAddress().getAddressLine1() + "|"+address.getAddress().getAddressLine2()+"||"
							+address.getAddress().getAddressLine3()+"|" + address.getAddress().getCity()  + "|"
							+ csd.getStateCode() + "|IN|" + address.getAddress().getPostalCode()
							+ "|"+addresstypeid+"||Y|||||||||||||||||||||||||+91|"+client.getMobileNo()+"|||||" + currentDateDetials1 + "|"+client.getOffice().getName()+"|" + currentDateDetials1
							+ "|01|Mr Vijayakumar|Manager|Head Office|0005|Vivardhana Microfinance Ltd|IN2828|"+i+"|0|0|0|"+(documentCount+1)+"||||||" + "\n");

					// client identity details
					int j =1;
					for(ClientIdentifier clientIdentifierData : identifiersData) {
						
						/*switch(clientIdentifierData.getDocumentType().getlabel()) {
						case "Ration card":
							fw.write(30 + "|" + j + "|Z|"+clientIdentifierData.getDocumentKey()+"||01|02|||||\n");
							break;
						case "Voter ID":
							fw.write(30 + "|" + j + "|B|"+clientIdentifierData.getDocumentKey()+"||01|02|||||\n");
							break;
						case "Passport":
							fw.write(30 + "|" + j + "|A|"+clientIdentifierData.getDocumentKey()+"|18-12-2020|01|02|||||\n");
							break;
					
						case "Aadhar Card":
							fw.write(30 + "|" + j + "|E|"+clientIdentifierData.getDocumentKey()+"||01|02|||||\n");
							break;
						case "Drivers License":
							fw.write(30 + "|" + j + "|D|"+clientIdentifierData.getDocumentKey()+"|18-12-2020|01|02|||||\n");
							break;
						case "PAN":
							fw.write(30 + "|" + j + "|C|"+clientIdentifierData.getDocumentKey()+"||01|02|||||\n");
							break;
						default :
							break;
						}
						j++;*/
						
						if(clientIdentifierData.getDocumentType().getlabel().equals("Aadhar")) {
							fw.write(30 + "|" + j + "|E|"+clientIdentifierData.getDocumentKey()+"||01|02|||||\n");
						}
					
					}
				/*	// doubt
					fw.write(40 + "|" + i
							+ "|1|01||MR|Param|||||||||||||||||||||||||||||||S1269879|12-10-2091|||||||||||||||||12-12-2016|Mumbai|12-12-2016|01|Mr SHIVAM Prakash|MANAGER|South Mumbai|2136|XYZ Bank|IN0467|||||\n");
*/
			

					
					
					List<DocumentData> clientDocumentDetailLists=new ArrayList<DocumentData>();
					for(ClientIdentifier clientDocumentDetails : identifiersData) {
						DocumentData documentData = this.cKYCReadPlatformService.readDocumentDetails("client_identifiers",clientDocumentDetails.getId());
						clientDocumentDetailLists.add(documentData);
						clientFileLocation.add(documentData.getLocation());
						switch (clientDocumentDetails.getDocumentType().getlabel()) {
						case "Voters ID":
							fw.write(70 + "|" + i + "|"+documentData.getFileName()+"|07|01|"+branchName+"||||\n");
							break;	
						case "Passport":
							fw.write(70 + "|" + i + "|"+documentData.getFileName()+"|05|01|"+branchName+"||||\n");
							break;
						case "Aadhar":
							fw.write(70 + "|" + i + "|"+documentData.getFileName()+"|04|01|"+branchName+"||||\n");
							break;
						case "Drivers License":
							fw.write(70 + "|" + i + "|"+documentData.getFileName()+"|06|01|"+branchName+"||||\n");
							break;
						case "PAN":
							fw.write(70 + "|" + i + "|"+documentData.getFileName()+"|03|01|"+branchName+"||||\n");
							break;
							
						default:
							break;
						}
						
					}
					
					
						
	
					
					
					// commented for vivardhana requirement
					/*DocumentData documentData = this.cKYCReadPlatformService.readDocumentDetails("clients",client.getId());
					clientFileLocation.add(documentData.getLocation());
					
					if(documentData != null) {
						fw.write(70 + "|" + i + "|"+documentData.getFileName()+"|09|02|EKM||||\n");
					}*/
					String profileLocation = client.getImage().getLocation(); 
					clientFileLocation.add(profileLocation);
					String profileLocationAlter = profileLocation.replace('\\', ',');
					String[] profileLocationName =profileLocationAlter.split(",");
					String clientProfileName = "";
					for(int c = 0;c <= profileLocationName.length-1;c++) {
						if(c == profileLocationName.length-1) {
							clientProfileName=profileLocationName[c];

						}
						}
					fw.write(70 + "|" + i + "|"+clientProfileName+"|02|01|"+branchName          +"||||\n");
				//	final DocumentData documentData = this.documentReadPlatformService.retrieveDocument("client_identifiers", client.getId(), documentId);
						
			

						// client document details
						
						
						
				/*	fw.write(70 + "|" + i + "|SampleProfile1.png|02|02|EKM||||\n");
					fw.write(70 + "|" + i + "|A_sample_of_Permanent_Account_Number_(PAN)_Card.jpg|03|02|EKM||||\n");
					fw.write(70 + "|" + i + "|Aadhaar-card-sample-300x212.png|04|02|EKM||||\n");
					fw.write(70 + "|" + i + "|hqdefault.jpg|07|02|EKM||||\n");
					fw.write(70 + "|" + i + "|687474703a2f2f692e696d6775722e636f6d2f646e5873344e442e706e67.png|09|02|EKM||||\n");*/

					FileInputStream fis = null;
					
					ZipOutputStream zipOut = null;
					
					String fileName = currentDateDetials+"_"+client.getAccountNumber();
				      fileDirectoryzip = new File(path, fileName+".zip");
				      try {
				    	  fileDirectoryzip.createNewFile();
				      
				      } 
				      catch (IOException e) {
				      e.printStackTrace();
				      }
					
					int length=0,width=0;
					
					try {
//						 FileOutputStream fos = new FileOutputStream("C:\\HedgeFinance\\ckyc\\"+fileName+".zip");
						 FileOutputStream fos = new FileOutputStream(fileDirectoryzip); //nag
						 zipOut = new ZipOutputStream(new BufferedOutputStream(fos));
						 for(String res : clientFileLocation) {
							 File input = new File(res);
							 File output;
							 String fname=input.getName();
							 if (fname.indexOf(".") > 0)
							 {
								 fname = fname.substring(0, fname.lastIndexOf("."));
						        //return fname;
							 }
							 Long testsize=input.length()/1024;
							 if(testsize>50)
							 {	 
							 BufferedImage image = ImageIO.read(input);
							 
						     BufferedImage resized = resize(image, 250, 250);
//						     output = new File("C:\\HedgeFinance\\images\\"+fname+".png");
						     output = new File(path,fname+".png");
						        ImageIO.write(resized, "png", output);
						        
						        
							 }
							 else {
							 output =new File(res);
							 }
							 fis = new FileInputStream(output);
							 ZipEntry ze = new ZipEntry(output.getName());
							 System.out.println("Zipping the file: "+output.getName());
					         zipOut.putNextEntry(ze);
					         byte[] tmp = new byte[4*1024];
					         int size = 0;
					         while((size = fis.read(tmp)) != -1){
					             zipOut.write(tmp, 0, size);
					         }
					         zipOut.flush();
					         fis.close();
					         //output.delete();
						}
						 zipOut.close();
					}catch (FileNotFoundException e) {
						e.printStackTrace();
					}catch (IOException e) {
						e.printStackTrace();
					}
					

					i++;
				}catch (IOException e) {
					e.printStackTrace();
				} catch (NullPointerException e) {
					e.printStackTrace();
				}
					}

			}
			

			fw.flush();
			fw.close();

		} catch (IOException e) {
			e.printStackTrace();
		} catch (NullPointerException e) {
			e.printStackTrace();
		}
		if(!errorClientDetailsForAdress.isEmpty() || !errorClientDetailsForFamily.isEmpty() || !errorClientDetailsForAdressGender.isEmpty()) {
			 errorFile = errorSavingFile(errorClientDetailsForAdress,errorClientDetailsForFamily,errorClientDetailsForAdressGender);
		}
		
		return errorFile;
	}

	private File errorSavingFile(List<Client> errorClientDetailsForAdress, List<Client> errorClientDetailsForFamily, List<Client> errorClientDetailsForAdressGender) {
		
		String home = System.getProperty("user.home");
		
		try {
					
//			FileWriter fwError = new FileWriter("C:\\HedgeFinance\\Error_File.txt");
			FileWriter fwError = new FileWriter(home+"/CKYC/Error_File.txt");
			
			fwError.write("----Given Client Details Found Some Error---- \n");
			
			if(!errorClientDetailsForAdress.isEmpty()) {
				fwError.write("----Given Client Details has error in Address Details---- \n");
				fwError.write("Client details\n");
				for(Client clientData : errorClientDetailsForAdress) {
				fwError.write("ClientId - "+clientData.getId()+",Client Name - "+clientData.getDisplayName()+"\n");
				}
			}
			if(!errorClientDetailsForFamily.isEmpty()) {
				fwError.write("----Given Client Details has error in Family Details---- \n");
				fwError.write("Client details\n");
				for(Client clientData : errorClientDetailsForFamily) {
				fwError.write("ClientId - "+clientData.getId()+",Client Name - "+clientData.getDisplayName()+"\n");
				}
			}
			if(!errorClientDetailsForAdressGender.isEmpty()) {
				fwError.write("----Given Client Details has error in gender Details---- \n");
				fwError.write("Client details\n");
				for(Client clientData : errorClientDetailsForAdressGender) {
				fwError.write("ClientId - "+clientData.getId()+",Client Name - "+clientData.getDisplayName()+"\n");
				}
			}
			
			fwError.flush();
			fwError.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
//		return new File("C:\\HedgeFinance\\Error_File.txt");
		return new File(home+"/CKYC/Error_File.txt");
	}
	
	public static byte[] getBytes(InputStream is) throws IOException {

	    int len;
	    int size = 1024;
	    byte[] buf;

	    if (is instanceof ByteArrayInputStream) {
	      size = is.available();
	      buf = new byte[size];
	      len = is.read(buf, 0, size);
	    } else {
	      ByteArrayOutputStream bos = new ByteArrayOutputStream();
	      buf = new byte[size];
	      while ((len = is.read(buf, 0, size)) != -1)
	        bos.write(buf, 0, len);
	      buf = bos.toByteArray();
	    }
	    return buf;
	  }
	
	private static BufferedImage resize(BufferedImage img, int height, int width) {
        java.awt.Image tmp = img.getScaledInstance(width, height, java.awt.Image.SCALE_SMOOTH);
        BufferedImage resized = new BufferedImage(width, height, BufferedImage.TYPE_USHORT_555_RGB);
        //TYPE_INT_ARGB
        Graphics2D g2d = resized.createGraphics();
        g2d.drawImage(tmp, 0, 0, null);
        g2d.dispose();
        g2d.setComposite(AlphaComposite.Src);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY);//VALUE_RENDER_QUALITY
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);//VALUE_ANTIALIAS_ON
        return resized;
    }
	

	
	
//	private static final String SERVER_UPLOAD_LOCATION_FOLDER = "C:\\HedgeFinance\\gckyc\\";
	private static final String SERVER_UPLOAD_LOCATION_FOLDER = "C:\\Users\\Administrator\\Desktop\\gCKYC\\";
	Long vaa=(long) 1;
	public Long readfile(final textcommand textcommand, final InputStream inputStream) {
        try
        {
        	String filePath = SERVER_UPLOAD_LOCATION_FOLDER + textcommand.getFileName();
//        	String filePath = SERVER_UPLOAD_LOCATION_FOLDER + "generatedckyc.txt";
        	
        	saveFile(inputStream, filePath);
//		File f = new File(filePath);
//        Scanner sc = new Scanner(f);
//        while(sc.hasNextLine()){
//            String line = sc.nextLine();
//            String[] details = line.split("|");
//            String id = details[0];
//            String name = details[1];
//            int age = Integer.parseInt(details[2]);
        	File file = new File(filePath);
        	BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            String line = null;
             
            while( (line = br.readLine())!= null ){
                // \\s+ means any number of whitespaces between tokens
            	String fullline=line;
            	String[] data = fullline.split("\\|", -1);
            	if(data[0].equals("20"))
            	{
            		if(!data[5].isEmpty()&&data[5]!=null)
            		{
                		String kycid=data[5];
                		String clientId=data[4];
                		final Client clientForUpdate = this.clientRepositoryWrapper.findOneWithNotFoundDetection(Long.parseLong(clientId));
                		clientForUpdate.setCkycId(kycid);
                		this.clientRepositoryWrapper.save(clientForUpdate);
                		}
            		
            	}
                
            }
            
        }
        
        catch (FileNotFoundException e) {         
            e.printStackTrace();
        } catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return vaa;
		
    }
	
	
	private void saveFile(InputStream uploadedInputStream, String serverLocation) {
	    try {
	        OutputStream outputStream = new FileOutputStream(new File(serverLocation));
	        int read = 0;
	        byte[] bytes = new byte[1024];
	        outputStream = new FileOutputStream(new File(serverLocation));
	        while ((read = uploadedInputStream.read(bytes)) != -1) {
	            outputStream.write(bytes, 0, read);
	        }
	        outputStream.flush();
	        outputStream.close();
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
	}
	
	
	
}
