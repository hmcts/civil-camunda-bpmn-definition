FROM hmctspublic.azurecr.io/base/java:21-distroless

COPY build/libs/empty.jar /opt/app/

EXPOSE 4000
CMD [ "empty.jar" ]
