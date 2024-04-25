package uk.gov.hmcts.reform.civil.bpmn;

import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;

public class ApplyNocDecisionDefendantLipTest extends BpmnBaseTest {

    public static final String MESSAGE_NAME = "APPLY_NOC_DECISION_DEFENDANT_LIP";
    public static final String PROCESS_ID = "APPLY_NOC_DECISION_DEFENDANT_LIP";

    public ApplyNocDecisionDefendantLipTest() {
        super("apply_noc_decision_defendant_lip.bpmn", PROCESS_ID);
    }

    @Test
    void shouldRunProcess() {
        //assert process has started
        assertFalse(processInstance.isEnded());
        //complete the start business process
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertCompleteExternalTask(
            startBusiness,
            START_BUSINESS_TOPIC,
            START_BUSINESS_EVENT,
            START_BUSINESS_ACTIVITY
        );

        //complete updating case details
        ExternalTask updateCaseDetails = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            updateCaseDetails,
            PROCESS_CASE_EVENT,
            "UPDATE_CASE_DETAILS_AFTER_NOC",
            "UpdateCaseDetailsAfterNoC"
        );

        //complete notify claimant
        ExternalTask notifyDefendantAfterNoc = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            notifyDefendantAfterNoc,
            PROCESS_CASE_EVENT,
            "NOTIFY_DEFENDANT_AFTER_NOC_APPROVAL",
            "NotifyDefendantLipAfterNocApproval"
        );

        //complete notify defendant
        ExternalTask notifyDefendantSolicitorAfterNoc = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            notifyDefendantSolicitorAfterNoc,
            PROCESS_CASE_EVENT,
            "NOTIFY_DEFENDANT_SOLICITOR_AFTER_NOC_APPROVAL",
            "NotifyDefendant1SolicitorRepresented"
        );

        //complete notify defendant
        ExternalTask notifyClaimant = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            notifyClaimant,
            PROCESS_CASE_EVENT,
            "NOTIFY_CLAIMANT_DEFENDANT_REPRESENTED",
            "NotifyClaimantLipDefendantRepresented"
        );

        //end business process
        ExternalTask endBusinessProcess = assertNextExternalTask(END_BUSINESS_PROCESS);
        completeBusinessProcess(endBusinessProcess);

        assertNoExternalTasksLeft();
    }
}
