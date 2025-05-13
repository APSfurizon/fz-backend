
FROM amazoncorretto:23-alpine-jdk

ENV APP_HOME=/app

COPY application-*.jar $APP_HOME/application.jar
COPY prodkeys/*-key-autocart.rsa $APP_HOME/
COPY hotel-names.json $APP_HOME/
COPY templates/ $APP_HOME/templates/
COPY libs/ $APP_HOME/libs/


RUN apk --no-cache -s upgrade && apk --no-cache upgrade && apk add musl-locales && apk add lang

WORKDIR $APP_HOME

RUN addgroup --system --gid 1001 fz-backend
RUN adduser --system --uid 1001 fz-backend
RUN chown -R fz-backend:fz-backend $APP_HOME/templates/
USER fz-backend

EXPOSE 9091

CMD ["java",  "-XX:+UseG1GC", "-Xms2048m", "-Xmx2048m", "-XX:MaxGCPauseMillis=500",  "-jar", "./application.jar"]
