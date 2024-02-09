package de.roamingthings.pipetoapi;

import lombok.Builder;
import software.amazon.awscdk.RemovalPolicy;
import software.amazon.awscdk.services.events.IApiDestination;
import software.amazon.awscdk.services.iam.Effect;
import software.amazon.awscdk.services.iam.PolicyDocument;
import software.amazon.awscdk.services.iam.PolicyStatement;
import software.amazon.awscdk.services.iam.Role;
import software.amazon.awscdk.services.iam.ServicePrincipal;
import software.amazon.awscdk.services.lambda.IFunction;
import software.amazon.awscdk.services.logs.ILogGroup;
import software.amazon.awscdk.services.logs.LogGroup;
import software.amazon.awscdk.services.logs.RetentionDays;
import software.amazon.awscdk.services.pipes.CfnPipe;
import software.amazon.awscdk.services.sqs.IQueue;
import software.constructs.Construct;

import java.util.List;
import java.util.Map;

public class ApiDestinationPipe extends Construct {

    private static final Number DEFAULT_BATCH_SIZE = 1;
    private static final Number DEFAULT_BATCHING_WINDOW_SECONDS = 6;

    public ApiDestinationPipe(Construct scope, String id, ApiDestinationPipeProps props) {
        super(scope, id);

        var logGroup = createLogGroup(getNode().getId());
        createPipe(props.sourceQueue, props.enrichmentFunction, props.apiDestinationTarget, logGroup);
    }

    private ILogGroup createLogGroup(String name) {
        return LogGroup.Builder.create(this, "LogGroup")
                .logGroupName("/aws/vendedlogs/pipes/%s".formatted(name))
                .retention(RetentionDays.ONE_DAY)
                .removalPolicy(RemovalPolicy.DESTROY)
                .build();
    }

    private Role createPipeRole(IQueue sourceQueue, IFunction enrichmentFunction, IApiDestination apiDestinationTarget, ILogGroup logGroup) {
        var role = Role.Builder.create(this, "Role")
                .inlinePolicies(Map.of(
                        "targetPolicy", createInvokeApiDestinationPolicy(apiDestinationTarget)
                ))
                .assumedBy(new ServicePrincipal("pipes.amazonaws.com"))
                .build();
        sourceQueue.grantConsumeMessages(role);
        enrichmentFunction.grantInvoke(role);
        logGroup.grantWrite(role);
        return role;
    }

    private static PolicyDocument createInvokeApiDestinationPolicy(IApiDestination apiDestinationTarget) {
        var apiDestinationArn = apiDestinationTarget.getApiDestinationArn();
        return PolicyDocument.Builder.create()
                .statements(List.of(
                        PolicyStatement.Builder.create()
                                .effect(Effect.ALLOW)
                                .actions(List.of("events:InvokeApiDestination"))
                                .resources(List.of(apiDestinationArn))
                                .build()
                ))
                .build();
    }

    private void createPipe(IQueue source, IFunction enrichment, IApiDestination target, ILogGroup logGroup) {
        var role = createPipeRole(source, enrichment, target, logGroup);
        CfnPipe.Builder.create(this, "Pipe")
                .source(source.getQueueArn())
                .sourceParameters(CfnPipe.PipeSourceParametersProperty.builder()
                        .sqsQueueParameters(CfnPipe.PipeSourceSqsQueueParametersProperty.builder()
                                .batchSize(DEFAULT_BATCH_SIZE)
                                .maximumBatchingWindowInSeconds(DEFAULT_BATCHING_WINDOW_SECONDS)
                                .build())
                        .build()
                )
                .enrichment(enrichment.getFunctionArn())
                .enrichmentParameters(CfnPipe.PipeEnrichmentParametersProperty.builder()
//                        .inputTemplate("${body})
                        .build())
                .target(target.getApiDestinationArn())
                .targetParameters(CfnPipe.PipeTargetParametersProperty.builder()
                        .httpParameters(CfnPipe.PipeTargetHttpParametersProperty.builder()
                                .headerParameters(Map.of(
                                        "X-Signature", "$.Headers.signature"
                                ))
                                .build())
                        .inputTemplate("<$.RequestBody>")
                        .build())
                .roleArn(role.getRoleArn())
                // See https://docs.aws.amazon.com/eventbridge/latest/pipes-reference/API_PipeLogConfiguration.html for
                // available logging configuration options
                // Available levels: OFF, ERROR, INFO, TRACE
                .logConfiguration(CfnPipe.PipeLogConfigurationProperty.builder()
                        .cloudwatchLogsLogDestination(CfnPipe.CloudwatchLogsLogDestinationProperty.builder()
                                .logGroupArn(logGroup.getLogGroupArn())
                                .build())
                        .level("TRACE")
                        .includeExecutionData(List.of("ALL"))
                        .build())
                .build();
    }

    @Builder
    public static class ApiDestinationPipeProps {
        private String name;
        private IQueue sourceQueue;
        private IFunction enrichmentFunction;
        private IApiDestination apiDestinationTarget;
    }
}
