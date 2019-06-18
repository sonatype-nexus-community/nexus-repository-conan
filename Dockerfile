ARG NEXUS_VERSION=3.15.1

FROM maven:3-jdk-8-alpine AS build
ARG NEXUS_VERSION=3.15.1
ARG NEXUS_BUILD=01

COPY . /nexus-repository-conan/
RUN cd /nexus-repository-conan/; sed -i "s/3.15.1-01/${NEXUS_VERSION}-${NEXUS_BUILD}/g" pom.xml; \
    mvn clean package -PbuildKar;

FROM sonatype/nexus3:$NEXUS_VERSION
ARG NEXUS_VERSION=3.15.1
ARG NEXUS_BUILD=01
ARG CONAN_VERSION=0.0.6
ARG DEPLOY_DIR=/opt/sonatype/nexus/deploy/
USER root
COPY --from=build /nexus-repository-conan/target/nexus-repository-conan-${CONAN_VERSION}-bundle.kar ${DEPLOY_DIR}
USER nexus
