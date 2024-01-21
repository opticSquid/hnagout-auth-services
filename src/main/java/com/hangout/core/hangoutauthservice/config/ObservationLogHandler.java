package com.hangout.core.hangoutauthservice.config;

import io.micrometer.common.KeyValue;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.stream.StreamSupport;

// Example of plugging in a custom handler that in this case will print a statement before and after all observations take place
@Component
public class ObservationLogHandler implements ObservationHandler<Observation.Context> {
    private static final Logger log = LoggerFactory.getLogger(ObservationLogHandler.class);

    @Override
    public void onStart(Observation.Context context) {
        log.debug("Before running the observation for context [{}], userType [{}]", context.getName(),
                getUserTypeFromContext(context));
    }

    @Override
    public void onStop(Observation.Context context) {
        log.debug("After running the observation for context [{}], userType [{}]", context.getName(),
                getUserTypeFromContext(context));
    }

    @Override
    public boolean supportsContext(Observation.Context context) {
        return true;
    }

    private String getUserTypeFromContext(Observation.Context context) {
        return StreamSupport.stream(context.getLowCardinalityKeyValues().spliterator(), false)
                .filter(keyValue -> "userType".equals(keyValue.getKey()))
                .map(KeyValue::getValue)
                .findFirst()
                .orElse("UNKNOWN");
    }

}
