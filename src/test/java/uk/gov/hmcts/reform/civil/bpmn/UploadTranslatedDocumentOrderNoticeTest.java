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

class UploadTranslatedDocumentOrderNoticeTest extends BpmnBaseTest {

    public static final String MESSAGE_NAME = "UPLOAD_TRANSLATED_DOCUMENT_ORDER_NOTICE";
    public static final String PROCESS_ID = "UPLOAD_TRANSLATED_DOCUMENT_ORDER_NOTICE";

    public UploadTranslatedDocumentOrderNoticeTest() {
        super("upload_translated_document_order_notice.bpmn", "UPLOAD_TRANSLATED_DOCUMENT_ORDER_NOTICE");
    }

    @ParameterizedTest
    @CsvSource({
        "true, true",
        "true, false",
        "false, true",
        "false, false"
    })
    void shouldSuccessfullyUploadTranslatedDocumentOrderNotice(boolean lipDefendant, boolean lipClaimant) {

        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        VariableMap variables = Variables.createVariables();
        variables.putValue("flowState", "MAIN.FULL_DEFENCE_PROCEED");
        variables.put(FLOW_FLAGS, Map.of(
            UNREPRESENTED_DEFENDANT_ONE, lipDefendant,
            LIP_CASE, lipClaimant,
            DASHBOARD_SERVICE_ENABLED, true
        ));

        //complete the start business process
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertCompleteExternalTask(
            startBusiness,
            START_BUSINESS_TOPIC,
            START_BUSINESS_EVENT,
            START_BUSINESS_ACTIVITY
        );

        //complete the notification to Claimant
        ExternalTask respondentNotification = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            respondentNotification,
            PROCESS_CASE_EVENT,
            "NOTIFY_CLAIMANT_UPLOADED_DOCUMENT_ORDER_NOTICE",
            "NotifyClaimantOfUploadedOrderNotice", variables
        );

        //complete the notification to Respondent
        ExternalTask respondent2Notification = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            respondent2Notification,
            PROCESS_CASE_EVENT,
            "NOTIFY_DEFENDANT_UPLOADED_DOCUMENT_ORDER_NOTICE",
            "NotifyDefendantOfUploadedOrderNotice", variables
        );

        if (lipDefendant) {
            ExternalTask respondentBulkPrint = assertNextExternalTask(PROCESS_CASE_EVENT);
            assertCompleteExternalTask(
                respondentBulkPrint,
                PROCESS_CASE_EVENT,
                "SEND_TRANSLATED_ORDER_TO_LIP_DEFENDANT",
                "BulkPrintOrderDefendant", variables
            );
        }

        if (lipClaimant) {
            ExternalTask claimantBulkPrint = assertNextExternalTask(PROCESS_CASE_EVENT);
            assertCompleteExternalTask(
                claimantBulkPrint,
                PROCESS_CASE_EVENT,
                "SEND_TRANSLATED_ORDER_TO_LIP_CLAIMANT",
                "BulkPrintOrderClaimant", variables
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
