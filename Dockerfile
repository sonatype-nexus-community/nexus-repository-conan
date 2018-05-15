ARG NEXUS_VERSION=3.11.0

FROM maven:3-jdk-8-alpine AS build
ARG NEXUS_VERSION=3.11.0
ARG NEXUS_BUILD=01

COPY . /nexus-repository-conan/
RUN cd /nexus-repository-conan/; sed -i "s/3.11.0-01/${NEXUS_VERSION}-${NEXUS_BUILD}/g" pom.xml; \
    mvn clean package;

FROM sonatype/nexus3:$NEXUS_VERSION
ARG NEXUS_VERSION=3.11.0
ARG NEXUS_BUILD=01
ARG CONAN_VERSION=0.0.2
ARG TARGET_DIR=/opt/sonatype/nexus/system/org/sonatype/nexus/plugins/nexus-repository-conan/${COMPOSER_VERSION}/
USER root
RUN mkdir -p ${TARGET_DIR}; \
    sed -i 's@nexus-repository-npm</feature>@nexus-repository-npm</feature>\n        <feature prerequisite="false" dependency="false">nexus-repository-conan</feature>@g' /opt/sonatype/nexus/system/com/sonatype/nexus/assemblies/nexus-oss-feature/${NEXUS_VERSION}-${NEXUS_BUILD}/nexus-oss-feature-${NEXUS_VERSION}-${NEXUS_BUILD}-features.xml; \
    sed -i 's@<feature name="nexus-repository-npm"@<feature name="nexus-repository-conan" description="org.sonatype.nexus.plugins:nexus-repository-conan" version="0.0.2">\n        <details>org.sonatype.nexus.plugins:nexus-repository-conan/details>\n        <bundle>mvn:org.sonatype.nexus.plugins/nexus-repository-conan/0.0.2</bundle>\n    </feature>\n    <feature name="nexus-repository-npm"@g' /opt/sonatype/nexus/system/com/sonatype/nexus/assemblies/nexus-oss-feature/${NEXUS_VERSION}-${NEXUS_BUILD}/nexus-oss-feature-${NEXUS_VERSION}-${NEXUS_BUILD}-features.xml;
COPY --from=build /nexus-repository-conan/target/nexus-repository-conan-${CONAN_VERSION}.jar ${TARGET_DIR}
USER nexus
