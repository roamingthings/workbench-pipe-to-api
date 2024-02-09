package de.roamingthings.workbench.pipetoapi;

import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import io.micronaut.function.aws.MicronautRequestHandler;
import io.micronaut.json.JsonMapper;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class RequestEnrichmentHandler extends MicronautRequestHandler<List<SQSEvent.SQSMessage>, List<ApiDestinationRequest>> {

    private static final Logger log = LoggerFactory.getLogger(RequestEnrichmentHandler.class);

    @Inject
    JsonMapper objectMapper;

    @Override
    public List<ApiDestinationRequest> execute(List<SQSEvent.SQSMessage> input) {
        log.info("Enriching {} messages", input.size());
        return input.parallelStream()
                .map(SQSEvent.SQSMessage::getBody)
                .map(ApiDestinationRequest::new)
                .toList();
    }
}
