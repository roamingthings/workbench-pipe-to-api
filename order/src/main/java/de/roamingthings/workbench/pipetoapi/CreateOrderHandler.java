package de.roamingthings.workbench.pipetoapi;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import io.micronaut.function.aws.MicronautRequestHandler;
import io.micronaut.json.JsonMapper;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CreateOrderHandler extends MicronautRequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {

    private static final Logger log = LoggerFactory.getLogger(CreateOrderHandler.class);

    @Inject
    JsonMapper objectMapper;

    @Override
    public APIGatewayV2HTTPResponse execute(APIGatewayV2HTTPEvent input) {
        log.info("Received request: {}", input);
        return APIGatewayV2HTTPResponse.builder()
                .withStatusCode(200)
                .build();
    }
}
