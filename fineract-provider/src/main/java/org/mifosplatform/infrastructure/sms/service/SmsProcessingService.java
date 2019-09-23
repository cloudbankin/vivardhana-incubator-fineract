package org.mifosplatform.infrastructure.sms.service;

import org.mifosplatform.infrastructure.sms.vo.SMSDataVO;
import org.mifosplatform.infrastructure.sms.vo.SMSDataVIVO;

public interface SmsProcessingService {

    boolean sendSMS(SMSDataVO data);

	boolean sendSMSinTamil(SMSDataVIVO data);

}