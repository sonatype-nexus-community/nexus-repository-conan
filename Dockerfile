ARG NEXUS_VERSION=3.18.1
ARG NEXUS_BUILD=01

FROM maven:3-jdk-8-alpine AS build
COPY . /nexus-repository-conan/
RUN cd /nexus-repository-conan/; mvn clean package -PbuildKar;

FROM sonatype/nexus3:$NEXUS_VERSION
ARG CONAN_VERSION=0.0.6
ARG DEPLOY_DIR=/opt/sonatype/nexus/deploy/
USER root
COPY --from=build /nexus-repository-conan/target/nexus-repository-conan-${CONAN_VERSION}-bundle.kar ${DEPLOY_DIR}
USER nexus
