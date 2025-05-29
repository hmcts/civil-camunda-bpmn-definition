package uk.gov.hmcts.reform.civil.bpmn;

import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;

public class UpdateLanguagePreferenceTest extends BpmnBaseTest {

    private static final String UPDATE_LANGUAGE_PREFERENCE_EVENT = "UPDATE_GA_LANGUAGE_PREFERENCE";
    private static final String UPDATE_LANGUAGE_PREFERENCE_ACTIVITY_ID = "UpdateGenAppLanguagePreference";

    public UpdateLanguagePreferenceTest() {
        super("update_language_preference.bpmn", "UPDATE_LANGUAGE_PREFERENCE_PROCESS_ID");
    }

    @Test
    void shouldRunProcess() {
        //assert process has started
        assertFalse(processInstance.isEnded());
        //complete the start business process
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertCompleteExternalTask(
            startBusiness,
            START_BUSINESS_TOPIC,
            START_BUSINESS_EVENT,
            START_BUSINESS_ACTIVITY
        );

        //Update Language preference for GA
        ExternalTask updateLanguagePreference = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            updateLanguagePreference,
            PROCESS_CASE_EVENT,
            UPDATE_LANGUAGE_PREFERENCE_EVENT,
            UPDATE_LANGUAGE_PREFERENCE_ACTIVITY_ID
        );

        //end business process
        ExternalTask endBusinessProcess = assertNextExternalTask(END_BUSINESS_PROCESS);
        completeBusinessProcess(endBusinessProcess);

        assertNoExternalTasksLeft();
    }
}
