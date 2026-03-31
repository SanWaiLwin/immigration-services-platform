package com.swl.hr.system.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swl.hr.system.request.ApiRequest;
import com.swl.hr.system.request.job.JobDataQueryRequest;
import com.swl.hr.system.response.ApiResponse;
import com.swl.hr.system.response.job.JobDataListResponse;
import com.swl.hr.system.service.JobDataService;
import com.swl.hr.system.util.CommonConstant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@WebMvcTest(controllers = JobDataController.class)
class JobDataControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JobDataService jobDataService;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String ENDPOINT = "/api/job_data";
    private static final String SUCCESS = CommonConstant.MSG_PREFIX_SUCCESS;
    private static final String FAILED = CommonConstant.MSG_PREFIX_FAILED;

    private String json(Object o) throws Exception {
        return objectMapper.writeValueAsString(o);
    }

    @Test
    @DisplayName("POST /api/job_data returns success with JobDataListResponse")
    void getJobData_success() throws Exception {
        // Prepare request
        JobDataQueryRequest query = new JobDataQueryRequest();
        query.setJobTitle("Software Engineer");
        query.setGender("Male");
        query.setFromSalary(new BigDecimal("60000"));
        query.setToSalary(new BigDecimal("120000"));
        query.setSort("jobTitle,salary");
        query.setSortType(Sort.Direction.ASC);
        query.setPage(0);
        query.setSize(10);

        ApiRequest<JobDataQueryRequest> apiReq = new ApiRequest<>(query);

        // Stub service response
        JobDataListResponse listResponse = new JobDataListResponse();
        listResponse.setJobDataList(Collections.emptyList());
        listResponse.setPage(0);
        listResponse.setSize(10);
        listResponse.setTotalElements(0);
        listResponse.setTotalPages(0);
        when(jobDataService.queryJobData(any(JobDataQueryRequest.class))).thenReturn(listResponse);

        // Execute
        mockMvc.perform(
                MockMvcRequestBuilders.post(ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(apiReq))
        )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(SUCCESS))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Job data list"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.page").value(0))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.size").value(10))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.totalElements").value(0))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.totalPages").value(0));
    }

    @Test
    @DisplayName("POST /api/job_data returns 400 when request body data is null")
    void getJobData_validationError_nullData() throws Exception {
        ApiRequest<JobDataQueryRequest> apiReq = new ApiRequest<>(null);

        mockMvc.perform(
                MockMvcRequestBuilders.post(ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(apiReq))
        )
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(FAILED))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("POST /api/job_data returns 400 when salary range invalid")
    void getJobData_validationError_invalidSalaryRange() throws Exception {
        JobDataQueryRequest query = new JobDataQueryRequest();
        query.setFromSalary(new BigDecimal("200000"));
        query.setToSalary(new BigDecimal("100000"));
        query.setPage(0);
        query.setSize(10);

        ApiRequest<JobDataQueryRequest> apiReq = new ApiRequest<>(query);

        mockMvc.perform(
                MockMvcRequestBuilders.post(ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(apiReq))
        )
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(FAILED))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value(org.hamcrest.Matchers.containsString("fromSalary must be <= toSalary")));
    }
}