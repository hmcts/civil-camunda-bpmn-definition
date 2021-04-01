package uk.gov.hmcts.reform.unspec.bpmn;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.camunda.bpm.engine.externaltask.LockedExternalTask;
import org.camunda.bpm.engine.management.JobDefinition;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.variable.VariableMap;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.camunda.bpm.engine.ProcessEngineConfiguration.createStandaloneInMemProcessEngineConfiguration;

public abstract class BpmnBaseTest {

    private static final String DIAGRAM_PATH = "camunda/%s";
    public static final String WORKER_ID = "test-worker";
    public static final String START_BUSINESS_TOPIC = "START_BUSINESS_PROCESS";
    public static final String START_BUSINESS_EVENT = "START_BUSINESS_PROCESS";
    public static final String START_BUSINESS_ACTIVITY = "StartBusinessProcessTaskId";
    public static final String PROCESS_CASE_EVENT = "processCaseEvent";
    public static final String END_BUSINESS_PROCESS = "END_BUSINESS_PROCESS";
    public static final String ERROR_CODE = "TEST_CODE";

    public final String bpmnFileName;
    public final String processId;
    public Deployment deployment;
    public Deployment endBusinessProcessDeployment;
    public Deployment startBusinessProcessDeployment;
    public static ProcessEngine engine;

    public ProcessInstance processInstance;

    public BpmnBaseTest(String bpmnFileName, String processId) {
        this.bpmnFileName = bpmnFileName;
        this.processId = processId;
    }

    @BeforeAll
    static void startEngine() {
        ProcessEngineConfiguration configuration = createStandaloneInMemProcessEngineConfiguration();

        engine = configuration.buildProcessEngine();
    }

    @BeforeEach
    void setup() {
        //deploy process
        startBusinessProcessDeployment = engine.getRepositoryService()
            .createDeployment()
            .addClasspathResource(String.format(DIAGRAM_PATH, "start_business_process.bpmn"))
            .deploy();
        endBusinessProcessDeployment = engine.getRepositoryService()
            .createDeployment()
            .addClasspathResource(String.format(DIAGRAM_PATH, "end_business_process.bpmn"))
            .deploy();
        deployment = engine.getRepositoryService()
            .createDeployment()
            .addClasspathResource(String.format(DIAGRAM_PATH, bpmnFileName))
            .deploy();
        processInstance = engine.getRuntimeService().startProcessInstanceByKey(processId);
    }

    @AfterEach
    void tearDown() {
        engine.getRepositoryService().deleteDeployment(startBusinessProcessDeployment.getId());
        engine.getRepositoryService().deleteDeployment(endBusinessProcessDeployment.getId());
        engine.getRepositoryService().deleteDeployment(deployment.getId());
    }

    @AfterAll
    static void shutDown() {
        engine.close();
    }

    void deployDiagram(String bpmnFileName) {
        deployment = engine.getRepositoryService()
            .createDeployment()
            .addClasspathResource(String.format(DIAGRAM_PATH, bpmnFileName))
            .deploy();
    }

    /**
     * Retrieves an explicit representation of a task to trigger a process execution, i.e whenever a wait
     * state is triggered in a bmpn diagram, for example, a timer event or a user task.
     *
     * @return a list of jobs in the current execution.
     */
    public List<JobDefinition> getJobs() {
        return engine.getManagementService().createJobDefinitionQuery().list();
    }

    /**
     * Retrieves a list of topic names defined within the bpmn diagram.
     *
     * @return the list of topics in the current execution.
     */
    public List<String> getTopics() {
        return engine.getExternalTaskService().getTopicNames();
    }

    /**
     * Retrieves a list of external tasks.
     *
     * @return a list of external tasks available in the current execution.
     */
    public List<ExternalTask> getExternalTasks() {
        return engine.getExternalTaskService().createExternalTaskQuery().list();
    }

    /**
     * Retrieves a process definition which has a start message event with the messageName.
     *
     * @param messageName the name of the message.
     * @return process definitions with given message start message event.
     */
    public ProcessDefinition getProcessDefinitionByMessage(String messageName) {
        return engine.getRepositoryService()
            .createProcessDefinitionQuery()
            .messageEventSubscriptionName(messageName)
            .singleResult();
    }

    /**
     * Fetches an external task by topic name and locks it to a worker ready for handling.
     *
     * @param topicName the name of the topic to fetch.
     * @return a list of external tasks locked to the worked id.
     */
    public List<LockedExternalTask> fetchAndLockTask(String topicName) {
        return engine.getExternalTaskService()
            .fetchAndLock(1, WORKER_ID)
            .topic(topicName, 100)
            .execute();
    }

    /**
     * Completes an external task with the given id.
     *
     * @param taskId the id of the external task to complete.
     */
    public void completeTask(String taskId) {
        engine.getExternalTaskService().complete(taskId, WORKER_ID);
    }

    /**
     * Completes an external task with the given id and variables.
     *
     * @param taskId the id of the external task to complete.
     */
    public void completeTask(String taskId, VariableMap variables) {
        engine.getExternalTaskService().complete(taskId, WORKER_ID, variables);
    }

    /**
     * Fails an external task with the given id and variables by throwing bpmn error.
     *
     * @param taskId the id of the external task to complete.
     */
    public void failTask(String taskId) {
        engine.getExternalTaskService().handleBpmnError(taskId, WORKER_ID, ERROR_CODE);
    }

    /**
     * Get external task for topic name.
     */
    public ExternalTask assertNextExternalTask(String topicName) {
        assertThat(getTopics()).containsOnly(topicName);

        List<ExternalTask> externalTasks = getExternalTasks();
        assertThat(externalTasks).hasSize(1);

        ExternalTask externalTask = externalTasks.get(0);
        assertThat(externalTask.getTopicName()).isEqualTo(topicName);

        return externalTask;
    }

    /**
     * Completes the external task with topic name.
     *
     * @param externalTask the id of the external task to complete.
     * @param topicName    is taskName.
     * @param caseEvent    is input variable for external task.
     * @param activityId   is input variable for camunda activity id.
     */
    public void assertCompleteExternalTask(ExternalTask externalTask, String topicName,
                                           String caseEvent, String activityId) {
        assertCompleteExternalTask(externalTask, topicName, caseEvent, activityId, null);
    }

    /**
     * Completes the external task with topic name and variables.
     *
     * @param externalTask the id of the external task to complete.
     * @param topicName    is taskName.
     * @param caseEvent    is input variable for external task.
     * @param activityId   is input variable for camunda activity id.
     * @param variables    is input variable for output variable map.
     */
    public void assertCompleteExternalTask(
        ExternalTask externalTask,
        String topicName,
        String caseEvent,
        String activityId,
        VariableMap variables
    ) {
        List<LockedExternalTask> lockedProcessTask = fetchAndLockTask(topicName);

        assertExternalTask(externalTask, topicName, caseEvent, activityId, lockedProcessTask);

        completeTask(lockedProcessTask.get(0).getId(), variables);
    }

    public void assertFailExternalTask(
        ExternalTask externalTask,
        String topicName,
        String caseEvent,
        String activityId
    ) {
        List<LockedExternalTask> lockedProcessTask = fetchAndLockTask(topicName);

        assertExternalTask(externalTask, topicName, caseEvent, activityId, lockedProcessTask);

        failTask(lockedProcessTask.get(0).getId());
    }

    public void assertNoExternalTasksLeft() {
        List<ExternalTask> externalTasksAfter = getExternalTasks();
        assertThat(externalTasksAfter).isEmpty();
    }

    /**
     * Completes the external task with topic name END_BUSINESS_PROCESS.
     *
     * @param externalTask the id of the external task to complete.
     */
    public void completeBusinessProcess(ExternalTask externalTask) {
        assertThat(externalTask.getTopicName()).isEqualTo("END_BUSINESS_PROCESS");

        List<LockedExternalTask> lockedEndBusinessProcessTask = fetchAndLockTask("END_BUSINESS_PROCESS");

        assertThat(lockedEndBusinessProcessTask).hasSize(1);
        completeTask(lockedEndBusinessProcessTask.get(0).getId());
    }

    private void assertExternalTask(
        ExternalTask externalTask,
        String topicName,
        String caseEvent,
        String activityId,
        List<LockedExternalTask> lockedProcessTask
    ) {
        assertThat(externalTask.getTopicName()).isEqualTo(topicName);

        assertThat(lockedProcessTask).hasSize(1);

        assertThat(lockedProcessTask.get(0).getVariables()).containsEntry("caseEvent", caseEvent);

        assertThat(lockedProcessTask.get(0).getActivityId()).isEqualTo(activityId);
    }
}
