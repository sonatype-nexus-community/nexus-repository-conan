ARG NEXUS_VERSION=3.13.0

FROM maven:3-jdk-8-alpine AS build
ARG NEXUS_VERSION=3.13.0
ARG NEXUS_BUILD=01

COPY . /nexus-repository-conan/
RUN cd /nexus-repository-conan/; sed -i "s/3.13.0-01/${NEXUS_VERSION}-${NEXUS_BUILD}/g" pom.xml; \
    mvn clean package;

FROM sonatype/nexus3:$NEXUS_VERSION
ARG NEXUS_VERSION=3.13.0
ARG NEXUS_BUILD=01
ARG CONAN_VERSION=0.0.4
ARG TARGET_DIR=/opt/sonatype/nexus/system/org/sonatype/nexus/plugins/nexus-repository-conan/${CONAN_VERSION}/
USER root
RUN mkdir -p ${TARGET_DIR}; \
    sed -i 's@nexus-repository-maven</feature>@nexus-repository-maven</feature>\n        <feature prerequisite="false" dependency="false" version="0.0.4">nexus-repository-conan</feature>@g' /opt/sonatype/nexus/system/org/sonatype/nexus/assemblies/nexus-core-feature/${NEXUS_VERSION}-${NEXUS_BUILD}/nexus-core-feature-${NEXUS_VERSION}-${NEXUS_BUILD}-features.xml; \
    sed -i 's@<feature name="nexus-repository-maven"@<feature name="nexus-repository-conan" description="org.sonatype.nexus.plugins:nexus-repository-conan" version="0.0.4">\n        <details>org.sonatype.nexus.plugins:nexus-repository-conan</details>\n        <bundle>mvn:org.sonatype.nexus.plugins/nexus-repository-conan/0.0.4</bundle>\n    </feature>\n    <feature name="nexus-repository-maven"@g' /opt/sonatype/nexus/system/org/sonatype/nexus/assemblies/nexus-core-feature/${NEXUS_VERSION}-${NEXUS_BUILD}/nexus-core-feature-${NEXUS_VERSION}-${NEXUS_BUILD}-features.xml;
COPY --from=build /nexus-repository-conan/target/nexus-repository-conan-${CONAN_VERSION}.jar ${TARGET_DIR}
USER nexus
