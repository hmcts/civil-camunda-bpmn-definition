package uk.gov.hmcts.reform.civil.bpmn;

import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import java.util.Map;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

class InitiateCoSCApplicationAfterPaymentTest extends BpmnBaseGAAfterPaymentTest {

    public static final String MESSAGE_NAME = "INITIATE_COSC_APPLICATION_AFTER_PAYMENT";
    public static final String PROCESS_ID = "COSC_INITIATE_AFTER_PAYMENT_PROCESS_ID";
    private static final String CHECK_PAID_IN_FULL_SCHED_DEADLINE = "CHECK_PAID_IN_FULL_SCHED_DEADLINE";
    private static final String CHECK_PAID_IN_FULL_SCHED_DEADLINE_ACTIVITY_ID = "CheckMarkPaidInFullAndAddSchedulerDeadline";
    private static final String CREATE_DASHBOARD_NOTIFICATION_COSC_NOT_PAID_FULL_CLAIMANT = "CREATE_DASHBOARD_NOTIFICATION_COSC_NOT_PAID_FULL_CLAIMANT";
    private static final String CREATE_DASHBOARD_NOTIFICATION_COSC_NOT_PAID_FULL_CLAIMANT_ACTIVITY_ID = "ClaimantDashboardNotificationMarkNotPaidInFull";
    public static final String APPLICATION_PROCESS_EVENT_GASPEC = "coscApplicationAfterPayment";
    private static final String CREATE_DASHBOARD_NOTIFICATION_COSC_NOT_PAID_FULL_DEFENDANT = "CREATE_DASHBOARD_NOTIFICATION_COSC_NOT_PAID_FULL_DEFENDANT";
    private static final String CREATE_DASHBOARD_NOTIFICATION_COSC_NOT_PAID_FULL_DEFENDANT_ACTIVITY_ID = "DefendantDashboardNotificationMarkNotPaidInFull";
    private static final String GENERATE_COSC_DOCUMENT = "GENERATE_COSC_DOCUMENT";
    private static final String GENERATE_COSC_DOCUMENT_ACTIVITY_ID = "GenerateCoscDocument";
    private static final String CREATE_DASHBOARD_NOTIFICATION_COSC_GEN_FOR_DEFENDANT = "CREATE_DASHBOARD_NOTIFICATION_COSC_GEN_FOR_DEFENDANT";
    private static final String CREATE_DASHBOARD_NOTIFICATION_COSC_GEN_FOR_DEFENDANT_ACTIVITY_ID = "DefendantDashboardNotificationCertificateGenerated";
    private static final String NOTIFY_APPLICANT_SOLICITOR1_FOR_PAID_IN_FULL_COSC = "NOTIFY_APPLICANT_SOLICITOR1_FOR_PAID_IN_FULL_COSC";
    private static final String NOTIFY_APPLICANT_SOLICITOR1_FOR_PAID_IN_FULL_COSC_ACTIVITY_ID = "NotifyApplicantSolicitorCoscApplication";

    public InitiateCoSCApplicationAfterPaymentTest() {
        super("initiate_cosc_application_after_payment.bpmn", PROCESS_ID);
    }

    @ParameterizedTest
    @CsvSource({"false,false", "false,true", "true,null"})
    void shouldSuccessfullyComplete_whenCalled(Boolean isJudgmentMarkedPaidInFull, Boolean isClaimantLR) {
        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        VariableMap variables = Variables.createVariables();
        variables.put("flowFlags", Map.of());
        variables.put("isJudgmentMarkedPaidInFull", isJudgmentMarkedPaidInFull);
        variables.put("isClaimantLR", isClaimantLR);

        //complete the start business process
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertCompleteExternalTask(
            startBusiness,
            START_BUSINESS_TOPIC,
            START_BUSINESS_EVENT,
            START_BUSINESS_ACTIVITY,
            variables
        );

        //complete Check Mark paid in full and add scheduler deadline
        ExternalTask checkMarkPaidInFull = assertNextExternalTask(APPLICATION_PROCESS_EVENT_GASPEC);
        assertCompleteExternalTask(
            checkMarkPaidInFull,
            APPLICATION_PROCESS_EVENT_GASPEC,
            CHECK_PAID_IN_FULL_SCHED_DEADLINE,
            CHECK_PAID_IN_FULL_SCHED_DEADLINE_ACTIVITY_ID,
            variables
        );

        //complete the Defendant notification
        if (!isJudgmentMarkedPaidInFull) {
            if (isClaimantLR) {
                // email notification for claimant
                ExternalTask claimantNotificationTask = assertNextExternalTask(APPLICATION_PROCESS_EVENT_GASPEC);
                assertCompleteExternalTask(
                    claimantNotificationTask,
                    APPLICATION_PROCESS_EVENT_GASPEC,
                    NOTIFY_APPLICANT_SOLICITOR1_FOR_PAID_IN_FULL_COSC,
                    NOTIFY_APPLICANT_SOLICITOR1_FOR_PAID_IN_FULL_COSC_ACTIVITY_ID,
                    variables
                );
            } else {
                // dashboard notification for claimant
                ExternalTask claimantNotificationTask = assertNextExternalTask(APPLICATION_PROCESS_EVENT_GASPEC);
                assertCompleteExternalTask(
                    claimantNotificationTask,
                    APPLICATION_PROCESS_EVENT_GASPEC,
                    CREATE_DASHBOARD_NOTIFICATION_COSC_NOT_PAID_FULL_CLAIMANT,
                    CREATE_DASHBOARD_NOTIFICATION_COSC_NOT_PAID_FULL_CLAIMANT_ACTIVITY_ID,
                    variables
                );
            }

            ExternalTask notificationTask = assertNextExternalTask(APPLICATION_PROCESS_EVENT_GASPEC);
            assertCompleteExternalTask(
                notificationTask,
                APPLICATION_PROCESS_EVENT_GASPEC,
                CREATE_DASHBOARD_NOTIFICATION_COSC_NOT_PAID_FULL_DEFENDANT,
                CREATE_DASHBOARD_NOTIFICATION_COSC_NOT_PAID_FULL_DEFENDANT_ACTIVITY_ID,
                variables
            );
        }

        //complete the CC notification
        if (isJudgmentMarkedPaidInFull) {
            //complete generate Document
            ExternalTask generateDoc = assertNextExternalTask(APPLICATION_PROCESS_EVENT_GASPEC);
            assertCompleteExternalTask(
                generateDoc,
                APPLICATION_PROCESS_EVENT_GASPEC,
                GENERATE_COSC_DOCUMENT,
                GENERATE_COSC_DOCUMENT_ACTIVITY_ID,
                variables
            );

            //complete the Defendant notification for certification generated
            ExternalTask generateDocNotification = assertNextExternalTask(APPLICATION_PROCESS_EVENT_GASPEC);
            assertCompleteExternalTask(
                generateDocNotification,
                APPLICATION_PROCESS_EVENT_GASPEC,
                CREATE_DASHBOARD_NOTIFICATION_COSC_GEN_FOR_DEFENDANT,
                CREATE_DASHBOARD_NOTIFICATION_COSC_GEN_FOR_DEFENDANT_ACTIVITY_ID,
                variables
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
