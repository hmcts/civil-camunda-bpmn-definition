package uk.gov.hmcts.reform.civil.bpmn;

import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;

public class ApplyNocDecisionLipTest  extends BpmnBaseTest {

    public static final String MESSAGE_NAME = "APPLY_NOC_DECISION_LIP";
    public static final String PROCESS_ID = "APPLY_NOC_DECISION_LIP";

    public ApplyNocDecisionLipTest() {
        super("apply_noc_decision_lip.bpmn", PROCESS_ID);
    }

    @ParameterizedTest
    @CsvSource({"true", "false"})
    void shouldRunProcess(boolean welshEnabled) {
        VariableMap variables = Variables.createVariables();
        variables.put(FLOW_FLAGS, Map.of(
            WELSH_ENABLED, welshEnabled
        ));

        //assert process has started
        assertFalse(processInstance.isEnded());
        //complete the start business process
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertCompleteExternalTask(
            startBusiness,
            START_BUSINESS_TOPIC,
            START_BUSINESS_EVENT,
            START_BUSINESS_ACTIVITY,
            variables
        );

        //complete updating case details
        ExternalTask updateCaseDetails = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            updateCaseDetails,
            PROCESS_CASE_EVENT,
            "UPDATE_CASE_DETAILS_AFTER_NOC",
            "UpdateCaseDetailsAfterNoC"
        );

        if (welshEnabled) {
            //update GA language flag
            ExternalTask updateGeneralApps = assertNextExternalTask(PROCESS_CASE_EVENT);
            assertCompleteExternalTask(
                updateGeneralApps,
                PROCESS_CASE_EVENT,
                "UPDATE_GA_LANGUAGE_PREFERENCE",
                "UpdateGenAppLanguagePreference"
            );
        }

        //complete notify claimant
        ExternalTask notifyClaimantAfterNoc = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            notifyClaimantAfterNoc,
            PROCESS_CASE_EVENT,
            "NOTIFY_CLAIMANT_LIP_AFTER_NOC_APPROVAL",
            "NotifyClaimantLipAfterNocApproval"
        );

        //complete notify defendant
        ExternalTask notifyDefendantAfterNoc = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            notifyDefendantAfterNoc,
            PROCESS_CASE_EVENT,
            "NOTIFY_DEFENDANT_LIP_CLAIMANT_REPRESENTED",
            "NotifyDefendantLipClaimantRepresented"
        );

        //complete notify defendant
        ExternalTask notifyClaimantSolicitor = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
                notifyDefendantAfterNoc,
                PROCESS_CASE_EVENT,
                "NOTIFY_APPLICANT_LIP_SOLICITOR",
                "NotifyApplicantLipSolicitor"
        );

        //end business process
        ExternalTask endBusinessProcess = assertNextExternalTask(END_BUSINESS_PROCESS);
        completeBusinessProcess(endBusinessProcess);

        assertNoExternalTasksLeft();
    }
}
