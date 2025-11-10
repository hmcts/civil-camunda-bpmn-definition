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

class UploadTranslatedDocumentOrderTest extends BpmnBaseTest {

    public static final String MESSAGE_NAME = "UPLOAD_TRANSLATED_DOCUMENT_ORDER";
    public static final String PROCESS_ID = "UPLOAD_TRANSLATED_DOCUMENT_ORDER";

    //CCD CASE EVENT
    public static final String NOTIFY_EVENT
        = "NOTIFY_EVENT";
    public static final String SEND_FINAL_ORDER_TO_LIP_DEFENDANT
        = "SEND_FINAL_ORDER_TO_LIP_DEFENDANT";
    public static final String SEND_FINAL_ORDER_TO_LIP_CLAIMANT
        = "SEND_FINAL_ORDER_TO_LIP_CLAIMANT";

    //ACTIVITY IDs
    private static final String NOTIFY_PARTIES_FOR_GENERATE_ORDER_ACTIVITY_ID
        = "GenerateOrderNotifyParties";
    private static final String SEND_FINAL_ORDER_TO_LIP_DEFENDANT_ACTIVITY_ID
        = "SendFinalOrderToDefendantLIP";
    private static final String SEND_FINAL_ORDER_TO_LIP_CLAIMANT_ACTIVITY_ID
        = "SendFinalOrderToClaimantLIP";

    public UploadTranslatedDocumentOrderTest() {
        super("upload_translated_document_order.bpmn", PROCESS_ID);
    }

    @Test
    void shouldSuccessfullyCompleteUploadTranslatedFinalOrder() {
        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        VariableMap variables = Variables.createVariables();
        variables.put("flowFlags", Map.of(
            UNREPRESENTED_DEFENDANT_ONE, false));

        //complete the start business process
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertCompleteExternalTask(startBusiness, START_BUSINESS_TOPIC,
                                   START_BUSINESS_EVENT, START_BUSINESS_ACTIVITY, variables);

        ExternalTask notificationTask;

        //complete the defendant1 notification
        notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(notificationTask, PROCESS_CASE_EVENT,
                                   NOTIFY_EVENT,
                                   NOTIFY_PARTIES_FOR_GENERATE_ORDER_ACTIVITY_ID,
                                   variables
        );

        //end business process
        ExternalTask endBusinessProcess = assertNextExternalTask(END_BUSINESS_PROCESS);
        completeBusinessProcess(endBusinessProcess);

        assertNoExternalTasksLeft();
    }

    @Test
    void shouldSuccessfullyCompleteUploadTranslatedFinalOrderLip() {
        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        VariableMap variables = Variables.createVariables();
        variables.put("flowFlags", Map.of());

        //complete the start business process
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertCompleteExternalTask(startBusiness, START_BUSINESS_TOPIC,
                                   START_BUSINESS_EVENT, START_BUSINESS_ACTIVITY, variables);

        ExternalTask notificationTask;

        //complete the defendant1 notification
        notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(notificationTask, PROCESS_CASE_EVENT,
                                   NOTIFY_EVENT,
                                   NOTIFY_PARTIES_FOR_GENERATE_ORDER_ACTIVITY_ID,
                                   variables
        );

        //end business process
        ExternalTask endBusinessProcess = assertNextExternalTask(END_BUSINESS_PROCESS);
        completeBusinessProcess(endBusinessProcess);

        assertNoExternalTasksLeft();
    }

    @Test
    void shouldSuccessfullyCompleteGenerateOrderNotificationsLipAndGenerateDashboard() {
        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        VariableMap variables = Variables.createVariables();
        variables.put("flowFlags", Map.of());

        //complete the start business process
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertCompleteExternalTask(startBusiness, START_BUSINESS_TOPIC,
                                   START_BUSINESS_EVENT, START_BUSINESS_ACTIVITY, variables);

        ExternalTask notificationTask;

        //complete the defendant1 notification
        notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(notificationTask, PROCESS_CASE_EVENT,
                                   NOTIFY_EVENT,
                                   NOTIFY_PARTIES_FOR_GENERATE_ORDER_ACTIVITY_ID,
                                   variables
        );

        //end business process
        ExternalTask endBusinessProcess = assertNextExternalTask(END_BUSINESS_PROCESS);
        completeBusinessProcess(endBusinessProcess);

        assertNoExternalTasksLeft();
    }

    @ParameterizedTest
    @CsvSource({
        "true, true",
        "true, false",
        "false, true",
        "false, false"
    })
    void shouldSuccessfullyCompleteGenerateOrderNotificationsAndBulkPrintLip(boolean lipDefendant, boolean lipClaimant) {
        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        VariableMap variables = Variables.createVariables();
        variables.put("flowFlags", Map.of(
            UNREPRESENTED_DEFENDANT_ONE, lipDefendant,
            LIP_CASE, lipClaimant));

        //complete the start business process
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertCompleteExternalTask(startBusiness, START_BUSINESS_TOPIC,
                                   START_BUSINESS_EVENT, START_BUSINESS_ACTIVITY, variables);

        if (lipDefendant) {
            //complete the bulk print
            ExternalTask respondentBulkPrint = assertNextExternalTask(PROCESS_CASE_EVENT);
            assertCompleteExternalTask(respondentBulkPrint,
                                       PROCESS_CASE_EVENT,
                                       SEND_FINAL_ORDER_TO_LIP_DEFENDANT,
                                       SEND_FINAL_ORDER_TO_LIP_DEFENDANT_ACTIVITY_ID,
                                       variables
            );
        }

        if (lipClaimant) {
            //complete the bulk print
            ExternalTask claimantBulkPrint = assertNextExternalTask(PROCESS_CASE_EVENT);
            assertCompleteExternalTask(claimantBulkPrint,
                                       PROCESS_CASE_EVENT,
                                       SEND_FINAL_ORDER_TO_LIP_CLAIMANT,
                                       SEND_FINAL_ORDER_TO_LIP_CLAIMANT_ACTIVITY_ID,
                                       variables
            );
        }

        ExternalTask notificationTask;
        //complete the defendant1 notification
        notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(notificationTask, PROCESS_CASE_EVENT,
                                   NOTIFY_EVENT,
                                   NOTIFY_PARTIES_FOR_GENERATE_ORDER_ACTIVITY_ID,
                                   variables
        );

        //end business process
        ExternalTask endBusinessProcess = assertNextExternalTask(END_BUSINESS_PROCESS);
        completeBusinessProcess(endBusinessProcess);

        assertNoExternalTasksLeft();
    }

    @Test
    void shouldSuccessfullyCompleteGenerateOrderNotificationsAndBulkPrintLip() {
        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        VariableMap variables = Variables.createVariables();
        variables.put("flowFlags", Map.of(
            UNREPRESENTED_DEFENDANT_ONE, true,
            LIP_CASE, true,
            DASHBOARD_SERVICE_ENABLED, true));

        //complete the start business process
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertCompleteExternalTask(startBusiness, START_BUSINESS_TOPIC,
                                   START_BUSINESS_EVENT, START_BUSINESS_ACTIVITY, variables);

        ExternalTask notificationTask;

        //complete the bulk print
        notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(notificationTask, PROCESS_CASE_EVENT,
                                   SEND_FINAL_ORDER_TO_LIP_DEFENDANT, SEND_FINAL_ORDER_TO_LIP_DEFENDANT_ACTIVITY_ID, variables
        );

        //complete the bulk print
        notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(notificationTask, PROCESS_CASE_EVENT,
                                   SEND_FINAL_ORDER_TO_LIP_CLAIMANT, SEND_FINAL_ORDER_TO_LIP_CLAIMANT_ACTIVITY_ID, variables
        );

        //complete the defendant1 notification
        notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(notificationTask, PROCESS_CASE_EVENT,
                                   NOTIFY_EVENT,
                                   NOTIFY_PARTIES_FOR_GENERATE_ORDER_ACTIVITY_ID,
                                   variables
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
