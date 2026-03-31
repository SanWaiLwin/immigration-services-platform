package com.swl.hr.system.strategy;

import com.swl.hr.system.request.job.JobDataQueryRequest;
import com.swl.hr.system.response.job.JobDataListResponse;
import com.swl.hr.system.response.job.JobDataResponse;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Component;

import java.util.*;
import org.springframework.data.domain.Sort;
import org.springframework.util.StringUtils;

import com.swl.hr.system.util.CommonConstant;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class JdbcTemplateJobDataQueryStrategy extends AbstractJobDataQueryStrategy implements JobDataQueryStrategy {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    private static final String TABLE_JOB_DATA = "job_data";
    private static final String PARAM_JOB_TITLE = "jobTitle";
    private static final String PARAM_GENDER = "gender";
    private static final String PARAM_FROM_SALARY = "fromSalary";
    private static final String PARAM_TO_SALARY = "toSalary";
    private static final String PARAM_SIZE = "size";
    private static final String PARAM_OFFSET = "offset";

    public JdbcTemplateJobDataQueryStrategy(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public JobDataListResponse query(JobDataQueryRequest req) {
        MapSqlParameterSource params = new MapSqlParameterSource();

        int page = defaultPage(req);
        int size = defaultSize(req);

        String where = buildWhereClause(req, params);
        String order = buildOrderClause(req);
        String limitOffset = buildLimitOffsetClause(page, size, params);

        String select = "SELECT " + CommonConstant.COL_JOB_TITLE + ", " + CommonConstant.COL_SALARY + ", " + CommonConstant.COL_GENDER;
        String dataSql = select + " FROM " + TABLE_JOB_DATA + where + order + limitOffset;

        List<JobDataResponse> items = jdbcTemplate.query(dataSql, params, rowMapper());
        Long total = jdbcTemplate.queryForObject("SELECT COUNT(1) FROM " + TABLE_JOB_DATA + where, params, Long.class);
        long totalElements = total != null ? total : 0L;
        int totalPages = size > 0 ? (int) Math.ceil((double) totalElements / size) : 0;

        JobDataListResponse resp = new JobDataListResponse();
        resp.setJobDataList(items);
        resp.setPage(page);
        resp.setSize(size);
        resp.setTotalElements(totalElements);
        resp.setTotalPages(totalPages);
        return resp;
    }

    private static final Map<String, String> SORT_COLUMN_MAP = Map.of(
            CommonConstant.FIELD_JOB_TITLE, CommonConstant.COL_JOB_TITLE,
            CommonConstant.FIELD_SALARY, CommonConstant.COL_SALARY,
            CommonConstant.FIELD_GENDER, CommonConstant.COL_GENDER
    );

    private List<String> resolveSortColumns(String sort) {
        List<String> keys = normalizeSortKeys(sort);
        List<String> cols = new ArrayList<>();
        for (String k : keys) {
            cols.add(SORT_COLUMN_MAP.getOrDefault(k, CommonConstant.COL_JOB_TITLE));
        }
        return cols;
    }

    private String buildWhereClause(JobDataQueryRequest req, MapSqlParameterSource params) {
        StringBuilder where = new StringBuilder(" WHERE 1=1");

        if (StringUtils.hasText(req.getJobTitle())) {
            addCiLike(params, where, CommonConstant.COL_JOB_TITLE, PARAM_JOB_TITLE, req.getJobTitle());
        }
        if (StringUtils.hasText(req.getGender())) {
            addCiEquals(params, where, CommonConstant.COL_GENDER, PARAM_GENDER, req.getGender());
        }
        if (req.getFromSalary() != null) {
            addMin(params, where, CommonConstant.COL_SALARY, PARAM_FROM_SALARY, req.getFromSalary());
        }
        if (req.getToSalary() != null) {
            addMax(params, where, CommonConstant.COL_SALARY, PARAM_TO_SALARY, req.getToSalary());
        }
        return where.toString();
    }

    private RowMapper<JobDataResponse> rowMapper() {
        return (rs, rowNum) -> {
            JobDataResponse dto = new JobDataResponse();
            dto.setJobTitle(rs.getString(CommonConstant.COL_JOB_TITLE));
            dto.setSalary(rs.getBigDecimal(CommonConstant.COL_SALARY));
            dto.setGender(rs.getString(CommonConstant.COL_GENDER));
            return dto;
        };
    }

    private String buildLimitOffsetClause(int page, int size, MapSqlParameterSource params) {
        int offset = page * size;
        params.addValue(PARAM_SIZE, size);
        params.addValue(PARAM_OFFSET, offset);
        return " LIMIT :" + PARAM_SIZE + " OFFSET :" + PARAM_OFFSET;
    }

    private void addCiLike(MapSqlParameterSource params, StringBuilder where, String column, String paramName, String value) {
        where.append(" AND LOWER(").append(column).append(") LIKE LOWER(:").append(paramName).append(")");
        params.addValue(paramName, "%" + value + "%");
    }

    private void addCiEquals(MapSqlParameterSource params, StringBuilder where, String column, String paramName, String value) {
        where.append(" AND LOWER(").append(column).append(") = LOWER(:").append(paramName).append(")");
        params.addValue(paramName, value);
    }

    private void addMin(MapSqlParameterSource params, StringBuilder where, String column, String paramName, Object value) {
        where.append(" AND ").append(column).append(" >= :").append(paramName);
        params.addValue(paramName, value);
    }

    private void addMax(MapSqlParameterSource params, StringBuilder where, String column, String paramName, Object value) {
        where.append(" AND ").append(column).append(" <= :").append(paramName);
        params.addValue(paramName, value);
    }

    private String buildOrderClause(JobDataQueryRequest req) {
        List<String> sortColumns = resolveSortColumns(req.getSort());
        String sortDir = (req.getSortType() == Sort.Direction.DESC) ? "DESC" : "ASC";
        String joined = String.join(", ", sortColumns.stream().map(c -> c + " " + sortDir).toList());
        return " ORDER BY " + joined;
    }

    @Override
    public String getName() {
        return CommonConstant.STRATEGY_JDBC;
    }
}