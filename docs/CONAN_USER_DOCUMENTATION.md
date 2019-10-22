# How to use Conan with NXRM

# Table Of Contents
* [Hosted Repository](#hosted-repository)
* [Proxy Repository](#proxy-repository)

## Hosted Repository
This guide assumes that you have already installed the Conan plugin into NXRM - instructions for doing so can be found
in the main Readme.md file at the root of the GitHub project.

1. Install Conan (if on OSX, you can do this via ```brew install conan```)
2. Create the conan repositories in NXRM (e.g. conan-hosted)
3. Add the conan security realm to NXRM: Login as an admin, go to settings > security > realms and add 
   ```Conan Bearer Token Realm```
4. (Optional) Conan will already have a remote configured so if you want it to exclusively use NXRM you will need to 
    remove that remote first via ```conan remote remove conan-center```. *Warning: If you do not do this conan will
    fetch from conan-center first and will circumvent NXRM*
5. Add your newly created repository/repositories to conan 
   ```conan remote add nxrm-conan-hosted http://localhost:8081/repository/conan-hosted/ false```. Note: the *false* at the
   end relates to whether you want to use SSL or not. If you have configured NXRM to have SSL enabled you can switch
   this to true.
6. Login to NXRM via conan ```conan user admin -p admin123 -r nxrm-conan-hosted```
7. You will now be able to use your normal conan commands and packages will be served through NXRM.

## Proxy Repository
You can create a proxy repository in Nexus Repository Manager (NXRM) that will cache packages from a remote conan repository such as
[conan-center](https://bintray.com/conan/conan-center). 

To proxy a conan repository, you simply create a new 'conan (proxy)' as documented in 
[Repository Management](https://help.sonatype.com/repomanager3/configuration/repository-management). 

Minimal configuration steps are:
- Define 'Name' - e.g. `conan-proxy`
- Define URL for 'Remote storage' - e.g. [https://conan.bintray.com/](https://conan.bintray.com/)
- Select a `Blob store` for `Storage`

Add your newly created repository/repositories to conan ```conan remote add conan-proxy http://localhost:8081/repository/conan-proxy/ false```. Note: the *false* at the
   end relates to whether you want to use SSL or not. If you have configured NXRM to have SSL enabled you can switch
   this to true.

(Optional) Conan will already have a remote configured so if you want it to exclusively use NXRM you will need to 
    remove that remote first via ```conan remote remove conan-center```. *Warning: If you do not do this conan will
    fetch from conan-center first and will circumvent NXRM*. To verify all other remotes have been removed ```conan remote list```.
