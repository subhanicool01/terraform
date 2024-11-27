FROM openjdk:11
ARG JAR_SOURCE
ARG JAR_SOURCE ${JAR_SOURCE}  #this formate use java_source is variable
ARG JAR_DEST 
ARG JAR_DEST ${JAR_DEST}
RUN mkdir /opt/admin
WORKDIR /opt/admin
COPY ["${JAR_SOURCE}", "/opt/admin/admin-service-jar"]
RUN chmod 777 /opt/admin
EXPOSE 8080 #it depends upon service which port you can open go to your resours.application.yaml file 
CMD ["java" "jar" "/opt/admin/admin-service-jar"]