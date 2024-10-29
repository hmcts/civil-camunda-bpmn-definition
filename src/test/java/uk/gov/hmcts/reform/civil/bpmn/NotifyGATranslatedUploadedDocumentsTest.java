package uk.gov.hmcts.reform.civil.bpmn;

import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;

public class NotifyGATranslatedUploadedDocumentsTest extends BpmnBaseGASpecTest {

    public static final String NOTIFY_EVENT = "processExternalCaseEventGASpec";
    private static final String START_BUSINESS_TOPIC = "START_GA_BUSINESS_PROCESS";
    private static final String LIP_APPLICANT = "LIP_APPLICANT";
    private static final String LIP_RESPONDENT = "LIP_RESPONDENT";
    public static final String END_BUSINESS_PROCESS = "END_BUSINESS_PROCESS_GASPEC_WITHOUT_WA_TASK";
    public static final String MAIN_CASE_TOPIC = "updateFromGACaseEvent";

    public NotifyGATranslatedUploadedDocumentsTest() {
        super("upload_translated_document_ga_lip_notify.bpmn", "UPLOAD_TRANSLATED_DOCUMENT_GA_LIP_ID");
    }

    @BeforeEach
    void setup() {
        //deploy process
        startBusinessProcessDeployment = engine.getRepositoryService()
            .createDeployment()
            .addClasspathResource(String.format(DIAGRAM_PATH,
                                                "start_business_process_in_general_application.bpmn"))
            .deploy();
        endBusinessProcessDeployment = engine.getRepositoryService()
            .createDeployment()
            .addClasspathResource(String.format(DIAGRAM_PATH, "end_general_application_business_process_without_WA_task.bpmn"))
            .deploy();
        deployment = engine.getRepositoryService()
            .createDeployment()
            .addClasspathResource(String.format(DIAGRAM_PATH, bpmnFileName))
            .deploy();
        processInstance = engine.getRuntimeService().startProcessInstanceByKey(processId);
    }

    @Test
    void shouldNotifyTranslatedDocumentUploaded() {
        VariableMap variables = Variables.createVariables();
        variables.put("flowFlags", Map.of(
            LIP_APPLICANT, false,
            LIP_RESPONDENT, false));

        //assert process has started
        assertFalse(processInstance.isEnded());
        //complete the start business process
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertCompleteExternalTask(
            startBusiness,
            START_BUSINESS_TOPIC,
            START_GA_BUSINESS_EVENT,
            START_GA_BUSINESS_ACTIVITY,
            variables
        );

        // update the docs to main case
        ExternalTask mainCaseDocumentUpdateTask = assertNextExternalTask(MAIN_CASE_TOPIC);
        assertCompleteExternalTask(
            mainCaseDocumentUpdateTask,
            MAIN_CASE_TOPIC,
            "ADD_PDF_TO_MAIN_CASE",
            "AddGADocumentsToMainCaseID",
            variables
        );
        //complete the applicant notification
        ExternalTask notificationApplicantTask = assertNextExternalTask(NOTIFY_EVENT);
        assertCompleteExternalTask(
            notificationApplicantTask,
            NOTIFY_EVENT,
            "NOTIFY_APPLICANT_TRANSLATED_DOCUMENT_UPLOADED_GA",
            "NotifyTranslatedDocumentUploadedToApplicantGA",
            variables
        );
        //complete the respondent notification
        ExternalTask notificationRespondentTask = assertNextExternalTask(NOTIFY_EVENT);
        assertCompleteExternalTask(
            notificationRespondentTask,
            NOTIFY_EVENT,
            "NOTIFY_RESPONDENT_TRANSLATED_DOCUMENT_UPLOADED_GA",
            "NotifyTranslatedDocumentUploadedToRespondentGA",
            variables
        );
        //complete the applicant dashboard notification
        ExternalTask notificationDashboardApplicantTask = assertNextExternalTask(NOTIFY_EVENT);
        assertCompleteExternalTask(
            notificationDashboardApplicantTask,
            NOTIFY_EVENT,
            "CREATE_APPLICANT_DASHBOARD_NOTIFICATION_TRANSLATED_DOC",
            "ApplicantDashboardTranslatedDocUploadedGA",
            variables
        );
        //complete the respondent dashboard notification
        ExternalTask notificationDashboardRespondentTask = assertNextExternalTask(NOTIFY_EVENT);
        assertCompleteExternalTask(
            notificationDashboardRespondentTask,
            NOTIFY_EVENT,
            "CREATE_RESPONDENT_DASHBOARD_NOTIFICATION_TRANSLATED_DOC",
            "RespondentDashboardTranslatedDocUploadedGA",
            variables
        );

        //end business process
        ExternalTask endBusinessProcess = assertNextExternalTask(END_BUSINESS_PROCESS);
        completeBusinessProcessForGADocUpload(endBusinessProcess);
        assertNoExternalTasksLeft();
    }
}
