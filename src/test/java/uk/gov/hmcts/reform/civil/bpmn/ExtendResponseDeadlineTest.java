package uk.gov.hmcts.reform.civil.bpmn;

import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class ExtendResponseDeadlineTest extends BpmnBaseTest {
    private static final String MESSAGE_NAME = "EXTEND_RESPONSE_DEADLINE";
    private static final String PROCESS_ID = "EXTEND_RESPONSE_DEADLINE_PROCESS_ID";
    private static final String NOTIFY_CLAIMANT_EVENT = "NOTIFY_CLAIMANT_CUI_FOR_DEADLINE_EXTENSION";
    private static final String NOTIFY_CLAIMANT_ACTIVITY_NAME = "DefendantResponseDeadlineExtensionNotifyClaimant";
    private static final String NOTIFY_LIP_DEFENDANT_EVENT = "NOTIFY_DEFENDANT_CUI_FOR_DEADLINE_EXTENSION";
    private static final String NOTIFY_LIP_DEFENDANT_ACTIVITY_NAME
        = "DefendantResponseDeadlineExtensionNotifyDefendant";

    public ExtendResponseDeadlineTest() {
        super("extend_response_deadline.bpmn", PROCESS_ID);
    }

    @ParameterizedTest
    @CsvSource({"true, null"})
    void shouldNotifyClaimantAndDefendantAboutResponseDeadlineExtension() {
        assertFalse(processInstance.isEnded());
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);
        VariableMap variables = Variables.createVariables();
        variables.putValue(FLOW_FLAGS, Map.of(UNREPRESENTED_DEFENDANT_ONE, true));

        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertCompleteExternalTask(
            startBusiness,
            START_BUSINESS_TOPIC,
            START_BUSINESS_EVENT,
            START_BUSINESS_ACTIVITY,
            variables
        );

        ExternalTask notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            notificationTask,
            PROCESS_CASE_EVENT,
            NOTIFY_CLAIMANT_EVENT,
            NOTIFY_CLAIMANT_ACTIVITY_NAME,
            variables
        );

        notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            notificationTask,
            PROCESS_CASE_EVENT,
            NOTIFY_LIP_DEFENDANT_EVENT,
            NOTIFY_LIP_DEFENDANT_ACTIVITY_NAME,
            variables
        );

        ExternalTask endBusinessProcess = assertNextExternalTask(END_BUSINESS_PROCESS);
        completeBusinessProcess(endBusinessProcess);

        assertNoExternalTasksLeft();

    }

}
