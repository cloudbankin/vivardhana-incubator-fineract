package org.finabile.portfolio.CKYC.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.fineract.infrastructure.core.service.Page;
import org.apache.fineract.infrastructure.core.service.PaginationHelper;
import org.apache.fineract.infrastructure.core.service.SearchParameters;
import org.apache.fineract.infrastructure.documentmanagement.data.DocumentData;
import org.apache.fineract.infrastructure.documentmanagement.domain.Image;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.client.domain.Client;
import org.apache.fineract.useradministration.domain.AppUser;
import org.finabile.portfolio.CKYC.data.CKYCData;
import org.finabile.portfolio.CKYC.exception.DateMisMatchException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.apache.fineract.infrastructure.core.service.RoutingDataSource;
import org.springframework.jdbc.core.RowMapper;

import org.springframework.jdbc.core.JdbcTemplate;

@Service
public class CKYCReadPlatformServiceImpl implements CKYCReadPlatformService {

	private final PlatformSecurityContext context;
	private final JdbcTemplate jdbcTemplate;
	private final PaginationHelper<CKYCData> paginationHelper = new PaginationHelper<>();

	@Autowired
	public CKYCReadPlatformServiceImpl(final PlatformSecurityContext context, final RoutingDataSource dataSource) {
		this.context = context;
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}

	@Override
	public Page<CKYCData> retrieveAllActivatedClients(SearchParameters searchParameters) {
		final AppUser currentUser = this.context.authenticatedUser();
		List<Object> paramList = new ArrayList<>(Arrays.asList());
		final CKYCMapper rm = new CKYCMapper();
		final StringBuilder sqlBuilder = new StringBuilder(200);
		sqlBuilder.append("select SQL_CALC_FOUND_ROWS ");
		sqlBuilder.append(rm.cKYCSchema());
		sqlBuilder.append(" order by mc.id desc ");
		sqlBuilder.append(" limit ").append(searchParameters.getLimit());
		sqlBuilder.append(" offset ").append(searchParameters.getOffset());
		final String sqlCountRows = "SELECT FOUND_ROWS()";

		return this.paginationHelper.fetchPage(this.jdbcTemplate, sqlCountRows, sqlBuilder.toString(),
				paramList.toArray(), rm);

	}

	private static final class CKYCMapper implements RowMapper<CKYCData> {

		public String cKYCSchema() {
			return " mc.id as id" + ",mo.name as officeName" + ",mc.display_name as clientName"
					+ ",mc.account_no as clientId" + ",mc.activation_date as clientActivationDate"
					+ " from  m_client mc left join m_office mo on mo.id = mc.office_id where mc.status_enum = 300 and "
					+ "mc.id not in(select client_id from hab_ckyc_details )";
		}

		@Override
		public CKYCData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {
			final long id = rs.getLong("id");
			final String officeName = rs.getString("officeName");
			final String clientName = rs.getString("clientName");
			final String clientId = rs.getString("clientId");
			final Date clientActivationDate = rs.getDate("clientActivationDate");

			return new CKYCData(id, officeName, clientName, clientId, clientActivationDate);
		}
	}

	@Override
	public List<CKYCData> retrieveSearchedData(String startDate, String endDate) {
		final AppUser currentUser = this.context.authenticatedUser();

		java.util.Date startDateFormat = null;
		java.util.Date endDateFormat = null;

		try {
			startDateFormat = new SimpleDateFormat("dd MMMM yyyy").parse(startDate);
			endDateFormat = new SimpleDateFormat("dd MMMM yyyy").parse(endDate);
		} catch (ParseException e) {
			e.printStackTrace();
		}

		validationForDate(startDateFormat, endDateFormat);

		String sd = new SimpleDateFormat("yyyy-MM-dd").format(startDateFormat);
		String ed = new SimpleDateFormat("yyyy-MM-dd").format(endDateFormat);

		final CKYCMapper rm = new CKYCMapper();
		final StringBuilder sqlBuilder = new StringBuilder(200);
		sqlBuilder.append("select ");
		sqlBuilder.append(rm.cKYCSchema());
		sqlBuilder.append(" and mc.activation_date between '" + sd + "' and '" + ed + "'");
		return this.jdbcTemplate.query(sqlBuilder.toString(), rm, new Object[] {});

	}

	private void validationForDate(Date sd, Date ed) {

		if (sd.after(new Date())) {
			throw new DateMisMatchException("error.bankRemittance.invalid.start.date",
					"Start date must be past or current only allowed");
		} else if (ed.before(sd)) {
			throw new DateMisMatchException("error.bankRemittance.invalid.end.date",
					"End date must be after the start date");
		} else if (ed.after(new Date())) {

			throw new DateMisMatchException("error.bankRemittance.invalid.end.date.future.exception",
					"End date must be after the start date or it may current date only allowed");
		}

	}

	@Override
	public List<CKYCData> retriveActivatedClientsData() {

		final AppUser currentUser = this.context.authenticatedUser();
		final CKYCGeneratedFileMapper rm = new CKYCGeneratedFileMapper();
		final StringBuilder sqlBuilder = new StringBuilder(200);
		sqlBuilder.append("select ");
		sqlBuilder.append(rm.cKYCGeneratedSchema());
		return this.jdbcTemplate.query(sqlBuilder.toString(), rm, new Object[] {});
	
	}
	private static final class CKYCGeneratedFileMapper implements RowMapper<CKYCData> {

		public String cKYCGeneratedSchema() {
			return " mc.id as id" + ",mo.name as officeName" + ",mc.display_name as clientName"
					+ ",mc.account_no as clientId" + ",mc.activation_date as clientActivationDate"
					+ " from  m_client mc left join m_office mo on mo.id = mc.office_id "
					+ " join hab_ckyc_details h where h.client_id=mc.id and  mc.status_enum = 300";
		}

		@Override
		public CKYCData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum)
				throws SQLException {
			final long id = rs.getLong("id");
			final String officeName = rs.getString("officeName");
			final String clientName = rs.getString("clientName");
			final String clientId = rs.getString("clientId");
			final Date clientActivationDate = rs.getDate("clientActivationDate");

			return new CKYCData(id, officeName, clientName, clientId, clientActivationDate);
	}
	}

	@Override
	public List<CKYCData> getClientFamilyDetails(Client client) {
		final AppUser currentUser = this.context.authenticatedUser();

		final ClientFamilyDetailsMapper rm = new ClientFamilyDetailsMapper();
		final StringBuilder sqlBuilder = new StringBuilder(200);
		sqlBuilder.append("select ");
		sqlBuilder.append(rm.clientFamilyDetailsSchema());
		return this.jdbcTemplate.query(sqlBuilder.toString(), rm, new Object[] {client.getId()});
	}
	private static final class ClientFamilyDetailsMapper implements RowMapper<CKYCData> {

		public String clientFamilyDetailsSchema() {
			return "fd.`Father Name` as fatherName,"
					+ "fd.`Mother Name` as motherName,"
					+ "fd.`Spouse Name` as spouseName,"
					+ "fd.`Marital Status` as maritalStatus,"
					+ "fd.Occupation_cd_Occupation as occupation "
					+ "from `CKYC Family Details` fd left join m_client mc on "
					+ "mc.id = fd.client_id where mc.id = ?";
		}

		@Override
		public CKYCData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {
			final String fatherName = rs.getString("fatherName");
			final String motherName = rs.getString("motherName");
			final String spouseName = rs.getString("spouseName");
			final String maritalStatus = rs.getString("maritalStatus");
			final String occupation = rs.getString("occupation");

			return new CKYCData(fatherName,motherName,spouseName,maritalStatus,occupation);
		}
	}

	@Override
	public DocumentData readDocumentDetails(String string, Long id) {
		final AppUser currentUser = this.context.authenticatedUser();

		final ClientDocumentDetailsMapper rm = new ClientDocumentDetailsMapper();
		final StringBuilder sqlBuilder = new StringBuilder(200);
		sqlBuilder.append("select ");
		sqlBuilder.append(rm.clientDocumentDetailsSchema());
		return this.jdbcTemplate.queryForObject(sqlBuilder.toString(), rm, new Object[] {string,id});
		}
	private static final class ClientDocumentDetailsMapper implements RowMapper<DocumentData> {

		public String clientDocumentDetailsSchema() {
			return "m.id as id, m.file_name as fileName ,"
					+ " m.name as Name,m.`type` as fileType,"
					+ "m.location as fileLocation from m_document m where"
					+ " m.parent_entity_type =? and m.parent_entity_id=? ";
		}

		@Override
		public DocumentData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {
			final long id = rs.getLong("id");
			final String fileName = rs.getString("fileName");
			final String Name = rs.getString("Name");
			final String fileType = rs.getString("fileType");
			final String fileLocation = rs.getString("fileLocation");

			return new DocumentData(id,fileName,Name,fileType,fileLocation);
		}
	}
	@Override
	public List<CKYCData> retrievePanData(String name, String panno) {
		final AppUser currentUser = this.context.authenticatedUser();

		


		final CKYCpanMapper rm = new CKYCpanMapper();
		final StringBuilder sqlBuilder = new StringBuilder(200);
		sqlBuilder.append("select ");
		sqlBuilder.append(rm.cKYCpanSchema());
		sqlBuilder.append(" and mc.external_id like '%" + panno + "%' or mc.display_name like '%" + name + "%'");
		return this.jdbcTemplate.query(sqlBuilder.toString(), rm, new Object[] {});

	}
	
	private static final class CKYCpanMapper implements RowMapper<CKYCData> {

		public String cKYCpanSchema() {
			return " distinct mc.id as id" + ",mo.name as officeName" + ",mc.display_name as clientName"
					+ ",mc.account_no as clientId" + ",mc.activation_date as clientActivationDate"
					+ " from  m_client mc left join m_office mo on mo.id = mc.office_id "
					+ "  where mc.status_enum = 300  ";
					
		}

		@Override
		public CKYCData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {
			final long id = rs.getLong("id");
			final String officeName = rs.getString("officeName");
			final String clientName = rs.getString("clientName");
			final String clientId = rs.getString("clientId");
			final Date clientActivationDate = rs.getDate("clientActivationDate");

			return new CKYCData(id, officeName, clientName, clientId, clientActivationDate);
		}
	}
	

}
