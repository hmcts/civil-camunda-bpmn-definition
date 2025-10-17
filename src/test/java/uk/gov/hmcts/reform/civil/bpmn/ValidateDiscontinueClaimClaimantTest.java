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

public class ValidateDiscontinueClaimClaimantTest extends BpmnBaseTest {

    public static final String MESSAGE_NAME = "VALIDATE_DISCONTINUE_CLAIM_CLAIMANT";
    public static final String PROCESS_ID = "VALIDATE_DISCONTINUE_CLAIM_CLAIMANT";

    //CCD CASE EVENTs
    public static final String UPDATE_VISIBILITY_NOTICE_OF_DISCONTINUANCE
        = "UPDATE_VISIBILITY_NOTICE_OF_DISCONTINUANCE";
    public static final String SEND_DISCONTINUANCE_LETTER_LIP_DEFENDANT1 = "SEND_DISCONTINUANCE_LETTER_LIP_DEFENDANT1";
    public static final String CREATE_DEFENDANT_DASHBOARD_NOTIFICATION_FOR_DISCONTINUANCE = "CREATE_DEFENDANT_DASHBOARD_NOTIFICATION_FOR_DISCONTINUANCE";
    public static final String NOTIFY_EVENT = "NOTIFY_EVENT";

    //ACTIVITY IDs
    public static final String UPDATE_VISIBILITY_NOTICE_OF_DISCONTINUANCE_ACTIVITY_ID
        = "UpdateVisibilityNoticeOfDiscontinuance";
    public static final String SEND_DISCONTINUANCE_LETTER_LIP_DEFENDANT1_ACTIVITY_ID = "PostNoticeOfDiscontinuanceDefendant1LiP";
    public static final String NOTIFY_DISCONTINUANCE_PARTIES_ACTIVITY_ID = "DiscontinuanceClaimNotifyParties";
    public static final String DEFENDANT_LIP_DASHBOARD_NOTIFICATION_FOR_DISCONTINUANCE_ACTIVITY_ID = "CreateDefendantDashboardNotificationsForDiscontinuance";

    public ValidateDiscontinueClaimClaimantTest() {
        super("validate_discontinue_claim_claimant.bpmn", PROCESS_ID);
    }

    @ParameterizedTest
    @CsvSource({
        "false, false, false, false",
        "true, false, false, false",
        "true, true, true, false",
        "true, true, true, true"
    })
    void shouldSuccessfullyComplete(boolean discontinuanceValidationSuccess, boolean unrepresentedDefendant1,
                                    boolean twoDefendants, boolean unrepresentedDefendant2) {

        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        VariableMap variables = Variables.createVariables();
        variables.put("discontinuanceValidationSuccess", discontinuanceValidationSuccess);
        variables.put(FLOW_FLAGS, Map.of(UNREPRESENTED_DEFENDANT_ONE, unrepresentedDefendant1,
                                         TWO_RESPONDENT_REPRESENTATIVES, twoDefendants,
                                         UNREPRESENTED_DEFENDANT_TWO, unrepresentedDefendant2));

        //complete the start business process
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertCompleteExternalTask(
            startBusiness,
            START_BUSINESS_TOPIC,
            START_BUSINESS_EVENT,
            START_BUSINESS_ACTIVITY,
            variables
        );

        //complete Notify Discontinuance parties
        ExternalTask claimant1Notification = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            claimant1Notification,
            PROCESS_CASE_EVENT,
            "NOTIFY_EVENT",
            NOTIFY_DISCONTINUANCE_PARTIES_ACTIVITY_ID,
            variables
        );

        //complete update visibility notice of discontinuance
        ExternalTask updateVisibilityTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            updateVisibilityTask,
            PROCESS_CASE_EVENT,
            UPDATE_VISIBILITY_NOTICE_OF_DISCONTINUANCE,
            UPDATE_VISIBILITY_NOTICE_OF_DISCONTINUANCE_ACTIVITY_ID,
            variables
        );

        if (discontinuanceValidationSuccess && unrepresentedDefendant1) {
            //complete Post Notice of Discontinuance Defendant 1 LiP
            ExternalTask postNoticeDiscontinuanceTask = assertNextExternalTask(PROCESS_CASE_EVENT);
            assertCompleteExternalTask(
                postNoticeDiscontinuanceTask,
                PROCESS_CASE_EVENT,
                SEND_DISCONTINUANCE_LETTER_LIP_DEFENDANT1,
                SEND_DISCONTINUANCE_LETTER_LIP_DEFENDANT1_ACTIVITY_ID,
                variables
            );

            //complete the dashboard notification to Defendant 1 LIP
            ExternalTask dashboardNotification = assertNextExternalTask(PROCESS_CASE_EVENT);
            assertCompleteExternalTask(
                dashboardNotification,
                PROCESS_CASE_EVENT,
                CREATE_DEFENDANT_DASHBOARD_NOTIFICATION_FOR_DISCONTINUANCE,
                DEFENDANT_LIP_DASHBOARD_NOTIFICATION_FOR_DISCONTINUANCE_ACTIVITY_ID,
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
