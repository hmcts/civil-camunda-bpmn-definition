package uk.gov.hmcts.reform.civil.bpmn;

import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

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
    public static final String TRIGGER_APPLICATION_PROCEEDS_IN_HERITAGE = "TRIGGER_APPLICATION_PROCEEDS_IN_HERITAGE";
    private static final String APPLICATION_PROCEEDS_IN_HERITAGE_ACTIVITY_ID = "UpdateGeneralApplicationStatus";
    public static final String APPLICATION_OFFLINE_UPDATE_CLAIM = "APPLICATION_OFFLINE_UPDATE_CLAIM";
    private static final String APPLICATION_OFFLINE_UPDATE_CLAIM_ACTIVITY_ID = "UpdateClaimWithApplicationStatus";

    public ApplyNocDecisionDefendantLipTest() {
        super("apply_noc_decision_defendant_lip.bpmn", PROCESS_ID);
    }

    @ParameterizedTest
    @CsvSource({"true", "false"})
    void shouldRunProcess(boolean welshEnabled) {

        VariableMap variables = Variables.createVariables();
        variables.put(FLOW_FLAGS, Map.of(
            LIP_CASE, false,
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

        //complete notify all parties
        ExternalTask notifyPartiesAfterNoc = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            notifyPartiesAfterNoc,
            PROCESS_CASE_EVENT,
            "NOTIFY_EVENT",
            "DefendantLipRepresentedWithNoCNotifier"
        );

        if (welshEnabled) {
            //update main claim language flag
            ExternalTask updateLanguage = assertNextExternalTask(PROCESS_CASE_EVENT);
            assertCompleteExternalTask(
                updateLanguage,
                PROCESS_CASE_EVENT,
                "RESET_LANGUAGE_PREFERENCE",
                "ResetLanguagePreferenceAfterNoC"
            );
        }

        //end business process
        ExternalTask endBusinessProcess = assertNextExternalTask(END_BUSINESS_PROCESS);
        completeBusinessProcess(endBusinessProcess);

        assertNoExternalTasksLeft();
    }

    @Test
    void shouldRunProcessIfClaimantIsNotRepresented() {

        VariableMap variables = Variables.createVariables();
        variables.put(FLOW_FLAGS, Map.of(
            LIP_CASE, true,
            DEFENDANT_NOC_ONLINE, false));

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

        //complete notify parties
        ExternalTask notifyParties = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            notifyParties,
            PROCESS_CASE_EVENT,
            "NOTIFY_EVENT",
            "DefendantLipRepresentedWithNoCNotifier"
        );

        //Proceed Offline
        ExternalTask proceedOfflineTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            proceedOfflineTask,
            PROCESS_CASE_EVENT,
            PROCEEDS_IN_HERITAGE_SYSTEM,
            PROCEEDS_IN_HERITAGE_SYSTEM_ACTIVITY_ID
        );

        //complete the dashboard notification
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

    @Test
    void shouldStayAsIsIfClaimantIsNotRepresentedWithDefendantNocOnline() {

        VariableMap variables = Variables.createVariables();
        variables.put(FLOW_FLAGS, Map.of(
            LIP_CASE, true,
            DEFENDANT_NOC_ONLINE, true,
            JBA_ISSUED_BEFORE_NOC, false,
            CLAIM_STATE_DURING_NOC, false));

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

        //complete notify parties
        ExternalTask notifyParties = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            notifyParties,
            PROCESS_CASE_EVENT,
            "NOTIFY_EVENT",
            "DefendantLipRepresentedWithNoCNotifier"
        );

        //complete dashboard notification
        ExternalTask dashboardClaimantNotification = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            dashboardClaimantNotification,
            PROCESS_CASE_EVENT,
            "CREATE_NOC_ONLINE_DASHBOARD_NOTIFICATION_FOR_CLAIMANT",
            "createOnlineDashboardNotificationForClaimant"
        );

        //end business process
        ExternalTask endBusinessProcess = assertNextExternalTask(END_BUSINESS_PROCESS);
        completeBusinessProcess(endBusinessProcess);

        assertNoExternalTasksLeft();
    }

    @ParameterizedTest
    @CsvSource({"MAIN.FULL_ADMISSION", "MAIN.PART_ADMISSION"})
    void shouldMoveTheCaseOffline_IfClaimStateIsClaimantIntentionForGivenResponseType(String responseType) {
        VariableMap variables = Variables.createVariables();
        variables.putValue("flowState", responseType);
        variables.put(FLOW_FLAGS, Map.of(
            LIP_CASE, true,
            DEFENDANT_NOC_ONLINE, true,
            JBA_ISSUED_BEFORE_NOC, false,
            CLAIM_STATE_DURING_NOC, true));

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
        
        //complete notify parties
        ExternalTask notifyParties = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            notifyParties,
            PROCESS_CASE_EVENT,
            "NOTIFY_EVENT",
            "DefendantLipRepresentedWithNoCNotifier"
        );

        //Proceed Offline
        ExternalTask proceedOfflineTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            proceedOfflineTask,
            PROCESS_CASE_EVENT,
            PROCEEDS_IN_HERITAGE_SYSTEM,
            PROCEEDS_IN_HERITAGE_SYSTEM_ACTIVITY_ID
        );

        //complete Dashboard notification
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

    @Test
    void shouldStayAsIsIfClaimantIsNotRepresentedWithDefendantResponseIsFullDefence() {
        VariableMap variables = Variables.createVariables();
        variables.putValue("flowState", "MAIN.FULL_DEFENCE");
        variables.put(FLOW_FLAGS, Map.of(
            LIP_CASE, true,
            DEFENDANT_NOC_ONLINE, true,
            JBA_ISSUED_BEFORE_NOC, false,
            CLAIM_STATE_DURING_NOC, true));

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

        //complete notify parties
        ExternalTask notifyParties = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            notifyParties,
            PROCESS_CASE_EVENT,
            "NOTIFY_EVENT",
            "DefendantLipRepresentedWithNoCNotifier"
        );

        //complete dashboard notification
        ExternalTask dashboardClaimantNotification = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            dashboardClaimantNotification,
            PROCESS_CASE_EVENT,
            "CREATE_NOC_ONLINE_DASHBOARD_NOTIFICATION_FOR_CLAIMANT",
            "createOnlineDashboardNotificationForClaimant"
        );

        //end business process
        ExternalTask endBusinessProcess = assertNextExternalTask(END_BUSINESS_PROCESS);
        completeBusinessProcess(endBusinessProcess);

        assertNoExternalTasksLeft();
    }

    @ParameterizedTest
    @CsvSource({"MAIN.FULL_ADMISSION", "MAIN.PART_ADMISSION"})
    void shouldMoveTheCaseOffline_IfJudgmentByAdmissionIssuedForCase(String responseType) {
        VariableMap variables = Variables.createVariables();
        variables.putValue("flowState", responseType);
        variables.put(FLOW_FLAGS, Map.of(
            LIP_CASE, true,
            DEFENDANT_NOC_ONLINE, true,
            JBA_ISSUED_BEFORE_NOC, true,
            CLAIM_STATE_DURING_NOC, false));

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
        
        //complete notify parties
        ExternalTask notifyParties = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            notifyParties,
            PROCESS_CASE_EVENT,
            "NOTIFY_EVENT",
            "DefendantLipRepresentedWithNoCNotifier"
        );

        //Proceed Offline
        ExternalTask proceedOfflineTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            proceedOfflineTask,
            PROCESS_CASE_EVENT,
            PROCEEDS_IN_HERITAGE_SYSTEM,
            PROCEEDS_IN_HERITAGE_SYSTEM_ACTIVITY_ID
        );

        //complete Dashboard notification
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

    @ParameterizedTest
    @CsvSource({"MAIN.FULL_ADMISSION", "MAIN.PART_ADMISSION"})
    void shouldMoveTheCaseOffline_MovedGAOfflineToo(String responseType) {
        VariableMap variables = Variables.createVariables();
        variables.putValue("flowState", responseType);
        variables.put(FLOW_FLAGS, Map.of(
            LIP_CASE, true,
            DEFENDANT_NOC_ONLINE, true,
            JBA_ISSUED_BEFORE_NOC, true,
            CLAIM_STATE_DURING_NOC, false,
            GENERAL_APPLICATION_ENABLED, true));

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

        //Update General Application Status
        ExternalTask updateApplicationStatus = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            updateApplicationStatus,
            PROCESS_CASE_EVENT,
            TRIGGER_APPLICATION_PROCEEDS_IN_HERITAGE,
            APPLICATION_PROCEEDS_IN_HERITAGE_ACTIVITY_ID
        );

        //Update Claim Details with General Application Status
        ExternalTask updateClaimWithApplicationStatus = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            updateClaimWithApplicationStatus,
            PROCESS_CASE_EVENT,
            APPLICATION_OFFLINE_UPDATE_CLAIM,
            APPLICATION_OFFLINE_UPDATE_CLAIM_ACTIVITY_ID
        );

        ExternalTask claimantGaDashboard = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            claimantGaDashboard,
            PROCESS_CASE_EVENT,
            "CREATE_DASHBOARD_NOTIFICATION_APPLICATION_PROCEED_OFFLINE_CLAIMANT",
            "claimantLipApplicationOfflineDashboardNotification"
        );

        ExternalTask defendantGaDashboard = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            defendantGaDashboard,
            PROCESS_CASE_EVENT,
            "CREATE_DASHBOARD_NOTIFICATION_APPLICATION_PROCEED_OFFLINE_DEFENDANT",
            "defendantLipApplicationOfflineDashboardNotification"
        );

        //complete Dashboard notification
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
