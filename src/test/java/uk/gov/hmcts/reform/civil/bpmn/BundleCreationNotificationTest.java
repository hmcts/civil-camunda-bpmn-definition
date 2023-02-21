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

class BundleCreationNotificationTest extends BpmnBaseTest {

    public static final String MESSAGE_NAME = "BUNDLE_CREATION_NOTIFICATION";
    public static final String PROCESS_ID = "BUNDLE_CREATION_NOTIFICATION";

    public BundleCreationNotificationTest() {
        super("bundle_creation_notification.bpmn", "BUNDLE_CREATION_NOTIFICATION");
    }

    @ParameterizedTest
    @CsvSource({"true", "false"})
    void shouldSuccessfullyCompleteTrialReadyMultiparty(boolean twoRepresentatives) {

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
        assertCompleteExternalTask(
            startBusiness,
            START_BUSINESS_TOPIC,
            START_BUSINESS_EVENT,
            START_BUSINESS_ACTIVITY,
            variables
        );

        //complete the notification for respondent 1
        ExternalTask respondentNotification = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(respondentNotification,
                                   PROCESS_CASE_EVENT,
                                   "NOTIFY_RESPONDENT_SOLICITOR1_FOR_BUNDLE_CREATION",
                                   "BundleCreationNotifyRespondentSolicitor1"
        );

        if (twoRepresentatives) {
            //complete the notification for respondent 2
            ExternalTask respondent2Notification = assertNextExternalTask(PROCESS_CASE_EVENT);
            assertCompleteExternalTask(respondent2Notification,
                                       PROCESS_CASE_EVENT,
                                       "NOTIFY_RESPONDENT_SOLICITOR2_FOR_BUNDLE_CREATION",
                                       "BundleCreationNotifyRespondentSolicitor2"
            );
        }

        //complete the notification for applicant solicitor
        ExternalTask applicantNotification = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(applicantNotification,
                                   PROCESS_CASE_EVENT,
                                   "NOTIFY_APPLICANT_SOLICITOR1_FOR_BUNDLE_CREATION",
                                   "BundleCreationNotifyApplicantSolicitor1"
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
