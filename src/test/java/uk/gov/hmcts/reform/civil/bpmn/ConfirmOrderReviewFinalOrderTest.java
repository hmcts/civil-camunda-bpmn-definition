package uk.gov.hmcts.reform.civil.bpmn;

import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class ConfirmOrderReviewFinalOrderTest extends BpmnBaseTest {

    public static final String MESSAGE_NAME = "CONFIRM_ORDER_REVIEW_FINAL_ORDER";
    public static final String PROCESS_ID = "CONFIRM_ORDER_REVIEW_FINAL_ORDER";

    //CCD CASE EVENT
    public static final String UPDATE_TASK_LIST_CONFIRM_ORDER_REVIEW_CLAIMANT
        = "UPDATE_TASK_LIST_CONFIRM_ORDER_REVIEW_CLAIMANT";
    public static final String UPDATE_TASK_LIST_CONFIRM_ORDER_REVIEW_DEFENDANT
        = "UPDATE_TASK_LIST_CONFIRM_ORDER_REVIEW_DEFENDANT";

    //ACTIVITY IDs
    private static final String UPDATE_TASK_LIST_CONFIRM_ORDER_REVIEW_CLAIMANT_ACTIVITY_ID
        = "UpdateTaskListConfirmOrderReviewClaimant";
    private static final String UPDATE_TASK_LIST_CONFIRM_ORDER_REVIEW_DEFENDANT_ACTIVITY_ID
        = "UpdateTaskListConfirmOrderReviewDefendant";

    public ConfirmOrderReviewFinalOrderTest() {
        super("confirm_order_review_final_order.bpmn", PROCESS_ID);
    }

    @Test
    void shouldSuccessfullyCompleteConfirmOrderReviewFinalOrder() {
        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        VariableMap variables = Variables.createVariables();

        //complete the start business process
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertCompleteExternalTask(startBusiness, START_BUSINESS_TOPIC,
                                   START_BUSINESS_EVENT, START_BUSINESS_ACTIVITY, variables
        );

        ExternalTask notificationTask;

        //complete the claimant update
        notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(notificationTask, PROCESS_CASE_EVENT,
                                   UPDATE_TASK_LIST_CONFIRM_ORDER_REVIEW_CLAIMANT,
                                   UPDATE_TASK_LIST_CONFIRM_ORDER_REVIEW_CLAIMANT_ACTIVITY_ID,
                                   variables
        );

        //complete the defendant update
        notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(notificationTask, PROCESS_CASE_EVENT,
                                   UPDATE_TASK_LIST_CONFIRM_ORDER_REVIEW_DEFENDANT,
                                   UPDATE_TASK_LIST_CONFIRM_ORDER_REVIEW_DEFENDANT_ACTIVITY_ID,
                                   variables
        );

        //end business process
        ExternalTask endBusinessProcess = assertNextExternalTask(END_BUSINESS_PROCESS);
        completeBusinessProcess(endBusinessProcess);

        assertNoExternalTasksLeft();
    }
}
