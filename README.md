# civil-camunda-bpmn-definition

Civil Camunda BPMN Definition

### Contents:
- [Building and deploying application](#building-and-deploying-the-application)
- [Camunda](#camunda)
- [Scheduled jobs](#scheduled-jobs)
- [Adding Git Conventions](#adding-git-conventions)

## Building and deploying the application

### Dependencies

The project is dependent on other Civil repositories:
- [civil-service](https://github.com/hmcts/civil-service)
- [civil-ccd-definition](https://github.com/hmcts/civil-ccd-definition)

To set up complete local environment for Civil check [civil-sdk](https://github.com/hmcts/civil-sdk)

### Preview environment

Preview environment will be created when opening new PR.
Camunda BPMN definitions will be pulled from the latest GitHub release.
Service instance will be running the latest version deployed to ACR.

To access XUI visit url (make sure that it starts with `https`, otherwise IDAM won't let you log in):
- `https://xui-civil-camunda-pr-PR_NUMBER.service.core-compute-preview.internal`

To access Camunda visit url (login and password are both `admin`):
- `https://camunda-civil-camunda-pr-PR_NUMBER.service.core-compute-preview.internal`

## Camunda

To spin up local instance of Camunda go to `civil-sdk` repo and follow the instructions.

Camunda UI runs on `http://localhost:9404`. You can login with:
```$xslt
username: demo
password: demo
```

The REST API is available at `http://localhost:9404/engine-rest/`. Documentation is available [here](https://docs.camunda.org/manual/latest/reference/rest/).

To upload all bpmn diagrams via the REST API go to`civil-sdk` repo and run `./bin/import-bpmn-diagram.sh` script.

The diagram must exist within `src/main/resources/camunda`.

By setting `CAMUNDA_BASE_URL` env variable you can also use this script to upload diagrams to Camunda in other environments.

## Scheduled jobs

The project defines a set of timer-driven Camunda processes that keep cases moving without manual input. The table below lists each job, the external topic it drives, the cadence (Quartz cron expression, UTC), and the high-level responsibility.

<!-- SCHEDULED_JOBS_TABLE_START -->
| Job | Purpose | Camunda topic(s) | Schedule (cron, UTC) | When it runs |
| --- | --- | --- | --- | --- |
| Bundle creation scheduler | Builds bundles for eligible hearings each evening. | `BUNDLE_CREATION_CHECK` | `0 0 21 * * ?` | Daily at 21:00 |
| Case dismissed scheduler | Automatically dismisses claims that have missed their deadlines. | `CASE_DISMISSED` | `0 5 16 * * ?` | Daily at 16:05 |
| Decision outcome scheduler | Moves cases awaiting judicial decisions into the decision outcome workflow. | `MOVE_TO_DECISION_OUTCOME` | `0 0 0 * * ?` | Daily at 00:00 |
| Defendant response deadline check scheduler | Sweeps for defendants whose response deadline elapsed and triggers enforcement. | `DEFENDANT_RESPONSE_DEADLINE_CHECK` | `0 1 16 * * ?` | Daily at 16:01 |
| Evidence upload scheduler | Prompts parties to upload evidence when deadlines are approaching. | `EVIDENCE_UPLOAD_CHECK` | `0 30 17 * * ?` | Daily at 17:30 |
| Full admit pay immediately no payment scheduler | Escalates full-admit cases where an immediate payment was promised but not received. | `FULL_ADMIT_PAY_IMMEDIATELY_NO_PAYMENT_CHECK` | `0 0 0 * * ?` | Daily at 00:00 |
| GA doc upload notify scheduler | Sends notifications when GA supporting documents are uploaded. | `GADocUploadNotifyScheduler` | `0 0 23 * * ?` | Daily at 23:00 |
| GA order made scheduler | Publishes GA order-made events to downstream services each afternoon. | `GAOrderMadeScheduler` | `0 15 16 ? * * *` | Daily at 16:15 |
| GA response deadline processor | Processes GA response deadlines, judge revisits and respondent checks. | `GAResponseDeadlineProcessor`<br>`GAJudgeRevisitProcessor`<br>`GARespondentResponseCheckScheduler` | `0 15 17 * * ?` | Daily at 17:15 |
| GA unless order scheduler | Enforces GA Unless Orders once the compliance deadline passes. | `GAUnlessOrderScheduler` | `0 0 16 ? * * *` | Daily at 16:00 |
| Generate CSV and send to MMT scheduler | Produces nightly CSV/JSON exports for the mediation service (MMT). | `GenerateCsvAndSendToMmt`<br>`GenerateJsonAndSendToMmt` | `0 0 1 ? * * *` | Daily at 01:00 |
| Hearing cvp link scheduler | Issues CVP/remote hearing links on a daily cadence. | `HEARING_CVP_LINK` | `0 0 0 ? * * *` | Daily at 00:00 |
| Hearing fee check scheduler | Checks for unpaid hearing fees and raises the necessary follow-up tasks. | `HEARING_FEE_CHECK` | `0 0 0 * * ?` | Daily at 00:00 |
| Incident retry scheduler | Retries failed external incident tasks each night. | `INCIDENT_RETRY_EVENT` | `0 1 23 * * ?` | Daily at 23:01 |
| Manage Stay WA Task Scheduler | Maintains WA tasks for stayed cases so that no follow-up is missed. | `MANAGE_STAY_WA_TASK_SCHEDULER` | `0 0 1 ? * * *` | Daily at 01:00 |
| Migrate cases scheduler | Reserved cron to re-run large case migration batches. | `MIGRATE_CASES_EVENTS` | `0 0 0 1 * ? 2080` | First day of each month until 2080 at 00:00 |
| Notify claim deadline scheduler | Notifies parties about upcoming claim deadlines, prior to dismissal. | `CASE_DISMISSED` | `0 5 0 * * ?` | Daily at 00:05 |
| Order Review Obligation check scheduler | Checks order review obligations and triggers outstanding actions. | `ORDER_REVIEW_OBLIGATION_CHECK` | `0 0 1 * * ?` | Daily at 01:00 |
| Polling event emitter scheduler | Emits polling events across the day so downstream pollers stay in sync. | `POLLING_EVENT_EMITTER` | `0 0 8-20 * * ?` | Hourly at the top of the hour from 08:00–20:00 |
| Proof of debt scheduler | Generates proof-of-debt artefacts for COSC-linked general applications. | `CoscApplicationProcessor` | `0 0 16 * * ?` | Daily at 16:00 |
| Request for reconsideration notification check scheduler | Ensures reconsideration notifications are sent when conditions are met. | `REQUEST_FOR_RECONSIDERATION_NOTIFICATION_CHECK` | `0 0 0 * * ?` | Daily at 00:00 |
| Retrigger cases scheduler | One-off cron to retrigger case updates as part of the 2026 migration plan. | `RETRIGGER_CASES_EVENTS` | `0 0 0 1 * ? 2026` | First day of each month until 2026 at 00:00 |
| Settlement no response from defendant scheduler | Moves settlement agreements forward when the defendant failed to respond. | `SETTLEMENT_NO_RESPONSE_FROM_DEFENDANT_CHECK` | `0 0 1 * * ?` | Daily at 01:00 |
| Spec automated hearing notice scheduler | Builds automated hearing notices for Spec claims twice per day. | `AUTOMATED_HEARING_NOTICE` | `0 0 0,12 ? * * *` | Twice daily at 00:00 and 12:00 |
| Take case offline scheduler | Transitions cases that must move off digital rails. | `TAKE_CASE_OFFLINE` | `0 5 16 * * ?` | Daily at 16:05 |
| Trial ready check scheduler | Verifies trial readiness status for outstanding cases. | `TRIAL_READY_CHECK` | `0 0 0 * * ?` | Daily at 00:00 |
| Trial ready notification scheduler | Sends notifications when trial readiness has been confirmed. | `TRIAL_READY_NOTIFICATION_CHECK` | `0 0 0 * * ?` | Daily at 00:00 |
| Unspec automated hearing notice scheduler | Builds automated hearing notices for Unspec claims twice per day. | `AUTOMATED_HEARING_NOTICE` | `0 0 0,12 ? * * *` | Twice daily at 00:00 and 12:00 |
| Update General application Case management Location | Future-dated cron to re-sync GA case management locations. | `RETRIGGER_GA_UPDATE_CMLOCATION_EVENTS` | `0 0 0 1 * ? 2046` | First day of each month until 2046 at 00:00 |
| Update location | Future-dated cron to re-sync the main case location. | `RETRIGGER_UPDATE_LOCATION_EVENTS` | `0 0 0 1 * ? 2046` | First day of each month until 2046 at 00:00 |
<!-- SCHEDULED_JOBS_TABLE_END -->

Run `python3 bin/update-scheduled-jobs-table.py` whenever a BPMN scheduler is added or updated so the table stays in sync. The script reads every BPMN timer, merges in the human-readable descriptions held in `config/scheduled-jobs.json`, and rewrites the table between the markers above. If you add a new scheduler (or change the purpose of an existing one), update the JSON file first so the generated table has meaningful text. The `Verify scheduled jobs table` GitHub Action reruns this script on `master` and fails if `README.md` would change.

## Adding Git Conventions

### Include the git conventions.
* Make sure your git version is at least 2.9 using the `git --version` command
* Run the following command:
```
git config --local core.hooksPath .git-config/hooks
```
Once the above is done, you will be required to follow specific conventions for your commit messages and branch names.

If you violate a convention, the git error message will report clearly the convention you should follow and provide
additional information where necessary.

*Optional:*
* Install this plugin in Chrome: https://github.com/refined-github/refined-github

  It will automatically set the title for new PRs according to the first commit message, so you won't have to change it manually.

  Note that it will also alter other behaviours in GitHub. Hopefully these will also be improvements to you.

*In case of problems*

1. Get in touch with your Technical Lead so that they can get you unblocked
2. If the rare eventuality that the above is not possible, you can disable enforcement of conventions using the following command

   `git config --local --unset core.hooksPath`

   Still, you shouldn't be doing it so make sure you get in touch with a Technical Lead soon afterwards.

## License
This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details
