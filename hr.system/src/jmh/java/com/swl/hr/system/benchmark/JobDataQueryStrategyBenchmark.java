package com.swl.hr.system.benchmark;

import com.swl.hr.system.request.job.JobDataQueryRequest;
import com.swl.hr.system.response.job.JobDataListResponse;
import com.swl.hr.system.strategy.JdbcTemplateJobDataQueryStrategy;
import com.swl.hr.system.strategy.JpaSpecificationJobDataQueryStrategy;
import com.swl.hr.system.util.CommonConstant;
import com.swl.hr.system.repository.JobDataRepository;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.data.domain.Sort;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Statement;
import java.util.concurrent.TimeUnit;

import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactory;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityManager;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 3)
@Measurement(iterations = 5)
@Fork(value = 1)
@State(Scope.Benchmark)
public class JobDataQueryStrategyBenchmark {

    private DataSource dataSource;
    private NamedParameterJdbcTemplate jdbcTemplate;
    private JdbcTemplateJobDataQueryStrategy strategy;
    private EntityManagerFactory emf;
    private EntityManager entityManager;
    private JpaSpecificationJobDataQueryStrategy jpaStrategy;

    @Setup(Level.Trial)
    public void setup() throws Exception {
        DriverManagerDataSource ds = new DriverManagerDataSource();
        ds.setDriverClassName("org.h2.Driver");
        ds.setUrl("jdbc:h2:mem:jobs;DB_CLOSE_DELAY=-1;MODE=MySQL");
        ds.setUsername("sa");
        ds.setPassword("");
        this.dataSource = ds;
        this.jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
        this.strategy = new JdbcTemplateJobDataQueryStrategy(jdbcTemplate);

        try (Connection conn = dataSource.getConnection(); Statement st = conn.createStatement()) {
            st.execute("CREATE TABLE IF NOT EXISTS job_data (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "timestamp VARCHAR(255), " +
                    "employer VARCHAR(255), " +
                    "location VARCHAR(255), " +
                    "job_title VARCHAR(255), " +
                    "years_at_employer DECIMAL(10,2), " +
                    "years_of_experience DECIMAL(10,2), " +
                    "salary DECIMAL(20,2), " +
                    "signing_bonus DECIMAL(15,2), " +
                    "annual_bonus DECIMAL(15,2), " +
                    "annual_stock_value_bonus VARCHAR(255), " +
                    "gender VARCHAR(255), " +
                    "additional_comments TEXT, " +
                    "created_date TIMESTAMP, " +
                    "updated_date TIMESTAMP" +
                    ")");
            st.execute("CREATE INDEX IF NOT EXISTS idx_job_title ON job_data(job_title)");
            st.execute("CREATE INDEX IF NOT EXISTS idx_gender ON job_data(gender)");
            st.execute("CREATE INDEX IF NOT EXISTS idx_salary ON job_data(salary)");
            // Populate with sample data
            st.execute("INSERT INTO job_data(job_title, salary, gender) SELECT 'Engineer', 80000 + (x * 100), CASE WHEN (x % 2) = 0 THEN 'Male' ELSE 'Female' END FROM SYSTEM_RANGE(1, 2000) x");
            st.execute("INSERT INTO job_data(job_title, salary, gender) SELECT 'Manager', 90000 + (x * 120), CASE WHEN (x % 2) = 0 THEN 'Female' ELSE 'Male' END FROM SYSTEM_RANGE(1, 2000) x");
        }
        // Set up JPA EntityManagerFactory and Repository after data is prepared
        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        vendorAdapter.setShowSql(false);
        vendorAdapter.setGenerateDdl(false);
        LocalContainerEntityManagerFactoryBean lcemfb = new LocalContainerEntityManagerFactoryBean();
        lcemfb.setDataSource(ds);
        lcemfb.setPackagesToScan("com.swl.hr.system.entity");
        lcemfb.setJpaVendorAdapter(vendorAdapter);
        java.util.Properties jpaProps = new java.util.Properties();
        jpaProps.put("hibernate.dialect", "org.hibernate.dialect.H2Dialect");
        jpaProps.put("hibernate.hbm2ddl.auto", "none");
        jpaProps.put("hibernate.id.new_generator_mappings", "true");
        lcemfb.setJpaProperties(jpaProps);
        lcemfb.afterPropertiesSet();
        this.emf = lcemfb.getObject();
        this.entityManager = emf.createEntityManager();
        JpaRepositoryFactory repoFactory = new JpaRepositoryFactory(entityManager);
        JobDataRepository repo = repoFactory.getRepository(JobDataRepository.class);
        this.jpaStrategy = new JpaSpecificationJobDataQueryStrategy(repo);
    }

    @TearDown(Level.Trial)
    public void tearDown() throws Exception {
        try (Connection conn = dataSource.getConnection(); Statement st = conn.createStatement()) {
            st.execute("DROP ALL OBJECTS");
        }
        if (entityManager != null) {
            entityManager.close();
        }
        if (emf != null) {
            emf.close();
        }
    }

    @Benchmark
    public void jdbc_query_by_title_sorted(JobDataQueryRequestHolder holder, Blackhole bh) {
        JobDataQueryRequest req = new JobDataQueryRequest();
        req.setJobTitle("Engineer");
        req.setSort(CommonConstant.FIELD_JOB_TITLE);
        req.setSortType(Sort.Direction.ASC);
        req.setPage(0);
        req.setSize(50);
        JobDataListResponse resp = strategy.query(req);
        bh.consume(resp);
    }

    // JPA benchmarks
    @Benchmark
    public void jpa_query_by_title_sorted(JobDataQueryRequestHolder holder, Blackhole bh) {
        JobDataQueryRequest req = new JobDataQueryRequest();
        req.setJobTitle("Engineer");
        req.setSort(CommonConstant.FIELD_JOB_TITLE);
        req.setSortType(Sort.Direction.ASC);
        req.setPage(0);
        req.setSize(50);
        JobDataListResponse resp = jpaStrategy.query(req);
        bh.consume(resp);
    }

    @Benchmark
    public void jdbc_query_salary_range_desc(JobDataQueryRequestHolder holder, Blackhole bh) {
        JobDataQueryRequest req = new JobDataQueryRequest();
        req.setFromSalary(new BigDecimal("85000"));
        req.setToSalary(new BigDecimal("100000"));
        req.setSort(CommonConstant.FIELD_SALARY);
        req.setSortType(Sort.Direction.DESC);
        req.setPage(0);
        req.setSize(50);
        JobDataListResponse resp = strategy.query(req);
        bh.consume(resp);
    }

    @Benchmark
    public void jpa_query_salary_range_desc(JobDataQueryRequestHolder holder, Blackhole bh) {
        JobDataQueryRequest req = new JobDataQueryRequest();
        req.setFromSalary(new BigDecimal("85000"));
        req.setToSalary(new BigDecimal("100000"));
        req.setSort(CommonConstant.FIELD_SALARY);
        req.setSortType(Sort.Direction.DESC);
        req.setPage(0);
        req.setSize(50);
        JobDataListResponse resp = jpaStrategy.query(req);
        bh.consume(resp);
    }

    @State(Scope.Thread)
    public static class JobDataQueryRequestHolder {
        // Reserved for per-thread setup if needed later
    }
}