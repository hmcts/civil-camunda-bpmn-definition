package uk.gov.hmcts.reform.civil.bpmn;

public class ExtendResponseDeadlineTest extends BpmnBaseTest{
    private static final String MESSAGE_NAME = "EXTEND_RESPONSE_DEADLINE";
    private static final String PROCESS_ID = "EXTEND_RESPONSE_DEADLINE_PROCESS_ID";
    private static final String NOTIFY_RPA_ON_CONTINUOUS_FEED = "NOTIFY_RPA_ON_CONTINUOUS_FEED";
    private static final String NOTIFY_RPA_ON_CONTINUOUS_FEED_ACTIVITY_ID = "NotifyRoboticsOnContinuousFeed";
    private static final String NOTIFY_CLAIMANT_EVENT = "NOTIFY_CLAIMANT_CUI_FOR_DEADLINE_EXTENSION";
    private static final String NOTIFY_CLAIMANT_ACTIVITY_NAME = "DefendantResponseDeadlineExtensionNotifyClaimant";
    private static final String NOTIFY_LIP_DEFENDANT_EVENT = "NOTIFY_DEFENDANT_CUI_FOR_DEADLINE_EXTENSION";
    private static final String NOTIFY_LIP_DEFENDANT_ACTIVITY_NAME = "DefendantResponseDeadlineExtensionNotifyDefendant";
    public ExtendResponseDeadlineTest() {
        super("extend_response_deadline.bpmn", PROCESS_ID);
    }


}
