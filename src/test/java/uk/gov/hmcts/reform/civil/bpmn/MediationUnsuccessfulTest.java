package uk.gov.hmcts.reform.civil.bpmn;

import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.jupiter.api.Test;
import java.util.Map;

public class MediationUnsuccessfulTest extends BpmnBaseTest {

    private static final String FILE_NAME = "mediation_unsuccessful.bpmn";
    private static final String MESSAGE_NAME = "MEDIATION_UNSUCCESSFUL";
    private static final String PROCESS_ID = "MEDIATION_UNSUCCESSFUL_ID";
    private static final String NOTIFY_MEDIATION_UNSUCCESSFUL_CLAIMANT_LR
        = "NOTIFY_MEDIATION_UNSUCCESSFUL_CLAIMANT_LR";
    private static final String NOTIFY_MEDIATION_UNSUCCESSFUL_DEFENDANT_LIP
        = "NOTIFY_MEDIATION_UNSUCCESSFUL_DEFENDANT_LIP";
    private static final String NOTIFY_MEDIATION_UNSUCCESSFUL_DEFENDANT_1_LR
        = "NOTIFY_MEDIATION_UNSUCCESSFUL_DEFENDANT_1_LR";
    private static final String NOTIFY_MEDIATION_UNSUCCESSFUL_DEFENDANT_2_LR
        = "NOTIFY_MEDIATION_UNSUCCESSFUL_DEFENDANT_2_LR";

    private static final String NOTIFY_MEDIATION_UNSUCCESSFUL_CLAIMANT_LR_ACTIVITY_ID
        = "SendMediationUnsuccessfulClaimantLR";
    private static final String NOTIFY_MEDIATION_UNSUCCESSFUL_DEFENDANT_LIP_ACTIVITY_ID
        = "SendMediationUnsuccessfulDefendantLIP";
    private static final String NOTIFY_MEDIATION_UNSUCCESSFUL_DEFENDANT_SOLICITOR_1_ACTIVITY_ID
        = "SendMediationUnsuccessfulDefendant1LR";
    private static final String NOTIFY_MEDIATION_UNSUCCESSFUL_DEFENDANT_SOLICITOR_2_ACTIVITY_ID
        = "SendMediationUnsuccessfulDefendant2LR";

    public MediationUnsuccessfulTest() {
        super(FILE_NAME, PROCESS_ID);
    }

    @Test
    void shouldSubmitUnsuccessfulMediationForLip() {
        assertProcessStartedWithMessage(MESSAGE_NAME, PROCESS_ID);
        VariableMap variables = Variables.createVariables();
        variables.put(FLOW_FLAGS, Map.of(
            ONE_RESPONDENT_REPRESENTATIVE, false,
            UNREPRESENTED_DEFENDANT_ONE, true
        ));
        startBusinessProcess(variables);

        ExternalTask notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(notificationTask,
                                   PROCESS_CASE_EVENT,
                                   NOTIFY_MEDIATION_UNSUCCESSFUL_CLAIMANT_LR,
                                   NOTIFY_MEDIATION_UNSUCCESSFUL_CLAIMANT_LR_ACTIVITY_ID,
                                   variables
        );

        notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(notificationTask,
                                   PROCESS_CASE_EVENT,
                                   NOTIFY_MEDIATION_UNSUCCESSFUL_DEFENDANT_LIP,
                                   NOTIFY_MEDIATION_UNSUCCESSFUL_DEFENDANT_LIP_ACTIVITY_ID,
                                   variables
        );

        completeBusinessProcess(assertNextExternalTask(END_BUSINESS_PROCESS));
    }

    @Test
    void shouldSubmitUnsuccessfulMediationFor1v1LR() {
        assertProcessStartedWithMessage(MESSAGE_NAME, PROCESS_ID);
        VariableMap variables = Variables.createVariables();
        variables.put(FLOW_FLAGS, Map.of(
            ONE_RESPONDENT_REPRESENTATIVE, true,
            UNREPRESENTED_DEFENDANT_ONE, false
        ));
        startBusinessProcess(variables);

        ExternalTask notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(notificationTask,
                                   PROCESS_CASE_EVENT,
                                   NOTIFY_MEDIATION_UNSUCCESSFUL_CLAIMANT_LR,
                                   NOTIFY_MEDIATION_UNSUCCESSFUL_CLAIMANT_LR_ACTIVITY_ID,
                                   variables
        );

        notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(notificationTask,
                                   PROCESS_CASE_EVENT,
                                   NOTIFY_MEDIATION_UNSUCCESSFUL_DEFENDANT_1_LR,
                                   NOTIFY_MEDIATION_UNSUCCESSFUL_DEFENDANT_SOLICITOR_1_ACTIVITY_ID,
                                   variables
        );

        completeBusinessProcess(assertNextExternalTask(END_BUSINESS_PROCESS));
    }

    @Test
    void shouldSubmitUnsuccessfulMediationFor1v2LR() {
        assertProcessStartedWithMessage(MESSAGE_NAME, PROCESS_ID);
        VariableMap variables = Variables.createVariables();
        variables.put(FLOW_FLAGS, Map.of(
            TWO_RESPONDENT_REPRESENTATIVES, true,
            UNREPRESENTED_DEFENDANT_ONE, false
        ));
        startBusinessProcess(variables);

        ExternalTask notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(notificationTask,
                                   PROCESS_CASE_EVENT,
                                   NOTIFY_MEDIATION_UNSUCCESSFUL_CLAIMANT_LR,
                                   NOTIFY_MEDIATION_UNSUCCESSFUL_CLAIMANT_LR_ACTIVITY_ID,
                                   variables
        );

        notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(notificationTask,
                                   PROCESS_CASE_EVENT,
                                   NOTIFY_MEDIATION_UNSUCCESSFUL_DEFENDANT_1_LR,
                                   NOTIFY_MEDIATION_UNSUCCESSFUL_DEFENDANT_SOLICITOR_1_ACTIVITY_ID,
                                   variables
        );

        notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(notificationTask,
                                   PROCESS_CASE_EVENT,
                                   NOTIFY_MEDIATION_UNSUCCESSFUL_DEFENDANT_2_LR,
                                   NOTIFY_MEDIATION_UNSUCCESSFUL_DEFENDANT_SOLICITOR_2_ACTIVITY_ID,
                                   variables
        );

        completeBusinessProcess(assertNextExternalTask(END_BUSINESS_PROCESS));
    }
}
