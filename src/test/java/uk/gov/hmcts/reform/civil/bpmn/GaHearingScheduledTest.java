package uk.gov.hmcts.reform.civil.bpmn;

import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

class GaHearingScheduledTest extends BpmnBaseHearingScheduledGATest {

    private static final String MESSAGE_NAME = "HEARING_SCHEDULED_GA";
    private static final String PROCESS_ID = "HEARING_SCHEDULED_PROCESS_ID";

    private static final String GENERATE_HEARING_NOTICE_EVENT = "GENERATE_HEARING_NOTICE_DOCUMENT";
    private static final String GENERATE_HEARING_FORM_ACTIVITY_ID = "GenerateHearingNoticeDocument";

    private static final String ADD_PDF_EVENT = "ADD_PDF_TO_MAIN_CASE";
    private static final String ADD_PDF_ID = "LinkDocumentToParentCase";

    private static final String NOTIFY_HEARING_NOTICE_EVENT = "NOTIFY_HEARING_NOTICE";
    private static final String NOTIFY_HEARING_NOTICE_ACTIVITY_ID = "NotifyHearingNotice";

    public GaHearingScheduledTest() {
        super("ga_hearing_scheduled_access.bpmn", PROCESS_ID);
    }

    @ParameterizedTest
    @ValueSource(strings = {"true", "false"})
    void shouldSuccessfullyCompleteCreatePDFDocument_whenCalled(Boolean rpaContinuousFeed) {
        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage(MESSAGE_NAME).getKey()).isEqualTo(PROCESS_ID);

        VariableMap variables = Variables.createVariables();
        variables.put("flowFlags", Map.of("RPA_CONTINUOUS_FEED", rpaContinuousFeed));

        //complete the start business process
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertCompleteExternalTask(
            startBusiness,
            START_BUSINESS_TOPIC,
            START_BUSINESS_EVENT,
            START_BUSINESS_ACTIVITY,
            variables
        );
        //Generate Hearing Notice Document
        ExternalTask generateHearingNoticeDocument = assertNextExternalTask(APPLICATION_PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            generateHearingNoticeDocument,
            APPLICATION_PROCESS_CASE_EVENT,
            GENERATE_HEARING_NOTICE_EVENT,
            GENERATE_HEARING_FORM_ACTIVITY_ID,
            variables
        );

        //Link Document to main case event
        ExternalTask addDocumentToMainCase = assertNextExternalTask(PROCESS_EXTERNAL_CASE_EVENT);
        assertCompleteExternalTask(
            addDocumentToMainCase,
            PROCESS_EXTERNAL_CASE_EVENT,
            ADD_PDF_EVENT,
            ADD_PDF_ID,
            variables
        );

        //Notify Hearing Notice
        ExternalTask notifyHearingNotice = assertNextExternalTask(PROCESS_EXTERNAL_CASE_EVENT);
        assertCompleteExternalTask(
            notifyHearingNotice,
            PROCESS_EXTERNAL_CASE_EVENT,
            NOTIFY_HEARING_NOTICE_EVENT,
            NOTIFY_HEARING_NOTICE_ACTIVITY_ID,
            variables
        );

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
