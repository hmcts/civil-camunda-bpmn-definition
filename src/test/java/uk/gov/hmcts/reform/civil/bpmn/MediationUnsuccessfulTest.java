package uk.gov.hmcts.reform.civil.bpmn;

import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.jupiter.api.Test;

public class MediationUnsuccessfulTest extends BpmnBaseTest {

    private static final String FILE_NAME = "mediation_unsuccessful.bpmn";
    private static final String MESSAGE_NAME = "MEDIATION_UNSUCCESSFUL";
    private static final String PROCESS_ID = "MEDIATION_UNSUCCESSFUL_ID";
    private static final String NOTIFY_MEDIATION_UNSUCCESSFUL_CLAIMANT_LR
        = "NOTIFY_MEDIATION_UNSUCCESSFUL_CLAIMANT_LR";
    private static final String NOTIFY_MEDIATION_UNSUCCESSFUL_DEFENDANT_LIP
        = "NOTIFY_MEDIATION_UNSUCCESSFUL_DEFENDANT_LIP";

    private static final String NOTIFY_MEDIATION_UNSUCCESSFUL_CLAIMANT_LR_ACTIVITY_ID
        = "SendMediationUnsuccessfulClaimantLR";

    private static final String NOTIFY_MEDIATION_UNSUCCESSFUL_DEFENDANT_LIP_ACTIVITY_ID
        = "SendMediationUnsuccessfulDefendantLIP";

    public MediationUnsuccessfulTest() {
        super(FILE_NAME, PROCESS_ID);
    }

    @Test
    void shouldSubmitUnsuccessfulMediation() {git add .
        assertProcessStartedWithMessage(MESSAGE_NAME, PROCESS_ID);
        VariableMap variables = Variables.createVariables();
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
}
