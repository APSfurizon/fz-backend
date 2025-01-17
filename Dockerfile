
FROM amazoncorretto:23-alpine

ENV APP_HOME=/app

COPY application-*.jar $APP_HOME/application.jar
#COPY templates/* $APP_HOME/templates/jte/
COPY prodkeys/*-key-autocart.rsa $APP_HOME/
#COPY jte-classes/* $APP_HOME/jte-classes/

RUN apk --no-cache -s upgrade && apk --no-cache upgrade

WORKDIR $APP_HOME

RUN addgroup --system --gid 1001 fz-backend
RUN adduser --system --uid 1001 fz-backend
#RUN mkdir $APP_HOME/data/
#RUN mkdir -p $APP_HOME/jte-classes/gg/jte/generated/ondemand/
#RUN chown fz-backend:fz-backend -R $APP_HOME/jte-classes/
#RUN chown fz-backend:fz-backend $APP_HOME/data/

USER fz-backend

EXPOSE 9091

CMD ["java",  "-XX:+UseG1GC", "-Xms4096m", "-Xmx4096m", "-XX:MaxGCPauseMillis=500",  "-jar", "./application.jar"]
