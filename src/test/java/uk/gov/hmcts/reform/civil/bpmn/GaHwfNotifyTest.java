package uk.gov.hmcts.reform.civil.bpmn;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class GaHwfNotifyTest extends BpmnBaseGASpecTest {

    private static final String FILE_NAME = "notify_ga_applicant_hwf_outcome.bpmn";
    private static final String MESSAGE_NAME = "NOTIFY_APPLICANT_LIP_HWF";
    private static final String PROCESS_ID = "GA_NOTIFY_HWF";
    private static final String ACTIVITY_ID = "Notify_App_Lip_Hwf";
    private static final String START_BUSINESS_TOPIC = "START_GA_BUSINESS_PROCESS";
    public static final String NOTIFY_EVENT = "applicationProcessCaseEventGASpec";

    public GaHwfNotifyTest() {
        super(FILE_NAME, PROCESS_ID);
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
                .addClasspathResource(String.format(DIAGRAM_PATH, "end_ga_business_process.bpmn"))
                .deploy();
        deployment = engine.getRepositoryService()
                .createDeployment()
                .addClasspathResource(String.format(DIAGRAM_PATH, bpmnFileName))
                .deploy();
        processInstance = engine.getRuntimeService().startProcessInstanceByKey(processId);
    }

    @Test
    void shouldSuccessfullyCompleteNotifyClaimant_whenCalled() {
        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        //complete the start business process
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertCompleteExternalTask(
                startBusiness,
                START_BUSINESS_TOPIC,
                START_GA_BUSINESS_EVENT,
                START_GA_BUSINESS_ACTIVITY
        );
        ExternalTask notifyTask = assertNextExternalTask(NOTIFY_EVENT);
        assertCompleteExternalTask(
                notifyTask,
                NOTIFY_EVENT,
                MESSAGE_NAME,
                ACTIVITY_ID
        );

        //end business process
        ExternalTask endBusinessProcess = assertNextExternalTask(END_GA_BUSINESS_PROCESS);
        completeGaBusinessProcess(endBusinessProcess);

        assertNoExternalTasksLeft();
    }
}
