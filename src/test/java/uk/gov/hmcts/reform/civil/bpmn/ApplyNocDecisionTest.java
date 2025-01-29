package uk.gov.hmcts.reform.civil.bpmn;

import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class ApplyNocDecisionTest extends BpmnBaseTest {

    public static final String MESSAGE_NAME = "APPLY_NOC_DECISION";
    public static final String PROCESS_ID = "APPLY_NOC_DECISION";

    //CCD CASE EVENTS
    private static final String NOTIFY_FORMER_SOLICITOR = "NOTIFY_FORMER_SOLICITOR";
    private static final String NOTIFY_OTHER_SOLICITOR_1 = "NOTIFY_OTHER_SOLICITOR_1";
    private static final String NOTIFY_OTHER_SOLICITOR_2 = "NOTIFY_OTHER_SOLICITOR_2";
    public static final String NOTIFY_APPLICANT_SOLICITOR_FOR_HEARING_FEE_AFTER_NOC
        = "NOTIFY_APPLICANT_SOLICITOR_FOR_HEARING_FEE_AFTER_NOC";

    //ACTIVITY IDs
    private static final String TASK_ID_NOTIFY_FORMER_SOLICITOR = "NotifyFormerSolicitor";
    private static final String TASK_ID_NOTIFY_OTHER_SOLICITOR_1 = "NotifyOtherSolicitor1";
    private static final String TASK_ID_NOTIFY_OTHER_SOLICITOR_2 = "NotifyOtherSolicitor2";
    private static final String TASK_ID_NOTIFY_CLAIMANT_UNPAID_FEE = "HearingFeeDueNotifyApplicantSolicitorAfterNoc";

    enum FlowState {
        IN_HEARING_READINESS,
        CLAIM_NOTIFIED;

        public String fullName() {
            return "MAIN" + "." + name();
        }
    }

    public ApplyNocDecisionTest() {
        super("apply_noc_decision.bpmn", PROCESS_ID);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldSuccessfullyCompleteEventsAfterNoticeOfChange(boolean is1v2) {
        //assert process has started
        assertFalse(processInstance.isEnded());
        VariableMap variables = Variables.createVariables();
        variables.putValue(FLOW_FLAGS, Map.of(TWO_RESPONDENT_REPRESENTATIVES, is1v2,
                                              ONE_RESPONDENT_REPRESENTATIVE, !is1v2));
        variables.putValue(FLOW_STATE, FlowState.CLAIM_NOTIFIED.fullName());

        //complete the start business process
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertCompleteExternalTask(startBusiness, START_BUSINESS_TOPIC, START_BUSINESS_EVENT, START_BUSINESS_ACTIVITY);

        //complete updating case details
        ExternalTask updateCaseDetails = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            updateCaseDetails,
            PROCESS_CASE_EVENT,
            "UPDATE_CASE_DETAILS_AFTER_NOC",
            "UpdateCaseDetailsAfterNoC"
        );

        //complete notify former solicitor
        ExternalTask notifyFormerSol = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(notifyFormerSol,
                                   PROCESS_CASE_EVENT,
                                   NOTIFY_FORMER_SOLICITOR,
                                   TASK_ID_NOTIFY_FORMER_SOLICITOR,
                                   variables);

        //complete notify other solicitor 1
        ExternalTask notifyOtherSol1 = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(notifyOtherSol1,
                                   PROCESS_CASE_EVENT,
                                   NOTIFY_OTHER_SOLICITOR_1,
                                   TASK_ID_NOTIFY_OTHER_SOLICITOR_1,
                                   variables);

        if (is1v2) {
            //complete notify other solicitor 2
            ExternalTask notifyOtherSol2 = assertNextExternalTask(PROCESS_CASE_EVENT);
            assertCompleteExternalTask(notifyOtherSol2,
                                       PROCESS_CASE_EVENT,
                                       NOTIFY_OTHER_SOLICITOR_2,
                                       TASK_ID_NOTIFY_OTHER_SOLICITOR_2,
                                       variables);
        }

        //end business process
        ExternalTask endBusinessProcess = assertNextExternalTask(END_BUSINESS_PROCESS);
        completeBusinessProcess(endBusinessProcess);

        assertNoExternalTasksLeft();
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldSuccessfullyCompleteEventsAfterNoticeOfChangeWhenInHearingReadiness(boolean is1v2) {
        //assert process has started
        assertFalse(processInstance.isEnded());
        VariableMap variables = Variables.createVariables();
        variables.putValue(FLOW_FLAGS, Map.of(TWO_RESPONDENT_REPRESENTATIVES, is1v2,
                                              ONE_RESPONDENT_REPRESENTATIVE, !is1v2));
        variables.putValue(FLOW_STATE, FlowState.IN_HEARING_READINESS.fullName());

        //complete the start business process
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertCompleteExternalTask(startBusiness, START_BUSINESS_TOPIC, START_BUSINESS_EVENT, START_BUSINESS_ACTIVITY);

        //complete updating case details
        ExternalTask updateCaseDetails = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            updateCaseDetails,
            PROCESS_CASE_EVENT,
            "UPDATE_CASE_DETAILS_AFTER_NOC",
            "UpdateCaseDetailsAfterNoC"
        );

        //complete notify former solicitor
        ExternalTask notifyFormerSol = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(notifyFormerSol,
                                   PROCESS_CASE_EVENT,
                                   NOTIFY_FORMER_SOLICITOR,
                                   TASK_ID_NOTIFY_FORMER_SOLICITOR,
                                   variables);

        //complete notify other solicitor 1
        ExternalTask notifyOtherSol1 = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(notifyOtherSol1,
                                   PROCESS_CASE_EVENT,
                                   NOTIFY_OTHER_SOLICITOR_1,
                                   TASK_ID_NOTIFY_OTHER_SOLICITOR_1,
                                   variables);

        if (is1v2) {
            //complete notify other solicitor 2
            ExternalTask notifyOtherSol2 = assertNextExternalTask(PROCESS_CASE_EVENT);
            assertCompleteExternalTask(notifyOtherSol2,
                                       PROCESS_CASE_EVENT,
                                       NOTIFY_OTHER_SOLICITOR_2,
                                       TASK_ID_NOTIFY_OTHER_SOLICITOR_2,
                                       variables);
        }

        //complete  notify claimant fee unpaid process
        ExternalTask notifyUnpaid = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(notifyUnpaid,
                                   PROCESS_CASE_EVENT,
                                   NOTIFY_APPLICANT_SOLICITOR_FOR_HEARING_FEE_AFTER_NOC,
                                   TASK_ID_NOTIFY_CLAIMANT_UNPAID_FEE,
                                   variables);

        //end business process
        ExternalTask endBusinessProcess = assertNextExternalTask(END_BUSINESS_PROCESS);
        completeBusinessProcess(endBusinessProcess);

        assertNoExternalTasksLeft();
    }

    @Test
    void shouldSucessfullyTriggerNotificationForNewDefendantLRForApplicantLip(){
        //assert process has started
        assertFalse(processInstance.isEnded());
        VariableMap variables = Variables.createVariables();
        variables.putValue(FLOW_FLAGS, Map.of("DEFENDANT_NOC_ONLINE",true,
                                              ONE_RESPONDENT_REPRESENTATIVE, true));
        variables.putValue(FLOW_STATE, FlowState.IN_HEARING_READINESS.fullName());

        //complete the start business process
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertCompleteExternalTask(startBusiness, START_BUSINESS_TOPIC, START_BUSINESS_EVENT, START_BUSINESS_ACTIVITY);

        //complete updating case details
        ExternalTask updateCaseDetails = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            updateCaseDetails,
            PROCESS_CASE_EVENT,
            "UPDATE_CASE_DETAILS_AFTER_NOC",
            "UpdateCaseDetailsAfterNoC"
        );

        //complete notify former solicitor
        ExternalTask notifyFormerSol = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(notifyFormerSol,
                                   PROCESS_CASE_EVENT,
                                   NOTIFY_FORMER_SOLICITOR,
                                   TASK_ID_NOTIFY_FORMER_SOLICITOR,
                                   variables);

        //complete notify other solicitor 1
        ExternalTask notifyOtherSol1 = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(notifyOtherSol1,
                                   PROCESS_CASE_EVENT,
                                   NOTIFY_OTHER_SOLICITOR_1,
                                   TASK_ID_NOTIFY_OTHER_SOLICITOR_1,
                                   variables);

        //complete notify other solicitor 1
        ExternalTask notifyNewDefendantSol = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(notifyNewDefendantSol,
                                   PROCESS_CASE_EVENT,
                                   "NOTIFY_NEW_DEFENDANT_SOLICITOR",
                                   "NotifyNewDefendantSolicitor1",
                                   variables);

        //complete  notify claimant fee unpaid process
        ExternalTask notifyUnpaid = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(notifyUnpaid,
                                   PROCESS_CASE_EVENT,
                                   NOTIFY_APPLICANT_SOLICITOR_FOR_HEARING_FEE_AFTER_NOC,
                                   TASK_ID_NOTIFY_CLAIMANT_UNPAID_FEE,
                                   variables);

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
