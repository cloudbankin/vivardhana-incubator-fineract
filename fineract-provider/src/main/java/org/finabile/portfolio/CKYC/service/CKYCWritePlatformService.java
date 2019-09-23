package org.finabile.portfolio.CKYC.service;

import java.io.File;
import java.io.InputStream;
import java.util.List;

import org.finabile.portfolio.CKYC.command.textcommand;

public interface CKYCWritePlatformService {

	File fileWriterForApprovedLoansDetails(List<String> strData,int value);
	
	Long readfile(textcommand textCommand, InputStream inputStream);

}
