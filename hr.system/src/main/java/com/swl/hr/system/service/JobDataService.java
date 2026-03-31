package com.swl.hr.system.service;

import com.swl.hr.system.request.job.JobDataQueryRequest;
import com.swl.hr.system.response.job.JobDataListResponse;

public interface JobDataService {
    JobDataListResponse queryJobData(JobDataQueryRequest req);
}