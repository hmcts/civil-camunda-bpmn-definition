FROM hmctspublic.azurecr.io/base/java:openjdk-11-distroless-1.2

COPY build/libs/empty.jar /opt/app/

EXPOSE 4000
CMD [ "empty.jar" ]
