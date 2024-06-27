package uk.gov.hmcts.reform.civil.bpmn;

import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;

public class ApplyNocDecisionDefendantLipTest extends BpmnBaseTest {

    public static final String MESSAGE_NAME = "APPLY_NOC_DECISION_DEFENDANT_LIP";
    public static final String PROCESS_ID = "APPLY_NOC_DECISION_DEFENDANT_LIP";
    private static final String FLOW_FLAGS = "flowFlags";
    private static final String NOTIFY_RPA_ON_CASE_HANDED_OFFLINE
        = "NOTIFY_RPA_ON_CASE_HANDED_OFFLINE";
    private static final String NOTIFY_RPA_ON_CASE_HANDED_OFFLINE_ACTIVITY_ID = "NotifyRoboticsOnCaseHandedOffline";
    private static final String PROCEEDS_IN_HERITAGE_SYSTEM_ACTIVITY_ID = "ProceedOffline";
    private static final String PROCEEDS_IN_HERITAGE_SYSTEM = "PROCEEDS_IN_HERITAGE_SYSTEM";
    private static final String CREATE_CLAIMANT_DASHBOARD_NOTIFICATION_FOR_DEFENDANT_NOC
        = "CREATE_CLAIMANT_DASHBOARD_NOTIFICATION_FOR_DEFENDANT_NOC";
    private static final String CREATE_CLAIMANT_DASHBOARD_NOTIFICATION_FOR_DEFENDANT_NOC_ACTIVITY_ID
        = "CreateClaimantDashboardNotificationDefendantNoc";

    public ApplyNocDecisionDefendantLipTest() {
        super("apply_noc_decision_defendant_lip.bpmn", PROCESS_ID);
    }

    @Test
    void shouldRunProcess() {

        VariableMap variables = Variables.createVariables();
        variables.put(FLOW_FLAGS, Map.of(
            LIP_CASE, false));

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

    @Test
    void shouldRunProcessIfClaimantIsNotRepresented() {

        VariableMap variables = Variables.createVariables();
        variables.put(FLOW_FLAGS, Map.of(
            LIP_CASE, true));

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

        //Proceed Offline
        ExternalTask proceedOfflineTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            proceedOfflineTask,
            PROCESS_CASE_EVENT,
            PROCEEDS_IN_HERITAGE_SYSTEM,
            PROCEEDS_IN_HERITAGE_SYSTEM_ACTIVITY_ID
        );

        //complete the Robotics notification
        ExternalTask dashboardNotificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            dashboardNotificationTask,
            PROCESS_CASE_EVENT,
            CREATE_CLAIMANT_DASHBOARD_NOTIFICATION_FOR_DEFENDANT_NOC,
            CREATE_CLAIMANT_DASHBOARD_NOTIFICATION_FOR_DEFENDANT_NOC_ACTIVITY_ID
        );

        //complete the Robotics notification
        ExternalTask forRobotics = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            forRobotics,
            PROCESS_CASE_EVENT,
            NOTIFY_RPA_ON_CASE_HANDED_OFFLINE,
            NOTIFY_RPA_ON_CASE_HANDED_OFFLINE_ACTIVITY_ID
        );

        //end business process
        ExternalTask endBusinessProcess = assertNextExternalTask(END_BUSINESS_PROCESS);
        completeBusinessProcess(endBusinessProcess);

        assertNoExternalTasksLeft();
    }
}
