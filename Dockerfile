FROM davidcaste/alpine-java-unlimited-jce:jre8
ARG JAR_FILE
ADD target/${JAR_FILE} burnaki.jar
ADD src/main/docker/wrapper.sh wrapper.sh
RUN bash -c 'chmod +x /wrapper.sh'
RUN bash -c 'touch /burnaki.jar'
ENTRYPOINT ["/bin/bash", "/wrapper.sh"]