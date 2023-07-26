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

class GenerateOrderNotificationTest extends BpmnBaseTest {

    public static final String MESSAGE_NAME = "GENERATE_ORDER_NOTIFICATION";
    public static final String PROCESS_ID = "GENERATE_ORDER_NOTIFICATION";

    //CCD CASE EVENT
    public static final String NOTIFY_RESPONDENT_SOLICITOR2_FOR_GENERATE_ORDER
        = "NOTIFY_RESPONDENT_SOLICITOR2_FOR_GENERATE_ORDER";
    public static final String NOTIFY_RESPONDENT_SOLICITOR1_FOR_GENERATE_ORDER
        = "NOTIFY_RESPONDENT_SOLICITOR1_FOR_GENERATE_ORDER";
    public static final String NOTIFY_APPLICANT_SOLICITOR1_FOR_GENERATE_ORDER
        = "NOTIFY_APPLICANT_SOLICITOR1_FOR_GENERATE_ORDER";

    //ACTIVITY IDs
    private static final String NOTIFY_RESPONDENT_SOLICITOR2_FOR_GENERATE_ORDER_ACTIVITY_ID
        = "GenerateOrderNotifyRespondentSolicitor2";
    private static final String NOTIFY_RESPONDENT_SOLICITOR1_FOR_GENERATE_ORDER_ACTIVITY_ID
        = "GenerateOrderNotifyRespondentSolicitor1";
    public static final String NOTIFY_APPLICANT_SOLICITOR1_FOR_GENERATE_ORDER_ACTIVITY_ID
        = "GenerateOrderNotifyApplicantSolicitor1";

    public GenerateOrderNotificationTest() {
        super("generate_order_notification.bpmn", PROCESS_ID);
    }

    @ParameterizedTest
    @CsvSource({"true", "false"})
    void shouldSuccessfullyCompleteGenerateOrderNotifications(boolean twoRepresentatives) {
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

        //complete the defendant1 notification
        notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(notificationTask, PROCESS_CASE_EVENT,
                                   NOTIFY_RESPONDENT_SOLICITOR1_FOR_GENERATE_ORDER,
                                   NOTIFY_RESPONDENT_SOLICITOR1_FOR_GENERATE_ORDER_ACTIVITY_ID,
                                   variables
        );

        if (twoRepresentatives) {
            //complete the defendant2 notification
            notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
            assertCompleteExternalTask(notificationTask, PROCESS_CASE_EVENT,
                                       NOTIFY_RESPONDENT_SOLICITOR2_FOR_GENERATE_ORDER,
                                       NOTIFY_RESPONDENT_SOLICITOR2_FOR_GENERATE_ORDER_ACTIVITY_ID,
                                       variables
            );
        }

        //complete the hearing form process
        notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(notificationTask, PROCESS_CASE_EVENT,
                                   NOTIFY_APPLICANT_SOLICITOR1_FOR_GENERATE_ORDER,
                                   NOTIFY_APPLICANT_SOLICITOR1_FOR_GENERATE_ORDER_ACTIVITY_ID,
                                   variables
        );

        //end business process
        ExternalTask endBusinessProcess = assertNextExternalTask(END_BUSINESS_PROCESS);
        completeBusinessProcess(endBusinessProcess);

        assertNoExternalTasksLeft();
    }

    @Test
    void shouldSuccessfullyCompleteGenerateOrderNotificationsLip() {
        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        VariableMap variables = Variables.createVariables();
        variables.put("flowFlags", Map.of(
            UNREPRESENTED_DEFENDANT_TWO, false));

        //complete the start business process
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertCompleteExternalTask(startBusiness, START_BUSINESS_TOPIC,
                                   START_BUSINESS_EVENT, START_BUSINESS_ACTIVITY, variables);

        ExternalTask notificationTask;

        //complete the defendant1 notification
        notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(notificationTask, PROCESS_CASE_EVENT,
                                   NOTIFY_RESPONDENT_SOLICITOR1_FOR_GENERATE_ORDER,
                                   NOTIFY_RESPONDENT_SOLICITOR1_FOR_GENERATE_ORDER_ACTIVITY_ID,
                                   variables
        );

        //complete applicant notification
        notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(notificationTask, PROCESS_CASE_EVENT,
                                   NOTIFY_APPLICANT_SOLICITOR1_FOR_GENERATE_ORDER,
                                   NOTIFY_APPLICANT_SOLICITOR1_FOR_GENERATE_ORDER_ACTIVITY_ID,
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
