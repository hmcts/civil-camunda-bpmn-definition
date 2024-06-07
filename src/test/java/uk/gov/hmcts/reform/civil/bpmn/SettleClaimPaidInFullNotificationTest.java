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

class SettleClaimPaidInFullNotificationTest extends BpmnBaseTest {

    public static final String MESSAGE_NAME = "SETTLE_CLAIM_MARKED_PAID_IN_FULL_NOTIFICATION";
    public static final String PROCESS_ID = "SETTLE_CLAIM_MARKED_PAID_IN_FULL_NOTIFICATION_ID";
    public static final String NOTIFY_SOLICITOR1_DEFENDANT_SETTLE_CLAIM_MARKED_PAID_IN_FULL_EVENT_ID1 = "NOTIFY_SOLICITOR1_DEFENDANT_SETTLE_CLAIM_MARKED_PAID_IN_FULL";
    public static final String NOTIFY_SOLICITOR1_DEFENDANT_SETTLE_CLAIM_MARKED_PAID_IN_FULL_ACTIVITY_ID = "NotifyDefendantSettleClaimMarkedPaidInFull1";
    public static final String NOTIFY_SOLICITOR2_DEFENDANT_SETTLE_CLAIM_MARKED_PAID_IN_FULL_EVENT_ID2 = "NOTIFY_SOLICITOR2_DEFENDANT_SETTLE_CLAIM_MARKED_PAID_IN_FULL";
    public static final String NOTIFY_SOLICITOR2_DEFENDANT_SETTLE_CLAIM_MARKED_PAID_IN_FULL_ACTIVITY_ID = "NotifyDefendantSettleClaimMarkedPaidInFull2";

    public SettleClaimPaidInFullNotificationTest() {
        super("settle_claim_paid_in_full_notification.bpmn", PROCESS_ID);
    }

    @ParameterizedTest
    @CsvSource({
        "true, true, true",
        "true, true, false",
        "true, false, true",
        "true, false, false",
        "false, true, true",
        "false, true, false",
        "false, false, true",
        "false, false, false"
    })
    void shouldSuccessfullyComplete(boolean twoRepresentatives, boolean isLiPDefendant, boolean isLiPDefendant2) {

        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        VariableMap variables = Variables.createVariables();
        variables.put(FLOW_FLAGS, Map.of(
            ONE_RESPONDENT_REPRESENTATIVE, !twoRepresentatives,
            TWO_RESPONDENT_REPRESENTATIVES, twoRepresentatives,
            UNREPRESENTED_DEFENDANT_ONE, isLiPDefendant,
            UNREPRESENTED_DEFENDANT_TWO, isLiPDefendant2
        ));

        //complete the start business process
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertCompleteExternalTask(
            startBusiness,
            START_BUSINESS_TOPIC,
            START_BUSINESS_EVENT,
            START_BUSINESS_ACTIVITY,
            variables
        );

        if (!isLiPDefendant) {
            //complete the notification to Respondent
            ExternalTask dashboardDefendant = assertNextExternalTask(PROCESS_CASE_EVENT);
            assertCompleteExternalTask(
                dashboardDefendant,
                PROCESS_CASE_EVENT,
                NOTIFY_SOLICITOR1_DEFENDANT_SETTLE_CLAIM_MARKED_PAID_IN_FULL_EVENT_ID1,
                NOTIFY_SOLICITOR1_DEFENDANT_SETTLE_CLAIM_MARKED_PAID_IN_FULL_ACTIVITY_ID,
                variables
            );

        }

        if (twoRepresentatives || (isLiPDefendant && !isLiPDefendant2)) {
            //complete the notification to Respondent2
            ExternalTask respondent2Notification = assertNextExternalTask(PROCESS_CASE_EVENT);
            assertCompleteExternalTask(
                respondent2Notification,
                PROCESS_CASE_EVENT,
                NOTIFY_SOLICITOR2_DEFENDANT_SETTLE_CLAIM_MARKED_PAID_IN_FULL_EVENT_ID2,
                NOTIFY_SOLICITOR2_DEFENDANT_SETTLE_CLAIM_MARKED_PAID_IN_FULL_ACTIVITY_ID,
                variables
            );
        }

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
