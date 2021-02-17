# civil-damages-camunda-bpmn-definition

Civil Damages Camunda BPMN Definition

### Contents:
- [Building and deploying application](#building-and-deploying-the-application)
- [Camunda](#camunda)

## Building and deploying the application

### Dependencies

The project is dependent on other Civil Damages repositories:
- [civil-damages-service](https://github.com/hmcts/civil-damages-service)
- [civil-damages-ccd-definition](https://github.com/hmcts/civil-damages-ccd-definition)

To set up complete local environment for Civil Damages check [civil-damages-sdk](https://github.com/hmcts/civil-damages-sdk)

### Preview environment

Preview environment will be created when opening new PR.
Camunda BPMN definitions will be pulled from the latest GitHub release.
Service instance will be running the latest version deployed to ACR.

To access XUI visit url (make sure that it starts with `https`, otherwise IDAM won't let you log in):
- `https://xui-civil-damages-camunda-pr-PR_NUMBER.service.core-compute-preview.internal`

To access Camunda visit url (login and password are both `admin`):
- `https://camunda-civil-damages-camunda-pr-PR_NUMBER.service.core-compute-preview.internal`

## Camunda

To spin up local instance of Camunda go to `civil-damages-sdk` repo and follow the instructions.

Camunda UI runs on `http://localhost:9404`. You can login with:
```$xslt
username: demo
password: demo
```

The REST API is available at `http://localhost:9404/engine-rest/`. Documentation is available [here](https://docs.camunda.org/manual/latest/reference/rest/).

To upload all bpmn diagrams via the REST API go to`civil-damages-sdk` repo and run `./bin/import-bpmn-diagram.sh` script.

The diagram must exist within `src/main/resources/camunda`.

By setting `CAMUNDA_BASE_URL` env variable you can also use this script to upload diagrams to Camunda in other environments.

## License
This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details

