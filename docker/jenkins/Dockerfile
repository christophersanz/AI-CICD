# Jenkins local de práctica (ADR-0010).
# - JDK 21: lo necesita ./mvnw.
# - Cliente de Docker con buildx: Testcontainers y docker build (el Dockerfile del proyecto usa BuildKit).
# - Plugins y configuración como código (JCasC): sin asistente inicial; usuario y job se crean al arrancar.
FROM jenkins/jenkins:lts-jdk21

USER root
COPY --from=docker:27-cli /usr/local/bin/docker /usr/local/bin/docker
COPY --from=docker/buildx-bin:0.19.3 /buildx /usr/libexec/docker/cli-plugins/docker-buildx

COPY plugins.txt /usr/share/jenkins/ref/plugins.txt
RUN jenkins-plugin-cli --plugin-file /usr/share/jenkins/ref/plugins.txt

COPY casc.yaml /var/jenkins_casc/jenkins.yaml
ENV CASC_JENKINS_CONFIG=/var/jenkins_casc/jenkins.yaml \
    JAVA_OPTS="-Djenkins.install.runSetupWizard=false"
