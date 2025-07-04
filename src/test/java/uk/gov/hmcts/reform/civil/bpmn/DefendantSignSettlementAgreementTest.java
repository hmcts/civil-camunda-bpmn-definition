package uk.gov.hmcts.reform.civil.bpmn;

import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Map;

class DefendantSignSettlementAgreementTest extends BpmnBaseTest {

    private static final String FILE_NAME = "defendant_sign_settlement_agreement.bpmn";
    private static final String MESSAGE_NAME = "DEFENDANT_SIGN_SETTLEMENT_AGREEMENT";
    private static final String PROCESS_ID = "DEFENDANT_SIGN_SETTLEMENT_AGREEMENT_PROCESS_ID";
    private static final String NOTIFY_EVENT =
        "NOTIFY_EVENT";
    private static final String DASHBOARD_NOTIFICATION_FOR_SIGN_SETTLEMENT_AGREEMENT_EVENT_ID =
        "CREATE_DASHBOARD_NOTIFICATION_FOR_SETTLEMENT_DEFENDANT_RESPONSE";
    private static final String DASHBOARD_NOTIFICATION_FOR_SIGN_SETTLEMENT_AGREEMENT_ACTIVITY_ID =
        "GenerateDashboardNotificationSignSettlementAgreement";
    private static final String NOTIFY_EVENT_ID =
        "DefendantSignSettlementNotify";

    private static final String GENERATE_LIP_SIGN_SETTLEMENT_AGREEMENT_FORM =
            "GENERATE_LIP_SIGN_SETTLEMENT_AGREEMENT_FORM";
    private static final String GENERATE_LIP_SIGN_SETTLEMENT_AGREEMENT_FORM_ID =
            "GenerateSignSettlementAgreement";

    public DefendantSignSettlementAgreementTest() {
        super(FILE_NAME, PROCESS_ID);
    }

    @Test
    void shouldSuccessfullyProcessDefendantSignAgreement() {
        assertProcessStartedWithMessage(MESSAGE_NAME, PROCESS_ID);
        VariableMap variables = Variables.createVariables();
        variables.putValue(FLOW_FLAGS, Map.of(
            WELSH_ENABLED, true
        ));
        startBusinessProcess(variables);
        generateSettlementAgreementDoc();
        notifyApplicantSignSettlementAgreement();
        generateDashboardNotificationSignSettlementAgreement();
        endBusinessProcess();
        assertNoExternalTasksLeft();
    }

    @ParameterizedTest
    @CsvSource({"true,false", "false,true", "true,true"})
    void shouldSkipTasksWhenLanguagePreferenceWelsh(boolean claimantBilingual, boolean defendantBilingual) {
        assertProcessStartedWithMessage(MESSAGE_NAME, PROCESS_ID);
        VariableMap variables = Variables.createVariables();
        variables.putValue(FLOW_FLAGS, Map.of(
            WELSH_ENABLED, true,
            CLAIM_ISSUE_BILINGUAL, claimantBilingual,
            RESPONDENT_RESPONSE_LANGUAGE_IS_BILINGUAL, defendantBilingual
        ));
        startBusinessProcess(variables);
        generateSettlementAgreementDoc();
        endBusinessProcess();
        assertNoExternalTasksLeft();
    }

    private void endBusinessProcess() {
        ExternalTask endBusinessProcess = assertNextExternalTask(END_BUSINESS_PROCESS);
        completeBusinessProcess(endBusinessProcess);
    }

    private void notifyApplicantSignSettlementAgreement() {
        ExternalTask notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            notificationTask,
            PROCESS_CASE_EVENT,
            NOTIFY_EVENT,
            NOTIFY_EVENT_ID
        );
    }

    private void generateDashboardNotificationSignSettlementAgreement() {
        ExternalTask notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            notificationTask,
            PROCESS_CASE_EVENT,
            DASHBOARD_NOTIFICATION_FOR_SIGN_SETTLEMENT_AGREEMENT_EVENT_ID,
            DASHBOARD_NOTIFICATION_FOR_SIGN_SETTLEMENT_AGREEMENT_ACTIVITY_ID
        );
    }

    private void generateSettlementAgreementDoc() {
        ExternalTask notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
                notificationTask,
                PROCESS_CASE_EVENT,
                GENERATE_LIP_SIGN_SETTLEMENT_AGREEMENT_FORM,
                GENERATE_LIP_SIGN_SETTLEMENT_AGREEMENT_FORM_ID
        );
    }
}
