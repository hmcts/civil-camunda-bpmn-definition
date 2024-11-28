package uk.gov.hmcts.reform.civil.bpmn;

import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import java.util.Map;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

class ProcessCoscApplicationTest extends BpmnBaseTest {

    private static final String MESSAGE_NAME = "PROCESS_COSC_APPLICATION";
    private static final String PROCESS_ID = "PROCESS_COSC_APPLICATION_PROCESS_ID";
    private static final String SEND_DETAILS_CJES = "sendDetailsToCJES";
    private static final String NOTIFY_RPA = "NOTIFY_RPA_ON_CONTINUOUS_FEED";
    private static final String NOTIFY_RPA_ACTIVITY_ID = "NotifyRPA";

    public ProcessCoscApplicationTest() {
        super("process_cosc_application.bpmn", PROCESS_ID);
    }

    @ParameterizedTest
    @CsvSource({
        "false, false",
        "false, true",
        "true, false",
        "true, true",
    })
    void shouldSuccessfullyCompleteAcknowledgeClaim_whenCalled(boolean cjes, boolean joFlag) {
        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        VariableMap variables = Variables.createVariables();
        variables.put("flowFlags", Map.of(
            IS_JO_LIVE_FEED_ACTIVE, joFlag
        ));
        variables.put(SEND_DETAILS_CJES, cjes);

        //complete the start business process
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertCompleteExternalTask(startBusiness,
                                   START_BUSINESS_TOPIC,
                                   START_BUSINESS_EVENT,
                                   START_BUSINESS_ACTIVITY,
                                   variables
        );

        ExternalTask checkMarkPaidInFull = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            checkMarkPaidInFull,
            PROCESS_CASE_EVENT,
            "CHECK_AND_MARK_PAID_IN_FULL",
            "CheckAndMarkDefendantPaidInFull"
        );

        if (cjes) {
            ExternalTask sendJudgement = assertNextExternalTask(PROCESS_CASE_EVENT);
            assertCompleteExternalTask(
                sendJudgement,
                PROCESS_CASE_EVENT,
                "SEND_JUDGMENT_DETAILS_CJES",
                "SendJudgmentDetailsCJES"
            );
        }

        ExternalTask generateCoSCDocument = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            generateCoSCDocument,
            PROCESS_CASE_EVENT,
            "GENERATE_COSC_DOCUMENT",
            "GenerateCoSCDocument"
        );

        ExternalTask notifyCertGenerated = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            notifyCertGenerated,
            PROCESS_CASE_EVENT,
            "CREATE_DASHBOARD_NOTIFICATION_COSC_GEN_FOR_DEFENDANT",
            "DefendantDashboardNotificationCertificateGenerated"
        );

        if (joFlag) {
            ExternalTask notifyRPA = assertNextExternalTask(PROCESS_CASE_EVENT);
            assertCompleteExternalTask(
                notifyRPA,
                PROCESS_CASE_EVENT,
                NOTIFY_RPA,
                NOTIFY_RPA_ACTIVITY_ID,
                variables
            );
        }

        //end business process
        ExternalTask endBusinessProcess = assertNextExternalTask(END_BUSINESS_PROCESS);
        completeBusinessProcess(endBusinessProcess);

        assertNoExternalTasksLeft();
    }

    @Test
    void shouldAbort_whenStartBusinessProcessThrowsAnError() {
        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        //fail the start business process
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertFailExternalTask(startBusiness, START_BUSINESS_TOPIC, START_BUSINESS_EVENT, START_BUSINESS_ACTIVITY);

        assertNoExternalTasksLeft();
    }
}
