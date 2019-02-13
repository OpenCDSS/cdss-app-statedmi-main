# cdss-app-statedmi-main #

This repository contains the StateDMI main application source code and supporting files for the development environment.
Multiple other repositories are used to create the application.
Eclipse is used for development and repositories currently contain Eclipse project files to facilitate
setting up the Eclipse development environment.

StateDMI is part of
[Colorado's Decision Support Systems (CDSS)](https://www.colorado.gov/cdss).
See the following online resources:

* [Colorado's Decision Support Systems](https://www.colorado.gov/cdss)
* [OpenCDSS](http://learn.openwaterfoundation.org/cdss-emod-dev/) - currently
hosted on the Open Water Foundation website while the OpenCDSS server is configured
* [StateDMI Developer Documentation](http://learn.openwaterfoundation.org/cdss-app-statedmi-doc-dev/) - currently
hosted on the Open Water Foundation website while the OpenCDSS server is configured
* [StateDMI User Documentation](http://learn.openwaterfoundation.org/cdss-app-statedmi-doc-user/) - currently
hosted on the Open Water Foundation website while the OpenCDSS server is configured

See the following sections in this page:

* [Repository Folder Structure](#repository-folder-structure)
* [Repository Dependencies](#repository-dependencies)
* [Development Environment Folder Structure](#development-environment-folder-structure)
* [Contributing](#contributing)
* [License](#license)
* [Contact](#contact)

-----

## Repository Folder Structure ##

The following are the main folders and files in this repository, listed alphabetically.
See also the [Development Environment Folder Structure](#development-environment-folder-structure)
for overall folder structure recommendations.

```
cdss-app-statedmi-main/       StateDMI source code and development working files.
  .classpath                  Eclipse configuration file.
  .git/                       Git repository folder (DO NOT MODIFY THIS except with Git tools).
  .gitattributes              Git configuration file for repository.
  .gitignore                  Git configuration file for repository.
  .project                    Eclipse configuration file.
  bin/                        Eclipse folder for compiled files (dynamic so ignored from repo).
  build-util/                 Utility scripts used in development environment.
  conf/                       Configuration files for installer build tools.
  doc/                        Word/PDF legacy documentation for StateDMI.
  externals/                  Third-party libraries and tools (may remove/move in future).
  graphics/                   Images (may remove/move in future).
  installer/                  StateDMI-specific files used to create installer.
  LICENSE.md                  StateDMI license file.
  nbproject/                  NetBeans project (legacy, may be removed).
  README.md                   This file.
  resources/                  Additional resources, such as runtime files for installer.
  scripts/                    Eclipse run and external tools configurations.
  src/                        StateDMI main application source code.
  test/                       Unit tests using JUnit, and functional tests.
```

## Repository Dependencies ##

Repository dependencies fall into two categories as indicated below.

### StateDMI Repository Dependencies ###

The main StateDMI code depends on other repositories
The following repositories are used to create the main StateDMI application.
Some repositories correspond to Eclipse projects and others are not used within Eclipse,
indicated as follows:

* Y - repository is included as Eclipse project.
* y - repository can be included as Eclipse project but does not need to be.  The project can be added to Eclipse to use the Git client,
but files are often edited external to Eclipse.
* N - repository is managed outside if Eclipse,
such as documentation managed with command line Git or other Git tools.

|**Repository**|**Eclipse project?**|**Description**|
|-------------------------------------------------------------------------------------------------------------|--|----------------------------------------------------|
|[`cdss-app-statedmi-doc-dev`](https://github.com/OpenCDSS/cdss-app-statedmi-doc-dev)              |N |StateDMI developer documentation (Markdown/MkDocs).|
|[`cdss-app-statedmi-doc-user`](https://github.com/OpenCDSS/cdss-app-statedmi-doc-user)            |N |StateDMI user documentation (Markdown/MkDocs).|
|`cdss-app-statedmi-main`                                                                                     |Y |StateDMI main application code (this repo).|
|[`cdss-app-statedmi-test`](https://github.com/OpenCDSS/cdss-app-statedmi-test)                    |y |StateDMI functional tests using StateDMI testing framework.|
|[`cdss-archive-nsis-2.46`](https://github.com/OpenCDSS/cdss-archive-nsis-2.46)                    |N |Archive of NSIS 2.46, to set up development environment.|
|[`cdss-lib-cdss-java`](https://github.com/OpenCDSS/cdss-lib-cdss-java)                            |Y |Library that is shared between CDSS components.|
|[`cdss-lib-common-java`](https://github.com/OpenCDSS/cdss-lib-common-java)                        |Y |Library of core utility code used by multiple repos.|
|[`cdss-lib-dmi-hydrobase-java`](https://github.com/OpenCDSS/cdss-lib-dmi-hydrobase-java)          |Y |Library to directly access Colorado's HydroBase database.|
|[`cdss-lib-dmi-hydrobase-rest-java`](https://github.com/OpenCDSS/cdss-lib-dmi-hydrobase-rest-java)|Y |Library to access Colorado's HydroBase REST API.|
|[`cdss-lib-models-java`](https://github.com/OpenCDSS/cdss-lib-models-java)                        |Y |Library to read/write CDSS StateCU and StateMod model files.|
|[`cdss-util-buildtools`](https://github.com/OpenCDSS/cdss-util-buildtools)                        |Y |Tools to create CDSS Java software installers.|

### Repositories that Depend on StateDMI Repository ###

This repository is not known to be a dependency for any other projects.

## Development Environment Folder Structure ##

The following folder structure is recommended for StateDMI development.
Top-level folders should be created as necessary.
Repositories are expected to be on the same folder level to allow cross-referencing
scripts in those repositories to work.

```
C:\Users\user\                               Windows user home folder (typical development environment).
/home/user/                                  Linux user home folder (not tested).
/cygdrive/C/Users/user                       Cygdrive home folder (not tested).
  cdss-dev/                                  Projects that are part of Colorado's Decision Support Systems.
    StateDMI/                                StateDMI product folder.
      eclipse-workspace/                     Folder for Eclipse workspace, which references Git repository folders.
                                             The workspace folder is not maintained in Git.
      git-repos/                             Git repositories for StateDMI.
        cdss-app-statedmi-doc-dev/           See repository list above.
        cdss-app-statedmi-doc-user/
        cdss-app-statedmi-main/
        cdss-app-statedmi-test/
        cdss-lib-cdss-java/
        cdss-lib-common-java/
        cdss-lib-dmi-hydrobase-java/
        cdss-lib-dmi-hydrobase-rest-java/
        cdss-lib-models-java/
        cdss-util-buildtools/
        ...others may be added...

```

## Contributing ##

Contributions to this project can be submitted using the following options:

1. StateDMI software developers with commit privileges can write to this repository
as per normal OpenCDSS development protocols.
2. Post an issue on GitHub with suggested change.  Provide information using the issue template.
3. Fork the repository, make changes, and do a pull request.
Contents of the current master branch should be merged with the fork to minimize
code review before committing the pull request.

See also the [OpenCDSS / StateDMI protocols](http://learn.openwaterfoundation.org/cdss-website-opencdss/statedmi/statedmi/).

## License ##

Copyright Colorado Department of Natural Resources.

The software is licensed under GPL v3+. See the [LICENSE.md](LICENSE.md) file.

## Contact ##

See the [OpenCDSS StateDMI information for product contacts](http://learn.openwaterfoundation.org/cdss-website-opencdss/statedmi/statedmi/#product-leadership).
