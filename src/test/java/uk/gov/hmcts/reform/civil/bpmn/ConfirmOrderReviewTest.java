package uk.gov.hmcts.reform.civil.bpmn;

import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class ConfirmOrderReviewTest extends BpmnBaseTest {

    public static final String MESSAGE_NAME = "CONFIRM_ORDER_REVIEW";
    public static final String PROCESS_ID = "CONFIRM_ORDER_REVIEW";

    //CCD CASE EVENT
    public static final String UPDATE_CONFIRM_REVIEW_ORDER_EVENT
        = "UPDATE_CONFIRM_REVIEW_ORDER_EVENT";

    //ACTIVITY IDs
    private static final String UPDATE_CONFIRM_REVIEW_ORDER_EVENT_ACTIVITY_ID
        = "UpdateConfirmOrderReviewEvent";

    public ConfirmOrderReviewTest() {
        super("confirm_order_review.bpmn", PROCESS_ID);
    }

    @Test
    void shouldSuccessfullyCompleteConfirmOrderReview() {
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

        //complete the claimant notification
        notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT_CONFIRM_ORDER_REVIEW);
        assertCompleteExternalTask(notificationTask, PROCESS_CASE_EVENT_CONFIRM_ORDER_REVIEW,
                                   UPDATE_CONFIRM_REVIEW_ORDER_EVENT,
                                   UPDATE_CONFIRM_REVIEW_ORDER_EVENT_ACTIVITY_ID,
                                   variables
        );

        //end business process
        ExternalTask endBusinessProcess = assertNextExternalTask(END_BUSINESS_PROCESS);
        completeBusinessProcess(endBusinessProcess);

        assertNoExternalTasksLeft();
    }
}
