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

class ApplicantTrialReadyNotifyOthersTest extends BpmnBaseTest {

    public static final String MESSAGE_NAME = "TRIAL_READY_NOTIFICATION";
    public static final String PROCESS_ID = "APPLICANT_TRIAL_READY_NOTIFY_OTHERS";

    //CCD CASE EVENT
    public static final String NOTIFY_RESPONDENT_SOLICITOR2_FOR_OTHER_TRIAL_READY
        = "NOTIFY_RESPONDENT_SOLICITOR2_FOR_OTHER_TRIAL_READY";
    public static final String NOTIFY_RESPONDENT_SOLICITOR1_FOR_OTHER_TRIAL_READY
        = "NOTIFY_RESPONDENT_SOLICITOR1_FOR_OTHER_TRIAL_READY";
    public static final String GENERATE_TRIAL_READY_FORM_APPLICANT
        = "GENERATE_TRIAL_READY_FORM_APPLICANT";

    //ACTIVITY IDs
    private static final String NOTIFY_RESPONDENT_SOLICITOR2_FOR_OTHER_TRIAL_READY_ACTIVITY_ID
        = "OtherTrialReadyNotifyRespondentSolicitor2";
    private static final String NOTIFY_RESPONDENT_SOLICITOR1_FOR_OTHER_TRIAL_READY_ACTIVITY_ID
        = "OtherTrialReadyNotifyRespondentSolicitor1";
    public static final String GENERATE_TRIAL_READY_FORM_APPLICANT_ACTIVITY_ID
        = "GenerateTrialReadyFormApplicant";

    public ApplicantTrialReadyNotifyOthersTest() {
        super("applicant_trial_ready_notify_others.bpmn", PROCESS_ID);
    }

    @ParameterizedTest
    @CsvSource({"true", "false"})
    void shouldSuccessfullyCompleteTrialReadyFormAndNotifyDefendantsHearing(boolean twoRepresentatives) {
        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        VariableMap variables = Variables.createVariables();
        variables.put("flowFlags", Map.of(
            ONE_RESPONDENT_REPRESENTATIVE, !twoRepresentatives,
            TWO_RESPONDENT_REPRESENTATIVES, twoRepresentatives,
            UNREPRESENTED_DEFENDANT_ONE, false));

        //complete the start business process
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertCompleteExternalTask(startBusiness, START_BUSINESS_TOPIC,
                                   START_BUSINESS_EVENT, START_BUSINESS_ACTIVITY, variables);

        ExternalTask notificationTask;
        if (twoRepresentatives) {
            //complete the defendant2 notification
            notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
            assertCompleteExternalTask(notificationTask, PROCESS_CASE_EVENT,
                                       NOTIFY_RESPONDENT_SOLICITOR2_FOR_OTHER_TRIAL_READY,
                                       NOTIFY_RESPONDENT_SOLICITOR2_FOR_OTHER_TRIAL_READY_ACTIVITY_ID,
                                       variables
            );
        }


        //complete the applicant notification
        notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(notificationTask, PROCESS_CASE_EVENT,
                                   NOTIFY_RESPONDENT_SOLICITOR1_FOR_OTHER_TRIAL_READY,
                                   NOTIFY_RESPONDENT_SOLICITOR1_FOR_OTHER_TRIAL_READY_ACTIVITY_ID,
                                   variables
        );

        //complete the hearing form process
        notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(notificationTask, PROCESS_CASE_EVENT,
                                   GENERATE_TRIAL_READY_FORM_APPLICANT,
                                   GENERATE_TRIAL_READY_FORM_APPLICANT_ACTIVITY_ID,
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
