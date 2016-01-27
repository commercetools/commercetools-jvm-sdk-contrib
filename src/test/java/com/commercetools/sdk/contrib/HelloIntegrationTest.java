package com.commercetools.sdk.contrib;

import io.sphere.sdk.projects.Project;
import io.sphere.sdk.projects.queries.ProjectGet;
import org.junit.ClassRule;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class HelloIntegrationTest {
    @ClassRule
    public static SphereClientRule client = new SphereClientRule();

    @Test
    public void projectContainsEurCurrency() {
        final Project project = client.executeBlocking(ProjectGet.of());
        assertThat(project.getCurrencies()).contains("EUR");
    }
}
