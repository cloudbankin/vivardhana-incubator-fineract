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
package org.apache.fineract.portfolio.collectionsheet.service;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.chrono.ChronoLocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResultBuilder;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.exception.endDateException;
import org.apache.fineract.infrastructure.core.exception.startDateException;
import org.apache.fineract.infrastructure.core.service.RoutingDataSource;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.client.data.LoanDetailData;
import org.apache.fineract.portfolio.collectionsheet.command.CollectionSheetBulkDisbursalCommand;
import org.apache.fineract.portfolio.collectionsheet.command.CollectionSheetBulkRepaymentCommand;
import org.apache.fineract.portfolio.collectionsheet.data.Bulk;
import org.apache.fineract.portfolio.collectionsheet.data.BulkReminderData;
import org.apache.fineract.portfolio.collectionsheet.data.CollectionSheetTransactionDataValidator;
import org.apache.fineract.portfolio.collectionsheet.serialization.CollectionSheetBulkDisbursalCommandFromApiJsonDeserializer;
import org.apache.fineract.portfolio.collectionsheet.serialization.CollectionSheetBulkRepaymentCommandFromApiJsonDeserializer;
import org.apache.fineract.portfolio.loanaccount.service.LoanWritePlatformService;
import org.apache.fineract.portfolio.meeting.service.MeetingWritePlatformService;
import org.apache.fineract.portfolio.paymentdetail.domain.PaymentDetail;
import org.apache.fineract.portfolio.paymentdetail.domain.PaymentDetailAssembler;
import org.apache.fineract.portfolio.paymentdetail.service.PaymentDetailWritePlatformService;
import org.apache.fineract.portfolio.paymenttype.data.PaymentTypeData;
import org.apache.fineract.portfolio.savings.data.SavingsAccountTransactionDTO;
import org.apache.fineract.portfolio.savings.domain.DepositAccountAssembler;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountTransaction;
import org.apache.fineract.portfolio.savings.service.DepositAccountWritePlatformService;
import org.apache.fineract.useradministration.domain.AppUser;
import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.sms.service.SmsProcessingServiceImpl;
import org.mifosplatform.infrastructure.sms.vo.SMSDataVIVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

@Service
public class CollectionSheetWritePlatformServiceJpaRepositoryImpl implements CollectionSheetWritePlatformService  
{

	private final JdbcTemplate jdbcTemplate;
    private final LoanWritePlatformService loanWritePlatformService;
    private final CollectionSheetBulkRepaymentCommandFromApiJsonDeserializer bulkRepaymentCommandFromApiJsonDeserializer;
    private final CollectionSheetBulkDisbursalCommandFromApiJsonDeserializer bulkDisbursalCommandFromApiJsonDeserializer;
    private final CollectionSheetTransactionDataValidator transactionDataValidator;
    private final MeetingWritePlatformService meetingWritePlatformService;
    private final DepositAccountAssembler accountAssembler;
    private final DepositAccountWritePlatformService accountWritePlatformService;
    private final PaymentDetailAssembler paymentDetailAssembler;
    private final PaymentDetailWritePlatformService paymentDetailWritePlatformService;
    private final SmsProcessingServiceImpl smsProcessingServiceImpl;
    private final PlatformSecurityContext context;


    @Autowired
    public CollectionSheetWritePlatformServiceJpaRepositoryImpl(final LoanWritePlatformService loanWritePlatformService, final RoutingDataSource dataSource,
            final CollectionSheetBulkRepaymentCommandFromApiJsonDeserializer bulkRepaymentCommandFromApiJsonDeserializer,
            final CollectionSheetBulkDisbursalCommandFromApiJsonDeserializer bulkDisbursalCommandFromApiJsonDeserializer,
            final CollectionSheetTransactionDataValidator transactionDataValidator,
            final MeetingWritePlatformService meetingWritePlatformService, final DepositAccountAssembler accountAssembler,
            final DepositAccountWritePlatformService accountWritePlatformService, final PaymentDetailAssembler paymentDetailAssembler, final PaymentDetailWritePlatformService paymentDetailWritePlatformService
            , final SmsProcessingServiceImpl smsProcessingServiceImpl,
            final PlatformSecurityContext context) {
        this.loanWritePlatformService = loanWritePlatformService;
        this.bulkRepaymentCommandFromApiJsonDeserializer = bulkRepaymentCommandFromApiJsonDeserializer;
        this.bulkDisbursalCommandFromApiJsonDeserializer = bulkDisbursalCommandFromApiJsonDeserializer;
        this.transactionDataValidator = transactionDataValidator;
        this.meetingWritePlatformService = meetingWritePlatformService;
        this.accountAssembler = accountAssembler;
        this.accountWritePlatformService = accountWritePlatformService;
        this.paymentDetailAssembler = paymentDetailAssembler;
        this.paymentDetailWritePlatformService = paymentDetailWritePlatformService;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.smsProcessingServiceImpl = smsProcessingServiceImpl;
        this.context = context;
    }

    @Override
    public CommandProcessingResult updateCollectionSheet(final JsonCommand command) {
    	
    	boolean checkMeetingDateWithRepaymentDate = true;

        this.transactionDataValidator.validateTransaction(command);

        final Map<String, Object> changes = new HashMap<>();
        changes.put("locale", command.locale());
        changes.put("dateFormat", command.dateFormat());

        final String noteText = command.stringValueOfParameterNamed("note");
        if (StringUtils.isNotBlank(noteText)) {
            changes.put("note", noteText);
        }
        
        Map<String, org.joda.time.LocalDate> transactionOrRepaymentdate = this.bulkRepaymentCommandFromApiJsonDeserializer.transactionOrRepaymentdate(command);
        
        for ( Map.Entry<String, org.joda.time.LocalDate> entry : transactionOrRepaymentdate.entrySet()) 
        {
        	String key = entry.getKey();
        	
        	if(key.equals("repaymentDate"))
        	{
        		checkMeetingDateWithRepaymentDate = false;
        	}
        }
        
        final PaymentDetail paymentDetail = this.paymentDetailWritePlatformService.createAndPersistPaymentDetail(command, changes);
        
        changes.putAll(updateBulkReapayments(command, paymentDetail));

        changes.putAll(updateBulkDisbursals(command));

        changes.putAll(updateBulkMandatorySavingsDuePayments(command, paymentDetail));
        
        // Habile changes Repayment Problem
        if(checkMeetingDateWithRepaymentDate == true)
        {
        	 this.meetingWritePlatformService.updateCollectionSheetAttendance(command);
        }
        
        //Habile changes 
        final Long officeId = command.longValueOfParameterNamed("officeId");

        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withEntityId(command.entityId()) //
                .withGroupId(command.entityId()) //
                .withOfficeId(officeId)//Habile changes 
                .with(changes).with(changes).build();
    }

    @Override
    public CommandProcessingResult saveIndividualCollectionSheet(final JsonCommand command) {

        this.transactionDataValidator.validateIndividualCollectionSheet(command);

        final Map<String, Object> changes = new HashMap<>();
        changes.put("locale", command.locale());
        changes.put("dateFormat", command.dateFormat());

        final String noteText = command.stringValueOfParameterNamed("note");
        if (StringUtils.isNotBlank(noteText)) {
            changes.put("note", noteText);
        }
        

        final PaymentDetail paymentDetail = null;

        changes.putAll(updateBulkReapayments(command, paymentDetail));

        changes.putAll(updateBulkDisbursals(command));

        changes.putAll(updateBulkMandatorySavingsDuePayments(command, paymentDetail));
        
        //Long officeId = command.officeId;
        //final Long officeId = command.longValueOfParameterNamed(ClientApiConstants.officeIdParamName);
        
        //Habile changes 
        final Long officeId = command.longValueOfParameterNamed("officeId");
        

        //Habile changes 
        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withEntityId(command.entityId()) //
                .withGroupId(command.entityId()) //
                .withOfficeId(officeId)// Habile changes 
                .with(changes).with(changes).build();
    }

    private Map<String, Object> updateBulkReapayments(final JsonCommand command, final PaymentDetail paymentDetail) {
        final Map<String, Object> changes = new HashMap<>();
        final CollectionSheetBulkRepaymentCommand bulkRepaymentCommand = this.bulkRepaymentCommandFromApiJsonDeserializer
                .commandFromApiJson(command.json(), paymentDetail);
        changes.putAll(this.loanWritePlatformService.makeLoanBulkRepayment(bulkRepaymentCommand));
        return changes;
    }

    private Map<String, Object> updateBulkDisbursals(final JsonCommand command) {
        final Map<String, Object> changes = new HashMap<>();
        final CollectionSheetBulkDisbursalCommand bulkDisbursalCommand = this.bulkDisbursalCommandFromApiJsonDeserializer
                .commandFromApiJson(command.json());
        changes.putAll(this.loanWritePlatformService.bulkLoanDisbursal(command, bulkDisbursalCommand, false));
        return changes;
    }

    private Map<String, Object> updateBulkMandatorySavingsDuePayments(final JsonCommand command, final PaymentDetail paymentDetail) {
        final Map<String, Object> changes = new HashMap<>();
        final Collection<SavingsAccountTransactionDTO> savingsTransactions = this.accountAssembler
                .assembleBulkMandatorySavingsAccountTransactionDTOs(command, paymentDetail);
        List<Long> depositTransactionIds = new ArrayList<>();
        for (SavingsAccountTransactionDTO savingsAccountTransactionDTO : savingsTransactions) {
            try {
                SavingsAccountTransaction savingsAccountTransaction =  this.accountWritePlatformService.mandatorySavingsAccountDeposit(savingsAccountTransactionDTO);
                depositTransactionIds.add(savingsAccountTransaction.getId());
            } catch (Exception e) {
                // TODO: handle exception
            }
        }
        changes.put("SavingsTransactions", depositTransactionIds);
        return changes;
    }

	@Override
	public CommandProcessingResult smsBulkReminder(JsonCommand command) {
		
//		final Long officeId = command.longValueOfParameterNamed("officeId");
//		final Long groupId = command.longValueOfParameterNamed("groupId");
//		final Long centerId = command.longValueOfParameterNamed("centerId");
//		final Long loanOfficerId = command.longValueOfParameterNamed("loanOfficerId");
		final String startDate = command.stringValueOfParameterNamed("startDate");
		final String endDate = command.stringValueOfParameterNamed("endDate");
		final JsonArray loanid =command.arrayOfParameterNamed("loans");
		Map<String, Object> response=null;
		
		
		SimpleDateFormat sdf3 = new SimpleDateFormat("dd MMMM yyyy", Locale.ENGLISH);
   	 Date startOrgDate = null;
   	 Date endOrgDate = null;
   	 try {
			 Date tempDate = sdf3.parse(startDate);
			 SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");
			 String date1= format1.format(tempDate);
			 startOrgDate = format1.parse(date1);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
   	 try {
			 Date tempDate = sdf3.parse(endDate);
			 SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");
			 String date1= format1.format(tempDate);
			 endOrgDate = format1.parse(date1);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
   	 	//final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
   	 	
//     final List<ApiParameterError> dataValidationErrors = new ArrayList<>();

   	 	final Map<String, Object> changes = new HashMap<>();
   	 	
   	 	for(JsonElement loan : loanid )
   	 	{
   	 		
   	 	
   	 	
   	 	
		final SmsDetailsMapper lm = new SmsDetailsMapper();  
	
		String res = lm.schema();
		LocalDate today = LocalDate.now();
		
		LocalDate prev= command.localDateValueOfParameterNamed("startDate");
		LocalDate future= command.localDateValueOfParameterNamed("endDate");

		res = res + " where ml.id = "+loan+" and ( lr.duedate between '"+prev+"' and '"+future+"')";
        if(prev.isBefore(today)){
        	 throw new startDateException(); 
        }
        else if(future.isBefore(today)) {
        	throw new endDateException();
        }
        	
//        else {
//            
//		if(loanOfficerId != null)
//		{
//			
//			res = res + " and ms.id = "+loanOfficerId;
//		}
//		
//		if(centerId != null)
//		{
//			
//			res = res + " and mgg.id = "+centerId;
//		}
//		
//		if(groupId != null)
//		{
//			
//			res = res + " and mg.id = "+groupId;
//		}
//		
//        }
		
		final List <BulkReminderData>  loanDetails = this.jdbcTemplate.query(res,lm,  new Object[] {});
		//,loanOfficerId,loanOfficerId,officeId,officeId,groupId,groupId
		ArrayList<Bulk> bulk = new ArrayList<Bulk>();
		response = new HashMap<String, Object>();
		for(BulkReminderData loanDetail : loanDetails )
		{
			String returnMsg;
			String mob = loanDetail.getMobileno();
			String name = loanDetail.getName();
			BigInteger am = loanDetail.getAmount().toBigInteger() ;
			String type = "bulkReminder";
			String template = " தங்கள் கடன் கணக்கு எண் " + loanDetail.getLoanAccountNumber()+" இன் " + loanDetail.getInstallmentNumber()+" வது தவணை தொகையான ரூபாய் " +am+" ஐ "+loanDetail.getDuedate()+" ஆம் தேதி செலுத்த வேண்டும். தயவு கூர்ந்து அதை "+loanDetail.getDuedate()+" தேதி அன்று செலு த்தும்படி கேட்டுக் கொள்கிறோம். -  விவர்தனா மைக்ரோ பைனான்ஸ் லிமிடெட், சென்னை " ; 
			
			SMSDataVIVO test = new SMSDataVIVO(type, mob, template);
			Boolean result = this.smsProcessingServiceImpl.sendSMSinTamil(test);
			
			if(result) {
				returnMsg = "Success";
				}
			else {
				returnMsg = "fail";
			}
			

	        if (StringUtils.isNotBlank(returnMsg)) {
	            changes.put(mob, returnMsg);
	        }
			changes.put("mobileNo", mob);
	        changes.put("response","Success");
			
			returnMsg = "SMS sent";
			
			if(mob == null) {
				mob="Mobile number not found";
				returnMsg = "SMS not sent";
			}
		
			
			Bulk sample = new Bulk(name,mob, returnMsg);
			bulk.add(sample);
			
		}
        
   	 	
		
		response.put("response", bulk);
		
   	 	}
        //Habile changes 
        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withEntityId(command.entityId()) //
                .withGroupId(command.entityId()) //
                // Habile changes 
                .with(response).build();	        

}
	
	
private static final class SmsDetailsMapper implements RowMapper<BulkReminderData> {
    	
    	/*
    	 * No change in the query but need to add lot of functions in sql
    	 * 
    	 */
    	
    	public String schema() {
			return "select ml.account_no as acc,mc.firstname as firstname, mc.mobile_no as mobileno,"
					+ " lr.duedate as duedate,lr.installment as installment,"
					+ " (((IFNULL(lr.principal_amount,0) + IFNULL(lr.interest_amount,0) + IFNULL(lr.fee_charges_amount,0) + IFNULL(lr.penalty_charges_amount,0))-IFNULL(lr.completed_derived,0))) as repaymentamount "
					+ " from m_client mc join m_loan ml on mc.id=ml.client_id and ml.loan_status_id in (300) "
					+ " left join m_staff ms on ml.loan_officer_id=ms.id left join m_office mo on mo.id=mc.office_id "
					+ " left join m_group_client mgc on mc.id=mgc.client_id join m_group mg on mg.id=mgc.group_id "
					+ " join m_group mgg on mgg.id=mg.parent_id join m_loan_repayment_schedule lr on lr.loan_id=ml.id  ";
		}
//and (ifnull(ml.loan_officer_id, -10) = ? or '-1' = ?) and (ifnull(mo.id, -10) = ? or '-1' = ?) and (ifnull(mg.id, -10) = ? or '-1' = ?
		@Override
		public BulkReminderData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum)
				throws SQLException {

			
			final String acc = rs.getString("acc");
			final String name = rs.getString("firstname");
			final String mobileno = rs.getString("mobileno");
			final String duedate = rs.getString("duedate");
			final String installment = rs.getString("installment");
			final BigDecimal repaymentamount = rs.getBigDecimal("repaymentamount");
			

			return new BulkReminderData(acc,name,mobileno,duedate,installment,repaymentamount,null);

		}
}


@Override
public void sampleSms() {
	ArrayList<Bulk> bulk = new ArrayList<Bulk>();
	final Map<String, Object> changes = new HashMap<>();
	String returnMsg = "";
	List<String> sample = new ArrayList<String>();
	
	//jai
	//sample.add("9994");
	
	for(String mob : sample) {
	
	String type ="bulkReminder";
	
	String template = "தங்களது கடன் கணக்கில் 06.01.2019 தேதி செலுத்த வேண்டிய தவணை தொகை ரூபாய் 1581ஐ செலுத்த தவறிவிட்டீர்கள். "
			+ "இந்த ரூபாய் 1581ஐ வரும் 12.01.2019 தேதிக்குள் செலுத்தவும். - விவர்தனா மைக்ரோ பைனான்ஸ் லிமிடெட்., சென்னை" ; 
	
/*	SMSDataVIVO test = new SMSDataVIVO(type, mob, template);
	Boolean result = this.smsProcessingServiceImpl.sendSMSinTamil(test);
	
	if(result) {
		returnMsg = "Success";
		}
	else {
		returnMsg = "fail";
	}
	*/

    if (StringUtils.isNotBlank(returnMsg)) {
        changes.put(mob, returnMsg);
    }
			changes.put("mobileNo", mob);
    changes.put("response","Success");
	
	returnMsg = "SMS sent";
	
	if(mob == null) {
		mob="Mobile number not found";
		returnMsg = "SMS not sent";
	}}

	
	Bulk sampleData = new Bulk("sam","00000", returnMsg);
	bulk.add(sampleData);
	
}
@Override
public List<BulkReminderData> smsReminderData(String officeId, String centerId, String groupId, String loanOfficerId, String startDate, String endDate) {


    final AppUser currentUser = this.context.authenticatedUser();
    final BulkReminderMapper rm = new BulkReminderMapper();
    final StringBuilder sqlBuilder = new StringBuilder(200);
    sqlBuilder.append("select ");
    sqlBuilder.append(rm.schema());
	LocalDate today =new LocalDate();
	String prev2 =null;
	String future2 =null;
	LocalDate prev =new LocalDate();
	LocalDate future =new LocalDate();

	try {
	Date	prev1=new SimpleDateFormat("dd MMMM yyyy").parse(startDate);
	Date	future1 = new SimpleDateFormat("dd MMMM yyyy").parse(endDate);
	 prev2 = new SimpleDateFormat("yyyy-MM-dd").format(prev1);
	 future2 = new SimpleDateFormat("yyyy-MM-dd").format(future1);


	prev =new LocalDate(prev2);
	future =new LocalDate(future2);

	
   
    if(prev.isBefore(today)){
    	 throw new startDateException(); 
    }
    else if(future.isBefore(today)) {
    	throw new endDateException();
    }
    	
    else {
        
	if(!loanOfficerId.isEmpty())
	{
		
		sqlBuilder.append(" and ms.id = "+loanOfficerId);
	}
	
	if(!centerId.isEmpty())
	{
		
		sqlBuilder.append(" and mgg.id = "+centerId);
	}
	
	if(!groupId.isEmpty())
	{
		
		sqlBuilder.append(" and mg.id = "+groupId);
	}
	
    }
	} catch (ParseException e) {
	e.printStackTrace();
	} catch(NullPointerException e) {
	e.printStackTrace();
	
	}


    return this.jdbcTemplate.
    		query(sqlBuilder.toString(), rm, new Object[] {prev, future,officeId});

}
  private static final class BulkReminderMapper implements RowMapper<BulkReminderData> {
    	public String schema() {
			return " ml.account_no as acc,mc.firstname as firstname, mc.mobile_no as mobileno, lr.duedate as duedate,lr.installment as installment, (((IFNULL(lr.principal_amount,0) + IFNULL(lr.interest_amount,0) + IFNULL(lr.fee_charges_amount,0) + IFNULL(lr.penalty_charges_amount,0))-IFNULL(lr.completed_derived,0))) as repaymentamount from m_client mc join m_loan ml on mc.id=ml.client_id and ml.loan_status_id in (300) left join m_staff ms on ml.loan_officer_id=ms.id left join m_office mo on mo.id=mc.office_id left join m_group_client mgc on mc.id=mgc.client_id join m_group mg on mg.id=mgc.group_id join m_group mgg on mgg.id=mg.parent_id join m_loan_repayment_schedule lr on lr.loan_id=ml.id where (lr.duedate between ? and ?  )  and mo.id = ? ";
		}
		@Override
		public BulkReminderData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum)
				throws SQLException {

			
			final String acc = rs.getString("acc");
			final String name = rs.getString("firstname");
			final String mobileno = rs.getString("mobileno");
			final String duedate = rs.getString("duedate");
			final String installment = rs.getString("installment");
			final BigDecimal repaymentamount = rs.getBigDecimal("repaymentamount");
			
				return new BulkReminderData(acc,name,mobileno,duedate,installment,repaymentamount,null);

		}
}
    }
