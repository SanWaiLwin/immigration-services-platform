package com.swl.hr.system.strategy;

import com.swl.hr.system.entity.JobData;
import com.swl.hr.system.repository.JobDataRepository;
import com.swl.hr.system.request.job.JobDataQueryRequest;
import com.swl.hr.system.response.job.JobDataListResponse;
import com.swl.hr.system.response.job.JobDataResponse;
import com.swl.hr.system.util.CommonConstant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JpaSpecificationJobDataQueryStrategyTest {

    @Mock
    private JobDataRepository jobDataRepository;

    @InjectMocks
    private JpaSpecificationJobDataQueryStrategy strategy;

    @Test
    @DisplayName("query builds Specification and Pageable, and maps Page to response")
    void query_buildsSpecAndPageable_andMapsResults() {
        JobDataQueryRequest req = new JobDataQueryRequest();
        req.setJobTitle("Dev");
        req.setGender("Male");
        req.setFromSalary(new BigDecimal("1000"));
        req.setToSalary(new BigDecimal("2000"));
        req.setSort("job_title,salary");
        req.setSortType(Sort.Direction.DESC);
        req.setPage(1);
        req.setSize(2);

        JobData e1 = new JobData();
        e1.setJobTitle("Developer");
        e1.setSalary(new BigDecimal("1500"));
        e1.setGender("Male");
        JobData e2 = new JobData();
        e2.setJobTitle("Senior Developer");
        e2.setSalary(new BigDecimal("1800"));
        e2.setGender("Male");
        List<JobData> entities = List.of(e1, e2);

        when(jobDataRepository.findAll(any(Specification.class), any(Pageable.class))).thenAnswer(inv -> {
            Pageable pageable = inv.getArgument(1);
            return new PageImpl<>(entities, pageable, 5);
        });

        JobDataListResponse resp = strategy.query(req);

        assertNotNull(resp);
        assertEquals(1, resp.getPage());
        assertEquals(2, resp.getSize());
        assertEquals(5L, resp.getTotalElements());
        assertEquals(3, resp.getTotalPages());
        assertNotNull(resp.getJobDataList());
        assertEquals(2, resp.getJobDataList().size());
        JobDataResponse first = resp.getJobDataList().get(0);
        assertEquals("Developer", first.getJobTitle());
        assertEquals(new BigDecimal("1500"), first.getSalary());
        assertEquals("Male", first.getGender());

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(jobDataRepository, times(1)).findAll(any(Specification.class), pageableCaptor.capture());
        Pageable captured = pageableCaptor.getValue();
        assertEquals(1, captured.getPageNumber());
        assertEquals(2, captured.getPageSize());
        Sort sort = captured.getSort();
        assertNotNull(sort.getOrderFor("jobTitle"));
        assertEquals(Sort.Direction.DESC, sort.getOrderFor("jobTitle").getDirection());
        assertNotNull(sort.getOrderFor("salary"));
        assertEquals(Sort.Direction.DESC, sort.getOrderFor("salary").getDirection());
    }

    @Test
    @DisplayName("query with no filters uses default sort by jobTitle ASC")
    void query_noFilters_defaultSort() {
        JobDataQueryRequest req = new JobDataQueryRequest();
        req.setPage(0);
        req.setSize(3);
        // no sort specified -> default

        when(jobDataRepository.findAll(any(Specification.class), any(Pageable.class))).thenAnswer(inv -> {
            Pageable pageable = inv.getArgument(1);
            return new PageImpl<>(List.of(), pageable, 0);
        });

        JobDataListResponse resp = strategy.query(req);
        assertNotNull(resp);
        assertEquals(0, resp.getPage());
        assertEquals(3, resp.getSize());
        assertEquals(0L, resp.getTotalElements());
        assertEquals(0, resp.getTotalPages());

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(jobDataRepository, times(1)).findAll(any(Specification.class), pageableCaptor.capture());
        Pageable captured = pageableCaptor.getValue();
        Sort sort = captured.getSort();
        assertNotNull(sort.getOrderFor("jobTitle"));
        assertEquals(Sort.Direction.ASC, sort.getOrderFor("jobTitle").getDirection());
    }

    @Test
    @DisplayName("getName returns 'jpa'")
    void getName_returnsJpa() {
        assertEquals(CommonConstant.STRATEGY_JPA, strategy.getName());
    }
}