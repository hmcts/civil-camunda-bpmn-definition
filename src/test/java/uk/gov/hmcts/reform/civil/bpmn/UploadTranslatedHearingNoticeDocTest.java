package uk.gov.hmcts.reform.civil.bpmn;

import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

class UploadTranslatedHearingNoticeDocTest extends BpmnBaseTest {

    public static final String MESSAGE_NAME = "UPLOAD_TRANSLATED_DOCUMENT_HEARING_NOTICE";
    public static final String PROCESS_ID = "UPLOAD_TRANSLATED_DOCUMENT_HEARING_NOTICE";

    //CCD CASE EVENT
    public static final String NOTIFY_EVENT
            = "NOTIFY_EVENT";
    public static final String SEND_HEARING_TO_LIP_DEFENDANT
            = "SEND_HEARING_TO_LIP_DEFENDANT";
    public static final String SEND_HEARING_TO_LIP_CLAIMANT
            = "SEND_HEARING_TO_LIP_CLAIMANT";

    //ACTIVITY IDs
    public static final String HEARING_NOTICE_GENERATOR_NOTIFIER
            = "HearingNoticeGeneratorNotifier";
    private static final String SEND_HEARING_TO_LIP_DEFENDANT_ACTIVITY_ID
            = "SendHearingToDefendantLIP";
    private static final String SEND_HEARING_TO_LIP_CLAIMANT_ACTIVITY_ID
            = "SendHearingToClaimantLIP";

    public UploadTranslatedHearingNoticeDocTest() {
        super("upload_translated_hearing_notice.bpmn", PROCESS_ID);
    }

    @Test
    void shouldSuccessfullyPostHearingLetterAndNotifyClaimantAndDefendantHearing_1v1() {
        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        //Setup Case as 1v1
        VariableMap variables = Variables.createVariables();
        variables.put(FLOW_FLAGS, Map.of(
                LIP_CASE, true,
                UNREPRESENTED_DEFENDANT_ONE, true
        ));

        //complete the start business process
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertCompleteExternalTask(startBusiness, START_BUSINESS_TOPIC, START_BUSINESS_EVENT, START_BUSINESS_ACTIVITY, variables);

        ExternalTask notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(notificationTask, PROCESS_CASE_EVENT,
                SEND_HEARING_TO_LIP_CLAIMANT, SEND_HEARING_TO_LIP_CLAIMANT_ACTIVITY_ID, variables
        );

        //complete the bulk print
        notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(notificationTask, PROCESS_CASE_EVENT,
                SEND_HEARING_TO_LIP_DEFENDANT, SEND_HEARING_TO_LIP_DEFENDANT_ACTIVITY_ID, variables
        );

        //complete the notify relevant parties notification
        notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(notificationTask, PROCESS_CASE_EVENT,
                NOTIFY_EVENT, HEARING_NOTICE_GENERATOR_NOTIFIER, variables
        );

        //end business process
        ExternalTask endBusinessProcess = assertNextExternalTask(END_BUSINESS_PROCESS);
        completeBusinessProcess(endBusinessProcess);

        assertNoExternalTasksLeft();
    }

    @Test
    void shouldSuccessfullyPostHearingLetterAndNotifyClaimantAndDefendantHearing_1v1_LipClaimant_LrDefendant() {
        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        //Setup Case as 1v1
        VariableMap variables = Variables.createVariables();
        variables.put(FLOW_FLAGS, Map.of(
                UNREPRESENTED_DEFENDANT_ONE, false,
                LIP_CASE, true,
                DASHBOARD_SERVICE_ENABLED, true
        ));

        //complete the start business process
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertCompleteExternalTask(startBusiness, START_BUSINESS_TOPIC, START_BUSINESS_EVENT, START_BUSINESS_ACTIVITY, variables);

        //complete the bulk print
        ExternalTask notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(notificationTask, PROCESS_CASE_EVENT,
                SEND_HEARING_TO_LIP_CLAIMANT, SEND_HEARING_TO_LIP_CLAIMANT_ACTIVITY_ID, variables
        );

        //complete the notify relevant parties notification
        notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(notificationTask, PROCESS_CASE_EVENT,
                NOTIFY_EVENT, HEARING_NOTICE_GENERATOR_NOTIFIER, variables
        );

        //end business process
        ExternalTask endBusinessProcess = assertNextExternalTask(END_BUSINESS_PROCESS);
        completeBusinessProcess(endBusinessProcess);

        assertNoExternalTasksLeft();
    }

    @Test
    void shouldSuccessfullyPostHearingLetterAndNotifyClaimantAndDefendantLipHearing_1v1() {
        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        //Setup Case as 1v1
        VariableMap variables = Variables.createVariables();
        variables.put(FLOW_FLAGS, Map.of(
                UNREPRESENTED_DEFENDANT_ONE, true));

        //complete the start business process
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertCompleteExternalTask(startBusiness, START_BUSINESS_TOPIC, START_BUSINESS_EVENT, START_BUSINESS_ACTIVITY, variables);

        //complete the bulk print
        ExternalTask notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(notificationTask, PROCESS_CASE_EVENT,
                SEND_HEARING_TO_LIP_DEFENDANT, SEND_HEARING_TO_LIP_DEFENDANT_ACTIVITY_ID, variables
        );

        //complete the notify relevant parties notification
        notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(notificationTask, PROCESS_CASE_EVENT,
                NOTIFY_EVENT, HEARING_NOTICE_GENERATOR_NOTIFIER, variables
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
