package com.swl.hr.system.service.impl;

import com.swl.hr.system.factory.JobDataQueryStrategyFactory;
import com.swl.hr.system.repository.JobDataRepository;
import com.swl.hr.system.request.job.JobDataQueryRequest;
import com.swl.hr.system.response.job.JobDataListResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JobDataServiceImplTest {

    @Mock
    private JobDataRepository jobDataRepository;

    @Mock
    private JobDataQueryStrategyFactory strategyContext;

    @InjectMocks
    private JobDataServiceImpl service;

    @Test
    @DisplayName("queryJobData delegates to strategy and returns its response")
    void queryJobData_delegatesToStrategyAndReturnsResponse() {
        JobDataQueryRequest req = new JobDataQueryRequest();
        req.setPage(1);
        req.setSize(5);

        JobDataListResponse expected = new JobDataListResponse();
        expected.setPage(1);
        expected.setSize(5);
        expected.setTotalElements(0);
        expected.setTotalPages(0);

        when(strategyContext.query(req)).thenReturn(expected);

        JobDataListResponse actual = service.queryJobData(req);

        assertSame(expected, actual, "Service should return response from strategy");
        verify(strategyContext, times(1)).query(req);
        verifyNoInteractions(jobDataRepository);
    }

    @Test
    @DisplayName("queryJobData forwards null request to strategy")
    void queryJobData_withNullRequest_forwardsToStrategy() {
        JobDataListResponse expected = new JobDataListResponse();
        when(strategyContext.query(null)).thenReturn(expected);

        JobDataListResponse actual = service.queryJobData(null);

        assertSame(expected, actual, "Service should forward null request to strategy");
        verify(strategyContext, times(1)).query(null);
        verifyNoInteractions(jobDataRepository);
    }
}