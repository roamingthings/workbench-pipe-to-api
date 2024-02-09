package de.roamingthings.pipetoapi;

import org.junit.jupiter.api.Test;
import software.amazon.awscdk.App;
import software.amazon.awscdk.assertions.Template;

import java.io.File;
import java.util.Map;

class PipeToApiStackTest {

    @Test
    void testAppStack() {
        if (new File(PipeToApiStack.functionPath()).exists()) {
            PipeToApiStack stack = new PipeToApiStack(new App(), "TestMicronautAppStack", PipeToApiStack.PipeToApiStackProps.builder()
                    .endpointUrl("https://example.com")
                    .build());
            Template template = Template.fromStack(stack);
            template.hasResourceProperties("AWS::Lambda::Function", Map.of(
                    "Handler", "de.roamingthings.workbench.pipetoapi.RequestEnrichmentHandler"
            ));
        }
    }
}
