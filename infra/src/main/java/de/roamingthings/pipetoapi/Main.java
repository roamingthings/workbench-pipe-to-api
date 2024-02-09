package de.roamingthings.pipetoapi;

import software.amazon.awscdk.App;

public class Main {
    public static void main(final String[] args) {
        App app = new App();
        new PipeToApiStack(app, "Workbench-PipeToApi-Stack", PipeToApiStack.PipeToApiStackProps.builder()
                .endpointUrl(endointUrl(app))
                .build());
        app.synth();
    }

    protected static String endointUrl(App app) {
        Object endpointUrl = app.getNode().tryGetContext("endpointUrl");
        if (!(endpointUrl instanceof String)) {
            System.err.println("""
                    You need to specify an endpoint URL context variable when deploying this stack.
                    For example if your endpoint is https://api.example.com, you would run:
                    cdk deploy -c endpointUrl=https://api.example.com
                    """.stripIndent());
            System.exit(1);
        }
        return (String) endpointUrl;
    }
}