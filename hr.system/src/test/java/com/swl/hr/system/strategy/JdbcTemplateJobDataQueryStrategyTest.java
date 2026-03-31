package com.swl.hr.system.strategy;

import com.swl.hr.system.request.job.JobDataQueryRequest;
import com.swl.hr.system.response.job.JobDataListResponse;
import com.swl.hr.system.response.job.JobDataResponse;
import com.swl.hr.system.util.CommonConstant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.util.List;
import java.sql.ResultSet;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JdbcTemplateJobDataQueryStrategyTest {

    @Mock
    private NamedParameterJdbcTemplate jdbcTemplate;

    @InjectMocks
    private JdbcTemplateJobDataQueryStrategy strategy;

    @Test
    @DisplayName("getName returns 'jdbc'")
    void getName_returnsJdbc() {
        assertEquals(CommonConstant.STRATEGY_JDBC, strategy.getName());
    }

    @Test
    @DisplayName("buildOrderClause uses normalized columns and sort direction")
    void buildOrderClause_buildsOrderClauseCorrectly() {
        JobDataQueryRequest req = new JobDataQueryRequest();
        req.setSort("job_title,salary");
        req.setSortType(Sort.Direction.DESC);

        // Use reflection to call private method buildOrderClause
        try {
            java.lang.reflect.Method m = JdbcTemplateJobDataQueryStrategy.class.getDeclaredMethod("buildOrderClause", JobDataQueryRequest.class);
            m.setAccessible(true);
            String order = (String) m.invoke(strategy, req);
            assertEquals(" ORDER BY job_title DESC, salary DESC", order);
        } catch (Exception e) {
            fail("Reflection invocation failed: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("query builds SQL with where/order/limit-offset and maps rows to response")
    void query_buildsSql_andMapsRows() {
        JobDataQueryRequest req = new JobDataQueryRequest();
        req.setJobTitle("Dev");
        req.setGender("Female");
        req.setFromSalary(new BigDecimal("500"));
        req.setToSalary(new BigDecimal("2500"));
        req.setSort("job_title,salary");
        req.setSortType(Sort.Direction.ASC);
        req.setPage(2);
        req.setSize(2);

        JobDataResponse r1 = new JobDataResponse();
        r1.setJobTitle("Developer");
        r1.setSalary(new BigDecimal("1000"));
        r1.setGender("Female");
        JobDataResponse r2 = new JobDataResponse();
        r2.setJobTitle("Lead Developer");
        r2.setSalary(new BigDecimal("2000"));
        r2.setGender("Female");

        when(jdbcTemplate.query(anyString(), any(MapSqlParameterSource.class), any(RowMapper.class)))
                .thenReturn(List.of(r1, r2));
        when(jdbcTemplate.queryForObject(startsWith("SELECT COUNT(1)"), any(MapSqlParameterSource.class), eq(Long.class)))
                .thenReturn(7L);

        JobDataListResponse resp = strategy.query(req);

        assertNotNull(resp);
        assertEquals(2, resp.getPage());
        assertEquals(2, resp.getSize());
        assertEquals(7L, resp.getTotalElements());
        assertEquals(4, resp.getTotalPages());
        assertNotNull(resp.getJobDataList());
        assertEquals(2, resp.getJobDataList().size());
        assertEquals("Developer", resp.getJobDataList().get(0).getJobTitle());
        assertEquals(new BigDecimal("1000"), resp.getJobDataList().get(0).getSalary());
        assertEquals("Female", resp.getJobDataList().get(0).getGender());

        verify(jdbcTemplate, times(1)).query(contains("FROM job_data"), any(MapSqlParameterSource.class), any(RowMapper.class));
        verify(jdbcTemplate, times(1)).queryForObject(contains("FROM job_data"), any(MapSqlParameterSource.class), eq(Long.class));
    }

    @Test
    @DisplayName("query with minimal request uses defaults and computes totals correctly")
    void query_minimalRequest_defaultsAndTotals() {
        JobDataQueryRequest req = new JobDataQueryRequest();
        strategy.defaultPageConfig = 0;
        strategy.defaultSizeConfig = 10;

        when(jdbcTemplate.query(anyString(), any(MapSqlParameterSource.class), any(RowMapper.class)))
                .thenReturn(List.of());
        when(jdbcTemplate.queryForObject(startsWith("SELECT COUNT(1)"), any(MapSqlParameterSource.class), eq(Long.class)))
                .thenReturn(0L);

        JobDataListResponse resp = strategy.query(req);
        assertNotNull(resp);
        assertEquals(0, resp.getPage());
        assertEquals(10, resp.getSize());
        assertEquals(0L, resp.getTotalElements());
        assertEquals(0, resp.getTotalPages());

        verify(jdbcTemplate, times(1)).query(anyString(), any(MapSqlParameterSource.class), any(RowMapper.class));
        verify(jdbcTemplate, times(1)).queryForObject(anyString(), any(MapSqlParameterSource.class), eq(Long.class));
    }

    @Test
    @DisplayName("rowMapper maps ResultSet to JobDataResponse correctly")
    void rowMapper_mapsFieldsCorrectly() throws Exception {
        JobDataQueryRequest req = new JobDataQueryRequest();
        strategy.defaultPageConfig = 0;
        strategy.defaultSizeConfig = 10;

        ResultSet rs = mock(ResultSet.class);
        when(rs.getString(CommonConstant.COL_JOB_TITLE)).thenReturn("Software Engineer");
        when(rs.getBigDecimal(CommonConstant.COL_SALARY)).thenReturn(new BigDecimal("75000"));
        when(rs.getString(CommonConstant.COL_GENDER)).thenReturn("Male");

        when(jdbcTemplate.queryForObject(startsWith("SELECT COUNT(1)"), any(MapSqlParameterSource.class), eq(Long.class)))
                .thenReturn(1L);

        when(jdbcTemplate.query(anyString(), any(MapSqlParameterSource.class), any(RowMapper.class)))
                .thenAnswer(invocation -> {
                    @SuppressWarnings("unchecked")
                    RowMapper<JobDataResponse> rm = (RowMapper<JobDataResponse>) invocation.getArgument(2);
                    JobDataResponse mapped = rm.mapRow(rs, 0);
                    return List.of(mapped);
                });

        JobDataListResponse resp = strategy.query(req);

        assertNotNull(resp);
        assertEquals(1, resp.getJobDataList().size());
        JobDataResponse dto = resp.getJobDataList().get(0);
        assertEquals("Software Engineer", dto.getJobTitle());
        assertEquals(new BigDecimal("75000"), dto.getSalary());
        assertEquals("Male", dto.getGender());
    }
}