package com.swl.hr.system.controller;

import com.swl.hr.system.request.ApiRequest;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.swl.hr.system.response.ApiResponse;
import com.swl.hr.system.service.JobDataService;
import com.swl.hr.system.request.job.JobDataQueryRequest;
import com.swl.hr.system.response.job.JobDataListResponse;
import com.swl.hr.system.util.CommonConstant;

import jakarta.validation.Valid;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.extern.slf4j.Slf4j;

@RestController
@Validated
@RequestMapping("/api")
@Slf4j
public class JobDataController {

    private final JobDataService jobDataService;

    public JobDataController(JobDataService jobDataService) {
        this.jobDataService = jobDataService;
    }

    @Operation(
        summary = "Get job data",
        description = "Query job data with filters and pagination. The default example is pre-populated for Swagger.",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiRequest.class),
                examples = {
                    @ExampleObject(
                        name = "Default",
                        value = "{\n  \"data\": {\n    \"jobTitle\": \"Software Engineer\",\n    \"gender\": \"Male\",\n    \"fromSalary\": 60000,\n    \"toSalary\": 120000,\n    \"sort\": \"jobTitle\",\n    \"sortType\": \"ASC\",\n    \"page\": 0,\n    \"size\": 10\n  }\n}"
                    )
                }
            )
        )
    )
    @PostMapping("/job_data")
    public ApiResponse<JobDataListResponse> getJobData(@Valid @RequestBody ApiRequest<JobDataQueryRequest> req) {
        JobDataQueryRequest request = req.getData();
        log.info("Job data query received: jobTitle='{}', gender='{}', fromSalary={}, toSalary={}, sort='{}', sortType='{}', page={}, size={}",
                request.getJobTitle(), request.getGender(), request.getFromSalary(), request.getToSalary(),
                request.getSort(), request.getSortType(), request.getPage(), request.getSize());
        JobDataListResponse resp = jobDataService.queryJobData(request);
        log.info("Job data query result: totalElements={}, totalPages={}, page={}, size={}",
                resp.getTotalElements(), resp.getTotalPages(), resp.getPage(), resp.getSize());
        return new ApiResponse<>(CommonConstant.MSG_PREFIX_SUCCESS, "Job data list", resp);
    }

}
