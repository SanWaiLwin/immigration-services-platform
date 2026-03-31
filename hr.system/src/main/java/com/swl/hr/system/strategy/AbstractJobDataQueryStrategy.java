package com.swl.hr.system.strategy;

import com.swl.hr.system.request.job.JobDataQueryRequest;
import com.swl.hr.system.util.CommonConstant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;

import java.util.Locale;
import java.util.Map;
import java.util.List;
import java.util.Arrays;
import java.util.stream.Collectors;

public abstract class AbstractJobDataQueryStrategy {

    @Value("${app.jobdata.page.default:0}")
    protected int defaultPageConfig;

    @Value("${app.jobdata.size.default:10}")
    protected int defaultSizeConfig;

    protected int defaultPage(JobDataQueryRequest req) {
        return req.getPage() != null ? req.getPage() : defaultPageConfig;
    }

    protected int defaultSize(JobDataQueryRequest req) {
        return req.getSize() != null ? req.getSize() : defaultSizeConfig;
    }

    private static final Map<String, String> SORT_ALIAS_MAP = Map.of(
        "jobtitle", CommonConstant.FIELD_JOB_TITLE,
        "job_title", CommonConstant.FIELD_JOB_TITLE,
        "salary", CommonConstant.FIELD_SALARY,
        "gender", CommonConstant.FIELD_GENDER
    );

    protected List<String> normalizeSortKeys(String sort) {
        if (!StringUtils.hasText(sort)) {
            return List.of(CommonConstant.FIELD_JOB_TITLE);
        }
        return Arrays.stream(sort.split(","))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .map(s -> s.toLowerCase(Locale.ROOT))
                .map(s -> SORT_ALIAS_MAP.getOrDefault(s, CommonConstant.FIELD_JOB_TITLE))
                .distinct()
                .collect(Collectors.toList());
    }
}