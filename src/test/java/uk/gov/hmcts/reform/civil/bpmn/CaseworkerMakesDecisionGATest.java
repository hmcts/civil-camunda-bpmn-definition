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

public class CaseworkerMakesDecisionGATest extends BpmnBaseJudgeGASpecTest {

    private static final String MESSAGE_NAME = "APPROVE_CONSENT_ORDER";
    private static final String PROCESS_ID = "APPROVE_CONSENT_ORDER_PROCESS_ID";

    private static final String GENERATE_CONSENT_ORDER_EVENT = "GENERATE_JUDGES_FORM";
    private static final String GENERATE_CONSENT_ORDER_ACTIVITY_ID = "GenerateConsentOrderDocument";

    private static final String ADD_PDF_EVENT = "ADD_PDF_TO_MAIN_CASE";
    private static final String ADD_PDF_ID = "AddDocumentToParentCase";

    private static final String NOTIFY_CONSENT_ORDER_CLAIMANT_EVENT =
        "START_APPLICANT_NOTIFICATION_PROCESS_MAKE_DECISION";
    private static final String NOTIFY_CONSENT_ORDER_CLAIMANT_ACTIVITY_ID = "NotifyConsentOrderClaimant";
    private static final String NOTIFY_CONSENT_ORDER_DEFENDANT_EVENT =
        "START_RESPONDENT_NOTIFICATION_PROCESS_MAKE_DECISION";
    private static final String NOTIFY_CONSENT_ORDER_DEFENDANT_ACTIVITY_ID = "NotifyConsentOrderDefendant";

    public CaseworkerMakesDecisionGATest() {
        super("caseworker_makes_decision_general_application.bpmn", PROCESS_ID);
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
        //Generate Consent Order Document
        ExternalTask generateHearingNoticeDocument = assertNextExternalTask(MAKE_DECISION_CASE_EVENT);
        assertCompleteExternalTask(
            generateHearingNoticeDocument,
            MAKE_DECISION_CASE_EVENT,
            GENERATE_CONSENT_ORDER_EVENT,
            GENERATE_CONSENT_ORDER_ACTIVITY_ID,
            variables
        );

        //Link Document to main case event
        ExternalTask addDocumentToMainCase = assertNextExternalTask(UPDATE_FROM_GA_CASE_EVENT);
        assertCompleteExternalTask(
            addDocumentToMainCase,
            UPDATE_FROM_GA_CASE_EVENT,
            ADD_PDF_EVENT,
            ADD_PDF_ID,
            variables
        );

        //Notify Consent Order Claimant
        ExternalTask notifyHearingNoticeClaimant = assertNextExternalTask(PROCESS_EXTERNAL_CASE_EVENT);
        assertCompleteExternalTask(
            notifyHearingNoticeClaimant,
            PROCESS_EXTERNAL_CASE_EVENT,
            NOTIFY_CONSENT_ORDER_CLAIMANT_EVENT,
            NOTIFY_CONSENT_ORDER_CLAIMANT_ACTIVITY_ID,
            variables
        );

        //Notify Consent Order Defendant(s)
        ExternalTask notifyHearingNoticeDefendant = assertNextExternalTask(PROCESS_EXTERNAL_CASE_EVENT);
        assertCompleteExternalTask(
            notifyHearingNoticeDefendant,
            PROCESS_EXTERNAL_CASE_EVENT,
            NOTIFY_CONSENT_ORDER_DEFENDANT_EVENT,
            NOTIFY_CONSENT_ORDER_DEFENDANT_ACTIVITY_ID,
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
