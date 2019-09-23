/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.sms.service;


import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import javax.net.ssl.HttpsURLConnection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.mifosplatform.infrastructure.sms.vo.EduwizeSMSResponseVO;
import org.mifosplatform.infrastructure.sms.vo.SMSDataVIVO;
import org.mifosplatform.infrastructure.sms.vo.SMSDataVO;
import org.mifosplatform.infrastructure.utils.CommonMethodsUtil;


@Service
public class SmsProcessingServiceImpl implements SmsProcessingService {

    private final static Logger logger = LoggerFactory.getLogger(SmsProcessingServiceImpl.class);

    
    @Autowired
	private  Environment propertyEnv;
    

    @SuppressWarnings("rawtypes")
	@Override
	public boolean sendSMS(SMSDataVO data) {

		boolean isSuccess = false;
		String templateName = "";

		try {
			
			if (CommonMethodsUtil.isBlank(data.getSmsTemplateType())) {
				logger.error("Error in sending Sms : Sms template type is blank");
				return isSuccess;
			}
			
			boolean isActive = Boolean.valueOf(propertyEnv.getProperty("sms."+data.getSmsTemplateType()+".isActive"));
			if (!isActive) {
				logger.error("Error in sending Sms : Sms trigger is inactive");
				return isSuccess;
			}

				templateName = propertyEnv.getProperty("sms.template."+data.getSmsTemplateType());
			
				String requestUrl = propertyEnv.getProperty("sms.url.send");
				URL url = new URL(requestUrl);
				
				HttpsURLConnection con = (HttpsURLConnection) url.openConnection();

				//add reuqest header
				con.setRequestMethod("POST");
				con.setRequestProperty("User-Agent", "Mozilla/5.0");
				con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

				String urlParameters = "username="
						+ propertyEnv.getProperty("sms.username") + "&password="
						+ propertyEnv.getProperty("sms.password") + "&destination="
						+ data.getPhoneNo() + "&template_name="
						+ templateName + "&"
						+ data.getTemplateParameterString() + "&response_format="
						+ propertyEnv.getProperty("sms.response_format") + "&sender_id="
						+ propertyEnv.getProperty("sms.sender") + "";
				// Send post request
				con.setDoOutput(true);
				DataOutputStream wr = new DataOutputStream(con.getOutputStream());
				wr.writeBytes(urlParameters);
				wr.flush();
				wr.close();
				
				if (con.getResponseCode()==200) {
					BufferedReader in = new BufferedReader(new InputStreamReader(
							con.getInputStream()));
					String inputLine;
					StringBuffer response = new StringBuffer();

					while ((inputLine = in.readLine()) != null) {
						response.append(inputLine);
					}
					in.close();
					
					EduwizeSMSResponseVO responseMessage = new EduwizeSMSResponseVO(response.toString());
							
					if(responseMessage.getResponse_code()==401)
					{
						isSuccess = true;
						logger.info(propertyEnv.getProperty("sms.sent.response") + " - "
								+ response.toString());
					}
					else {
					logger.error(propertyEnv.getProperty("sms.sent.response") + " - "
							+ response.toString());
					}
			}

			con.disconnect();
		} catch (Exception ex) {
			isSuccess = false;
			logger.error(propertyEnv.getProperty("sms.sent.exception") + " - "
							+ ex.getMessage());
			return isSuccess;
		}
		return isSuccess;
	}
    
    
    @SuppressWarnings("rawtypes")
	@Override
	public boolean sendSMSinTamil(SMSDataVIVO data) {

		boolean isSuccess = false;

		try {
			
			if (CommonMethodsUtil.isBlank(data.getSmsTemplateType())) {
				logger.error("Error in sending Sms : Sms template type is blank");
				return isSuccess;
			}
			
			boolean isActive = Boolean.valueOf(propertyEnv.getProperty("sms."+data.getSmsTemplateType()+".isActive"));
			if (!isActive) {
				logger.error("Error in sending Sms : Sms trigger is inactive");
				return isSuccess;
			}
			
//    		String contentMessage = "தங்களது கடன் விண்ணப்பத்தை ஏற்று, தாங்கள் கோரி உள்ள கடன் தொகையான ரூபாய் 30000 அனுமதிக்கப்பட்டுஉள்ளது. இதில் காப்பீடு மற்றும் பரிசீலனை தொகையாக ரூபாய் 704 பிடித்தம் போக, ரூபாய் 29296 தங்களது SBI வங்கி சேமிப்பு கணக்கு எண் HABILE12345 இற்கு இரண்டு நாட்களில் அனுப்பப்படும். இதற்கான வட்டி ஆண்டிற்கு 24 சதவிகிதம். இந்த கடன் தொகையை மாதம் ரூபாய் 1,581 விகிதம் 24 மாதங்களில் திருப்பி செலுத்தவேண்டும். - விவர்தனா மைக்ரோபைனான்ஸ் லிமிடெட்.";
        	
			String contentMessage = data.getMsg();
			
			String requestUrl = "https://fastsms.way2mint.com/SendSMS/sendmsg.php?"
			    	+ "uname=" + URLEncoder.encode(propertyEnv.getProperty("sms.username"), "UTF-8")
			    	+ "&pass=" + URLEncoder.encode(propertyEnv.getProperty("sms.password"), "UTF-8")
			    	+ "&send="+ URLEncoder.encode(propertyEnv.getProperty("sms.sender"), "UTF-8")
			    	+ "&dest="+ URLEncoder.encode(data.getPhoneNo(), "UTF-8")
			    	+ "&msg="+ URLEncoder.encode(contentMessage, "UTF-8") + "&unicode=1";

			    	System.out.println("URL--------->"+requestUrl);

			    	URL url = new URL(requestUrl);

			    	HttpURLConnection uc = (HttpURLConnection) url.openConnection();
			    	uc.disconnect();
	    	int	response = uc.getResponseCode();
	    	logger.info(propertyEnv.getProperty("sms.sent.response") + " - "+ response);
	    	isSuccess = true;
			System.out.println(response);				
		
		} catch (Exception ex) {
			isSuccess = false;
			logger.error(propertyEnv.getProperty("sms.sent.exception") + " - "
							+ ex.getMessage());
			return isSuccess;
		}
		return isSuccess;
	}

    public static void main(String args[]){
    	SmsProcessingServiceImpl sms=new SmsProcessingServiceImpl();
    	int response = 0;
    	try {
    		String[] ary = {"Prashanth","INR","100","05/04/2018"};
//			String message =  "Dear {0}, you need to pay the installment amount of {1} {2} before {3}.If already paid please ignore it - Cash Suvidha";
			
    		String message = "தங்களது கடன் விண்ணப்பத்தை ஏற்று, தாங்கள் கோரி உள்ள கடன் தொகையான ரூபாய் ....... அனுமதிக்கப்பட்டுஉள்ளது. இதில் காப்பீடு மற்றும் பரிசீலனை தொகையாக ரூபாய்";
    		
    		String test = "தங்களது கடன் விண்ணப்பத்தை ஏற்று, தாங்கள் கோரி உள்ள கடன் தொகையான ரூபாய் ....... அனுமதிக்கப்பட்டுஉள்ளது. இதில் காப்பீடு மற்றும் பரிசீலனை தொகையாக ரூபாய்";
    		System.out.print(test);
    		String content="";
			for(int i=0;i<ary.length;i++){
				// content=content+ary[i]+",";
				String s=String.valueOf(i);
				message=message.replace('{'+s+'}', ary[i]);
			}
			System.out.println(message);
    	String username = "Vivardhana";
    	String password = "api@123";
    	String sender = "ALERTS";
    	String sendto = "9944595166";
    //	String message="Hello there!";
    	String requestUrl = "https://fastsms.way2mint.com/SendSMS/sendmsg.php?"
    	+ "uname=" + URLEncoder.encode(username, "UTF-8")
    	+ "&pass=" + URLEncoder.encode(password, "UTF-8")
    	+ "&send="+ URLEncoder.encode(sender, "UTF-8")
    	+ "&dest="+ URLEncoder.encode(sendto, "UTF-8") 
    	+ "&msg="+ URLEncoder.encode(message, "UTF-8") + "&unicode=1";
    	

    	System.out.println("URL--------->"+requestUrl);

    	URL url = new URL(requestUrl);

    	HttpURLConnection uc = (HttpURLConnection) url.openConnection();
    	uc.disconnect();

    	response = uc.getResponseCode();

    	System.out.println(response);

    	} catch (Exception ex) {
    	System.out.println(ex.getMessage());

    	}
    	
    }


}