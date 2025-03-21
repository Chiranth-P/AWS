package group1.server;

import java.net.URI;

import javax.annotation.Priority;
import javax.enterprise.inject.Alternative;
import javax.inject.Inject;

import io.helidon.microprofile.server.ServerCdiExtension;
import io.helidon.microprofile.tests.junit5.AddBean;
import io.helidon.microprofile.tests.junit5.HelidonTest;

import com.oracle.bmc.Region;
import com.oracle.bmc.loggingingestion.Logging;
import com.oracle.bmc.loggingingestion.requests.PutLogsRequest;
import com.oracle.bmc.loggingingestion.responses.PutLogsResponse;
import com.oracle.bmc.monitoring.Monitoring;
import com.oracle.bmc.monitoring.MonitoringPaginators;
import com.oracle.bmc.monitoring.MonitoringWaiters;
import com.oracle.bmc.monitoring.requests.*;
import com.oracle.bmc.monitoring.responses.*;
import group1.client.api.ApiException;
import group1.client.api.GreetApi;
import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static javax.interceptor.Interceptor.Priority.APPLICATION;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@HelidonTest
@AddBean(GreetResourceMockedTest.MockLogging.class)
@AddBean(GreetResourceMockedTest.MockMonitoring.class)
class GreetResourceMockedTest {
    private GreetApi greetApi;

    @Inject
    private ServerCdiExtension serverCdiExtension;

    private static RuntimeException putLogsException, postMetricDataException = null;

    @BeforeEach
    void beforeEach() {
        URI uriInfo = URI.create(String.format("http://localhost:%s/",
                                               serverCdiExtension.port()
        ));
        greetApi = RestClientBuilder.newBuilder()
                .baseUri(uriInfo)
                .build(GreetApi.class);
    }
    @Test
    void testHelloWorld() throws ApiException {
        assertThat(Common.getDefaultMessage(greetApi), is("Hello World!"));
        assertThat(Common.getMessage(greetApi, "Joe"), is("Hello Joe!"));

        // Change Greeting test
        Common.updateGreeting(greetApi, "Hola");
        assertThat(Common.getDefaultMessage(greetApi), is("Hola World!"));
    }

    @Test
    void testOciFailureShouldNotCauseInternalError() throws ApiException {
        putLogsException = new RuntimeException("Simulate Logging.putLogs Exception");
        postMetricDataException = new RuntimeException("Simulate Logging.postMetricDataException");
        assertThat(Common.getDefaultMessage(greetApi), is("Hello World!"));
        // reset exception variables
        putLogsException = postMetricDataException = null;
    }

    @Alternative
    @Priority(APPLICATION + 1)
    static class MockLogging implements Logging {

        private String endpoint;

        private MockLogging() {
            super();
        }

        @Override
        public void close() {}

        @Override
        public String getEndpoint() {
            return this.endpoint;
        }

        @Override
        public void setRegion(Region region) {}

        @Override
        public void useRealmSpecificEndpointTemplate(boolean b) {}

        @Override
        public void setEndpoint(String endpoint) {
            this.endpoint = endpoint;
        }

        @Override
        public void setRegion(String region) {}

        @Override
        public void refreshClient() {}

        @Override
        public PutLogsResponse putLogs(PutLogsRequest request) {
            if (putLogsException != null) {
                throw putLogsException;
            }
            return PutLogsResponse.builder()
                    .__httpStatusCode__(200)
                    .build();
        }
    }

    @Alternative
    @Priority(APPLICATION + 1)
    static class MockMonitoring implements Monitoring {
        @Override
        public void setEndpoint(String s) {}

        @Override
        public String getEndpoint() {return "http://www.DummyEndpoint.com";}

        @Override
        public void setRegion(Region region) {}

        @Override
        public void useRealmSpecificEndpointTemplate(boolean b) {}

        @Override
        public void setRegion(String s) {}

        @Override
        public ChangeAlarmCompartmentResponse changeAlarmCompartment(ChangeAlarmCompartmentRequest changeAlarmCompartmentRequest) {
            return null;
        }

        @Override
        public CreateAlarmResponse createAlarm(CreateAlarmRequest createAlarmRequest) {return null;}

        @Override
        public DeleteAlarmResponse deleteAlarm(DeleteAlarmRequest deleteAlarmRequest) {return null;}

        @Override
        public GetAlarmResponse getAlarm(GetAlarmRequest getAlarmRequest) {return null;}

        @Override
        public GetAlarmHistoryResponse getAlarmHistory(GetAlarmHistoryRequest getAlarmHistoryRequest) {return null;}

        @Override
        public ListAlarmsResponse listAlarms(ListAlarmsRequest listAlarmsRequest) {return null;}

        @Override
        public ListAlarmsStatusResponse listAlarmsStatus(ListAlarmsStatusRequest listAlarmsStatusRequest) {
            return null;
        }

        @Override
        public ListMetricsResponse listMetrics(ListMetricsRequest listMetricsRequest) {return null;}

        @Override
        public void refreshClient() {}

        @Override
        public PostMetricDataResponse postMetricData(PostMetricDataRequest postMetricDataRequest) {
            if (postMetricDataException != null) {
                throw postMetricDataException;
            }
            return PostMetricDataResponse.builder()
                    .__httpStatusCode__(200)
                    .build();
        }

        @Override
        public RemoveAlarmSuppressionResponse removeAlarmSuppression(RemoveAlarmSuppressionRequest removeAlarmSuppressionRequest) {
            return null;
        }

        @Override
        public RetrieveDimensionStatesResponse retrieveDimensionStates(RetrieveDimensionStatesRequest retrieveDimensionStatesRequest) {
            return null;
        }

        @Override
        public SummarizeMetricsDataResponse summarizeMetricsData(SummarizeMetricsDataRequest summarizeMetricsDataRequest) {
            return null;
        }

        @Override
        public UpdateAlarmResponse updateAlarm(UpdateAlarmRequest updateAlarmRequest) {return null;}

        @Override
        public MonitoringWaiters getWaiters() {return null;}

        @Override
        public MonitoringPaginators getPaginators() {return null;}

        @Override
        public void close() {}
    }
}
