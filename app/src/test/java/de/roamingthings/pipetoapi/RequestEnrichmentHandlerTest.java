package de.roamingthings.pipetoapi;

import au.com.origin.snapshots.Expect;
import au.com.origin.snapshots.junit5.SnapshotExtension;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import de.roamingthings.workbench.pipetoapi.RequestEnrichmentHandler;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;

@ExtendWith(SnapshotExtension.class)
class RequestEnrichmentHandlerTest {

    static RequestEnrichmentHandler handler;

    Expect expect;

    @BeforeAll
    static void setupServer() {
        handler = new RequestEnrichmentHandler();
    }

    @AfterAll
    static void stopServer() {
        if (handler != null) {
            handler.getApplicationContext().close();
        }
    }

    @Test
    void testHandler() {
        var sqsMessage = new SQSEvent.SQSMessage();
        sqsMessage.setBody("""
            {
              "Foo": "Bar"
            }
        """.trim());
        var input = List.of(
                sqsMessage
        );

        var response = handler.execute(input);

        expect.serializer("json").toMatchSnapshot(response);
    }
}
