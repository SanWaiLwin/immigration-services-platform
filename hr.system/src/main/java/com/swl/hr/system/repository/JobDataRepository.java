package com.swl.hr.system.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.swl.hr.system.entity.JobData;

public interface JobDataRepository extends JpaRepository<JobData, Long>, JpaSpecificationExecutor<JobData> {
}