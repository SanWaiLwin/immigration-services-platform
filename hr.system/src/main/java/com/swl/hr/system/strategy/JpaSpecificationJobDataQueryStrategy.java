package com.swl.hr.system.strategy;

import com.swl.hr.system.entity.JobData;
import com.swl.hr.system.repository.JobDataRepository;
import com.swl.hr.system.request.job.JobDataQueryRequest;
import com.swl.hr.system.response.job.JobDataListResponse;
import com.swl.hr.system.response.job.JobDataResponse;
import com.swl.hr.system.specification.CommonSpecifications;
import com.swl.hr.system.util.CommonUtil;
import com.swl.hr.system.util.CommonConstant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class JpaSpecificationJobDataQueryStrategy extends AbstractJobDataQueryStrategy implements JobDataQueryStrategy {

    private final JobDataRepository jobDataRepository;

    public JpaSpecificationJobDataQueryStrategy(JobDataRepository jobDataRepository) {
        this.jobDataRepository = jobDataRepository;
    }

    @Override
    public JobDataListResponse query(JobDataQueryRequest req) {
        Specification<JobData> s1 = CommonSpecifications.containsIgnoreCase(CommonConstant.FIELD_JOB_TITLE, req.getJobTitle());
        Specification<JobData> s2 = CommonSpecifications.equalsIgnoreCase(CommonConstant.FIELD_GENDER, req.getGender());
        Specification<JobData> s3 = CommonSpecifications.between(CommonConstant.FIELD_SALARY, req.getFromSalary(), req.getToSalary());

        Specification<JobData> spec = Specification.where(null);
        if (s1 != null) spec = spec.and(s1);
        if (s2 != null) spec = spec.and(s2);
        if (s3 != null) spec = spec.and(s3);

        List<String> sortFields = normalizeSortKeys(req.getSort());
        Sort.Direction dir = (req.getSortType() != null) ? req.getSortType() : Sort.Direction.ASC;
        Pageable pageable = CommonUtil.buildPageable(defaultPage(req), defaultSize(req), sortFields, dir, CommonConstant.FIELD_JOB_TITLE);

        log.debug("Executing JPA query with spec and pageable: sort='{}', normalized='{}', sortType='{}', page={}, size={}",
                req.getSort(), sortFields, dir, defaultPage(req), defaultSize(req));
        Page<JobData> pageData = jobDataRepository.findAll(spec, pageable);

        List<JobDataResponse> items = pageData.getContent().stream()
                .map(this::toJobData)
                .collect(Collectors.toList());

        JobDataListResponse resp = new JobDataListResponse();
        resp.setJobDataList(items);
        resp.setPage(req.getPage());
        resp.setSize(req.getSize());
        resp.setTotalElements(pageData.getTotalElements());
        resp.setTotalPages(pageData.getTotalPages());
        return resp;
    }

    @Override
    public String getName() {
        return CommonConstant.STRATEGY_JPA;
    }

    private JobDataResponse toJobData(JobData entity) {
        JobDataResponse dto = new JobDataResponse();
        dto.setJobTitle(entity.getJobTitle());
        dto.setSalary(entity.getSalary());
        dto.setGender(entity.getGender());
        return dto;
    }
}