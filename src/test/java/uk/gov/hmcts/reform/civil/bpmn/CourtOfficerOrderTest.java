package uk.gov.hmcts.reform.civil.bpmn;

import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class CourtOfficerOrderTest extends BpmnBaseTest {

    public static final String PROCESS_ID = "COURT_OFFICER_ORDER_ID";

    public static final String MESSAGE_NAME = "COURT_OFFICER_ORDER";
    public static final String CREATE_DASHBOARD_NOTIFICATION_COURT_OFFICER_ORDER_CLAIMANT
        = "CREATE_DASHBOARD_NOTIFICATION_COURT_OFFICER_ORDER_CLAIMANT";
    public static final String CREATE_DASHBOARD_NOTIFICATION_COURT_OFFICER_ORDER_DEFENDANT
        = "CREATE_DASHBOARD_NOTIFICATION_COURT_OFFICER_ORDER_DEFENDANT";
    public static final String CREATE_DASHBOARD_NOTIFICATION_COURT_OFFICER_ORDER_CLAIMANT_ACTIVITY_ID
        = "GenerateDashboardNotificationCOOClaimant";
    public static final String CREATE_DASHBOARD_NOTIFICATION_COURT_OFFICER_ORDER_DEFENDANT_ACTIVITY_ID
        = "GenerateDashboardNotificationCOODefendant";
    public static final String NOTIFY_RESPONDENT_SOLICITOR2_FOR_GENERATE_ORDER
        = "NOTIFY_RESPONDENT_SOLICITOR2_FOR_GENERATE_ORDER";
    public static final String NOTIFY_RESPONDENT_SOLICITOR1_FOR_GENERATE_ORDER
        = "NOTIFY_RESPONDENT_SOLICITOR1_FOR_GENERATE_ORDER";
    public static final String NOTIFY_APPLICANT_SOLICITOR1_FOR_GENERATE_ORDER
        = "NOTIFY_APPLICANT_SOLICITOR1_FOR_GENERATE_ORDER";
    private static final String NOTIFY_RESPONDENT_SOLICITOR2_FOR_GENERATE_ORDER_ACTIVITY_ID
        = "GenerateOrderNotifyRespondentSolicitor2";
    private static final String NOTIFY_RESPONDENT_SOLICITOR1_FOR_GENERATE_ORDER_ACTIVITY_ID
        = "GenerateOrderNotifyRespondentSolicitor1";
    public static final String NOTIFY_APPLICANT_SOLICITOR1_FOR_GENERATE_ORDER_ACTIVITY_ID
        = "GenerateOrderNotifyApplicantSolicitor1";
    public CourtOfficerOrderTest() {
        super("court_officer_order.bpmn", PROCESS_ID);
    }

    @ParameterizedTest
    @CsvSource({"true", "false"})
    void shouldSuccessfullyCompleteCourtOfficerOrder(boolean twoRepresentatives) {
        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        VariableMap variables = Variables.createVariables();
        variables.put("flowFlags", Map.of(
            ONE_RESPONDENT_REPRESENTATIVE, !twoRepresentatives,
            TWO_RESPONDENT_REPRESENTATIVES, twoRepresentatives,
            UNREPRESENTED_DEFENDANT_ONE, false,
            DASHBOARD_SERVICE_ENABLED, false,
            CASE_PROGRESSION_ENABLED, false
        ));

        //complete the start business process
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertCompleteExternalTask(startBusiness, START_BUSINESS_TOPIC,
                                   START_BUSINESS_EVENT, START_BUSINESS_ACTIVITY, variables
        );

        ExternalTask notificationTask;

        //complete the claimant notification
        notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(notificationTask, PROCESS_CASE_EVENT,
                                   NOTIFY_APPLICANT_SOLICITOR1_FOR_COURT_OFFICER_ORDER,
                                   NOTIFY_APPLICANT_SOLICITOR1_FOR_COURT_OFFICER_ORDER_ACTIVITY_ID,
                                   variables
        );
        if (twoRepresentatives) {
            //complete the defendant notification
            notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
            assertCompleteExternalTask(notificationTask, PROCESS_CASE_EVENT,
                                       NOTIFY_RESPONDENT_SOLICITOR2_FOR_GENERATE_ORDER,
                                       NOTIFY_RESPONDENT_SOLICITOR2_FOR_GENERATE_ORDER_ACTIVITY_ID,
                                       variables
            );
        }
        notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(notificationTask, PROCESS_CASE_EVENT,
                                   NOTIFY_RESPONDENT_SOLICITOR1_FOR_GENERATE_ORDER,
                                   NOTIFY_RESPONDENT_SOLICITOR1_FOR_GENERATE_ORDER_ACTIVITY_ID,
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
        variables.put("flowFlags", Map.of(
            UNREPRESENTED_DEFENDANT_TWO, false,
            DASHBOARD_SERVICE_ENABLED, true,
            CASE_PROGRESSION_ENABLED, true));

        //complete the start business process
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertCompleteExternalTask(startBusiness, START_BUSINESS_TOPIC,
                                   START_BUSINESS_EVENT, START_BUSINESS_ACTIVITY, variables);

        ExternalTask notificationTask;

        //complete the claimant notification
        notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(notificationTask, PROCESS_CASE_EVENT,
                                   NOTIFY_APPLICANT_SOLICITOR1_FOR_GENERATE_ORDER,
                                   NOTIFY_APPLICANT_SOLICITOR1_FOR_GENERATE_ORDER_ACTIVITY_ID,
                                   variables
        );
        notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(notificationTask, PROCESS_CASE_EVENT,
                                   NOTIFY_RESPONDENT_SOLICITOR1_FOR_GENERATE_ORDER,
                                   NOTIFY_RESPONDENT_SOLICITOR1_FOR_GENERATE_ORDER_ACTIVITY_ID,
                                   variables
        );

        //complete the hearing form process
        notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(notificationTask, PROCESS_CASE_EVENT,
                                   "NOTIFY_RESPONDENT_SOLICITOR1_FOR_GENERATE_ORDER",
                                   "GenerateOrderNotifyRespondentSolicitor1",
                                   variables
        );

        //end business process
        ExternalTask endBusinessProcess = assertNextExternalTask(END_BUSINESS_PROCESS);
        completeBusinessProcess(endBusinessProcess);

        assertNoExternalTasksLeft();
    }
}
