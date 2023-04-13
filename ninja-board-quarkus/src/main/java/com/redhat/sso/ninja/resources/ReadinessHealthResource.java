package com.redhat.sso.ninja.resources;

import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Readiness;

/**
 * ReadnessHealthResource
 */
@Readiness
public class ReadinessHealthResource implements HealthCheck{

    @Override
    public HealthCheckResponse call() {
        return HealthCheckResponse.up("Readiness");
    }

    
}