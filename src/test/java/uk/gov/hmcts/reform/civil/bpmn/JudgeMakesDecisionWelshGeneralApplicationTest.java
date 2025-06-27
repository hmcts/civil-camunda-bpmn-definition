package uk.gov.hmcts.reform.civil.bpmn;

import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class JudgeMakesDecisionWelshGeneralApplicationTest extends BpmnBaseGASpecTest {

    public static final String END_BUSINESS_PROCESS = "END_BUSINESS_PROCESS_GASPEC_WITHOUT_WA_TASK";
    private static final String PROCESS_ID = "GA_MAKE_DECISION_PROCESS_ID";
    private static final String WELSH_ENABLED_FOR_JUDGE_DECISION = "WELSH_ENABLED_FOR_JUDGE_DECISION";
    private static final String MESSAGE_NAME = "MAKE_DECISION";
    public static final String START_BUSINESS_TOPIC = "START_GA_BUSINESS_PROCESS";
    public static final String START_BUSINESS_EVENT = "START_GA_BUSINESS_PROCESS";
    public static final String START_BUSINESS_ACTIVITY = "StartGeneralApplicationBusinessProcessTaskId";
    public static final String PROCESS_EXTERNAL_CASE_EVENT = "processExternalCaseEventGASpec";
    public static final String APPLICATION_PROCESS_CASE_EVENT = "applicationProcessCaseEventGASpec";
    //Obtain Additional fee value
    private static final String OBTAIN_ADDITIONAL_FEE_VALUE_EVENT = "OBTAIN_ADDITIONAL_FEE_VALUE";
    private static final String OBTAIN_ADDITIONAL_FEE_VALUE_ID = "ObtainAdditionalFeeValue";
    //Obtain fee reference
    private static final String OBTAIN_ADDIIONAL_FEE_REFERENCE_EVENT = "OBTAIN_ADDITIONAL_PAYMENT_REF";
    private static final String OBTAIN_ADDIIONAL_FEE_REFERENCE_ID = "ObtainAdditionalPaymentReference";
    //Create PDF
    private static final String CREATE_PDF_EVENT = "GENERATE_JUDGES_FORM";
    private static final String CREATE_PDF_ID = "CreatePDFDocument";
    public JudgeMakesDecisionWelshGeneralApplicationTest() {
        super("judge_makes_decision_general_application.bpmn", "GA_MAKE_DECISION_PROCESS_ID");
    }

    @BeforeEach
    void setup() {
        //deploy process
        startBusinessProcessDeployment = engine.getRepositoryService()
            .createDeployment()
            .addClasspathResource(String.format(
                DIAGRAM_PATH,
                "start_business_process_in_general_application.bpmn"
            ))
            .deploy();
        endBusinessProcessDeployment = engine.getRepositoryService()
            .createDeployment()
            .addClasspathResource(String.format(
                DIAGRAM_PATH,
                "end_general_application_business_process_without_WA_task.bpmn"
            ))
            .deploy();
        deployment = engine.getRepositoryService()
            .createDeployment()
            .addClasspathResource(String.format(DIAGRAM_PATH, bpmnFileName))
            .deploy();
        processInstance = engine.getRuntimeService().startProcessInstanceByKey(processId);
    }

    @Test
    void shouldPauseNotificationAndStateChangeForWelshApplication() {
        VariableMap variables = Variables.createVariables();
        variables.put("flowFlags", Map.of(
            WELSH_ENABLED_FOR_JUDGE_DECISION, "true"));

        //assert process has started
        assertFalse(processInstance.isEnded());
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
        //Obtain Additional Fee Value
        ExternalTask additionalFeeValueProcess = assertNextExternalTask(PROCESS_EXTERNAL_CASE_EVENT);
        assertCompleteExternalTask(
            additionalFeeValueProcess,
            PROCESS_EXTERNAL_CASE_EVENT,
            OBTAIN_ADDITIONAL_FEE_VALUE_EVENT,
            OBTAIN_ADDITIONAL_FEE_VALUE_ID,
            variables
        );

        //Obtain Additional Payment Reference
        ExternalTask additionalPaymentRefProcess = assertNextExternalTask(PROCESS_EXTERNAL_CASE_EVENT);
        assertCompleteExternalTask(
            additionalPaymentRefProcess,
            PROCESS_EXTERNAL_CASE_EVENT,
            OBTAIN_ADDIIONAL_FEE_REFERENCE_EVENT,
            OBTAIN_ADDIIONAL_FEE_REFERENCE_ID,
            variables
        );

        //complete the document generation
        ExternalTask documentGeneration = assertNextExternalTask(APPLICATION_PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            documentGeneration,
            APPLICATION_PROCESS_CASE_EVENT,
            CREATE_PDF_EVENT,
            CREATE_PDF_ID,
            variables
        );


        //end business process
        ExternalTask endBusinessProcess = assertNextExternalTask(END_BUSINESS_PROCESS);
        completeBusinessProcessForGADocUpload(endBusinessProcess);

        assertNoExternalTasksLeft();
    }
}
