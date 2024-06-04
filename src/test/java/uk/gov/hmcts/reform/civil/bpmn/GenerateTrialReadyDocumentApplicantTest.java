package uk.gov.hmcts.reform.civil.bpmn;

import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

class GenerateTrialReadyDocumentApplicantTest extends BpmnBaseTest {

    public static final String MESSAGE_NAME = "GENERATE_TRIAL_READY_DOCUMENT_APPLICANT";
    public static final String PROCESS_ID = "GENERATE_TRIAL_READY_DOCUMENT_APPLICANT";

    //CCD CASE EVENT
    public static final String GENERATE_TRIAL_READY_FORM_APPLICANT
        = "GENERATE_TRIAL_READY_FORM_APPLICANT";

    public static final String CREATE_DASHBOARD_NOTIFICATION_TRIAL_ARRANGEMENTS_NOTIFY_DEFENDANT
        = "CREATE_DASHBOARD_NOTIFICATION_TRIAL_ARRANGEMENTS_NOTIFY_DEFENDANT";

    public static final String CREATE_DASHBOARD_NOTIFICATION_TRIAL_ARRANGEMENTS_NOTIFY_DEFENDANT_ACTIVITY_ID
        = "GenerateDefendantDashboardNotificationTrialArrangementsNotifyParty";

    //ACTIVITY IDs
    public static final String GENERATE_TRIAL_READY_FORM_APPLICANT_ACTIVITY_ID
        = "GenerateTrialReadyFormApplicant";

    public GenerateTrialReadyDocumentApplicantTest() {
        super("generate_trial_ready_document_applicant.bpmn", PROCESS_ID);
    }

    @Test
    void shouldSuccessfullyCompleteTrialReadyFormAndNotifyDefendantsHearing() {
        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        VariableMap variables = Variables.createVariables();
        variables.putValue("flowFlags", Map.of("DASHBOARD_SERVICE_ENABLED", true));
        variables.putValue("flowFlags", Map.of("CASE_PROGRESSION_ENABLED", true));

        //complete the start business process
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertCompleteExternalTask(startBusiness, START_BUSINESS_TOPIC,
                                   START_BUSINESS_EVENT, START_BUSINESS_ACTIVITY, variables);

        ExternalTask notificationTask;

        //complete the hearing form process
        notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(notificationTask, PROCESS_CASE_EVENT,
                                   GENERATE_TRIAL_READY_FORM_APPLICANT,
                                   GENERATE_TRIAL_READY_FORM_APPLICANT_ACTIVITY_ID
        );

        notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(notificationTask, PROCESS_CASE_EVENT,
                                   CREATE_DASHBOARD_NOTIFICATION_TRIAL_ARRANGEMENTS_NOTIFY_DEFENDANT,
                                   CREATE_DASHBOARD_NOTIFICATION_TRIAL_ARRANGEMENTS_NOTIFY_DEFENDANT_ACTIVITY_ID
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
