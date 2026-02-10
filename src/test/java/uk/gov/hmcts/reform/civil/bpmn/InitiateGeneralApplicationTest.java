package uk.gov.hmcts.reform.civil.bpmn;

import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static uk.gov.hmcts.reform.civil.bpmn.BpmnBaseTest.DASHBOARD_NOTIFICATION_EVENT;

class InitiateGeneralApplicationTest extends BpmnBaseGASpecTest {

    //BPMN Settings
    private static final String MESSAGE_NAME = "INITIATE_GENERAL_APPLICATION";
    private static final String PROCESS_ID = "GA_INITIATE_PROCESS_ID";
    //create general application Case
    private static final String CREATE_GENERAL_APPLICATION_EVENT = "CREATE_GENERAL_APPLICATION_CASE";
    private static final String CREATE_GENERAL_APPLICATION_ID = "CreateGeneralApplicationCase";
    //Link General Application Case to Parent Case
    private static final String LINK_GENERAL_APPLICATIONC_CASE_TO_PARENT_CASE_EVENT
        = "LINK_GENERAL_APPLICATION_CASE_TO_PARENT_CASE";
    private static final String LINK_GENERAL_APPLICATIONC_CASE_TO_PARENT_CASE_ID
        = "LinkGeneralApplicationCaseToParentCase";
    //Assigning of roles
    private static final String ASSIGNIN_OF_ROLES_EVENT = "ASSIGN_GA_ROLES";
    private static final String ASSIGNIN_OF_ROLES_ID = "AssigningOfRoles";
    //Fee Validation
    private static final String VALIDATE_FEE_EVENT = "VALIDATE_FEE_GASPEC";
    private static final String VALIDATE_FEE_ID = "GeneralApplicationValidateFee";

    //Make Service Request
    private static final String MAKE_SERVICE_REQ_EVENT = "MAKE_PAYMENT_SERVICE_REQ_GASPEC";
    private static final String MAKE_SERVICE_REQ_ID = "GeneralApplicationPaymentServiceReq";

    //Make Service Request
    private static final String GENERATE_DRAFT_DOC_EVENT = "GENERATE_DRAFT_DOCUMENT";
    private static final String GENERATE_DRAFT_DOC_ID = "DraftDocumentGenerator";

    //Notifying respondents
    private static final String NOTYFYING_RESPONDENTS_EVENT = "NOTIFY_GENERAL_APPLICATION_RESPONDENT";
    private static final String GENERAL_APPLICATION_NOTIYFYING_ID = "GeneralApplicationNotifying";

    private static final String LIP_APPLICANT = "LIP_APPLICANT";
    private static final String LIP_RESPONDENT = "LIP_RESPONDENT";
    //Update CUI dashboard
    //Notifying respondents
    private static final String UPDATE_CLAIMANT_DASHBOARD_GA_EVENT = "UPDATE_CLAIMANT_TASK_LIST_GA";
    private static final String UPDATE_RESPONDENT_DASHBOARD_GA_EVENT = "UPDATE_RESPONDENT_TASK_LIST_GA";
    private static final String GENERAL_APPLICATION_CLAIMANT_TASK_LIST_ID = "GeneralApplicationClaimantTaskList";
    private static final String GENERAL_APPLICATION_RESPONDENT_TASK_LIST_ID = "GeneralApplicationRespondentTaskList";
     private static final String CREATE_DASHBOARD_NOTIFICATION_APPLICATION_ISSUED_ACTIVITY_ID
        = "GenerateDashboardNotificationsGaApplicationIssued";

    public InitiateGeneralApplicationTest() {
        super("initiate_general_application.bpmn", "GA_INITIATE_PROCESS_ID");
    }

    @ParameterizedTest
    @CsvSource({"false,false", "true,false", "true,true", "false,true"})
    void shouldSuccessfullyCompleteCreateGeneralApplication_whenCalled(boolean isLipApplicant, boolean isLipRespondent) {
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

        //complete the document generation
        ExternalTask documentGeneration = assertNextExternalTask(CREATE_APPLICATION_CASE_EVENT);
        assertCompleteExternalTask(
            documentGeneration,
            CREATE_APPLICATION_CASE_EVENT,
            CREATE_GENERAL_APPLICATION_EVENT,
            CREATE_GENERAL_APPLICATION_ID,
            variables
        );

        //link general application case to parent case
        ExternalTask linkCases = assertNextExternalTask(APPLICATION_EVENT_GASPEC);
        assertCompleteExternalTask(
            linkCases,
            APPLICATION_EVENT_GASPEC,
            LINK_GENERAL_APPLICATIONC_CASE_TO_PARENT_CASE_EVENT,
            LINK_GENERAL_APPLICATIONC_CASE_TO_PARENT_CASE_ID,
            variables
        );

        //assigne of roles
        ExternalTask assigneRoles = assertNextExternalTask(APPLICATION_EVENT_GASPEC);
        assertCompleteExternalTask(
            assigneRoles,
            APPLICATION_EVENT_GASPEC,
            ASSIGNIN_OF_ROLES_EVENT,
            ASSIGNIN_OF_ROLES_ID,
            variables
        );

        //validate fee
        ExternalTask validateFee = assertNextExternalTask(APPLICATION_EVENT_GASPEC);
        assertCompleteExternalTask(
            validateFee,
            APPLICATION_EVENT_GASPEC,
            VALIDATE_FEE_EVENT,
            VALIDATE_FEE_ID,
            variables
        );
        //make service request
        ExternalTask makeServiceRequest = assertNextExternalTask(APPLICATION_EVENT_GASPEC);
        assertCompleteExternalTask(
            makeServiceRequest,
            APPLICATION_EVENT_GASPEC,
            MAKE_SERVICE_REQ_EVENT,
            MAKE_SERVICE_REQ_ID,
            variables
        );

        //make service request
        ExternalTask generateDraftDoc = assertNextExternalTask(APPLICATION_EVENT_GASPEC);
        assertCompleteExternalTask(
            generateDraftDoc,
            APPLICATION_EVENT_GASPEC,
            GENERATE_DRAFT_DOC_EVENT,
            GENERATE_DRAFT_DOC_ID,
            variables
        );

        //notify respondents
        ExternalTask notifyRespondents = assertNextExternalTask(APPLICATION_EVENT_GASPEC);
        assertCompleteExternalTask(
            notifyRespondents,
            APPLICATION_EVENT_GASPEC,
            NOTYFYING_RESPONDENTS_EVENT,
            GENERAL_APPLICATION_NOTIYFYING_ID,
            variables
        );

        //dashboard notification
        ExternalTask dashboardNotificationForGa = assertNextExternalTask(APPLICATION_EVENT_GASPEC);
        assertCompleteExternalTask(
            dashboardNotificationForGa,
            APPLICATION_EVENT_GASPEC,
            DASHBOARD_NOTIFICATION_EVENT,
            CREATE_DASHBOARD_NOTIFICATION_APPLICATION_ISSUED_ACTIVITY_ID,
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
