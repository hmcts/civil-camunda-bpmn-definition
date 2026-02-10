package uk.gov.hmcts.reform.civil.bpmn;

import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static uk.gov.hmcts.reform.civil.bpmn.BpmnBaseTest.DASHBOARD_NOTIFICATION_EVENT;

class GaHearingScheduledTest extends BpmnBaseHearingScheduledGATest {

    private static final String MESSAGE_NAME = "HEARING_SCHEDULED_GA";
    private static final String PROCESS_ID = "GA_HEARING_SCHEDULED_PROCESS_ID";
    private static final String WELSH_ENABLED_FOR_JUDGE_DECISION = "WELSH_ENABLED_FOR_JUDGE_DECISION";
    private static final String GENERATE_HEARING_NOTICE_EVENT = "GENERATE_HEARING_NOTICE_DOCUMENT";
    private static final String GENERATE_HEARING_FORM_ACTIVITY_ID = "GenerateHearingNoticeDocument";
    private static final String ADD_PDF_EVENT = "ADD_PDF_TO_MAIN_CASE";
    private static final String ADD_PDF_ID = "LinkDocumentToParentCase";

    private static final String NOTIFY_HEARING_NOTICE_CLAIMANT_EVENT = "NOTIFY_HEARING_NOTICE_CLAIMANT";
    private static final String NOTIFY_HEARING_NOTICE_CLAIMANT_ACTIVITY_ID = "NotifyHearingNoticeClaimant";
    private static final String NOTIFY_HEARING_NOTICE_DEFENDANT_EVENT = "NOTIFY_HEARING_NOTICE_DEFENDANT";
    private static final String NOTIFY_HEARING_NOTICE_DEFENDANT_ACTIVITY_ID = "NotifyHearingNoticeDefendant";

    private static final String LIP_APPLICANT = "LIP_APPLICANT";
    private static final String LIP_RESPONDENT = "LIP_RESPONDENT";

    private static final String CREATE_DASHBOARD_NOTIFICATION_MAKE_DECISION_ACTIVITY_ID
        = "GenerateDashboardNotificationsGaMakeDecision";

    public GaHearingScheduledTest() {
        super("ga_hearing_scheduled_access.bpmn", PROCESS_ID);
    }

    @BeforeEach
    void setup() {

        //deploy process
        startBusinessProcessDeployment = engine.getRepositoryService()
            .createDeployment()
            .addClasspathResource(String.format(DIAGRAM_PATH,
                                                "start_hearing_scheduled_business_process.bpmn"))
            .deploy();
        endBusinessProcessDeployment = engine.getRepositoryService()
            .createDeployment()
            .addClasspathResource(String.format(DIAGRAM_PATH, "end_hearing_scheduled_business_process.bpmn"))
            .deploy();
        endBusinessProcessWithoutTaskDeployment = engine.getRepositoryService()
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
    void shouldSuccessfullyCompleteCreatePDFDocumentIfWelshApplication_whenCalled() {
        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        VariableMap variables = Variables.createVariables();
        variables.put("flowFlags", Map.of(
            WELSH_ENABLED_FOR_JUDGE_DECISION, true,
            LIP_APPLICANT, false,
            LIP_RESPONDENT, false));

        //complete the start business process
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertCompleteExternalTask(
            startBusiness,
            START_BUSINESS_TOPIC,
            START_BUSINESS_EVENT,
            START_BUSINESS_ACTIVITY,
            variables
        );
        //Generate Hearing Notice Document
        ExternalTask generateHearingNoticeDocument = assertNextExternalTask(APPLICATION_PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            generateHearingNoticeDocument,
            APPLICATION_PROCESS_CASE_EVENT,
            GENERATE_HEARING_NOTICE_EVENT,
            GENERATE_HEARING_FORM_ACTIVITY_ID,
            variables
        );

        ExternalTask endBusinessProcess = assertNextExternalTask(END_BUSINESS_PROCESS_WITHOUT_TASK);
        completeBusinessProcessForGADocUpload(endBusinessProcess);
        assertNoExternalTasksLeft();
    }

    @Test
    void shouldSuccessfullyCompleteCreatePDFDocument_whenCalled() {
        //assert process has started
        assertFalse(processInstance.isEnded());

        VariableMap variables = Variables.createVariables();
        variables.put("flowFlags", Map.of(
            LIP_APPLICANT, false,
            LIP_RESPONDENT, false
        ));

        //assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        //complete the start business process
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertCompleteExternalTask(
            startBusiness,
            START_BUSINESS_TOPIC,
            START_BUSINESS_EVENT,
            START_BUSINESS_ACTIVITY,
            variables
        );
        //Generate Hearing Notice Document
        ExternalTask generateHearingNoticeDocument = assertNextExternalTask(APPLICATION_PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            generateHearingNoticeDocument,
            APPLICATION_PROCESS_CASE_EVENT,
            GENERATE_HEARING_NOTICE_EVENT,
            GENERATE_HEARING_FORM_ACTIVITY_ID,
            variables
        );

        //Link Document to main case event
        ExternalTask addDocumentToMainCase = assertNextExternalTask(UPDATE_FROM_GA_CASE_EVENT);
        assertCompleteExternalTask(
            addDocumentToMainCase,
            UPDATE_FROM_GA_CASE_EVENT,
            ADD_PDF_EVENT,
            ADD_PDF_ID,
            variables
        );

        //Notify Hearing Notice Claimant
        ExternalTask notifyHearingNoticeClaimant = assertNextExternalTask(PROCESS_EXTERNAL_CASE_EVENT);
        assertCompleteExternalTask(
            notifyHearingNoticeClaimant,
            PROCESS_EXTERNAL_CASE_EVENT,
            NOTIFY_HEARING_NOTICE_CLAIMANT_EVENT,
            NOTIFY_HEARING_NOTICE_CLAIMANT_ACTIVITY_ID,
            variables
        );

        //Notify Hearing Notice Defendant(s)
        ExternalTask notifyHearingNoticeDefendant = assertNextExternalTask(PROCESS_EXTERNAL_CASE_EVENT);
        assertCompleteExternalTask(
            notifyHearingNoticeDefendant,
            PROCESS_EXTERNAL_CASE_EVENT,
            NOTIFY_HEARING_NOTICE_DEFENDANT_EVENT,
            NOTIFY_HEARING_NOTICE_DEFENDANT_ACTIVITY_ID,
            variables
        );

        //dashboard Hearing Notice
        ExternalTask dashboardNotificationTask = assertNextExternalTask(PROCESS_EXTERNAL_CASE_EVENT);
        assertCompleteExternalTask(
            dashboardNotificationTask,
            PROCESS_EXTERNAL_CASE_EVENT,
            DASHBOARD_NOTIFICATION_EVENT,
            CREATE_DASHBOARD_NOTIFICATION_MAKE_DECISION_ACTIVITY_ID,
            variables
        );

        //end business process
        ExternalTask endBusinessProcess = assertNextExternalTask(END_BUSINESS_PROCESS);
        completeBusinessProcess(endBusinessProcess);

        assertNoExternalTasksLeft();
    }

    @Test
    void shouldSuccessfullyCompleteCreatePDFDocumentForLIPvLIP_whenCalled() {
        //assert process has started
        assertFalse(processInstance.isEnded());

        VariableMap variables = Variables.createVariables();
        variables.put("flowFlags", Map.of(
            LIP_APPLICANT, true,
            LIP_RESPONDENT, true
        ));

        //assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        //complete the start business process
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertCompleteExternalTask(
            startBusiness,
            START_BUSINESS_TOPIC,
            START_BUSINESS_EVENT,
            START_BUSINESS_ACTIVITY,
            variables
        );
        //Generate Hearing Notice Document
        ExternalTask generateHearingNoticeDocument = assertNextExternalTask(APPLICATION_PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            generateHearingNoticeDocument,
            APPLICATION_PROCESS_CASE_EVENT,
            GENERATE_HEARING_NOTICE_EVENT,
            GENERATE_HEARING_FORM_ACTIVITY_ID,
            variables
        );

        //Link Document to main case event
        ExternalTask addDocumentToMainCase = assertNextExternalTask(UPDATE_FROM_GA_CASE_EVENT);
        assertCompleteExternalTask(
            addDocumentToMainCase,
            UPDATE_FROM_GA_CASE_EVENT,
            ADD_PDF_EVENT,
            ADD_PDF_ID,
            variables
        );

        //Notify Hearing Notice Claimant
        ExternalTask notifyHearingNoticeClaimant = assertNextExternalTask(PROCESS_EXTERNAL_CASE_EVENT);
        assertCompleteExternalTask(
            notifyHearingNoticeClaimant,
            PROCESS_EXTERNAL_CASE_EVENT,
            NOTIFY_HEARING_NOTICE_CLAIMANT_EVENT,
            NOTIFY_HEARING_NOTICE_CLAIMANT_ACTIVITY_ID,
            variables
        );

        //Notify Hearing Notice Defendant(s)
        ExternalTask notifyHearingNoticeDefendant = assertNextExternalTask(PROCESS_EXTERNAL_CASE_EVENT);
        assertCompleteExternalTask(
            notifyHearingNoticeDefendant,
            PROCESS_EXTERNAL_CASE_EVENT,
            NOTIFY_HEARING_NOTICE_DEFENDANT_EVENT,
            NOTIFY_HEARING_NOTICE_DEFENDANT_ACTIVITY_ID,
            variables
        );

        //dashboard Hearing Notice
        ExternalTask dashboardNotificationTask = assertNextExternalTask(PROCESS_EXTERNAL_CASE_EVENT);
        assertCompleteExternalTask(
            dashboardNotificationTask,
            PROCESS_EXTERNAL_CASE_EVENT,
            DASHBOARD_NOTIFICATION_EVENT,
            CREATE_DASHBOARD_NOTIFICATION_MAKE_DECISION_ACTIVITY_ID,
            variables
        );

        //end business process
        ExternalTask endBusinessProcess = assertNextExternalTask(END_BUSINESS_PROCESS);
        completeBusinessProcess(endBusinessProcess);

        assertNoExternalTasksLeft();
    }

    @Test
    void shouldSuccessfullyCompleteCreatePDFDocumentForLRvLIP_whenCalled() {
        //assert process has started
        assertFalse(processInstance.isEnded());

        VariableMap variables = Variables.createVariables();
        variables.put("flowFlags", Map.of(
            LIP_APPLICANT, false,
            LIP_RESPONDENT, true
        ));

        //assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        //complete the start business process
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertCompleteExternalTask(
            startBusiness,
            START_BUSINESS_TOPIC,
            START_BUSINESS_EVENT,
            START_BUSINESS_ACTIVITY,
            variables
        );
        //Generate Hearing Notice Document
        ExternalTask generateHearingNoticeDocument = assertNextExternalTask(APPLICATION_PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            generateHearingNoticeDocument,
            APPLICATION_PROCESS_CASE_EVENT,
            GENERATE_HEARING_NOTICE_EVENT,
            GENERATE_HEARING_FORM_ACTIVITY_ID,
            variables
        );

        //Link Document to main case event
        ExternalTask addDocumentToMainCase = assertNextExternalTask(UPDATE_FROM_GA_CASE_EVENT);
        assertCompleteExternalTask(
            addDocumentToMainCase,
            UPDATE_FROM_GA_CASE_EVENT,
            ADD_PDF_EVENT,
            ADD_PDF_ID,
            variables
        );

        //Notify Hearing Notice Claimant
        ExternalTask notifyHearingNoticeClaimant = assertNextExternalTask(PROCESS_EXTERNAL_CASE_EVENT);
        assertCompleteExternalTask(
            notifyHearingNoticeClaimant,
            PROCESS_EXTERNAL_CASE_EVENT,
            NOTIFY_HEARING_NOTICE_CLAIMANT_EVENT,
            NOTIFY_HEARING_NOTICE_CLAIMANT_ACTIVITY_ID,
            variables
        );

        //Notify Hearing Notice Defendant(s)
        ExternalTask notifyHearingNoticeDefendant = assertNextExternalTask(PROCESS_EXTERNAL_CASE_EVENT);
        assertCompleteExternalTask(
            notifyHearingNoticeDefendant,
            PROCESS_EXTERNAL_CASE_EVENT,
            NOTIFY_HEARING_NOTICE_DEFENDANT_EVENT,
            NOTIFY_HEARING_NOTICE_DEFENDANT_ACTIVITY_ID,
            variables
        );

        //dashboard Hearing Notice Defendant
        ExternalTask dashboardNotificationTask = assertNextExternalTask(PROCESS_EXTERNAL_CASE_EVENT);
        assertCompleteExternalTask(
            dashboardNotificationTask,
            PROCESS_EXTERNAL_CASE_EVENT,
            DASHBOARD_NOTIFICATION_EVENT,
            CREATE_DASHBOARD_NOTIFICATION_MAKE_DECISION_ACTIVITY_ID,
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
