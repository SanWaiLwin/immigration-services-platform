package com.swl.hr.system.factory;

import com.swl.hr.system.request.job.JobDataQueryRequest;
import com.swl.hr.system.response.job.JobDataListResponse;
import com.swl.hr.system.strategy.JobDataQueryStrategy;
import com.swl.hr.system.util.CommonConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@Slf4j
public class JobDataQueryStrategyFactory {

    private final Map<String, JobDataQueryStrategy> strategiesByName;
    private final String strategyFlag;
    private static final String DEFAULT_STRATEGY = CommonConstant.STRATEGY_JPA;

    public JobDataQueryStrategyFactory(List<JobDataQueryStrategy> strategies,
                                       @Value("${app.jobdata.query.strategy:jpa}") String strategyFlag) {
        // Map strategy names to instances, normalize keys to lowercase defensively
        this.strategiesByName = strategies.stream()
                .collect(Collectors.toMap(s -> s.getName().toLowerCase(Locale.ROOT), Function.identity()));
        this.strategyFlag = strategyFlag == null ? DEFAULT_STRATEGY : strategyFlag.toLowerCase(Locale.ROOT);
    }

    public JobDataListResponse query(JobDataQueryRequest req) {
        JobDataQueryStrategy strategy = selectStrategy(strategyFlag);
        log.debug("Using '{}' strategy to query job data", strategy.getName());
        return strategy.query(req);
    }

    /**
     * Returns the name of the currently active strategy based on configuration flag and availability.
     */
    public String getActiveStrategyName() {
        return selectStrategy(strategyFlag).getName();
    }

    /**
     * Returns the available strategy names registered in the factory.
     */
    public Set<String> availableStrategyNames() {
        return strategiesByName.keySet();
    }

    /**
     * Resolves which strategy to use given a flag. Falls back to DEFAULT_STRATEGY if the flag is unknown.
     * Throws IllegalStateException when no strategies are registered or the fallback is unavailable.
     */
    private JobDataQueryStrategy selectStrategy(String flag) {
        if (strategiesByName.isEmpty()) {
            throw new IllegalStateException("No JobDataQueryStrategy implementations are registered");
        }
        String key = (flag == null || flag.isBlank()) ? DEFAULT_STRATEGY : flag.toLowerCase(Locale.ROOT);
        JobDataQueryStrategy strategy = strategiesByName.get(key);
        if (strategy != null) {
            return strategy;
        }
        log.warn("Unknown strategy flag '{}', falling back to '{}'", flag, DEFAULT_STRATEGY);
        JobDataQueryStrategy fallback = strategiesByName.get(DEFAULT_STRATEGY);
        if (fallback == null) {
            throw new IllegalStateException("Fallback strategy '" + DEFAULT_STRATEGY + "' not available");
        }
        return fallback;
    }
}