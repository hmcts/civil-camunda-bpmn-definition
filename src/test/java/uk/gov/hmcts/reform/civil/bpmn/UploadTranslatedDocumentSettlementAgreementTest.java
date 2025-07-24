package uk.gov.hmcts.reform.civil.bpmn;

import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

class UploadTranslatedDocumentSettlementAgreementTest extends BpmnBaseTest {

    public static final String MESSAGE_NAME = "UPLOAD_TRANSLATED_DOCUMENT_SETTLEMENT_AGREEMENT";
    public static final String PROCESS_ID = "UPLOAD_TRANSLATED_SETTLEMENT_AGREEMENT_PROCESS_ID";

    private static final String NOTIFY_EVENT =
        "NOTIFY_EVENT";
    private static final String DASHBOARD_NOTIFICATION_FOR_SIGN_SETTLEMENT_AGREEMENT_EVENT_ID =
        "CREATE_DASHBOARD_NOTIFICATION_FOR_SETTLEMENT_DEFENDANT_RESPONSE";
    private static final String DASHBOARD_NOTIFICATION_FOR_SIGN_SETTLEMENT_AGREEMENT_ACTIVITY_ID =
        "GenerateDashboardNotificationSignSettlementAgreement";
    private static final String NOTIFY_EVENT_ID =
        "DefendantSignSettlementNotify";

    public UploadTranslatedDocumentSettlementAgreementTest() {
        super("upload_translated_document_settlement_agreement.bpmn", PROCESS_ID);
    }

    @Test
    void shouldSuccessfullyCompleteUploadTranslatedSettlementAgreement() {
        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        VariableMap variables = Variables.createVariables();
        //complete the start business process
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertCompleteExternalTask(startBusiness,
                                   START_BUSINESS_TOPIC,
                                   START_BUSINESS_EVENT,
                                   START_BUSINESS_ACTIVITY,
                                   variables);

        notifyApplicantSignSettlementAgreement();
        generateDashboardNotificationSignSettlementAgreement();

        //end business process
        ExternalTask endBusinessProcess = assertNextExternalTask(END_BUSINESS_PROCESS);
        completeBusinessProcess(endBusinessProcess);

        assertNoExternalTasksLeft();
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
}
