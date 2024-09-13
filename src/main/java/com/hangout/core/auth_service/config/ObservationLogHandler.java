package com.hangout.core.auth_service.config;

import org.springframework.stereotype.Component;

import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationHandler;
import lombok.extern.slf4j.Slf4j;

// Example of plugging in a custom handler that in this case will print a statement before and after all observations take place
@Component
@Slf4j
public class ObservationLogHandler implements ObservationHandler<Observation.Context> {

    @Override
    public void onStart(Observation.Context context) {
        log.debug("Before running the observation for context [{}]", context.getName());
    }

    @Override
    public void onStop(Observation.Context context) {
        log.debug("After running the observation for context [{}]", context.getName());
    }

    @Override
    public boolean supportsContext(Observation.Context context) {
        return true;
    }
}
