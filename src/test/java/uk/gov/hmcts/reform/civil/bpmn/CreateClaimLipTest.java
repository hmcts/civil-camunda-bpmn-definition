package uk.gov.hmcts.reform.civil.bpmn;

import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.jupiter.api.Test;

import java.util.Map;

public class CreateClaimLipTest extends BpmnBaseTest {

    private static final String FILE_NAME = "create_lip_claim.bpmn";
    private static final String MESSAGE_NAME = "CREATE_LIP_CLAIM";
    private static final String PROCESS_ID = "CREATE_LIP_CLAIM_PROCESS_ID";
    private static final String NOTIFY_RPA_ON_CONTINUOUS_FEED_EVENT = "NOTIFY_RPA_ON_CONTINUOUS_FEED";
    private static final String NOTIFY_RPA_ON_CONTINUOUS_FEED_ACTIVITY_ID = "NotifyRoboticsOnContinuousFeed";

    public CreateClaimLipTest() {
        super(FILE_NAME, PROCESS_ID);
    }

    @Test
    void shouldSuccessfullyCreateLipClaim() {
        assertProcessStartedWithMessage(MESSAGE_NAME, PROCESS_ID);
        VariableMap variables = Variables.createVariables();
        variables.put(FLOW_FLAGS, Map.of(
            "RPA_CONTINUOUS_FEED", true,
            "PIP_ENABLED", true));
        startBusinessProcess(variables);
        ExternalTask forRobotics = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            forRobotics,
            PROCESS_CASE_EVENT,
            NOTIFY_RPA_ON_CONTINUOUS_FEED_EVENT,
            NOTIFY_RPA_ON_CONTINUOUS_FEED_ACTIVITY_ID,
            variables
        );
        completeBusinessProcess(assertNextExternalTask(END_BUSINESS_PROCESS));
    }
}
