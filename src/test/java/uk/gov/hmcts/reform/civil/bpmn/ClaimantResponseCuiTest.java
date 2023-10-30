package uk.gov.hmcts.reform.civil.bpmn;

import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class ClaimantResponseCuiTest extends BpmnBaseTest {

    private static final String MESSAGE_NAME = "CLAIMANT_RESPONSE_CUI";
    private static final String PROCESS_ID = "CLAIMANT_RESPONSE_CUI_PROCESS_ID";
    private static final String JUDICIAL_REFERRAL_EVENT = "JUDICIAL_REFERRAL";
    private static final String JUDICIAL_REFERRAL_ACTIVITY_ID = "JudicialReferral";
    //CCD Case Event
    private static final String NOTIFY_LIP_RESPONDENT_CLAIMANT_CONFIRM_TO_PROCEED
        = "NOTIFY_LIP_RESPONDENT_CLAIMANT_CONFIRM_TO_PROCEED";

    private static final String NOTIFY_LIP_APPLICANT_CLAIMANT_CONFIRM_TO_PROCEED
        = "NOTIFY_LIP_APPLICANT_CLAIMANT_CONFIRM_TO_PROCEED";

    //Activity IDs
    private static final String NOTIFY_LIP_RESPONDENT_CLAIMANT_CONFIRM_TO_PROCEED_ACTIVITY_ID
        = "NotifyLiPRespondentClaimantConfirmToProceed";

    private static final String NOTIFY_LIP_APPLICANT_CLAIMANT_CONFIRM_TO_PROCEED_ACTIVITY_ID
        = "NotifyLiPApplicantClaimantConfirmToProceed";

    public ClaimantResponseCuiTest() {
        super(
            "claimant_response_cui.bpmn",
            "CLAIMANT_RESPONSE_CUI_PROCESS_ID"
        );
    }

    @Test
    void shouldRunProcess() {

        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        VariableMap variables = Variables.createVariables();
        variables.putValue("flowState", "MAIN.PART_ADMIT_NOT_SETTLED_NO_MEDIATION");
        variables.put(FLOW_FLAGS, Map.of(
            ONE_RESPONDENT_REPRESENTATIVE, true,
            TWO_RESPONDENT_REPRESENTATIVES, false,
            GENERAL_APPLICATION_ENABLED, true,
            IS_MULTI_TRACK, true
        ));
        assertCompleteExternalTask(
            startBusiness,
            START_BUSINESS_TOPIC,
            START_BUSINESS_EVENT,
            START_BUSINESS_ACTIVITY,
            variables
        );
        ExternalTask judicialReferral = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            judicialReferral,
            PROCESS_CASE_EVENT,
            JUDICIAL_REFERRAL_EVENT,
            JUDICIAL_REFERRAL_ACTIVITY_ID,
            variables
        );

        notifyRespondentClaimantConfirmsToProceed();
        notifyApplicantClaimantConfirmsToProceed();
        endBusinessProcess();
        assertNoExternalTasksLeft();
    }

    @Test
    void shouldRunProcess_ClaimIsInMediation() {

        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        VariableMap variables = Variables.createVariables();
        variables.putValue("flowState", "MAIN.IN_MEDIATION");
        assertCompleteExternalTask(
            startBusiness,
            START_BUSINESS_TOPIC,
            START_BUSINESS_EVENT,
            START_BUSINESS_ACTIVITY,
            variables
        );

        notifyRespondentClaimantConfirmsToProceed();
        notifyApplicantClaimantConfirmsToProceed();
        endBusinessProcess();
        assertNoExternalTasksLeft();
    }

    private void notifyRespondentClaimantConfirmsToProceed() {
        ExternalTask notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            notificationTask,
            PROCESS_CASE_EVENT,
            NOTIFY_LIP_RESPONDENT_CLAIMANT_CONFIRM_TO_PROCEED,
            NOTIFY_LIP_RESPONDENT_CLAIMANT_CONFIRM_TO_PROCEED_ACTIVITY_ID
        );
    }

    private void notifyApplicantClaimantConfirmsToProceed() {
        ExternalTask notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            notificationTask,
            PROCESS_CASE_EVENT,
            NOTIFY_LIP_APPLICANT_CLAIMANT_CONFIRM_TO_PROCEED,
            NOTIFY_LIP_APPLICANT_CLAIMANT_CONFIRM_TO_PROCEED_ACTIVITY_ID
        );
    }

    private void endBusinessProcess() {
        ExternalTask endBusinessProcess = assertNextExternalTask(END_BUSINESS_PROCESS);
        completeBusinessProcess(endBusinessProcess);
    }

}
