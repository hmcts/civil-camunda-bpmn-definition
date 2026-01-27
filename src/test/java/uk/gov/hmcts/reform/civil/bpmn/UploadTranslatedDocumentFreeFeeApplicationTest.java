package uk.gov.hmcts.reform.civil.bpmn;

import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static uk.gov.hmcts.reform.civil.bpmn.BpmnBaseGASpecTest.APPLICATION_EVENT_GASPEC;

public class UploadTranslatedDocumentFreeFeeApplicationTest extends BpmnBaseGAAfterPaymentTest {

    //BPMN Settings
    private static final String MESSAGE_NAME = "UPLOAD_TRANSLATED_DOCUMENT_FOR_FREE_FEE_APPLICATION";
    private static final String PROCESS_ID = "UPLOAD_TRANSLATED_FREE_GA_SUMMARY_DOC";
    private static final String NOTIFYING_RESPONDENTS_EVENT = "NOTIFY_GENERAL_APPLICATION_RESPONDENT";
    private static final String GENERAL_APPLICATION_NOTIYFYING_ID = "GeneralApplicationNotifying";
    private static final String LIP_APPLICANT = "LIP_APPLICANT";
    private static final String LIP_RESPONDENT = "LIP_RESPONDENT";
    private static final String UPDATE_CLAIMANT_DASHBOARD_GA_EVENT = "UPDATE_CLAIMANT_TASK_LIST_GA";
    private static final String UPDATE_RESPONDENT_DASHBOARD_GA_EVENT = "UPDATE_RESPONDENT_TASK_LIST_GA";
    private static final String GENERAL_APPLICATION_CLAIMANT_TASK_LIST_ID = "GeneralApplicationClaimantTaskList";
    private static final String GENERAL_APPLICATION_RESPONDENT_TASK_LIST_ID = "GeneralApplicationRespondentTaskList";

    private static final String CREATE_DASHBOARD_NOTIFICATION_GA_FREE_FEE_ACTIVITY_ID
        = "GenerateDashboardNotificationsGAFreeFee";

    public UploadTranslatedDocumentFreeFeeApplicationTest() {
        super("upload_translated_document_free_fee_application.bpmn", PROCESS_ID);
    }

    @ParameterizedTest
    @CsvSource({"false,false", "true,false", "true,true", "false,true"})
    public void shouldProcessPausedTaskPost_UploadTranslatedDocEvent(boolean isLipApplicant, boolean isLipRespondent) {

        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        VariableMap variables = Variables.createVariables();
        variables.put("flowFlags", Map.of(
            LIP_APPLICANT, isLipApplicant,
            LIP_RESPONDENT, isLipRespondent
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

        //notify respondents
        ExternalTask notifyRespondents = assertNextExternalTask(APPLICATION_EVENT_GASPEC);
        assertCompleteExternalTask(
            notifyRespondents,
            APPLICATION_EVENT_GASPEC,
            NOTIFYING_RESPONDENTS_EVENT,
            GENERAL_APPLICATION_NOTIYFYING_ID,
            variables
        );

        // Free Fee notification
        ExternalTask dashboardNotificationForGaFreeFee = assertNextExternalTask(APPLICATION_EVENT_GASPEC);
        assertCompleteExternalTask(
            dashboardNotificationForGaFreeFee,
            APPLICATION_EVENT_GASPEC,
            DASHBOARD_NOTIFICATION_GA_EVENT,
            CREATE_DASHBOARD_NOTIFICATION_GA_FREE_FEE_ACTIVITY_ID,
            variables
        );

        //end business process
        ExternalTask endBusinessProcess = assertNextExternalTask(END_BUSINESS_PROCESS);
        completeBusinessProcess(endBusinessProcess);

        if (isLipApplicant || isLipRespondent) {
            //update dashboard
            ExternalTask updateCuiClaimantDashboard = assertNextExternalTask(APPLICATION_EVENT_GASPEC);
            assertCompleteExternalTask(
                updateCuiClaimantDashboard,
                APPLICATION_EVENT_GASPEC,
                UPDATE_CLAIMANT_DASHBOARD_GA_EVENT,
                GENERAL_APPLICATION_CLAIMANT_TASK_LIST_ID,
                variables
            );

            ExternalTask updateCuiDefendantDashboard = assertNextExternalTask(APPLICATION_EVENT_GASPEC);
            assertCompleteExternalTask(
                updateCuiDefendantDashboard,
                APPLICATION_EVENT_GASPEC,
                UPDATE_RESPONDENT_DASHBOARD_GA_EVENT,
                GENERAL_APPLICATION_RESPONDENT_TASK_LIST_ID,
                variables
            );
        }
        assertNoExternalTasksLeft();
    }
}
