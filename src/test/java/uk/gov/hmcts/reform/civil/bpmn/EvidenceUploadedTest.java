package uk.gov.hmcts.reform.civil.bpmn;

import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class EvidenceUploadedTest extends BpmnBaseTest {

    private static final String MESSAGE_NAME = "EVIDENCE_UPLOADED";
    private static final String PROCESS_ID = "EVIDENCE_UPLOADED_PROCESS_ID";

    private static final String CREATE_DASHBOARD_NOTIFICATION_EVIDENCE_UPLOADED_CLAIMANT = "CREATE_DASHBOARD_NOTIFICATION_EVIDENCE_UPLOADED_CLAIMANT";
    private static final String CREATE_DASHBOARD_NOTIFICATION_EVIDENCE_UPLOADED_CLAIMANT_ACTIVITY_ID = "GenerateDashboardNotificationEvidenceUploadedClaimant";
    private static final String CREATE_DASHBOARD_NOTIFICATION_EVIDENCE_UPLOADED_DEFENDANT = "CREATE_DASHBOARD_NOTIFICATION_EVIDENCE_UPLOADED_DEFENDANT";
    private static final String CREATE_DASHBOARD_NOTIFICATION_EVIDENCE_UPLOADED_DEFENDANT_ACTIVITY_ID = "GenerateDashboardNotificationEvidenceUploadedDefendant";

    public EvidenceUploadedTest() {
        super("evidence_uploaded.bpmn", "EVIDENCE_UPLOADED_PROCESS_ID");
    }

    @Test
    void shouldSuccessfullyCompleteEvidenceUpload_whenCalled() {
        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        //complete the start business process
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertCompleteExternalTask(startBusiness,
                                   START_BUSINESS_TOPIC,
                                   START_BUSINESS_EVENT,
                                   START_BUSINESS_ACTIVITY);

        //complete the notification to applicant
        ExternalTask documentGeneration = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(documentGeneration,
                                   PROCESS_CASE_EVENT,
                                   CREATE_DASHBOARD_NOTIFICATION_EVIDENCE_UPLOADED_CLAIMANT,
                                   CREATE_DASHBOARD_NOTIFICATION_EVIDENCE_UPLOADED_CLAIMANT_ACTIVITY_ID);

        //complete the notification to defendant
        ExternalTask notification = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(notification,
                                   PROCESS_CASE_EVENT,
                                   CREATE_DASHBOARD_NOTIFICATION_EVIDENCE_UPLOADED_DEFENDANT,
                                   CREATE_DASHBOARD_NOTIFICATION_EVIDENCE_UPLOADED_DEFENDANT_ACTIVITY_ID);

        //end business process
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
