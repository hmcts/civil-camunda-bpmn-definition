package uk.gov.hmcts.reform.civil.bpmn;

import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class GaHwfAddUpdateTest extends BpmnBaseGASpecTest {

    private static final String FILE_NAME = "ga_hwf_add_update.bpmn";
    private static final String MESSAGE_NAME = "UPDATE_GA_ADD_HWF";
    private static final String PROCESS_ID = "GA_UPDATE_ADD_HWF";
    private static final String ACTIVITY_ID = "Update_App_Add_Hwf";
    private static final String START_BUSINESS_TOPIC = "START_GA_BUSINESS_PROCESS";
    public static final String UPDATE_TOPIC = "applicationProcessCaseEventGASpec";
    public static final String UPDATE_EVENT = "MODIFY_STATE_AFTER_ADDITIONAL_FEE_PAID";

    public GaHwfAddUpdateTest() {
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
                .addClasspathResource(String.format(DIAGRAM_PATH, "end_ga_hwf_notify_process.bpmn"))
                .deploy();
        deployment = engine.getRepositoryService()
                .createDeployment()
                .addClasspathResource(String.format(DIAGRAM_PATH, bpmnFileName))
                .deploy();
        processInstance = engine.getRuntimeService().startProcessInstanceByKey(processId);
    }

    @Test
    void shouldSuccessfullyCompleteNotifyApplicant_whenCalled() {
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
        ExternalTask notifyTask = assertNextExternalTask(UPDATE_TOPIC);
        assertCompleteExternalTask(
                notifyTask,
                UPDATE_TOPIC,
                UPDATE_EVENT,
                ACTIVITY_ID
        );

        //end business process
        ExternalTask endBusinessProcess = assertNextExternalTask(END_GA_HWF_NOTIFY_PROCESS);
        completeGaBusinessProcess(endBusinessProcess);

        assertNoExternalTasksLeft();
    }
}
