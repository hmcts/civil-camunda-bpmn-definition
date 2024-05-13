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

class RequestNonDivergentJudgementByAdmissionTest extends BpmnBaseTest {

    public static final String MESSAGE_NAME = "JUDGEMENT_BY_ADMISSION_NON_DIVERGENT_SPEC";
    public static final String PROCESS_ID = "JUDGEMENT_BY_ADMISSION_NON_DIVERGENT_SPEC_ID";
    public static final String CREATE_DASHBOARD_NOTIFICATION_FOR_CCJ_REQUEST_FOR_APPLICANT1 = "CREATE_DASHBOARD_NOTIFICATION_FOR_CCJ_REQUEST_FOR_APPLICANT1";
    public static final String CREATE_DASHBOARD_NOTIFICATION_FOR_CCJ_REQUEST_FOR_APPLICANT1_ACTIVITY_ID = "GenerateDashboardNotificationClaimantIntentCCJRequestedForApplicant1";
    public static final String CREATE_DASHBOARD_NOTIFICATION_FOR_CCJ_REQUEST_FOR_RESPONDENT1 = "CREATE_DASHBOARD_NOTIFICATION_FOR_CCJ_REQUEST_FOR_RESPONDENT1";
    public static final String CREATE_DASHBOARD_NOTIFICATION_FOR_CCJ_REQUEST_FOR_RESPONDENT1_ACTIVITY_ID = "GenerateDashboardNotificationClaimantIntentCCJRequestedForRespondent1";

    public RequestNonDivergentJudgementByAdmissionTest() {
        super("judgement_by_admission_non_divergent_spec.bpmn", PROCESS_ID);
    }

    @ParameterizedTest
    @CsvSource({"true", "false"})
    void shouldSuccessfullyCompleteRequestJudgementByAdmission_withLr(boolean isLiPDefendant) {
        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        VariableMap variables = Variables.createVariables();
        variables.put(FLOW_FLAGS, Map.of(
            "LIP_CASE", false,
            "DASHBOARD_SERVICE_ENABLED", true,
            UNREPRESENTED_DEFENDANT_ONE, isLiPDefendant));

        //complete the start business process
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertCompleteExternalTask(
            startBusiness,
            START_BUSINESS_TOPIC,
            START_BUSINESS_EVENT,
            START_BUSINESS_ACTIVITY,
            variables);

        ExternalTask dashboardNotificationRespondent1 = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            dashboardNotificationRespondent1,
            PROCESS_CASE_EVENT,
            CREATE_DASHBOARD_NOTIFICATION_FOR_CCJ_REQUEST_FOR_RESPONDENT1,
            CREATE_DASHBOARD_NOTIFICATION_FOR_CCJ_REQUEST_FOR_RESPONDENT1_ACTIVITY_ID
        );

        if(isLiPDefendant) {
            // should send letter to LiP respondent
            ExternalTask sendLipLetter = assertNextExternalTask(PROCESS_CASE_EVENT);
            assertCompleteExternalTask(
                sendLipLetter,
                PROCESS_CASE_EVENT,
                "JUDGMENT_BY_ADMISSION_DEFENDANT1_PIN_IN_LETTER",
                "SendJudgmentByAdmissionLiPLetterDef1",
                variables
            );
        }

        //end business process
        ExternalTask endBusinessProcess = assertNextExternalTask(END_BUSINESS_PROCESS);
        completeBusinessProcess(endBusinessProcess);

        assertNoExternalTasksLeft();
    }

    @ParameterizedTest
    @CsvSource({"true", "false"})
    void shouldSuccessfullyCompleteRequestJudgementByAdmission_withLipClaimant(boolean isLiPDefendant) {
        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        VariableMap variables = Variables.createVariables();
        variables.put(FLOW_FLAGS, Map.of(
            "LIP_CASE", true,
            "DASHBOARD_SERVICE_ENABLED", true,
            UNREPRESENTED_DEFENDANT_ONE, isLiPDefendant));

        //complete the start business process
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertCompleteExternalTask(
            startBusiness,
            START_BUSINESS_TOPIC,
            START_BUSINESS_EVENT,
            START_BUSINESS_ACTIVITY,
            variables);

        ExternalTask dashboardNotification = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            dashboardNotification,
            PROCESS_CASE_EVENT,
            CREATE_DASHBOARD_NOTIFICATION_FOR_CCJ_REQUEST_FOR_APPLICANT1,
            CREATE_DASHBOARD_NOTIFICATION_FOR_CCJ_REQUEST_FOR_APPLICANT1_ACTIVITY_ID
        );

        ExternalTask dashboardNotificationRespondent1 = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            dashboardNotificationRespondent1,
            PROCESS_CASE_EVENT,
            CREATE_DASHBOARD_NOTIFICATION_FOR_CCJ_REQUEST_FOR_RESPONDENT1,
            CREATE_DASHBOARD_NOTIFICATION_FOR_CCJ_REQUEST_FOR_RESPONDENT1_ACTIVITY_ID
        );

        if(isLiPDefendant) {
            // should send letter to LiP respondent
            ExternalTask sendLipLetter = assertNextExternalTask(PROCESS_CASE_EVENT);
            assertCompleteExternalTask(
                sendLipLetter,
                PROCESS_CASE_EVENT,
                "JUDGMENT_BY_ADMISSION_DEFENDANT1_PIN_IN_LETTER",
                "SendJudgmentByAdmissionLiPLetterDef1",
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
