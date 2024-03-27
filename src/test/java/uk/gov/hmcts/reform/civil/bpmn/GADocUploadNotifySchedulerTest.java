package uk.gov.hmcts.reform.civil.bpmn;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.text.ParseException;
import java.time.LocalDateTime;
import java.util.List;

import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.camunda.bpm.engine.externaltask.LockedExternalTask;
import org.camunda.bpm.engine.impl.calendar.CronExpression;
import org.camunda.bpm.engine.management.JobDefinition;
import org.junit.jupiter.api.Test;

public class GADocUploadNotifySchedulerTest extends BpmnBaseTest {

    public static final String GA_DOC_UPLOAD_NOTIFY_SCHEDULER = "GADocUploadNotifyScheduler";

    public GADocUploadNotifySchedulerTest() {
        super("general_application_document_upload_notify_scheduler.bpmn", "GA_DOC_UPLOAD_NOTIFY_SCHEDULER");
    }
}
