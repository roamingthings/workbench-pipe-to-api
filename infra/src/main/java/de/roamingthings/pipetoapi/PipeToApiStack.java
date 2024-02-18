package de.roamingthings.pipetoapi;

import io.micronaut.aws.cdk.function.MicronautFunction;
import io.micronaut.aws.cdk.function.MicronautFunctionFile;
import io.micronaut.starter.application.ApplicationType;
import io.micronaut.starter.options.BuildTool;
import software.amazon.awscdk.CfnOutput;
import software.amazon.awscdk.Duration;
import software.amazon.awscdk.Environment;
import software.amazon.awscdk.IStackSynthesizer;
import software.amazon.awscdk.PermissionsBoundary;
import software.amazon.awscdk.SecretValue;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.events.ApiDestination;
import software.amazon.awscdk.services.events.Authorization;
import software.amazon.awscdk.services.events.Connection;
import software.amazon.awscdk.services.events.HttpMethod;
import software.amazon.awscdk.services.lambda.Alias;
import software.amazon.awscdk.services.lambda.Architecture;
import software.amazon.awscdk.services.lambda.Code;
import software.amazon.awscdk.services.lambda.Runtime;
import software.amazon.awscdk.services.lambda.SnapStartConf;
import software.amazon.awscdk.services.lambda.Tracing;
import software.amazon.awscdk.services.logs.RetentionDays;
import software.amazon.awscdk.services.sqs.Queue;
import software.constructs.Construct;

import java.util.HashMap;
import java.util.Map;

public class PipeToApiStack extends Stack {

    public static final String UNSAFE_DEMONSTRATION_API_KEY = "my-api-key";

    public PipeToApiStack(Construct parent, String id, PipeToApiStackProps props) {
        super(parent, id, props);

        createOrderFunction();

        var sourceQueue = Queue.Builder.create(this, "Source")
                .build();

        var environmentVariables = new HashMap<String, String>();
        var function = MicronautFunction.create(ApplicationType.FUNCTION,
                false,
                this,
                "RequestEnrichment")
                .runtime(Runtime.JAVA_21)
                .handler("de.roamingthings.workbench.pipetoapi.RequestEnrichmentHandler")
                .environment(environmentVariables)
                .code(Code.fromAsset(enrichmentFunctionPath()))
                .timeout(Duration.seconds(10))
                .memorySize(768)
                .logRetention(RetentionDays.ONE_DAY)
                .logFormat("JSON")
                .tracing(Tracing.ACTIVE)
                .architecture(Architecture.X86_64)
                .snapStart(SnapStartConf.ON_PUBLISHED_VERSIONS)
                .build();
        var alias = Alias.Builder.create(this, "RequestEnrichmentAlias")
                .aliasName("live")
                .version(function.getCurrentVersion())
                .build();

        var apiDestinationTarget = createApiDestinationTarget(
                props.endpointUrl,
                Authorization.apiKey("Authorization", SecretValue.unsafePlainText(UNSAFE_DEMONSTRATION_API_KEY))
        );

        new ApiDestinationPipe(this, "ApiDestinationPipe", ApiDestinationPipe.ApiDestinationPipeProps.builder()
                .sourceQueue(sourceQueue)
                .enrichmentFunction(alias)
                .apiDestinationTarget(apiDestinationTarget)
                .build());
    }

    private void createOrderFunction() {
        var createOrderFunctionEnvironmentVariables = new HashMap<String, String>();
        var createOrderFunction = MicronautFunction.create(ApplicationType.FUNCTION,
                false,
                this,
                "CreateOrder")
                .runtime(Runtime.JAVA_21)
                .handler("de.roamingthings.workbench.pipetoapi.CreateOrderHandler")
                .environment(createOrderFunctionEnvironmentVariables)
                .code(Code.fromAsset(orderFunctionPath()))
                .timeout(Duration.seconds(10))
                .memorySize(768)
                .logRetention(RetentionDays.ONE_DAY)
                .logFormat("JSON")
                .tracing(Tracing.ACTIVE)
                .architecture(Architecture.X86_64)
                .snapStart(SnapStartConf.ON_PUBLISHED_VERSIONS)
                .build();
        Alias.Builder.create(this, "CreateOrderAlias")
                .aliasName("live")
                .version(createOrderFunction.getCurrentVersion())
                .build();
        var functionUrl = createOrderFunction.addFunctionUrl();
        CfnOutput.Builder.create(this, "CreateOrderFunctionUrl")
                .value(functionUrl.getUrl())
                .description("URL of the CreateOrder function")
                .build();
    }

    public static String enrichmentFunctionPath() {
        return "../enrichment/build/libs/" + functionFilename("enrichment");
    }

    public static String orderFunctionPath() {
        return "../order/build/libs/" + functionFilename("order");
    }

    public static String functionFilename(String moduleName) {
        return MicronautFunctionFile.builder()
            .graalVMNative(false)
            .version("0.1")
            .archiveBaseName(moduleName)
            .buildTool(BuildTool.GRADLE)
            .build();
    }

    private ApiDestination createApiDestinationTarget(String endpointUrl, Authorization authorization) {
         var connection = Connection.Builder.create(this, "Connection")
                 .authorization(authorization)
                 .description("Connection with 3rd party API")
                 .build();

         return ApiDestination.Builder.create(this, "ApiDestination")
                 .endpoint(endpointUrl)
                 .httpMethod(HttpMethod.POST)
                 .rateLimitPerSecond(5)
                 .connection(connection)
                 .build();
     }

     @lombok.Builder
     public static class PipeToApiStackProps implements StackProps {
        private final Boolean analyticsReporting;
        private final Boolean crossRegionReferences;
        private final String description;
        private final Environment env;
        private final PermissionsBoundary permissionsBoundary;
        private final String stackName;
        private final Boolean suppressTemplateIndentation;
        private final IStackSynthesizer synthesizer;
        private final Map<String, String> tags;
        private final Boolean terminationProtection;

         private final String endpointUrl;
     }
}