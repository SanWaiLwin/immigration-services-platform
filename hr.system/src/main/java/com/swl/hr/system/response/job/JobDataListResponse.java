package com.swl.hr.system.response.job;

import java.util.List;

import lombok.Data;

@Data
public class JobDataListResponse {
    private List<JobDataResponse> jobDataList;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
}