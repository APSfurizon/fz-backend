
FROM amazoncorretto:23-alpine

ENV APP_HOME=/app

COPY application-*.jar $APP_HOME/application.jar
COPY templates/* $APP_HOME/templates/
COPY *-key-autocart.rsa $APP_HOME/

RUN apk --no-cache -s upgrade && apk --no-cache upgrade

WORKDIR $APP_HOME

RUN addgroup --system --gid 1001 fz-backend
RUN adduser --system --uid 1001 fz-backend

USER fz-backend

EXPOSE 8081

CMD ["java",  "-XX:+UseG1GC", "-Xms1024m", "-Xmx1024m", "-XX:MaxGCPauseMillis=500",  "-jar", "./application.jar"]