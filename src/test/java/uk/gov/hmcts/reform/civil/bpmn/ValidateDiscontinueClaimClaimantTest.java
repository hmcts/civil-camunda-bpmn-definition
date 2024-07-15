package uk.gov.hmcts.reform.civil.bpmn;

import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class ValidateDiscontinueClaimClaimantTest extends BpmnBaseTest {

    public static final String MESSAGE_NAME = "VALIDATE_DISCONTINUE_CLAIM_CLAIMANT";
    public static final String PROCESS_ID = "VALIDATE_DISCONTINUE_CLAIM_CLAIMANT";

    //CCD CASE EVENT
    public static final String NOTIFY_CLAIMANT_LR_VALIDATION_DICONTINUANCE_FAILURE
        = "NOTIFY_CLAIMANT_LR_VALIDATION_DICONTINUANCE_FAILURE";

    //ACTIVITY IDs
    public static final String NOTIFY_VALIDATION_DICONTINUANCE_FAILURE_CLAIMANT_ACTIVITY_ID
        = "NotifyValidationFailureClaimant";

    public ValidateDiscontinueClaimClaimantTest() {
        super("validate_discontinue_claim_claimant.bpmn", PROCESS_ID);
    }

    @ParameterizedTest
    @CsvSource({"true", "false"})
    void shouldSuccessfullyComplete(boolean discontinuanceValidationSuccess) {

        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        VariableMap variables = Variables.createVariables();
        variables.put("discontinuanceValidationSuccess", discontinuanceValidationSuccess);

        //complete the start business process
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertCompleteExternalTask(
            startBusiness,
            START_BUSINESS_TOPIC,
            START_BUSINESS_EVENT,
            START_BUSINESS_ACTIVITY,
            variables
        );

        if (!discontinuanceValidationSuccess) {
            //complete send email notification to claimant
            ExternalTask notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
            assertCompleteExternalTask(
                notificationTask,
                PROCESS_CASE_EVENT,
                NOTIFY_CLAIMANT_LR_VALIDATION_DICONTINUANCE_FAILURE,
                NOTIFY_VALIDATION_DICONTINUANCE_FAILURE_CLAIMANT_ACTIVITY_ID,
                variables
            );
        }

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
