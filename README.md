<!--

    Sonatype Nexus (TM) Open Source Version
    Copyright (c) 2017-present Sonatype, Inc.
    All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.

    This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
    which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.

    Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
    of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
    Eclipse Foundation. All other trademarks are the property of their respective owners.

-->
# Nexus Repository Conan Format

[![Maven Central](https://img.shields.io/maven-central/v/org.sonatype.nexus.plugins/nexus-repository-conan.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22org.sonatype.nexus.plugins%22%20AND%20a:%22nexus-repository-conan%22)
[![CircleCI](https://circleci.com/gh/sonatype-nexus-community/nexus-repository-conan.svg?style=shield)](https://circleci.com/gh/sonatype-nexus-community/nexus-repository-conan)
[![Join the chat at https://gitter.im/sonatype/nexus-developers](https://badges.gitter.im/sonatype/nexus-developers.svg)](https://gitter.im/sonatype/nexus-developers?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)
[![DepShield Badge](https://depshield.sonatype.org/badges/sonatype-nexus-community/nexus-repository-conan/depshield.svg)](https://depshield.github.io)

> **Huzzah!** Conan is now part of Nexus Repository Manager. Version 3.20 includes the Conan plugin by default. 
>The plugin source code is now in [nexus-public](https://github.com/sonatype/nexus-public) in [nexus-repository-conan](https://github.com/sonatype/nexus-public/tree/master/plugins/nexus-repository-conan).

> **Filing issues:** Upgrade to the latest version of Nexus Repository Manager 3, to get the latest fixes and improvements, before filing any issues or feature requests at https://issues.sonatype.org/.

> **Upgrading:** If you are using a version prior to 3.20 and upgrade to a newer version you will not be able to install the community plugin. 
>No other changes are required and your existing data will remain intact.

# Table Of Contents
* [Developing](#developing)
   * [Contribution Guidelines](#contribution-guidelines)
   * [Requirements](#requirements)
   * [Building](#building)
* [Using Conan with Nexus Repository Manager 3](#using-conan-with-nexus-repository-manager-3)
* [Installing the plugin](#installing-the-plugin)
   * [Permanent Reinstall](#permanent-reinstall)
* [Installing the plugin in old NXRM versions](#installing-the-plugin-in-old-versions)
   * [Easiest Install](#easiest-install)
   * [Temporary Install](#temporary-install)
   * [Permament Install](#permanent-install)
* [The Fine Print](#the-fine-print)
* [Getting Help](#getting-help)

## Developing

### Contribution Guidelines

Go read [our contribution guidelines](/.github/CONTRIBUTING.md) to get a bit more familiar with how
we would like things to flow.

### Requirements

* [Apache Maven 3.3.3+](https://maven.apache.org/install.html)
* OpenJDK 8
* Network access to https://repository.sonatype.org/content/groups/sonatype-public-grid

Also, there is a good amount of information available at [Bundle Development](https://help.sonatype.com/display/NXRM3/Bundle+Development#BundleDevelopment-BundleDevelopmentOverview)

### Building

To build the project and generate the bundle use Maven

    mvn clean install

If everything checks out, the bundle for Conan should be available in the `target` folder

## Using Conan with Nexus Repository Manager 3

[We have detailed instructions on how to get started here!](https://help.sonatype.com/repomanager3/formats/conan-repositories)

## Compatibility with Nexus Repository Manager 3 Versions

The table below outlines what version of Nexus Repository Manager the plugin was built against:

| Plugin Version    | Nexus Repository Manager Version |
|-------------------|----------------------------------|
| v0.0.1            | <3.11.0                          |
| v0.0.2 - v0.0.6   | >=3.11.0                         |
| v1.0.0 In product | >=3.22.0                         |


## Installing the plugin

In Nexus Repository Manager 3.22+ Conan proxy is already included, so there is no need to install it.
But if you want to reinstall the plugin with your improvements then the following instructions will be useful.
Note: Using an unofficial version of the plugin is not supported by the Sonatype Support team.
Conan hosted repository is not supported and disabled by default. You can enable hosted repo by setting
nexus.conan.hosted.enabled=true in NXRM's properties file.

### Permanent Reinstall

* Copy the bundle into: `<nexus_dir>/system/org/sonatype/nexus/plugins/nexus-repository-conan/1.0.0/nexus-repository-conan-1.0.0.jar`
* Modify xml to introduce the plugin:
  * OSS edition: `<nexus_dir>/system/com/sonatype/nexus/assemblies/nexus-oss-feature/3.x.y/nexus-oss-feature-3.x.y-features.xml`

  * PRO edition: `<nexus_dir>/system/com/sonatype/nexus/assemblies/nexus-pro-feature/3.x.y/nexus-pro-feature-3.x.y-features.xml`

   ```
         <feature version="3.x.y.xy" prerequisite="false" dependency="false">nexus-repository-rubygems</feature>
         <feature version="1.0.0" prerequisite="false" dependency="false">nexus-repository-conan</feature>
         <feature version="3.x.y.xy" prerequisite="false" dependency="false">nexus-repository-yum</feature>
     </features>
   ```

   And
   ```
       <feature name="nexus-repository-conan" description="org.sonatype.nexus.plugins:nexus-repository-conan" version="1.0.0">
        <details>org.sonatype.nexus.plugins:nexus-repository-conan</details>
        <bundle>mvn:org.sonatype.nexus.plugins/nexus-repository-conan/1.1.0</bundle>
       </feature>
    </features>
   ```

### Installing the plugin in old versions

For older versions there are a range of options for installing the Conan plugin. You'll need to build it first, and
then install the plugin with one of the options shown below:

### Easiest Install
	
Thanks to some upstream work in Nexus Repository (versions newer than 3.15), it's become a LOT easier to install a plugin. To install the `conan` plugin, follow these steps:

 * Build the plugin with `mvn clean package -PbuildKar`
 * Copy the `nexus-repository-conan-0.0.6-bundle.kar` file from your `target` folder to the `deploy` folder for your Nexus Repository installation.
	
Once you've done this, go ahead and either restart Nexus Repo, or go ahead and start it if it wasn't running to begin with.
	
You should see `conan (hosted)` and `conan (proxy)` in the available Repository Recipes to use, if all has gone according to plan :)

### Temporary Install

Installations done via the Karaf console will be wiped out with every restart of Nexus Repository. This is a
good installation path if you are just testing or doing development on the plugin.

* Enable NXRM's console: edit `<nexus_dir>/bin/nexus.vmoptions` and change `karaf.startLocalConsole`  to `true`.

  More details here: https://help.sonatype.com/display/NXRM3/Bundle+Development+Overview

* Run NXRM's console:
  ```
  # sudo su - nexus
  $ cd <nexus_dir>/bin
  $ ./nexus run
  > bundle:install file:///tmp/nexus-repository-conan-0.0.6.jar
  > bundle:list
  ```
  (look for org.sonatype.nexus.plugins:nexus-repository-conan ID, should be the last one)
  ```
  > bundle:start <org.sonatype.nexus.plugins:nexus-repository-conan ID>
  ```

### Permanent Install

For more permanent installs of the nexus-repository-conan plugin, follow these instructions:

* Copy the bundle (nexus-repository-conan-0.0.6.jar) into <nexus_dir>/deploy

This will cause the plugin to be loaded with each restart of Nexus Repository. As well, this folder is monitored
by Nexus Repository and the plugin should load within 60 seconds of being copied there if Nexus Repository
is running. You will still need to start the bundle using the karaf commands mentioned in the temporary install.

This will cause the plugin to be loaded and started with each startup of Nexus Repository.

## The Fine Print

Starting from version 3.22 the Conan plugin is supported by Sonatype, but still is a contribution of ours
to the open source community (read: you!).

Remember:

* Use this contribution at the risk tolerance that you have
* Do NOT file Sonatype support tickets related to Conan support
* DO file issues here on GitHub, so that the community can pitch in

Phew, that was easier than I thought. Last but not least of all:

Have fun creating and using this plugin and the Nexus platform, we are glad to have you here!

## Getting help

Looking to contribute to our code but need some help? There's a few ways to get information:

* Chat with us on [Gitter](https://gitter.im/sonatype/nexus-developers)
* Check out the [Nexus3](http://stackoverflow.com/questions/tagged/nexus3) tag on Stack Overflow
* Check out the [Nexus Repository User List](https://groups.google.com/a/glists.sonatype.com/forum/?hl=en#!forum/nexus-users)
