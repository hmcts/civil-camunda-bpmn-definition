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

class RecordJudgmentDeterminationMeansNotificationTest extends BpmnBaseTest {

    public static final String MESSAGE_NAME = "RECORD_JUDGMENT_NOTIFICATION";
    public static final String PROCESS_ID = "RECORD_JUDGMENT_NOTIFICATION";

    public RecordJudgmentDeterminationMeansNotificationTest() {
        super("record_judgment_notification.bpmn", "RECORD_JUDGMENT_NOTIFICATION");
    }

    @ParameterizedTest
    @CsvSource({
        "true, true, true",
        "true, true, false",
        "true, false, true",
        "true, false, false",
        "false, true, true",
        "false, true, false",
        "false, false, true",
        "false, false, false"
    })
    void shouldSuccessfullyCompleteRecordJudgmentNotificationMultiparty(boolean twoRepresentatives, boolean isLiPDefendant, boolean dashboardServiceEnabled) {

        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        VariableMap variables = Variables.createVariables();
        variables.put(FLOW_FLAGS, Map.of(
            ONE_RESPONDENT_REPRESENTATIVE, !twoRepresentatives,
            TWO_RESPONDENT_REPRESENTATIVES, twoRepresentatives,
            UNREPRESENTED_DEFENDANT_ONE, isLiPDefendant,
            DASHBOARD_SERVICE_ENABLED, dashboardServiceEnabled));
        variables.put("judgmentRecordedReason", "DETERMINATION_OF_MEANS");

        //complete the start business process
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertCompleteExternalTask(
            startBusiness,
            START_BUSINESS_TOPIC,
            START_BUSINESS_EVENT,
            START_BUSINESS_ACTIVITY,
            variables
        );

        ExternalTask sendJudgement = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            sendJudgement,
            PROCESS_CASE_EVENT,
            "SEND_JUDGMENT_DETAILS_CJES",
            "SendJudgmentDetailsCJES"
        );

        ExternalTask claimantDoc = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            claimantDoc,
            PROCESS_CASE_EVENT,
            "GEN_JUDGMENT_BY_DETERMINATION_DOC_CLAIMANT",
            "GenerateClaimantJudgmentByDeterminationDoc"
        );

        ExternalTask defendantDoc = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            defendantDoc,
            PROCESS_CASE_EVENT,
            "GEN_JUDGMENT_BY_DETERMINATION_DOC_DEFENDANT",
            "GenerateDefendantJudgmentByDeterminationDoc"
        );

        if (isLiPDefendant) {
            // should send letter to LiP respondent
            ExternalTask sendLipLetter = assertNextExternalTask(PROCESS_CASE_EVENT);
            assertCompleteExternalTask(
                sendLipLetter,
                PROCESS_CASE_EVENT,
                "POST_JO_DEFENDANT1_PIN_IN_LETTER",
                "SendDJLetterLIPDefendant1",
                variables
            );

            if (dashboardServiceEnabled) {
                //complete generate dashboard notification to defendant
                ExternalTask respondent1DashboardNotification = assertNextExternalTask(PROCESS_CASE_EVENT);
                assertCompleteExternalTask(
                    respondent1DashboardNotification,
                    PROCESS_CASE_EVENT,
                    "CREATE_DASHBOARD_NOTIFICATION_RECORD_JUDGMENT_DEFENDANT",
                    "GenerateDashboardNotificationRecordJudgmentDefendant",
                    variables
                );
            }
        }

        //complete the notification for respondent 1
        ExternalTask respondentNotification = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            respondentNotification,
            PROCESS_CASE_EVENT,
            "NOTIFY_RESPONDENT1_FOR_RECORD_JUDGMENT",
            "RecordJudgmentNotifyRespondent1"
        );

        if (twoRepresentatives) {
            //complete the notification for respondent 2
            ExternalTask respondent2Notification = assertNextExternalTask(PROCESS_CASE_EVENT);
            assertCompleteExternalTask(
                respondent2Notification,
                PROCESS_CASE_EVENT,
                "NOTIFY_RESPONDENT2_FOR_RECORD_JUDGMENT",
                "RecordJudgmentNotifyRespondentSolicitor2"
            );
        }

        //complete the notification for applicant solicitor
        ExternalTask applicantNotification = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(applicantNotification,
                                   PROCESS_CASE_EVENT,
                                   "NOTIFY_APPLICANT_FOR_RECORD_JUDGMENT",
                                   "RecordJudgmentNotifyApplicantSolicitor1"
        );

        //end business process
        ExternalTask endBusinessProcess = assertNextExternalTask(END_BUSINESS_PROCESS);
        completeBusinessProcess(endBusinessProcess);

        assertNoExternalTasksLeft();
    }

    @Test
    void shouldBypassProcessesWhenJudgementRecordedReasonIsNotDeterminationOfMeans() {
        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        VariableMap variables = Variables.createVariables();
        variables.put("judgmentRecordedReason", "SOMETHING_ELSE");

        //complete the start business process
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertCompleteExternalTask(
            startBusiness,
            START_BUSINESS_TOPIC,
            START_BUSINESS_EVENT,
            START_BUSINESS_ACTIVITY,
            variables
        );

        ExternalTask sendJudgement = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            sendJudgement,
            PROCESS_CASE_EVENT,
            "SEND_JUDGMENT_DETAILS_CJES",
            "SendJudgmentDetailsCJES"
        );

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
