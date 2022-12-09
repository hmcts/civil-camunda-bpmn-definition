package uk.gov.hmcts.reform.civil.bpmn;

import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

class HearingProcessTest extends BpmnBaseTest {

    public static final String MESSAGE_NAME = "HEARING_SCHEDULED";
    public static final String PROCESS_ID = "HEARING_PROCESS";

    //CCD CASE EVENT
    public static final String NOTIFY_CLAIMANT_HEARING
        = "NOTIFY_CLAIMANT_HEARING";
    public static final String NOTIFY_DEFENDANT_HEARING
        = "NOTIFY_DEFENDANT_HEARING";
    public static final String GENERATE_HEARING_FORM
        = "GENERATE_HEARING_FORM";
    public static final String CREATE_SERVICE_REQUEST_API
        = "CREATE_SERVICE_REQUEST_API";

    //ACTIVITY IDs
    private static final String NOTIFY_CLAIMANT_HEARING_ACTIVITY_ID
        = "NotifyClaimantHearing";
    private static final String NOTIFY_DEFENDANT_HEARING_ACTIVITY_ID
        = "NotifyDefendantHearing";
    public static final String GENERATE_HEARING_FORM_ACTIVITY_ID
        = "GenerateHearingForm";
    private static final String CREATE_SERVICE_REQUEST_API_ACTIVITY_ID
        = "ServiceRequestAPI";

    public HearingProcessTest() {
        super("hearing_process.bpmn", PROCESS_ID);
    }

    @Test
    void shouldSuccessfullyCompleteHearingFormAndNotifyClaimantAndDefendantHearing() {
        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        //complete the start business process
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertCompleteExternalTask(startBusiness, START_BUSINESS_TOPIC, START_BUSINESS_EVENT, START_BUSINESS_ACTIVITY);

        //complete the service request process
        ExternalTask notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(notificationTask, PROCESS_CASE_EVENT,
                                   CREATE_SERVICE_REQUEST_API, CREATE_SERVICE_REQUEST_API_ACTIVITY_ID
        );

        //complete the hearing form process
        notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(notificationTask, PROCESS_CASE_EVENT,
                                   GENERATE_HEARING_FORM, GENERATE_HEARING_FORM_ACTIVITY_ID
        );

        //complete the claimant notification
        notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(notificationTask, PROCESS_CASE_EVENT,
                                   NOTIFY_CLAIMANT_HEARING, NOTIFY_CLAIMANT_HEARING_ACTIVITY_ID
        );

        //complete the defendant notification
        notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(notificationTask, PROCESS_CASE_EVENT,
                                   NOTIFY_DEFENDANT_HEARING, NOTIFY_DEFENDANT_HEARING_ACTIVITY_ID
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
