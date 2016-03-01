package com.commercetools.sdk.contrib;

import io.sphere.sdk.client.*;
import org.junit.rules.ExternalResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.Duration;
import java.util.Properties;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;

public final class SphereClientRule extends ExternalResource implements BlockingSphereClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(SphereClientRule.class);
    private BlockingSphereClient client;

    @Override
    public void close() {
        LOGGER.warn("it is not recommended to close the client directly in " + getClass().getName());
        client.close();
    }

    @Override
    public <T> CompletionStage<T> execute(final SphereRequest<T> sphereRequest) {
        return client.execute(sphereRequest);
    }

    @Override
    public <T> T executeBlocking(final SphereRequest<T> sphereRequest) {
        return client.executeBlocking(sphereRequest);
    }

    @Override
    public <T> T executeBlocking(final SphereRequest<T> sphereRequest, final long l, final TimeUnit timeUnit) {
        return client.executeBlocking(sphereRequest, l, timeUnit);
    }

    @Override
    public <T> T executeBlocking(final SphereRequest<T> sphereRequest, final Duration duration) {
        return client.executeBlocking(sphereRequest, duration);
    }

    @Override
    protected void after() {
        client.close();
    }

    @Override
    protected void before() throws Throwable {
        if (System.getenv("JVM_SDK_CONTRIB_IT_PROJECT_KEY") != null) {
            initializeClientFromEnv();
        } else {
            initializeClientFromProperties();
        }
    }

    private void initializeClientFromEnv() {
        final SphereClientConfig config = SphereClientConfig.ofEnvironmentVariables("JVM_SDK_CONTRIB_IT");
        initializeClient(config);
    }

    private void initializeClientFromProperties() throws IOException {
        final File file = findFile(new File("integrationtest.properties").getAbsoluteFile(), 5);
        try (final FileInputStream fileInputStream = new FileInputStream(file)) {
            final Properties properties = new Properties();
            properties.load(fileInputStream);
            final SphereClientConfig config = SphereClientConfig.ofProperties(properties, "");
            initializeClient(config);
        }
    }

    private void initializeClient(final SphereClientConfig config) {
        final SphereClient underlying = SphereClientFactory.of().createClient(config);
        client = BlockingSphereClient.of(underlying, 20, TimeUnit.SECONDS);
    }

    private static File findFile(final File initial, final int ttl) {
        if (ttl <= 0 || initial.exists()) {
            return initial;
        } else {
            final String name = initial.getName();
            final File parentFile = initial.getParentFile().getParentFile();
            final File newInitial = new File(parentFile, name).getAbsoluteFile();
            return findFile(newInitial, ttl - 1);
        }
    }
}
