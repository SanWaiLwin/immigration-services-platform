package com.swl.hr.system.strategy;

import com.swl.hr.system.request.job.JobDataQueryRequest;
import com.swl.hr.system.response.job.JobDataListResponse;

public interface JobDataQueryStrategy {
    JobDataListResponse query(JobDataQueryRequest req);
    String getName();
}