package uk.gov.hmcts.reform.civil.bpmn;

import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class AmendRestitchBundleTest extends BpmnBaseTest {

    public static final String MESSAGE_NAME = "AMEND_RESTITCH_BUNDLE";
    public static final String PROCESS_ID = "AMEND_RESTITCH_BUNDLE";

    //CCD CASE EVENT
    private static final String NOTIFY_EVENT = "NOTIFY_EVENT";

    //ACTIVITY IDs
    private static final String NOTIFY_AMEND_RESTITCH_BUNDLE_ACTIVITY_ID = "AmendRestitchBundleNotify";

    private static final String AMEND_RESTITCH_BUNDLE = "GenerateDashboardNotificationsAmendRestitchBundle";

    public AmendRestitchBundleTest() {
        super("amend_restitch_bundle.bpmn", PROCESS_ID);
    }

    private static Stream<Arguments> provideArgumentSource() {
        return Stream.of(
                Arguments.of(
                        true, false
                ),
                Arguments.of(
                        false, false
                ),
                Arguments.of(
                        true, true
                ),
                Arguments.of(
                        false, true
                )
        );
    }

    @ParameterizedTest
    @MethodSource("provideArgumentSource")
    void shouldSuccessfullyCompleteAmendRestitchBundle(boolean twoRespondents, boolean dashboardServiceEnabled) {
        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        VariableMap variables = Variables.createVariables();
        variables.put("flowFlags", Map.of(
                UNREPRESENTED_DEFENDANT_ONE, false,
                DASHBOARD_SERVICE_ENABLED, dashboardServiceEnabled,
                ONE_RESPONDENT_REPRESENTATIVE, !twoRespondents,
                TWO_RESPONDENT_REPRESENTATIVES, twoRespondents
        ));

        //complete the start business process
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertCompleteExternalTask(startBusiness, START_BUSINESS_TOPIC,
                START_BUSINESS_EVENT, START_BUSINESS_ACTIVITY, variables
        );

        ExternalTask notificationTask;

        //complete the claimant notification
        notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(notificationTask, PROCESS_CASE_EVENT,
                NOTIFY_EVENT,
                NOTIFY_AMEND_RESTITCH_BUNDLE_ACTIVITY_ID,
                variables
        );

        if (dashboardServiceEnabled) {
            //complete the dashboard notification
            notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
            assertCompleteExternalTask(notificationTask, PROCESS_CASE_EVENT,
                    DASHBOARD_NOTIFICATION_EVENT,
                    AMEND_RESTITCH_BUNDLE,
                    variables
            );
        }

        //end business process
        ExternalTask endBusinessProcess = assertNextExternalTask(END_BUSINESS_PROCESS);
        completeBusinessProcess(endBusinessProcess);

        assertNoExternalTasksLeft();
    }
}
