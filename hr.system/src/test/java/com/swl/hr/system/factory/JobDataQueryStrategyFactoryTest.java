package com.swl.hr.system.factory;

import com.swl.hr.system.request.job.JobDataQueryRequest;
import com.swl.hr.system.response.job.JobDataListResponse;
import com.swl.hr.system.strategy.JobDataQueryStrategy;
import com.swl.hr.system.util.CommonConstant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class JobDataQueryStrategyFactoryTest {

    private static class DummyStrategy implements JobDataQueryStrategy {
        private final String name;
        DummyStrategy(String name) { this.name = name; }
        @Override public JobDataListResponse query(JobDataQueryRequest req) { return new JobDataListResponse(); }
        @Override public String getName() { return name; }
    }

    @Nested
    class SelectionByFlag {
        @Test
        @DisplayName("Selects JPA strategy when flag is 'jpa'")
        void selectsJpaWhenFlagIsJpa() {
            JobDataQueryStrategy jpa = new DummyStrategy(CommonConstant.STRATEGY_JPA);
            JobDataQueryStrategy jdbc = new DummyStrategy(CommonConstant.STRATEGY_JDBC);
            JobDataQueryStrategyFactory factory = new JobDataQueryStrategyFactory(List.of(jpa, jdbc), "jpa");

            assertEquals(CommonConstant.STRATEGY_JPA, factory.getActiveStrategyName());
            assertTrue(factory.availableStrategyNames().containsAll(Set.of("jpa", "jdbc")));
        }

        @Test
        @DisplayName("Selects JDBC strategy when flag is 'jdbc'")
        void selectsJdbcWhenFlagIsJdbc() {
            JobDataQueryStrategy jpa = new DummyStrategy(CommonConstant.STRATEGY_JPA);
            JobDataQueryStrategy jdbc = new DummyStrategy(CommonConstant.STRATEGY_JDBC);
            JobDataQueryStrategyFactory factory = new JobDataQueryStrategyFactory(List.of(jpa, jdbc), "jdbc");

            assertEquals(CommonConstant.STRATEGY_JDBC, factory.getActiveStrategyName());
        }

        @Test
        @DisplayName("Falls back to JPA when flag is unknown")
        void fallsBackToJpaWhenFlagUnknown() {
            JobDataQueryStrategy jpa = new DummyStrategy(CommonConstant.STRATEGY_JPA);
            JobDataQueryStrategy jdbc = new DummyStrategy(CommonConstant.STRATEGY_JDBC);
            JobDataQueryStrategyFactory factory = new JobDataQueryStrategyFactory(List.of(jpa, jdbc), "unknown");

            assertEquals(CommonConstant.STRATEGY_JPA, factory.getActiveStrategyName());
        }

        @Test
        @DisplayName("Uses default JPA when flag is null or blank")
        void usesDefaultWhenFlagNullOrBlank() {
            JobDataQueryStrategy jpa = new DummyStrategy(CommonConstant.STRATEGY_JPA);
            JobDataQueryStrategy jdbc = new DummyStrategy(CommonConstant.STRATEGY_JDBC);
            JobDataQueryStrategyFactory factoryNull = new JobDataQueryStrategyFactory(List.of(jpa, jdbc), null);
            JobDataQueryStrategyFactory factoryBlank = new JobDataQueryStrategyFactory(List.of(jpa, jdbc), "  ");

            assertEquals(CommonConstant.STRATEGY_JPA, factoryNull.getActiveStrategyName());
            assertEquals(CommonConstant.STRATEGY_JPA, factoryBlank.getActiveStrategyName());
        }
    }

    @Nested
    class ErrorCases {
        @Test
        @DisplayName("Throws when no strategies are registered")
        void throwsWhenNoStrategiesRegistered() {
            JobDataQueryStrategyFactory factory = new JobDataQueryStrategyFactory(List.of(), "jpa");
            IllegalStateException ex = assertThrows(IllegalStateException.class, factory::getActiveStrategyName);
            assertTrue(ex.getMessage().toLowerCase().contains("no jobdataquerystrategy"));
        }

        @Test
        @DisplayName("Throws when fallback is unavailable")
        void throwsWhenFallbackUnavailable() {
            JobDataQueryStrategy jdbc = new DummyStrategy(CommonConstant.STRATEGY_JDBC);
            JobDataQueryStrategyFactory factory = new JobDataQueryStrategyFactory(List.of(jdbc), "unknown");
            IllegalStateException ex = assertThrows(IllegalStateException.class, factory::getActiveStrategyName);
            assertTrue(ex.getMessage().toLowerCase().contains("fallback"));
        }
    }

    @Test
    @DisplayName("query delegates to selected strategy")
    void queryDelegatesToSelectedStrategy() {
        JobDataQueryStrategy jpa = new JobDataQueryStrategy() {
            @Override
            public JobDataListResponse query(JobDataQueryRequest req) {
                JobDataListResponse r = new JobDataListResponse();
                r.setPage(1);
                return r;
            }
            @Override public String getName() { return CommonConstant.STRATEGY_JPA; }
        };
        JobDataQueryStrategyFactory factory = new JobDataQueryStrategyFactory(List.of(jpa), "jpa");
        JobDataListResponse resp = factory.query(new JobDataQueryRequest());
        assertEquals(1, resp.getPage());
    }
}