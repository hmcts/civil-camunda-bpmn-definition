package uk.gov.hmcts.reform.civil.bpmn;

import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

class UploadTranslatedDocumentSDOTest extends BpmnBaseTest {

    public static final String MESSAGE_NAME = "UPLOAD_TRANSLATED_DOCUMENT_SDO";
    public static final String PROCESS_ID = "UPLOAD_TRANSLATED_DOCUMENT_SDO";

    public UploadTranslatedDocumentSDOTest() {
        super("upload_translated_document_sdo.bpmn", PROCESS_ID);
    }

    @Test
    void shouldSuccessfullyCompleteUploadTranslatedSDO() {
        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        VariableMap variables = Variables.createVariables();
        variables.putValue(FLOW_FLAGS, Map.of(
            DASHBOARD_SERVICE_ENABLED, true,
            CASE_PROGRESSION_ENABLED, true
        ));

        //complete the start business process
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertCompleteExternalTask(startBusiness,
                                   START_BUSINESS_TOPIC,
                                   START_BUSINESS_EVENT,
                                   START_BUSINESS_ACTIVITY,
                                   variables);

        //complete the notification to applicant(s) solicitor
        ExternalTask applicantsNotification = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            applicantsNotification,
            PROCESS_CASE_EVENT,
            "NOTIFY_APPLICANTS_SOLICITOR_SDO_TRIGGERED",
            "CreateSDONotifyApplicantsSolicitor",
            variables
        );

        //complete the notification to respondent 1 solicitor
        ExternalTask respondent1Notification = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            respondent1Notification,
            PROCESS_CASE_EVENT,
            "NOTIFY_RESPONDENT_SOLICITOR1_SDO_TRIGGERED",
            "CreateSDONotifyRespondentSolicitor1",
            variables
        );

        //complete the notification to respondent 2 solicitor
        ExternalTask respondent2Notification = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            respondent2Notification,
            PROCESS_CASE_EVENT,
            "NOTIFY_RESPONDENT_SOLICITOR2_SDO_TRIGGERED",
            "CreateSDONotifyRespondentSolicitor2",
            variables
        );

        //end business process
        ExternalTask endBusinessProcess = assertNextExternalTask(END_BUSINESS_PROCESS);
        completeBusinessProcess(endBusinessProcess);

        assertNoExternalTasksLeft();
    }

    @Test
    void shouldSuccessfullyCompleteUploadTranslatedSDOWhenDashboardDisabled() {
        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        VariableMap variables = Variables.createVariables();
        variables.putValue(FLOW_FLAGS, Map.of(
            DASHBOARD_SERVICE_ENABLED, false
        ));

        //complete the start business process
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertCompleteExternalTask(startBusiness,
                                   START_BUSINESS_TOPIC,
                                   START_BUSINESS_EVENT,
                                   START_BUSINESS_ACTIVITY,
                                   variables);

        //complete the notification to applicant(s) solicitor
        ExternalTask applicantsNotification = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            applicantsNotification,
            PROCESS_CASE_EVENT,
            "NOTIFY_APPLICANTS_SOLICITOR_SDO_TRIGGERED",
            "CreateSDONotifyApplicantsSolicitor",
            variables
        );

        //complete the notification to respondent 1 solicitor
        ExternalTask respondent1Notification = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            respondent1Notification,
            PROCESS_CASE_EVENT,
            "NOTIFY_RESPONDENT_SOLICITOR1_SDO_TRIGGERED",
            "CreateSDONotifyRespondentSolicitor1",
            variables
        );

        //complete the notification to respondent 2 solicitor
        ExternalTask respondent2Notification = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            respondent2Notification,
            PROCESS_CASE_EVENT,
            "NOTIFY_RESPONDENT_SOLICITOR2_SDO_TRIGGERED",
            "CreateSDONotifyRespondentSolicitor2",
            variables
        );

        //end business process
        ExternalTask endBusinessProcess = assertNextExternalTask(END_BUSINESS_PROCESS);
        completeBusinessProcess(endBusinessProcess);

        assertNoExternalTasksLeft();
    }

    @Test
    void shouldSuccessfullyCompleteUploadTranslatedSDOForLiPDefendant() {
        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        VariableMap variables = Variables.createVariables();
        variables.putValue(FLOW_FLAGS, Map.of(
            UNREPRESENTED_DEFENDANT_ONE, true,
            DASHBOARD_SERVICE_ENABLED, true,
            LIP_CASE, true,
            CASE_PROGRESSION_ENABLED, true
        ));

        //complete the start business process
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertCompleteExternalTask(startBusiness,
                                   START_BUSINESS_TOPIC,
                                   START_BUSINESS_EVENT,
                                   START_BUSINESS_ACTIVITY,
                                   variables);

        //complete the notification to applicant(s) solicitor
        ExternalTask applicantsNotification = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            applicantsNotification,
            PROCESS_CASE_EVENT,
            "NOTIFY_APPLICANTS_SOLICITOR_SDO_TRIGGERED",
            "CreateSDONotifyApplicantsSolicitor",
            variables
        );

        //Trigger Bulk Print
        ExternalTask sendSDOOrderToClaimant = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            sendSDOOrderToClaimant,
            PROCESS_CASE_EVENT,
            "SEND_SDO_ORDER_TO_LIP_CLAIMANT",
            "SendSDOToClaimantLIP",
            variables
        );

        ExternalTask sendSDOOrderToDefendant = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            sendSDOOrderToDefendant,
            PROCESS_CASE_EVENT,
            "SEND_SDO_ORDER_TO_LIP_DEFENDANT",
            "SendSDOToDefendantLIP",
            variables
        );

        //complete the notification to respondent 1 solicitor
        ExternalTask respondent1Notification = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            respondent1Notification,
            PROCESS_CASE_EVENT,
            "NOTIFY_RESPONDENT_SOLICITOR1_SDO_TRIGGERED",
            "CreateSDONotifyRespondentSolicitor1",
            variables
        );

        //complete the notification to respondent 2 solicitor
        ExternalTask respondent2Notification = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            respondent2Notification,
            PROCESS_CASE_EVENT,
            "NOTIFY_RESPONDENT_SOLICITOR2_SDO_TRIGGERED",
            "CreateSDONotifyRespondentSolicitor2",
            variables
        );

        //end business process
        ExternalTask endBusinessProcess = assertNextExternalTask(END_BUSINESS_PROCESS);
        completeBusinessProcess(endBusinessProcess);

        assertNoExternalTasksLeft();
    }

    @Test
    void shouldSuccessfullyCompleteUploadTranslatedSDPForLiPvLrClaim() {
        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        VariableMap variables = Variables.createVariables();
        variables.putValue(FLOW_FLAGS, Map.of(
            UNREPRESENTED_DEFENDANT_ONE, false,
            DASHBOARD_SERVICE_ENABLED, true,
            LIP_CASE, true,
            CASE_PROGRESSION_ENABLED, true
        ));

        //complete the start business process
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertCompleteExternalTask(startBusiness,
                                   START_BUSINESS_TOPIC,
                                   START_BUSINESS_EVENT,
                                   START_BUSINESS_ACTIVITY,
                                   variables);

        //complete the notification to applicant(s) solicitor
        ExternalTask applicantsNotification = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            applicantsNotification,
            PROCESS_CASE_EVENT,
            "NOTIFY_APPLICANTS_SOLICITOR_SDO_TRIGGERED",
            "CreateSDONotifyApplicantsSolicitor",
            variables
        );

        //Trigger Bulk Print
        ExternalTask sendSDOOrderToClaimant = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            sendSDOOrderToClaimant,
            PROCESS_CASE_EVENT,
            "SEND_SDO_ORDER_TO_LIP_CLAIMANT",
            "SendSDOToClaimantLIP",
            variables
        );

        //complete the notification to respondent 1 solicitor
        ExternalTask respondent1Notification = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            respondent1Notification,
            PROCESS_CASE_EVENT,
            "NOTIFY_RESPONDENT_SOLICITOR1_SDO_TRIGGERED",
            "CreateSDONotifyRespondentSolicitor1",
            variables
        );

        //complete the notification to respondent 2 solicitor
        ExternalTask respondent2Notification = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            respondent2Notification,
            PROCESS_CASE_EVENT,
            "NOTIFY_RESPONDENT_SOLICITOR2_SDO_TRIGGERED",
            "CreateSDONotifyRespondentSolicitor2",
            variables
        );

        //end business process
        ExternalTask endBusinessProcess = assertNextExternalTask(END_BUSINESS_PROCESS);
        completeBusinessProcess(endBusinessProcess);

        assertNoExternalTasksLeft();
    }

    @Test
    void shouldSuccessfullyCompleteUploadTranslatedSDOForLrvLrClaim() {
        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        VariableMap variables = Variables.createVariables();
        variables.putValue(FLOW_FLAGS, Map.of(
            UNREPRESENTED_DEFENDANT_ONE, false,
            DASHBOARD_SERVICE_ENABLED, true,
            LIP_CASE, false,
            CASE_PROGRESSION_ENABLED, true
        ));

        //complete the start business process
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertCompleteExternalTask(startBusiness,
                                   START_BUSINESS_TOPIC,
                                   START_BUSINESS_EVENT,
                                   START_BUSINESS_ACTIVITY,
                                   variables);

        //complete the notification to applicant(s) solicitor
        ExternalTask applicantsNotification = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            applicantsNotification,
            PROCESS_CASE_EVENT,
            "NOTIFY_APPLICANTS_SOLICITOR_SDO_TRIGGERED",
            "CreateSDONotifyApplicantsSolicitor",
            variables
        );

        //complete the notification to respondent 1 solicitor
        ExternalTask respondent1Notification = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            respondent1Notification,
            PROCESS_CASE_EVENT,
            "NOTIFY_RESPONDENT_SOLICITOR1_SDO_TRIGGERED",
            "CreateSDONotifyRespondentSolicitor1",
            variables
        );

        //complete the notification to respondent 2 solicitor
        ExternalTask respondent2Notification = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            respondent2Notification,
            PROCESS_CASE_EVENT,
            "NOTIFY_RESPONDENT_SOLICITOR2_SDO_TRIGGERED",
            "CreateSDONotifyRespondentSolicitor2",
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
