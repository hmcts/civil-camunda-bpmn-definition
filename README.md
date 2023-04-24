# civil-camunda-bpmn-definition

Civil Camunda BPMN Definition


### Contents:
- [Building and deploying application](#building-and-deploying-the-application)
- [Camunda](#camunda)
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

