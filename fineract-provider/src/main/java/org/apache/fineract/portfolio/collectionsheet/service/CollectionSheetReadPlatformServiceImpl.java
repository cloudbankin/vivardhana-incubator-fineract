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

import static org.apache.fineract.portfolio.collectionsheet.CollectionSheetConstants.calendarIdParamName;
import static org.apache.fineract.portfolio.collectionsheet.CollectionSheetConstants.officeIdParamName;
import static org.apache.fineract.portfolio.collectionsheet.CollectionSheetConstants.staffIdParamName;
import static org.apache.fineract.portfolio.collectionsheet.CollectionSheetConstants.transactionDateParamName;
import static org.apache.fineract.portfolio.collectionsheet.CollectionSheetConstants.repaymentDateParamName;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.commands.service.CommandWrapperBuilder;
import org.apache.fineract.infrastructure.codes.service.CodeValueReadPlatformService;
import org.apache.fineract.infrastructure.configuration.domain.ConfigurationDomainService;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.api.JsonQuery;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.infrastructure.core.domain.JdbcSupport;
import org.apache.fineract.infrastructure.core.exception.endDateException;
import org.apache.fineract.infrastructure.core.exception.startDateException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.core.service.RoutingDataSource;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.organisation.monetary.data.CurrencyData;
import org.apache.fineract.organisation.office.data.OfficeData;
import org.apache.fineract.portfolio.calendar.domain.Calendar;
import org.apache.fineract.portfolio.calendar.domain.CalendarEntityType;
import org.apache.fineract.portfolio.calendar.domain.CalendarInstanceRepository;
import org.apache.fineract.portfolio.calendar.domain.CalendarRepositoryWrapper;
import org.apache.fineract.portfolio.calendar.exception.NotValidRecurringDateException;
import org.apache.fineract.portfolio.calendar.service.CalendarReadPlatformService;
import org.apache.fineract.portfolio.collectionsheet.data.BulkReminderData;
import org.apache.fineract.portfolio.collectionsheet.data.IndividualClientData;
import org.apache.fineract.portfolio.collectionsheet.data.IndividualCollectionSheetData;
import org.apache.fineract.portfolio.collectionsheet.data.IndividualCollectionSheetLoanFlatData;
import org.apache.fineract.portfolio.collectionsheet.data.JLGClientData;
import org.apache.fineract.portfolio.collectionsheet.data.JLGCollectionSheetData;
import org.apache.fineract.portfolio.collectionsheet.data.JLGCollectionSheetFlatData;
import org.apache.fineract.portfolio.collectionsheet.data.JLGGroupData;
import org.apache.fineract.portfolio.collectionsheet.data.LoanDueData;
import org.apache.fineract.portfolio.collectionsheet.data.SavingsDueData;
import org.apache.fineract.portfolio.collectionsheet.serialization.CollectionSheetGenerateCommandFromApiJsonDeserializer;
import org.apache.fineract.portfolio.group.data.CenterData;
import org.apache.fineract.portfolio.group.data.GroupGeneralData;
import org.apache.fineract.portfolio.group.service.CenterReadPlatformService;
import org.apache.fineract.portfolio.group.service.GroupReadPlatformService;
import org.apache.fineract.portfolio.loanproduct.data.LoanProductData;
import org.apache.fineract.portfolio.meeting.attendance.service.AttendanceDropdownReadPlatformService;
import org.apache.fineract.portfolio.meeting.attendance.service.AttendanceEnumerations;
import org.apache.fineract.portfolio.paymenttype.data.PaymentTypeData;
import org.apache.fineract.portfolio.paymenttype.service.PaymentTypeReadPlatformService;
import org.apache.fineract.portfolio.savings.data.SavingsProductData;
import org.apache.fineract.useradministration.domain.AppUser;
import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Service;

import com.google.gson.JsonElement;

@Service
public class CollectionSheetReadPlatformServiceImpl implements CollectionSheetReadPlatformService {

    private final PlatformSecurityContext context;
    private final NamedParameterJdbcTemplate namedParameterjdbcTemplate;
    private final CenterReadPlatformService centerReadPlatformService;
    private final GroupReadPlatformService groupReadPlatformService;
    private final CollectionSheetGenerateCommandFromApiJsonDeserializer collectionSheetGenerateCommandFromApiJsonDeserializer;
    private final CalendarRepositoryWrapper calendarRepositoryWrapper;
    private final AttendanceDropdownReadPlatformService attendanceDropdownReadPlatformService;
    private final MandatorySavingsCollectionsheetExtractor mandatorySavingsExtractor = new MandatorySavingsCollectionsheetExtractor();
    private final PaymentTypeReadPlatformService paymentTypeReadPlatformService;
    private final CalendarReadPlatformService calendarReadPlatformService;
    private final ConfigurationDomainService configurationDomainService;
    private final JdbcTemplate jdbcTemplate;



    @Autowired
    public CollectionSheetReadPlatformServiceImpl(final PlatformSecurityContext context, final RoutingDataSource dataSource,
            final CenterReadPlatformService centerReadPlatformService, final GroupReadPlatformService groupReadPlatformService,
            final CollectionSheetGenerateCommandFromApiJsonDeserializer collectionSheetGenerateCommandFromApiJsonDeserializer,
            final CalendarRepositoryWrapper calendarRepositoryWrapper,
            final AttendanceDropdownReadPlatformService attendanceDropdownReadPlatformService,
      final PaymentTypeReadPlatformService paymentTypeReadPlatformService,
            final CalendarReadPlatformService calendarReadPlatformService, final ConfigurationDomainService configurationDomainService
) {
        this.context = context;
        this.centerReadPlatformService = centerReadPlatformService;
        this.namedParameterjdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
        this.collectionSheetGenerateCommandFromApiJsonDeserializer = collectionSheetGenerateCommandFromApiJsonDeserializer;
        this.groupReadPlatformService = groupReadPlatformService;
        this.calendarRepositoryWrapper = calendarRepositoryWrapper;
        this.attendanceDropdownReadPlatformService = attendanceDropdownReadPlatformService;
        this.paymentTypeReadPlatformService = paymentTypeReadPlatformService;
        this.calendarReadPlatformService = calendarReadPlatformService;
        this.configurationDomainService = configurationDomainService;
        this.jdbcTemplate = new JdbcTemplate(dataSource);

    }

    /*
     * Reads all the loans which are due for disbursement or collection and
     * builds hierarchical data structure for collections sheet with hierarchy
     * Groups >> Clients >> Loans.
     */
    @SuppressWarnings("null")
    private JLGCollectionSheetData buildJLGCollectionSheet(final LocalDate dueDate,
            final Collection<JLGCollectionSheetFlatData> jlgCollectionSheetFlatData, final Long officeId) {
    	//Habile changes 
        boolean firstTime = true;
        Long prevGroupId = null;
        Long prevClientId = null;
        final Collection<PaymentTypeData> paymentOptions = this.paymentTypeReadPlatformService.retrieveAllPaymentTypes();
                

        final List<JLGGroupData> jlgGroupsData = new ArrayList<>();
        List<JLGClientData> clientsData = new ArrayList<>();
        List<LoanDueData> loansDueData = new ArrayList<>();

        JLGCollectionSheetData jlgCollectionSheetData = null;
        JLGCollectionSheetFlatData prevCollectioSheetFlatData = null;
        JLGCollectionSheetFlatData corrCollectioSheetFlatData = null;
        final Set<LoanProductData> loanProducts = new HashSet<>();
        if (jlgCollectionSheetFlatData != null) {

            for (final JLGCollectionSheetFlatData collectionSheetFlatData : jlgCollectionSheetFlatData) {

                if (collectionSheetFlatData.getProductId() != null) {
                    loanProducts.add(LoanProductData.lookupWithCurrency(collectionSheetFlatData.getProductId(),
                            collectionSheetFlatData.getProductShortName(), collectionSheetFlatData.getCurrency()));
                }
                corrCollectioSheetFlatData = collectionSheetFlatData;

                if (firstTime || collectionSheetFlatData.getGroupId().equals(prevGroupId)) {
                    if (firstTime || collectionSheetFlatData.getClientId().equals(prevClientId)) {
                        if (collectionSheetFlatData.getLoanId() != null) {
                            loansDueData.add(collectionSheetFlatData.getLoanDueData());
                        }
                    } else {
                        final JLGClientData clientData = prevCollectioSheetFlatData.getClientData();
                        clientData.setLoans(loansDueData);
                        clientsData.add(clientData);
                        loansDueData = new ArrayList<>();

                        if (collectionSheetFlatData.getLoanId() != null) {
                            loansDueData.add(collectionSheetFlatData.getLoanDueData());
                        }

                    }
                } else {

                    final JLGClientData clientData = prevCollectioSheetFlatData.getClientData();
                    clientData.setLoans(loansDueData);
                    clientsData.add(clientData);

                    final JLGGroupData jlgGroupData = prevCollectioSheetFlatData.getJLGGroupData();
                    jlgGroupData.setClients(clientsData);

                    jlgGroupsData.add(jlgGroupData);

                    loansDueData = new ArrayList<>();
                    clientsData = new ArrayList<>();

                    if (collectionSheetFlatData.getLoanId() != null) {
                        loansDueData.add(collectionSheetFlatData.getLoanDueData());
                    }
                }

                prevClientId = collectionSheetFlatData.getClientId();
                prevGroupId = collectionSheetFlatData.getGroupId();
                prevCollectioSheetFlatData = collectionSheetFlatData;
                firstTime = false;
            }

            // FIXME Need to check last loan is added under previous
            // client/group or new client / previous group or new client / new
            // group
            if (corrCollectioSheetFlatData != null) {
                final JLGClientData lastClientData = corrCollectioSheetFlatData.getClientData();
                lastClientData.setLoans(loansDueData);
                clientsData.add(lastClientData);

                final JLGGroupData jlgGroupData = corrCollectioSheetFlatData.getJLGGroupData();
                jlgGroupData.setClients(clientsData);
                jlgGroupsData.add(jlgGroupData);
            }

            jlgCollectionSheetData = JLGCollectionSheetData.instance(dueDate, loanProducts, jlgGroupsData,
                    this.attendanceDropdownReadPlatformService.retrieveAttendanceTypeOptions(), paymentOptions, officeId);
        }

        return jlgCollectionSheetData;
    }

    private static final class JLGCollectionSheetFaltDataMapper implements RowMapper<JLGCollectionSheetFlatData> {

        public String collectionSheetSchema(final boolean isCenterCollection) {
            StringBuffer sql = new StringBuffer(400);
            sql.append("SELECT loandata.*, sum(lc.amount_outstanding_derived) as chargesDue from ")
                    .append("(SELECT gp.display_name As groupName, ")
                    .append("gp.id As groupId, ")
                    .append("cl.display_name As clientName, ")
                    .append("of.id as officeId, of.name as officeName, ")//Habile changes 
                    .append("sf.id As staffId, ")
                    .append("sf.display_name As staffName, ")
                    .append("gl.id As levelId, ")
                    .append("gl.level_name As levelName, ")
                    .append("cl.id As clientId, ")
                    .append("ln.id As loanId, ")
                    .append("ln.account_no As accountId, ")
                    .append("ln.loan_status_id As accountStatusId, ")
                    .append("pl.short_name As productShortName, ")
                    .append("ln.product_id As productId, ")
                    .append("ln.currency_code as currencyCode, ln.currency_digits as currencyDigits, ln.currency_multiplesof as inMultiplesOf, rc.`name` as currencyName, rc.display_symbol as currencyDisplaySymbol, rc.internationalized_name_code as currencyNameCode, ")
                    .append("if(ln.loan_status_id = 200 , ln.principal_amount , null) As disbursementAmount, ")
                    .append("sum(ifnull(if(ln.loan_status_id = 300, ls.principal_amount, 0.0), 0.0) - ifnull(if(ln.loan_status_id = 300, ls.principal_completed_derived, 0.0), 0.0)) As principalDue, ")
                    .append("ln.principal_repaid_derived As principalPaid, ")
                    .append("sum(ifnull(if(ln.loan_status_id = 300, ls.interest_amount, 0.0), 0.0) - ifnull(if(ln.loan_status_id = 300, ls.interest_completed_derived, 0.0), 0.0) - IFNULL(IF(ln.loan_status_id = 300, ls.interest_waived_derived, 0.0), 0.0)) As interestDue, ")
                    .append("ln.interest_repaid_derived As interestPaid, ")
                    .append("sum(ifnull(if(ln.loan_status_id = 300, ls.fee_charges_amount, 0.0), 0.0) - ifnull(if(ln.loan_status_id = 300, ls.fee_charges_completed_derived, 0.0), 0.0)) As feeDue, ")
                    .append("ln.fee_charges_repaid_derived As feePaid, ")
                    .append("ca.attendance_type_enum as attendanceTypeId ")
                    .append("FROM m_group gp ")
                    .append("LEFT JOIN m_office of ON of.id = gp.office_id AND of.hierarchy like :officeHierarchy ")
                    .append("JOIN m_group_level gl ON gl.id = gp.level_Id ")
                    .append("LEFT JOIN m_staff sf ON sf.id = gp.staff_id ")
                    .append("JOIN m_group_client gc ON gc.group_id = gp.id ")
                    .append("JOIN m_client cl ON cl.id = gc.client_id ")
                    .append("LEFT JOIN m_loan ln ON cl.id = ln.client_id  and ln.group_id=gp.id AND ln.group_id is not null AND ( ln.loan_status_id = 300 ) ")
                    .append("LEFT JOIN m_product_loan pl ON pl.id = ln.product_id ")
                    .append("LEFT JOIN m_currency rc on rc.`code` = ln.currency_code ")
                    .append("LEFT JOIN m_loan_repayment_schedule ls ON ls.loan_id = ln.id AND ls.completed_derived = 0 AND ls.duedate <= :dueDate ")
                    .append("left join m_calendar_instance ci on gp.parent_id = ci.entity_id and ci.entity_type_enum =:entityTypeId ")
                    .append("left join m_meeting mt on ci.id = mt.calendar_instance_id and mt.meeting_date =:dueDate ")
                    .append("left join m_client_attendance ca on ca.meeting_id=mt.id and ca.client_id=cl.id ");

            if (isCenterCollection) {
                sql.append("WHERE gp.parent_id = :centerId ");
            } else {
                sql.append("WHERE gp.id = :groupId ");
            }
            sql.append("and (ln.loan_status_id != 200 AND ln.loan_status_id != 100) ");
            
            sql.append("and (gp.status_enum = 300 or (gp.status_enum = 600 and gp.closedon_date >= :dueDate)) ")
                    .append("and (cl.status_enum = 300 or (cl.status_enum = 600 and cl.closedon_date >= :dueDate)) ")
                    .append("GROUP BY gp.id ,cl.id , ln.id ORDER BY gp.id , cl.id , ln.id ").append(") loandata ")
                    .append("LEFT JOIN m_loan_charge lc ON lc.loan_id = loandata.loanId AND lc.is_paid_derived = 0 AND lc.is_active = 1 ")
                    .append("AND ( lc.due_for_collection_as_of_date  <= :dueDate OR lc.charge_time_enum = 1) ")
                    .append("GROUP BY loandata.groupId, ").append("loandata.clientId, ").append("loandata.loanId ")
                    .append("ORDER BY loandata.groupId, ").append("loandata.clientId, ").append("loandata.loanId ");

            return sql.toString();

        }

        @Override
        public JLGCollectionSheetFlatData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {

            final String groupName = rs.getString("groupName");
            final Long groupId = JdbcSupport.getLong(rs, "groupId");
            final Long staffId = JdbcSupport.getLong(rs, "staffId");
            final String staffName = rs.getString("staffName");
            final Long levelId = JdbcSupport.getLong(rs, "levelId");
            final String levelName = rs.getString("levelName");
            final String clientName = rs.getString("clientName");
            final Long clientId = JdbcSupport.getLong(rs, "clientId");
            final Long loanId = JdbcSupport.getLong(rs, "loanId");
            final String accountId = rs.getString("accountId");
            final Integer accountStatusId = JdbcSupport.getInteger(rs, "accountStatusId");
            final String productShortName = rs.getString("productShortName");
            final Long productId = JdbcSupport.getLong(rs, "productId");

            final String currencyCode = rs.getString("currencyCode");
            final String currencyName = rs.getString("currencyName");
            final String currencyNameCode = rs.getString("currencyNameCode");
            final String currencyDisplaySymbol = rs.getString("currencyDisplaySymbol");
            final Integer currencyDigits = JdbcSupport.getInteger(rs, "currencyDigits");
            final Integer inMultiplesOf = JdbcSupport.getInteger(rs, "inMultiplesOf");
            CurrencyData currencyData = null;
            if (currencyCode != null) {
                currencyData = new CurrencyData(currencyCode, currencyName, currencyDigits, inMultiplesOf, currencyDisplaySymbol,
                        currencyNameCode);
            }

            final BigDecimal disbursementAmount = rs.getBigDecimal("disbursementAmount");
            final BigDecimal principalDue = rs.getBigDecimal("principalDue");
            final BigDecimal principalPaid = rs.getBigDecimal("principalPaid");
            final BigDecimal interestDue = rs.getBigDecimal("interestDue");
            final BigDecimal interestPaid = rs.getBigDecimal("interestPaid");
            final BigDecimal chargesDue = rs.getBigDecimal("chargesDue");
            final BigDecimal feeDue = rs.getBigDecimal("feeDue");
            final BigDecimal feePaid = rs.getBigDecimal("feePaid");

            final Integer attendanceTypeId = rs.getInt("attendanceTypeId");
            final EnumOptionData attendanceType = AttendanceEnumerations.attendanceType(attendanceTypeId);
            
            final Long officeId = rs.getLong("officeId"); //Habile changes
            final String officeName = rs.getString("officeName");

            return new JLGCollectionSheetFlatData(groupName, groupId, staffId, staffName, levelId, levelName, clientName, clientId, loanId,
                    accountId, accountStatusId, productShortName, productId, currencyData, disbursementAmount, principalDue, principalPaid,
                    interestDue, interestPaid, chargesDue, attendanceType, feeDue, feePaid,officeId,officeName);
        }

    }

    @Override
    public JLGCollectionSheetData generateGroupCollectionSheet(final Long groupId, final JsonQuery query) {

        this.collectionSheetGenerateCommandFromApiJsonDeserializer.validateForGenerateCollectionSheet(query.json());

        final Long calendarId = query.longValueOfParameterNamed(calendarIdParamName);
        final LocalDate transactionDate = query.localDateValueOfParameterNamed(transactionDateParamName);
        final LocalDate repaymentDate = query.localDateValueOfParameterNamed(repaymentDateParamName); //Habile changes Repayment Problem
        final DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        
        //Habile Changes Repayment Problem 
        String transactionDateStr = null;
  
        if(transactionDate != null)
        {
        	transactionDateStr = df.format(transactionDate.toDate());
        }
        else if(repaymentDate != null)
        {
        	transactionDateStr = df.format(repaymentDate.toDate());
        }

        final Calendar calendar = this.calendarRepositoryWrapper.findOneWithNotFoundDetection(calendarId);
        // check if transaction against calendar effective from date
        
        final GroupGeneralData group = this.groupReadPlatformService.retrieveOne(groupId);
        
        // entityType should be center if it's within a center
        final CalendarEntityType entityType = (group.isChildGroup()) ? CalendarEntityType.CENTERS : CalendarEntityType.GROUPS;
        
        Long entityId = null;
        if(group.isChildGroup()){
        	entityId = group.getParentId();
        }else{
        	entityId = group.getId();
        }

        Boolean isSkipMeetingOnFirstDay = false;
        Integer numberOfDays = 0;
        boolean isSkipRepaymentOnFirstMonthEnabled = this.configurationDomainService.isSkippingMeetingOnFirstDayOfMonthEnabled();
        if(isSkipRepaymentOnFirstMonthEnabled){
            numberOfDays = this.configurationDomainService.retreivePeroidInNumberOfDaysForSkipMeetingDate().intValue();
            isSkipMeetingOnFirstDay = this.calendarReadPlatformService.isCalendarAssociatedWithEntity(entityId, calendar.getId(), 
            		entityType.getValue().longValue());
        }
        
        //Habile changes Repayment Problem
        if(transactionDate != null && repaymentDate == null)
        {
	        if (!calendar.isValidRecurringDate(transactionDate, isSkipMeetingOnFirstDay, numberOfDays)) { throw new NotValidRecurringDateException("collectionsheet", "The date '"
	                + transactionDate + "' is not a valid meeting date.", transactionDate); }
        }

        final AppUser currentUser = this.context.authenticatedUser();
        final String hierarchy = currentUser.getOffice().getHierarchy();
        final String officeHierarchy = hierarchy + "%";

        

        final JLGCollectionSheetFaltDataMapper mapper = new JLGCollectionSheetFaltDataMapper();


        final SqlParameterSource namedParameters = new MapSqlParameterSource().addValue("dueDate", transactionDateStr)
                .addValue("groupId", group.getId()).addValue("officeHierarchy", officeHierarchy)
                .addValue("entityTypeId", entityType.getValue());

        final Collection<JLGCollectionSheetFlatData> collectionSheetFlatDatas = this.namedParameterjdbcTemplate.query(
                mapper.collectionSheetSchema(false), namedParameters, mapper);
        
        //Habile changes 
        Long officetest = group.getOfficeId();
        
        
        // loan data for collection sheet
        JLGCollectionSheetData collectionSheetData = buildJLGCollectionSheet(transactionDate, collectionSheetFlatDatas, group.getOfficeId());

        // mandatory savings data for collection sheet
        Collection<JLGGroupData> groupsWithSavingsData = this.namedParameterjdbcTemplate.query(
                mandatorySavingsExtractor.collectionSheetSchema(false), namedParameters, mandatorySavingsExtractor);

        // merge savings data into loan data
        mergeSavingsGroupDataIntoCollectionsheetData(groupsWithSavingsData, collectionSheetData);

        collectionSheetData = JLGCollectionSheetData.withSavingsProducts(collectionSheetData,
                retrieveSavingsProducts(groupsWithSavingsData));

        return collectionSheetData;
    }

    private void mergeSavingsGroupDataIntoCollectionsheetData(final Collection<JLGGroupData> groupsWithSavingsData,
            final JLGCollectionSheetData collectionSheetData) {
        final List<JLGGroupData> groupsWithLoanData = (List<JLGGroupData>) collectionSheetData.getGroups();
        for (JLGGroupData groupSavingsData : groupsWithSavingsData) {
            if (groupsWithLoanData.contains(groupSavingsData)) {
                mergeGroup(groupSavingsData, groupsWithLoanData);
            } else {
                groupsWithLoanData.add(groupSavingsData);
            }
        }

    }

    private void mergeGroup(final JLGGroupData groupSavingsData, final List<JLGGroupData> groupsWithLoanData) {
        final int index = groupsWithLoanData.indexOf(groupSavingsData);

        if (index < 0) return;

        JLGGroupData groupLoanData = groupsWithLoanData.get(index);
        List<JLGClientData> clientsLoanData = (List<JLGClientData>) groupLoanData.getClients();
        List<JLGClientData> clientsSavingsData = (List<JLGClientData>) groupSavingsData.getClients();

        for (JLGClientData clientSavingsData : clientsSavingsData) {
            if (clientsLoanData.contains(clientSavingsData)) {
                mergeClient(clientSavingsData, clientsLoanData);
            } else {
                clientsLoanData.add(clientSavingsData);
            }
        }
    }

    private void mergeClient(final JLGClientData clientSavingsData, List<JLGClientData> clientsLoanData) {
        final int index = clientsLoanData.indexOf(clientSavingsData);

        if (index < 0) return;

        JLGClientData clientLoanData = clientsLoanData.get(index);
        clientLoanData.setSavings(clientSavingsData.getSavings());
    }

    private Collection<SavingsProductData> retrieveSavingsProducts(Collection<JLGGroupData> groupsWithSavingsData) {
        List<SavingsProductData> savingsProducts = new ArrayList<>();
        for (JLGGroupData groupSavingsData : groupsWithSavingsData) {
            Collection<JLGClientData> clientsSavingsData = groupSavingsData.getClients();
            for (JLGClientData clientSavingsData : clientsSavingsData) {
                Collection<SavingsDueData> savingsDatas = clientSavingsData.getSavings();
                for (SavingsDueData savingsDueData : savingsDatas) {
                    final SavingsProductData savingsProduct = SavingsProductData.lookup(savingsDueData.productId(),
                            savingsDueData.productName());
                    savingsProduct.setDepositAccountType(savingsDueData.getDepositAccountType());
                    if (!savingsProducts.contains(savingsProduct)) {
                        savingsProducts.add(savingsProduct);
                    }
                }
            }
        }
        return savingsProducts;
    }

    @Override
    public JLGCollectionSheetData generateCenterCollectionSheet(final Long centerId, final JsonQuery query) {

        this.collectionSheetGenerateCommandFromApiJsonDeserializer.validateForGenerateCollectionSheet(query.json());

        final AppUser currentUser = this.context.authenticatedUser();
        final String hierarchy = currentUser.getOffice().getHierarchy();
        final String officeHierarchy = hierarchy + "%";

        final CenterData center = this.centerReadPlatformService.retrieveOne(centerId);

        final LocalDate transactionDate = query.localDateValueOfParameterNamed(transactionDateParamName);
        final LocalDate repaymentDate = query.localDateValueOfParameterNamed(repaymentDateParamName);//Habile changes Repayment Problem 
        final DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        
        //final String dueDateStr = df.format(transactionDate.toDate());
        
        //Habile Changes Repayment Problem 
       
        String dueDateStr = null;
  
        if(transactionDate != null)
        {
        	dueDateStr = df.format(transactionDate.toDate());
        }
        else if(repaymentDate != null)
        {
        	dueDateStr = df.format(repaymentDate.toDate());
        }

        final JLGCollectionSheetFaltDataMapper mapper = new JLGCollectionSheetFaltDataMapper();

        StringBuilder sql = new StringBuilder(mapper.collectionSheetSchema(true));

        final SqlParameterSource namedParameters = new MapSqlParameterSource().addValue("dueDate", dueDateStr)
                .addValue("centerId", center.getId()).addValue("officeHierarchy", officeHierarchy)
                .addValue("entityTypeId", CalendarEntityType.CENTERS.getValue());

        final Collection<JLGCollectionSheetFlatData> collectionSheetFlatDatas = this.namedParameterjdbcTemplate.query(sql.toString(),
                namedParameters, mapper);

        // loan data for collection sheet changed to null 
        JLGCollectionSheetData collectionSheetData = buildJLGCollectionSheet(transactionDate, collectionSheetFlatDatas, null );

        // mandatory savings data for collection sheet
        Collection<JLGGroupData> groupsWithSavingsData = this.namedParameterjdbcTemplate.query(
                mandatorySavingsExtractor.collectionSheetSchema(true), namedParameters, mandatorySavingsExtractor);

        // merge savings data into loan data
        mergeSavingsGroupDataIntoCollectionsheetData(groupsWithSavingsData, collectionSheetData);

        collectionSheetData = JLGCollectionSheetData.withSavingsProducts(collectionSheetData,
                retrieveSavingsProducts(groupsWithSavingsData));

        return collectionSheetData;
    }

    private static final class MandatorySavingsCollectionsheetExtractor implements ResultSetExtractor<Collection<JLGGroupData>> {

        private final GroupSavingsDataMapper groupSavingsDataMapper = new GroupSavingsDataMapper();

        public String collectionSheetSchema(final boolean isCenterCollection) {

            final StringBuffer sql = new StringBuffer(400);
            sql.append("SELECT gp.display_name As groupName, ")
                    .append("gp.id As groupId, ")
                    .append("cl.display_name As clientName, ")
                    .append("cl.id As clientId, ")
                    .append("sf.id As staffId, ")
                    .append("sf.display_name As staffName, ")
                    .append("gl.id As levelId, ")
                    .append("gl.level_name As levelName, ")
                    .append("sa.id As savingsId, ")
                    .append("sa.account_no As accountId, ")
                    .append("sa.status_enum As accountStatusId, ")
                    .append("sp.short_name As productShortName, ")
                    .append("sp.id As productId, ")
                    .append("sa.currency_code as currencyCode, ")
                    .append("sa.currency_digits as currencyDigits, ")
                    .append("sa.currency_multiplesof as inMultiplesOf, ")
                    .append("rc.`name` as currencyName, ")
                    .append("rc.display_symbol as currencyDisplaySymbol, ")
                    .append("if(sa.deposit_type_enum=100,'Saving Deposit',if(sa.deposit_type_enum=300,'Recurring Deposit','Current Deposit')) as depositAccountType, ")
                    .append("rc.internationalized_name_code as currencyNameCode, ")
                    .append("sum(ifnull(mss.deposit_amount,0) - ifnull(mss.deposit_amount_completed_derived,0)) as dueAmount ")

                    .append("FROM m_group gp ")
                    .append("LEFT JOIN m_office of ON of.id = gp.office_id AND of.hierarchy like :officeHierarchy ")
                    .append("JOIN m_group_level gl ON gl.id = gp.level_Id ")
                    .append("LEFT JOIN m_staff sf ON sf.id = gp.staff_id ")
                    .append("JOIN m_group_client gc ON gc.group_id = gp.id ")
                    .append("JOIN m_client cl ON cl.id = gc.client_id ")
                    .append("JOIN m_savings_account sa ON sa.client_id=cl.id and sa.status_enum=300 ")
                    .append("JOIN m_savings_product sp ON sa.product_id=sp.id ")
                    .append("LEFT JOIN m_deposit_account_recurring_detail dard ON sa.id = dard.savings_account_id AND dard.is_mandatory = true AND dard.is_calendar_inherited = true ")
                    .append("LEFT JOIN m_mandatory_savings_schedule mss ON mss.savings_account_id=sa.id AND mss.duedate <= :dueDate ")
                    .append("LEFT JOIN m_currency rc on rc.`code` = sa.currency_code ");

            if (isCenterCollection) {
                sql.append("WHERE gp.parent_id = :centerId ");
            } else {
                sql.append("WHERE gp.id = :groupId ");
            }

            sql.append("and (gp.status_enum = 300 or (gp.status_enum = 600 and gp.closedon_date >= :dueDate)) ")
                    .append("and (cl.status_enum = 300 or (cl.status_enum = 600 and cl.closedon_date >= :dueDate)) ")
                    .append("GROUP BY gp.id ,cl.id , sa.id ORDER BY gp.id , cl.id , sa.id ");

            return sql.toString();
        }

        @Override
        public Collection<JLGGroupData> extractData(ResultSet rs) throws SQLException, DataAccessException {
            List<JLGGroupData> groups = new ArrayList<>();

            JLGGroupData group = null;
            int groupIndex = 0;
            boolean isEndOfRecords = false;
            // move cursor to first row.
            final boolean isNotEmtyResultSet = rs.next();

            if (isNotEmtyResultSet) {
                while (!isEndOfRecords) {
                    group = groupSavingsDataMapper.mapRowData(rs, groupIndex++);
                    groups.add(group);
                    isEndOfRecords = rs.isAfterLast();
                }
            }

            return groups;
        }
    }

    private static final class GroupSavingsDataMapper implements RowMapper<JLGGroupData> {

        private final ClientSavingsDataMapper clientSavingsDataMapper = new ClientSavingsDataMapper();

        private GroupSavingsDataMapper() {}

        public JLGGroupData mapRowData(ResultSet rs, int rowNum) throws SQLException {
            final List<JLGClientData> clients = new ArrayList<>();
            final JLGGroupData group = this.mapRow(rs, rowNum);
            final Long previousGroupId = group.getGroupId();

            // first client row of new group
            JLGClientData client = clientSavingsDataMapper.mapRowData(rs, rowNum);
            clients.add(client);

            // if its not after last row loop
            while (!rs.isAfterLast()) {
                final Long groupId = JdbcSupport.getLong(rs, "groupId");
                if (previousGroupId != null && groupId.compareTo(previousGroupId) != 0) {
                    // return for next group details
                    return JLGGroupData.withClients(group, clients);
                }
                client = clientSavingsDataMapper.mapRowData(rs, rowNum);
                clients.add(client);
            }

            return JLGGroupData.withClients(group, clients);
        }

        @Override
        public JLGGroupData mapRow(ResultSet rs, @SuppressWarnings("unused") int rowNum) throws SQLException {

            final String groupName = rs.getString("groupName");
            final Long groupId = JdbcSupport.getLong(rs, "groupId");
            final Long staffId = JdbcSupport.getLong(rs, "staffId");
            final String staffName = rs.getString("staffName");
            final Long levelId = JdbcSupport.getLong(rs, "levelId");
            final String levelName = rs.getString("levelName");
            return JLGGroupData.instance(groupId, groupName, staffId, staffName, levelId, levelName);
        }
    }

    private static final class ClientSavingsDataMapper implements RowMapper<JLGClientData> {

        private final SavingsDueDataMapper savingsDueDataMapper = new SavingsDueDataMapper();

        private ClientSavingsDataMapper() {}

        public JLGClientData mapRowData(ResultSet rs, int rowNum) throws SQLException {

            List<SavingsDueData> savings = new ArrayList<>();

            JLGClientData client = this.mapRow(rs, rowNum);
            final Long previousClientId = client.getClientId();

            // first savings row of new client record
            SavingsDueData saving = savingsDueDataMapper.mapRow(rs, rowNum);
            savings.add(saving);

            while (rs.next()) {
                final Long clientId = JdbcSupport.getLong(rs, "clientId");
                if (previousClientId != null && clientId.compareTo(previousClientId) != 0) {
                    // client id changes then return for next client data
                    return JLGClientData.withSavings(client, savings);
                }
                saving = savingsDueDataMapper.mapRow(rs, rowNum);
                savings.add(saving);
            }
            return JLGClientData.withSavings(client, savings);
        }

        @Override
        public JLGClientData mapRow(ResultSet rs, @SuppressWarnings("unused") int rowNum) throws SQLException {

            final String clientName = rs.getString("clientName");
            final Long clientId = JdbcSupport.getLong(rs, "clientId");
            // final Integer attendanceTypeId = rs.getInt("attendanceTypeId");
            // final EnumOptionData attendanceType =
            // AttendanceEnumerations.attendanceType(attendanceTypeId);
            final EnumOptionData attendanceType = null;

            return JLGClientData.instance(clientId, clientName, attendanceType);
        }
    }

    private static final class SavingsDueDataMapper implements RowMapper<SavingsDueData> {

        private SavingsDueDataMapper() {}

        @Override
        public SavingsDueData mapRow(ResultSet rs, int rowNum) throws SQLException {
            final Long savingsId = rs.getLong("savingsId");
            final String accountId = rs.getString("accountId");
            final Integer accountStatusId = JdbcSupport.getInteger(rs, "accountStatusId");
            final String productName = rs.getString("productShortName");
            final Long productId = rs.getLong("productId");
            final BigDecimal dueAmount = rs.getBigDecimal("dueAmount");
            final String currencyCode = rs.getString("currencyCode");
            final String currencyName = rs.getString("currencyName");
            final String currencyNameCode = rs.getString("currencyNameCode");
            final String currencyDisplaySymbol = rs.getString("currencyDisplaySymbol");
            final Integer currencyDigits = JdbcSupport.getInteger(rs, "currencyDigits");
            final Integer inMultiplesOf = JdbcSupport.getInteger(rs, "inMultiplesOf");
            final String depositAccountType = rs.getString("depositAccountType");
            // currency
            final CurrencyData currency = new CurrencyData(currencyCode, currencyName, currencyDigits, inMultiplesOf,
                    currencyDisplaySymbol, currencyNameCode);

            return SavingsDueData.instance(savingsId, accountId, accountStatusId, productName, productId, currency, dueAmount, depositAccountType);
        }
    }

    @Override
    public IndividualCollectionSheetData generateIndividualCollectionSheet(final JsonQuery query) {

        this.collectionSheetGenerateCommandFromApiJsonDeserializer.validateForGenerateCollectionSheetOfIndividuals(query.json());

        final LocalDate transactionDate = query.localDateValueOfParameterNamed(transactionDateParamName);
        final DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        final String transactionDateStr = df.format(transactionDate.toDate());

        final AppUser currentUser = this.context.authenticatedUser();
        final String hierarchy = currentUser.getOffice().getHierarchy();
        final String officeHierarchy = hierarchy + "%";

        final Long officeId = query.longValueOfParameterNamed(officeIdParamName);
        final Long staffId = query.longValueOfParameterNamed(staffIdParamName);
        final boolean checkForOfficeId = officeId != null;
        final boolean checkForStaffId = staffId != null;

        //Habile changes 
        final IndividualCollectionSheetFaltDataMapper mapper = new IndividualCollectionSheetFaltDataMapper(checkForOfficeId,
                checkForStaffId);

        final SqlParameterSource namedParameters = new MapSqlParameterSource().addValue("dueDate", transactionDateStr).addValue(
                "officeHierarchy", officeHierarchy);

        if (checkForOfficeId) {
            ((MapSqlParameterSource) namedParameters).addValue("officeId", officeId);
        }
        if (checkForStaffId) {
            ((MapSqlParameterSource) namedParameters).addValue("staffId", staffId);
        }

        //Habile changes 
        final Collection<IndividualCollectionSheetLoanFlatData> collectionSheetFlatDatas = this.namedParameterjdbcTemplate.query(
                mapper.sqlSchema(), namedParameters, mapper);

        IndividualMandatorySavingsCollectionsheetExtractor mandatorySavingsExtractor = new IndividualMandatorySavingsCollectionsheetExtractor(
                checkForOfficeId, checkForStaffId);
        // mandatory savings data for collection sheet
        Collection<IndividualClientData> clientData = this.namedParameterjdbcTemplate.query(
                mandatorySavingsExtractor.collectionSheetSchema(), namedParameters, mandatorySavingsExtractor);

        // merge savings data into loan data
        mergeLoanData(collectionSheetFlatDatas, (List<IndividualClientData>) clientData);
        
        final Collection<PaymentTypeData> paymentOptions = this.paymentTypeReadPlatformService.retrieveAllPaymentTypes();

        return IndividualCollectionSheetData.instance(transactionDate, clientData, paymentOptions);

    }

    private static final class IndividualCollectionSheetFaltDataMapper implements RowMapper<IndividualCollectionSheetLoanFlatData> {

        private final String sql;

        public IndividualCollectionSheetFaltDataMapper(final boolean checkForOfficeId, final boolean checkforStaffId) {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT loandata.*, sum(lc.amount_outstanding_derived) as chargesDue ");
            sb.append("from (SELECT cl.display_name As clientName, ");
            sb.append("of.id as officeId, of.name as officeName, ");//Habile changes 
            sb.append("cl.id As clientId, ln.id As loanId, ln.account_no As accountId, ln.loan_status_id As accountStatusId,");
            sb.append(" pl.short_name As productShortName, ln.product_id As productId, ");
            sb.append("ln.currency_code as currencyCode, ln.currency_digits as currencyDigits, ln.currency_multiplesof as inMultiplesOf, ");
            sb.append("rc.`name` as currencyName, rc.display_symbol as currencyDisplaySymbol, rc.internationalized_name_code as currencyNameCode, ");
            sb.append("if(ln.loan_status_id = 200 , ln.principal_amount , null) As disbursementAmount, ");
            sb.append("sum(ifnull(if(ln.loan_status_id = 300, ls.principal_amount, 0.0), 0.0) - ifnull(if(ln.loan_status_id = 300, ls.principal_completed_derived, 0.0), 0.0)) As principalDue, ");
            sb.append("ln.principal_repaid_derived As principalPaid, ");
            sb.append("sum(ifnull(if(ln.loan_status_id = 300, ls.interest_amount, 0.0), 0.0) - ifnull(if(ln.loan_status_id = 300, ls.interest_completed_derived, 0.0), 0.0)) As interestDue, ");
            sb.append("ln.interest_repaid_derived As interestPaid, ");
            sb.append("sum(ifnull(if(ln.loan_status_id = 300, ls.fee_charges_amount, 0.0), 0.0) - ifnull(if(ln.loan_status_id = 300, ls.fee_charges_completed_derived, 0.0), 0.0)) As feeDue, ");
            sb.append("ln.fee_charges_repaid_derived As feePaid ");
            sb.append("FROM m_loan ln ");
            sb.append("JOIN m_client cl ON cl.id = ln.client_id  ");
            sb.append("LEFT JOIN m_office of ON of.id = cl.office_id  AND of.hierarchy like :officeHierarchy ");
            sb.append("LEFT JOIN m_product_loan pl ON pl.id = ln.product_id ");
            sb.append("LEFT JOIN m_currency rc on rc.`code` = ln.currency_code ");
            sb.append("JOIN m_loan_repayment_schedule ls ON ls.loan_id = ln.id AND ls.completed_derived = 0 AND ls.duedate <= :dueDate ");
            sb.append("where ");
            if (checkForOfficeId) {
                sb.append("of.id = :officeId and ");
            }
            if (checkforStaffId) {
                sb.append("ln.loan_officer_id = :staffId and ");
            }
            sb.append("(ln.loan_status_id = 300) ");
            sb.append("and ln.group_id is null GROUP BY cl.id , ln.id ORDER BY cl.id , ln.id ) loandata ");
            sb.append("LEFT JOIN m_loan_charge lc ON lc.loan_id = loandata.loanId AND lc.is_paid_derived = 0 AND lc.is_active = 1 AND ( lc.due_for_collection_as_of_date  <= :dueDate OR lc.charge_time_enum = 1) ");
            sb.append("GROUP BY loandata.clientId, loandata.loanId ORDER BY loandata.clientId, loandata.loanId ");

            sql = sb.toString();
        }

        public String sqlSchema() {
            return sql;
        }

        @Override
        public IndividualCollectionSheetLoanFlatData mapRow(ResultSet rs, @SuppressWarnings("unused") int rowNum) throws SQLException {
            final String clientName = rs.getString("clientName");
            final Long clientId = JdbcSupport.getLong(rs, "clientId");
            final Long loanId = JdbcSupport.getLong(rs, "loanId");
            final String accountId = rs.getString("accountId");
            final Integer accountStatusId = JdbcSupport.getInteger(rs, "accountStatusId");
            final String productShortName = rs.getString("productShortName");
            final Long productId = JdbcSupport.getLong(rs, "productId");

            final String currencyCode = rs.getString("currencyCode");
            final String currencyName = rs.getString("currencyName");
            final String currencyNameCode = rs.getString("currencyNameCode");
            final String currencyDisplaySymbol = rs.getString("currencyDisplaySymbol");
            final Integer currencyDigits = JdbcSupport.getInteger(rs, "currencyDigits");
            final Integer inMultiplesOf = JdbcSupport.getInteger(rs, "inMultiplesOf");
            CurrencyData currencyData = null;
            if (currencyCode != null) {
                currencyData = new CurrencyData(currencyCode, currencyName, currencyDigits, inMultiplesOf, currencyDisplaySymbol,
                        currencyNameCode);
            }

            final BigDecimal disbursementAmount = rs.getBigDecimal("disbursementAmount");
            final BigDecimal principalDue = rs.getBigDecimal("principalDue");
            final BigDecimal principalPaid = rs.getBigDecimal("principalPaid");
            final BigDecimal interestDue = rs.getBigDecimal("interestDue");
            final BigDecimal interestPaid = rs.getBigDecimal("interestPaid");
            final BigDecimal chargesDue = rs.getBigDecimal("chargesDue");
            final BigDecimal feeDue = rs.getBigDecimal("feeDue");
            final BigDecimal feePaid = rs.getBigDecimal("feePaid");
            
            //Habile changes
            final Long officeId = rs.getLong("officeId");
            final String officeName = rs.getString("officeName");

            //Habile changes 
            return new IndividualCollectionSheetLoanFlatData(clientName, clientId, loanId, accountId, accountStatusId, productShortName,
                    productId, currencyData, disbursementAmount, principalDue, principalPaid, interestDue, interestPaid, chargesDue,
                    feeDue, feePaid,officeId,officeName);
        }

    }

    private static final class IndividualMandatorySavingsCollectionsheetExtractor implements
            ResultSetExtractor<Collection<IndividualClientData>> {

        private final SavingsDueDataMapper savingsDueDataMapper = new SavingsDueDataMapper();

        private final String sql;

        public IndividualMandatorySavingsCollectionsheetExtractor(final boolean checkForOfficeId, final boolean checkforStaffId) {

            final StringBuffer sb = new StringBuffer(400);
            sb.append("SELECT if(sa.deposit_type_enum=100,'Saving Deposit',if(sa.deposit_type_enum=300,'Recurring Deposit','Current Deposit')) as depositAccountType, cl.display_name As clientName, cl.id As clientId, ");
            sb.append("sa.id As savingsId, sa.account_no As accountId, sa.status_enum As accountStatusId, ");
            sb.append("sp.short_name As productShortName, sp.id As productId, ");
            sb.append("sa.currency_code as currencyCode, sa.currency_digits as currencyDigits, sa.currency_multiplesof as inMultiplesOf, ");
            sb.append("rc.`name` as currencyName, rc.display_symbol as currencyDisplaySymbol, rc.internationalized_name_code as currencyNameCode, ");
            sb.append("sum(ifnull(mss.deposit_amount,0) - ifnull(mss.deposit_amount_completed_derived,0)) as dueAmount ");
            sb.append("FROM m_savings_account sa ");
            sb.append("JOIN m_client cl ON cl.id = sa.client_id ");
            sb.append("JOIN m_savings_product sp ON sa.product_id=sp.id ");
            sb.append("LEFT JOIN m_deposit_account_recurring_detail dard ON sa.id = dard.savings_account_id AND dard.is_mandatory = true AND dard.is_calendar_inherited = false ");
            sb.append("LEFT JOIN m_mandatory_savings_schedule mss ON mss.savings_account_id=sa.id AND mss.completed_derived = 0 AND mss.duedate <= :dueDate ");
            sb.append("LEFT JOIN m_office of ON of.id = cl.office_id AND of.hierarchy like :officeHierarchy ");
            sb.append("LEFT JOIN m_currency rc on rc.`code` = sa.currency_code ");
            sb.append("WHERE sa.status_enum=300 and sa.group_id is null and sa.deposit_type_enum in (100,300,400) ");
            sb.append("and (cl.status_enum = 300 or (cl.status_enum = 600 and cl.closedon_date >= :dueDate)) ");
            if (checkForOfficeId) {
                sb.append("and of.id = :officeId ");
            }
            if (checkforStaffId) {
                sb.append("and sa.field_officer_id = :staffId ");
            }
            sb.append("GROUP BY cl.id , sa.id ORDER BY cl.id , sa.id ");

            this.sql = sb.toString();
        }

        public String collectionSheetSchema() {
            return this.sql;
        }

        @SuppressWarnings("null")
        @Override
        public Collection<IndividualClientData> extractData(ResultSet rs) throws SQLException, DataAccessException {
            List<IndividualClientData> clientData = new ArrayList<>();
            int rowNum = 0;

            IndividualClientData client = null;
            Long previousClientId = null;

            while (rs.next()) {
                final Long clientId = JdbcSupport.getLong(rs, "clientId");
                if (previousClientId == null || clientId.compareTo(previousClientId) != 0) {
                    final String clientName = rs.getString("clientName");
                    client = IndividualClientData.instance(clientId, clientName);
                    client = IndividualClientData.withSavings(client, new ArrayList<SavingsDueData>());
                    clientData.add(client);
                    previousClientId = clientId;
                }
                SavingsDueData saving = savingsDueDataMapper.mapRow(rs, rowNum);
                client.addSavings(saving);
                rowNum++;
            }

            return clientData;
        }
    }

    private void mergeLoanData(final Collection<IndividualCollectionSheetLoanFlatData> loanFlatDatas, List<IndividualClientData> clientDatas) {

        IndividualClientData clientSavingsData = null;
        for (IndividualCollectionSheetLoanFlatData loanFlatData : loanFlatDatas) {
            IndividualClientData clientData = loanFlatData.getClientData();
            if (clientSavingsData == null || !clientSavingsData.equals(clientData)) {
                if (clientDatas.contains(clientData)) {
                    final int index = clientDatas.indexOf(clientData);
                    if (index < 0) return;
                    clientSavingsData = clientDatas.get(index);
                    clientSavingsData.setLoans(new ArrayList<LoanDueData>());
                } else {
                    clientSavingsData = clientData;
                    clientSavingsData.setLoans(new ArrayList<LoanDueData>());
                    clientDatas.add(clientSavingsData);
                }
            }
            clientSavingsData.addLoans(loanFlatData.getLoanDueData());
        }
    }

    
    @Override
    public List<BulkReminderData> smsReminderData(String officeId, String centerId, String groupId, String loanOfficerId, String startDate, String endDate) {


        final AppUser currentUser = this.context.authenticatedUser();
        final BulkReminderMapper rm = new BulkReminderMapper();
        final StringBuilder sqlBuilder = new StringBuilder(200);
        sqlBuilder.append("select ");
        sqlBuilder.append(rm.bulkdata());
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
    	
    	sqlBuilder.append(" and lr.duedate between '"+prev+"' and '"+future+"'");
    	//return this.jdbcTemplate.query(sql, rm, new Object[] {});

        return this.jdbcTemplate.query(sqlBuilder.toString(), rm, new Object[] {officeId});

    }
      private static final class BulkReminderMapper implements RowMapper<BulkReminderData> {
        	public String bulkdata() {
    			return " ml.id as id,ml.account_no as acc,mc.firstname as firstname, mc.mobile_no as mobileno, "
    					+ "lr.duedate as duedate,lr.installment as installment, (((IFNULL(lr.principal_amount,0) + "
    					+ "IFNULL(lr.interest_amount,0) + IFNULL(lr.fee_charges_amount,0) "
    					+ "+ IFNULL(lr.penalty_charges_amount,0))-IFNULL(lr.completed_derived,0))) as repaymentamount "
    					+ "from m_client mc join m_loan ml on mc.id=ml.client_id and ml.loan_status_id in (300) left join "
    					+ "m_staff ms on ml.loan_officer_id=ms.id left join m_office mo on mo.id=mc.office_id left join "
    					+ "m_group_client mgc on mc.id=mgc.client_id join m_group mg on mg.id=mgc.group_id join m_group mgg "
    					+ "on mgg.id=mg.parent_id join m_loan_repayment_schedule lr on lr.loan_id=ml.id where "
    					+ " mo.id = ? ";
    		}

		@Override
		public BulkReminderData mapRow(final ResultSet rs,@SuppressWarnings("unused")final int rowNum) throws SQLException {
			
			final Integer id = rs.getInt("id"); 
			final String acc = rs.getString("acc");
			final String name = rs.getString("firstname");
			final String mobileno = rs.getString("mobileno");
			final String duedate = rs.getString("duedate");
			final String installment = rs.getString("installment");
			final BigDecimal repaymentamount = rs.getBigDecimal("repaymentamount");

			return new BulkReminderData(acc, name, mobileno, duedate, installment, repaymentamount, id);
		}

    }


	
}
