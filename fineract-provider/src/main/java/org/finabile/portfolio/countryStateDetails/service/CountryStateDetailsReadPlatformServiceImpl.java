package org.finabile.portfolio.countryStateDetails.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.fineract.infrastructure.core.service.RoutingDataSource;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.useradministration.domain.AppUser;
import org.finabile.portfolio.CKYC.data.CKYCData;
import org.finabile.portfolio.countryStateDetails.data.CountryStateDetailsData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
@Service
public class CountryStateDetailsReadPlatformServiceImpl implements CountryStateDetailsReadPlatformService{

	private final PlatformSecurityContext context;
	private final JdbcTemplate jdbcTemplate;
	
	@Autowired
	public CountryStateDetailsReadPlatformServiceImpl(final PlatformSecurityContext context,final RoutingDataSource dataSource) {
		this.context = context;
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}

	@Override
	public List<CountryStateDetailsData> retrieveStateDetails(Long pincodeMatch) {
		final AppUser currentUser = this.context.authenticatedUser();
		final CountryStateDetailsMapper rm = new CountryStateDetailsMapper();
		final StringBuilder sqlBuilder = new StringBuilder(200);
		sqlBuilder.append("select ");
		sqlBuilder.append(rm.countryStateDetailsSchema());
		return this.jdbcTemplate.query(sqlBuilder.toString(), rm, new Object[] {});

	}
	
	@Override
	public List<CountryStateDetailsData> retrieveStateDetails1(Long pincodeMatch) {
		final AppUser currentUser = this.context.authenticatedUser();
		final CountryStateDetailsMapper rm = new CountryStateDetailsMapper();
		final StringBuilder sqlBuilder = new StringBuilder(200);
		sqlBuilder.append("select ");
		sqlBuilder.append(rm.countryStateDetailsSchema());
		sqlBuilder.append("where s.pincode like '"+pincodeMatch+"%'");
		return this.jdbcTemplate.query(sqlBuilder.toString(), rm, new Object[] {});

	}
	private static final class CountryStateDetailsMapper implements RowMapper<CountryStateDetailsData> {

		public String countryStateDetailsSchema() {
			String sql= " s.id as id,s.pincode as pincode,"
					+ "s.district as district,"
					+ "s.state_code as stateCode,"
					+ "s.state as state "
					+ "from hab_statedetails s ";
			return sql;
		}

		@Override
		public CountryStateDetailsData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {
			final long id = rs.getLong("id");
			final Long pincode = rs.getLong("pincode");
			final String district = rs.getString("district");
			final String stateCode = rs.getString("stateCode");
			final String state = rs.getString("state");

			return new CountryStateDetailsData(id, pincode, district, stateCode, state);
		}
	}

}
