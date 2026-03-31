package com.swl.hr.system.service.impl;

import com.swl.hr.system.service.JobDataService;
import org.springframework.stereotype.Service;

import com.swl.hr.system.repository.JobDataRepository;
import com.swl.hr.system.request.job.JobDataQueryRequest;
import com.swl.hr.system.response.job.JobDataListResponse;
import com.swl.hr.system.factory.JobDataQueryStrategyFactory;

@Service
public class JobDataServiceImpl implements JobDataService {

    private final JobDataRepository jobDataRepository;
    private final JobDataQueryStrategyFactory strategyContext;

    public JobDataServiceImpl(JobDataRepository jobDataRepository, JobDataQueryStrategyFactory strategyContext) {
        this.jobDataRepository = jobDataRepository;
        this.strategyContext = strategyContext;
    }

    @Override
    public JobDataListResponse queryJobData(JobDataQueryRequest req) {
        return strategyContext.query(req);
    }

}