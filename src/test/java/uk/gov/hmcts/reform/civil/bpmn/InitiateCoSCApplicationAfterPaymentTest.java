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
import static uk.gov.hmcts.reform.civil.bpmn.BpmnBaseTest.DASHBOARD_NOTIFICATION_EVENT;
import static uk.gov.hmcts.reform.civil.bpmn.BpmnBaseTest.IS_JO_LIVE_FEED_ACTIVE;
import static uk.gov.hmcts.reform.civil.bpmn.BpmnBaseTest.PROCESS_CASE_EVENT;

class InitiateCoSCApplicationAfterPaymentTest extends BpmnBaseGAAfterPaymentTest {

    public static final String MESSAGE_NAME = "INITIATE_COSC_APPLICATION_AFTER_PAYMENT";
    public static final String PROCESS_ID = "COSC_INITIATE_AFTER_PAYMENT_PROCESS_ID";
    private static final String CHECK_PAID_IN_FULL_SCHED_DEADLINE = "CHECK_PAID_IN_FULL_SCHED_DEADLINE";
    private static final String CHECK_PAID_IN_FULL_SCHED_DEADLINE_ACTIVITY_ID = "CheckMarkPaidInFullAndAddSchedulerDeadline";
    public static final String APPLICATION_PROCESS_EVENT_GASPEC = "coscApplicationAfterPayment";
    private static final String GENERATE_COSC_DOCUMENT = "GENERATE_COSC_DOCUMENT";
    private static final String GENERATE_COSC_DOCUMENT_ACTIVITY_ID = "GenerateCoscDocument";
    private static final String NOTIFY_APPLICANT_SOLICITOR1_FOR_PAID_IN_FULL_COSC = "NOTIFY_APPLICANT_SOLICITOR1_FOR_PAID_IN_FULL_COSC";
    private static final String NOTIFY_APPLICANT_SOLICITOR1_FOR_PAID_IN_FULL_COSC_ACTIVITY_ID = "NotifyApplicantSolicitorCoscApplication";
    private static final String NOTIFY_RPA = "NOTIFY_RPA_ON_CONTINUOUS_FEED";
    private static final String NOTIFY_RPA_ACTIVITY_ID = "NotifyRPA";
    private static final String GENERATE_DASHBOARD_NOTIFICATIONS_INITIATE_COSC = "GenerateDashboardNotificationsInitiateCOSC";

    public InitiateCoSCApplicationAfterPaymentTest() {
        super("initiate_cosc_application_after_payment.bpmn", PROCESS_ID);
    }

    @ParameterizedTest
    @CsvSource({
        "false,false,null",
        "false,true,null",
        "true,null,true",
        "true,null,false",
    })
    void shouldSuccessfullyComplete_whenCalled(Boolean isJudgmentMarkedPaidInFull, Boolean isClaimantLR, Boolean joFlag) {
        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        VariableMap variables = Variables.createVariables();
        variables.put("flowFlags", Map.of(
            IS_JO_LIVE_FEED_ACTIVE, joFlag
        ));
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
            }
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
        }

        if (joFlag) {
            ExternalTask notificationTask = assertNextExternalTask(APPLICATION_PROCESS_EVENT_GASPEC);
            assertCompleteExternalTask(
                notificationTask,
                APPLICATION_PROCESS_EVENT_GASPEC,
                NOTIFY_RPA,
                NOTIFY_RPA_ACTIVITY_ID,
                variables
            );
        }

        //complete the dashboard notifications
        ExternalTask notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(notificationTask, PROCESS_CASE_EVENT,
                                   DASHBOARD_NOTIFICATION_EVENT,
                                   GENERATE_DASHBOARD_NOTIFICATIONS_INITIATE_COSC,
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
