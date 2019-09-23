package org.finabile.portfolio.bankremittance.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.fineract.infrastructure.codes.data.CodeValueData;
import org.apache.fineract.infrastructure.codes.service.CodeValueReadPlatformService;
import org.apache.fineract.infrastructure.core.service.Page;
import org.apache.fineract.infrastructure.core.service.PaginationHelper;
import org.apache.fineract.infrastructure.core.service.RoutingDataSource;
import org.apache.fineract.infrastructure.core.service.SearchParameters;
import org.apache.fineract.infrastructure.documentmanagement.data.FileData;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.client.data.ClientData;
import org.apache.fineract.useradministration.domain.AppUser;
import org.finabile.portfolio.bankremittance.api.BankRemittanceConstants;
import org.finabile.portfolio.bankremittance.data.BankRemittanceData;
import org.finabile.portfolio.bankremittance.exception.DateMisMatchException;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;


@Service
public class BankRemittanceReadPlatformServiceImpl implements BankRemittanceReadPlatformService {

	private final CodeValueReadPlatformService codeValueReadPlatformService;
	private final PlatformSecurityContext context;
	private final JdbcTemplate jdbcTemplate;
    private final PaginationHelper<BankRemittanceData> paginationHelper = new PaginationHelper<>();


	@Autowired
	public BankRemittanceReadPlatformServiceImpl(final CodeValueReadPlatformService codeValueReadPlatformService,
			final PlatformSecurityContext context, final RoutingDataSource dataSource
			) {
		this.codeValueReadPlatformService = codeValueReadPlatformService;
		this.context = context;
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}

	@Override
	public BankRemittanceData retrieveTemplate() {
		final List<CodeValueData> bankOptions = new ArrayList<>(
				this.codeValueReadPlatformService.retrieveCodeValuesByCode(BankRemittanceConstants.BANK_DETAILS));
		return BankRemittanceData.template(bankOptions);
	}

	@Override
	public Page<BankRemittanceData> retrieveAllApprovedLoans(SearchParameters searchParameters) {

		final AppUser currentUser = this.context.authenticatedUser();
		 List<Object> paramList = new ArrayList<>(Arrays.asList());
		final bankRemittanceMapper rm = new bankRemittanceMapper();
		final StringBuilder sqlBuilder = new StringBuilder(200);
		sqlBuilder.append("select SQL_CALC_FOUND_ROWS ");
		sqlBuilder.append(rm.bankRemittanceSchema());
		sqlBuilder.append(" order by l.id desc ");
		 sqlBuilder.append(" limit ").append(searchParameters.getLimit());
             sqlBuilder.append(" offset ").append(searchParameters.getOffset());
             final String sqlCountRows = "SELECT FOUND_ROWS()";

     return this.paginationHelper.fetchPage(this.jdbcTemplate, sqlCountRows, sqlBuilder.toString(),paramList.toArray(), rm);

	//	return this.jdbcTemplate.query(sqlBuilder.toString(), rm, new Object[] {});

		// List<Loan> listOfApprovedLoans=this.loanRepository.findAllApprovedLoans();

	}

	private static final class bankRemittanceMapper implements RowMapper<BankRemittanceData> {

		public String bankRemittanceSchema() {
			return " l.id as id,c.account_no as accountNumber,c.display_name as clientFullName, "
					+ "bd.`account holder NAME` as accountHolderName, "
					+ "(l.principal_amount - l.total_charges_due_at_disbursement_derived) AS principal, "
					+ "bd.`account number` as bankAccountnumber, "
					+ "mg.display_name as groupDisplayName,mg.external_id as groupId, "
					+ "bd.`ifsc code` as IFSCCode,bd.`bank NAME` as bankName, "
					+ "bd.`benificiary address` as address , o.name as officeName FROM m_office o "
					+ "JOIN m_client c ON c.office_id=o.id JOIN m_loan l "
					+ "ON l.client_id = c.id LEFT JOIN `bank detail` bd ON bd.client_id=c.id "
					+ "LEFT JOIN m_group_client mgc ON mgc.client_id=c.id LEFT JOIN m_group mg "
					+ "ON mg.id=mgc.group_id  left join hab_bank_remittance_generator_details hbrgd on hbrgd.loan_id=l.id WHERE l.loan_status_id=200 and hbrgd.loan_id is null";
		}

		@Override
		public BankRemittanceData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum)
				throws SQLException {
			final long id = rs.getLong("id");
			final String accountNumber = rs.getString("accountNumber");
			final String clientFullName = rs.getString("clientFullName");
			final String accountHolderName = rs.getString("accountHolderName");
			final BigDecimal principal = rs.getBigDecimal("principal");
			final String bankAccountnumber = rs.getString("bankAccountnumber");
			final String groupDisplayName = rs.getString("groupDisplayName");
			final String groupId = rs.getString("groupId");
			final String IFSCCode = rs.getString("IFSCCode");
			final String bankName = rs.getString("bankName");
			final String address = rs.getString("address");
			final String officeName = rs.getString("officeName");

			return new BankRemittanceData(id,accountNumber, clientFullName, accountHolderName, principal,
					bankAccountnumber, groupDisplayName, groupId, IFSCCode, bankName, address,officeName,0L,null);
		}
	}

	@Override
	public List<BankRemittanceData> retrieveSearchedData(String bankId, String startDate, String endDate) {
		final AppUser currentUser = this.context.authenticatedUser();
	
		java.util.Date startDateFormat = null ;
		java.util.Date endDateFormat = null;
		try {
			startDateFormat = new SimpleDateFormat("dd MMMM yyyy").parse(startDate);
			endDateFormat = new SimpleDateFormat("dd MMMM yyyy").parse(endDate);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		validationForDate(startDateFormat,endDateFormat);
		
		String sd = new SimpleDateFormat("yyyy-MM-dd").format(startDateFormat);
		String ed = new SimpleDateFormat("yyyy-MM-dd").format(endDateFormat);
	
		final bankRemittanceMapper rm = new bankRemittanceMapper();
		final StringBuilder sqlBuilder = new StringBuilder(200);
		sqlBuilder.append("select ");
		sqlBuilder.append(rm.bankRemittanceSchema());
		//sqlBuilder.append(" and l.approvedon_date between '"+sd+ "' and '"+ed+"'");
		//SELECT DATEADD(day, 2, '2017/08/25') AS DateAdd
		sqlBuilder.append(" and l.approvedon_date between '"+sd+ "' and '"+ed+"'");
		return this.jdbcTemplate.query(sqlBuilder.toString(), rm, new Object[] {});

	}

	private void validationForDate(Date sd, Date ed) {
		
		if(sd.after(new Date())){
			throw new DateMisMatchException("error.bankRemittance.invalid.start.date","Start date must be past or current only allowed");
		}else if(ed.before(sd)) {
			throw new DateMisMatchException("error.bankRemittance.invalid.end.date","End date must be after the start date");
		}else if(ed.after(new Date())) {
			
			throw new DateMisMatchException("error.bankRemittance.invalid.end.date.future.exception","End date must be after the start date or it may current date only allowed");
		}

	}

	@Override
	public BankRemittanceData readBankDetails(Long clientId) {
		final AppUser currentUser = this.context.authenticatedUser();
		final bankDetailsMapper rm = new bankDetailsMapper();
		final StringBuilder sqlBuilder = new StringBuilder(200);
		sqlBuilder.append("select ");
		sqlBuilder.append(rm.bankDetailsSchema());

		return this.jdbcTemplate.queryForObject(sqlBuilder.toString(), rm, new Object[] {clientId});
		
	}
	private static final class bankDetailsMapper implements RowMapper<BankRemittanceData> {

		public String bankDetailsSchema() {
			return " bd.id as id,bd.`Account Number` as accountNumber,bd.`Bank Name` as bankName, " + 
					"bd.`Account Holder Name` as accoundHolderName,bd.`IFSC Code` as IFSCCode,bd.`Benificiary Address` as address " + 
					"from `bank detail` bd left join m_client a on a.id=bd.client_id where a.id=?";
		}

		@Override
		public BankRemittanceData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum)
				throws SQLException {
			final long id = rs.getLong("id");
			final String accountNumber = rs.getString("accountNumber");
			final String bankName = rs.getString("bankName");
			final String accountHolderName = rs.getString("accoundHolderName");
			final String IFSCCode = rs.getString("IFSCCode");
			final String address = rs.getString("address");
			
			
			

			return new BankRemittanceData(id,accountNumber, null, accountHolderName, null,
					null, null, null, IFSCCode, bankName, address,null,0L,null);
		}
	}

	@Override
	public FileData retrieveFileData() {
		 final File file = new File("/home/habileos2/Desktop/test.txt");
		 HttpServletResponse response = null;
		 try {
			FileInputStream FIS = new FileInputStream(file);
			ServletOutputStream out = response.getOutputStream();
			byte[] outputByte = new byte[4096];
			while(FIS.read(outputByte, 0, 4096) != -1)
			{
				out.write(outputByte, 0, 4096);
			}
			FIS.close();
			out.flush();
			out.close();


		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	        return new FileData(file,"test","text/html");
	}

	@Override
	public List<BankRemittanceData> retrieveApprovedLoansData() {
	
		final AppUser currentUser = this.context.authenticatedUser();
		final bankRemittanceGeneratedFileMapper rm = new bankRemittanceGeneratedFileMapper();
		final StringBuilder sqlBuilder = new StringBuilder(200);
		sqlBuilder.append("select ");
		sqlBuilder.append(rm.bankRemittanceGeneratedSchema());
		return this.jdbcTemplate.query(sqlBuilder.toString(), rm, new Object[] {});
	}
	private static final class bankRemittanceGeneratedFileMapper implements RowMapper<BankRemittanceData> {

		public String bankRemittanceGeneratedSchema() {
			return " l.id as id,c.account_no as accountNumber,c.display_name as clientFullName, "
					+ "bd.`account holder NAME` as accountHolderName, "
					+ "(l.principal_amount - l.total_charges_due_at_disbursement_derived) AS principal, "
					+ "bd.`account number` as bankAccountnumber, "
					+ "mg.display_name as groupDisplayName,mg.external_id as groupId, "
					+ "bd.`ifsc code` as IFSCCode,bd.`bank NAME` as bankName, "
					+ "bd.`benificiary address` as address , o.name as officeName "
					+ ",mcv.id as bankId,mcv.code_value as bankCodeName"
					+ " FROM m_office o "
					+ "JOIN m_client c ON c.office_id=o.id JOIN m_loan l "
					+ "ON l.client_id = c.id LEFT JOIN `bank detail` bd ON bd.client_id=c.id "
					+ "LEFT JOIN m_group_client mgc ON mgc.client_id=c.id LEFT JOIN m_group mg "
					+ "ON mg.id=mgc.group_id  left join hab_bank_remittance_generator_details hbrgd on hbrgd.loan_id=l.id left join m_code_value mcv on mcv.id=hbrgd.bank_id WHERE l.loan_status_id=200 and hbrgd.loan_id is not null";
		}

		@Override
		public BankRemittanceData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum)
				throws SQLException {
			final long id = rs.getLong("id");
			final String accountNumber = rs.getString("accountNumber");
			final String clientFullName = rs.getString("clientFullName");
			final String accountHolderName = rs.getString("accountHolderName");
			final BigDecimal principal = rs.getBigDecimal("principal");
			final String bankAccountnumber = rs.getString("bankAccountnumber");
			final String groupDisplayName = rs.getString("groupDisplayName");
			final String groupId = rs.getString("groupId");
			final String IFSCCode = rs.getString("IFSCCode");
			final String bankName = rs.getString("bankName");
			final String address = rs.getString("address");
			final String officeName = rs.getString("officeName");
			final long bankId = rs.getLong("bankId");
			final String bankCodeName = rs.getString("bankCodeName");

			return new BankRemittanceData(id,accountNumber, clientFullName, accountHolderName, principal,
					bankAccountnumber, groupDisplayName, groupId, IFSCCode, bankName, address,officeName,bankId,bankCodeName);
		}
	}

	}
