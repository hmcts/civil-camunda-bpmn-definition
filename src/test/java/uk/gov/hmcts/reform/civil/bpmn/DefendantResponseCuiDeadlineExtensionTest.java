package uk.gov.hmcts.reform.civil.bpmn;

import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class DefendantResponseCuiDeadlineExtensionTest extends BpmnBaseTest {


    public static final String MESSAGE_NAME = "DEFENDANT_RESPONSE_CUI_DEADLINE_EXTENSION";
    public static final String PROCESS_ID = "DEFENDANT_RESPONSE_CUI_DEADLINE_EXTENSION_PROCESS_ID";

    //CCD Case Event
    private static final String NOTIFY_CLAIMANT_CUI_FOR_DEADLINE_EXTENSION
        = "NOTIFY_CLAIMANT_CUI_FOR_DEADLINE_EXTENSION";
    private static final String NOTIFY_CLAIMANT_CUI_FOR_DEADLINE_EXTENSION_PROCESS_ID
        = "DefendantResponseDeadlineExtensionNotifyClaimant";
    private static final String NOTIFY_DEFENDANT_CUI_FOR_DEADLINE_EXTENSION
        = "NOTIFY_DEFENDANT_CUI_FOR_DEADLINE_EXTENSION";
    private static final String NOTIFY_DEFENDANT_CUI_FOR_DEADLINE_EXTENSION_PROCESS_ID
        = "DefendantResponseDeadlineExtensionNotifyDefendant";

    public DefendantResponseCuiDeadlineExtensionTest() {
        super(
            "defendant_response_cui_deadline_extension.bpmn",
            PROCESS_ID
        );
    }

    @Test
    void shouldNotifyClaimantAndDefendantOnResponseDeadlineExtensionRequestSuccessfully() {

        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        assertBusinessProcessHasStarted();
        verifyClaimantNotificationOnResponseDeadlineExtensionRequest();
        verifyDefendantNotificationOnResponseDeadlineExtensionRequest();


        endBusinessProcess();
        assertNoExternalTasksLeft();
    }

    private void verifyClaimantNotificationOnResponseDeadlineExtensionRequest() {
        verifyTaskIsComplete(
            NOTIFY_CLAIMANT_CUI_FOR_DEADLINE_EXTENSION,
            NOTIFY_CLAIMANT_CUI_FOR_DEADLINE_EXTENSION_PROCESS_ID
        );
    }

    private void verifyDefendantNotificationOnResponseDeadlineExtensionRequest() {
        verifyTaskIsComplete(
            NOTIFY_DEFENDANT_CUI_FOR_DEADLINE_EXTENSION,
            NOTIFY_DEFENDANT_CUI_FOR_DEADLINE_EXTENSION_PROCESS_ID
        );
    }

    private void verifyTaskIsComplete(String caseEvent, String actionId) {
        ExternalTask externalTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            externalTask,
            PROCESS_CASE_EVENT,
            caseEvent,
            actionId
        );
    }

    private void assertBusinessProcessHasStarted() {
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertCompleteExternalTask(
            startBusiness,
            START_BUSINESS_TOPIC,
            START_BUSINESS_EVENT,
            START_BUSINESS_ACTIVITY
        );
    }

    private void endBusinessProcess() {
        ExternalTask endBusinessProcess = assertNextExternalTask(END_BUSINESS_PROCESS);
        completeBusinessProcess(endBusinessProcess);
    }

}
