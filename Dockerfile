FROM hmctsprod.azurecr.io/base/java:17-distroless

COPY build/libs/civil-camunda-bpmn-definition.jar /opt/app/

EXPOSE 4000
CMD [ "civil-camunda-bpmn-definition.jar" ]
