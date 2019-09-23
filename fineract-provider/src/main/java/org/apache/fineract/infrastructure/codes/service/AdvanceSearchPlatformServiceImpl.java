package org.apache.fineract.infrastructure.codes.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

import org.apache.fineract.infrastructure.codes.data.AdvanceSearchData;
import org.apache.fineract.infrastructure.core.service.RoutingDataSource;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
public class AdvanceSearchPlatformServiceImpl implements AdvanceSearchPlatformService
{
	private final JdbcTemplate jdbcTemplate;
    private final PlatformSecurityContext context;
    
    @Autowired
    public AdvanceSearchPlatformServiceImpl(final PlatformSecurityContext context, final RoutingDataSource dataSource) {
        this.context = context;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }
    
    private static final class AdvanceSearchDataMapper implements RowMapper<AdvanceSearchData> {

		@Override
		public AdvanceSearchData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException
		{
			
			 final Long id = rs.getLong("id");
	         final String name = rs.getString("name");
			
			return AdvanceSearchData.instance(id, name);
		}
    	
    	
    }
    
	@Override
	public Collection<AdvanceSearchData> retrieveLoanProducts()
	{
		this.context.authenticatedUser();
		
		final AdvanceSearchDataMapper rm = new AdvanceSearchDataMapper();
		
		//select id , name from m_product_loan
		final String sql = "select id , name from m_product_loan order by id";
		
		return this.jdbcTemplate.query(sql, rm);
	}

	@Override
	public Collection<AdvanceSearchData> retrieveFunds() {
		
		this.context.authenticatedUser();
		
		final AdvanceSearchDataMapper rm = new AdvanceSearchDataMapper();
		
		final String sql = "select id , name from m_fund order by id";
		
		return this.jdbcTemplate.query(sql, rm);
	}

}
