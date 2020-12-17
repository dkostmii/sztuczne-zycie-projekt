# sztuczne-zycie-projekt

#### [See guide](Guide/GUIDE.md)

> Make sure you have
> 1. Installed:
>     * [Git](https://git-scm.com/)
>     * [Maven](https://maven.apache.org/download.cgi)
>     * [OpenJDK](https://jdk.java.net/15/)
> 2. Added environment variables ([How to add?](https://docs.oracle.com/en/database/oracle/r-enterprise/1.5.1/oread/creating-and-modifying-environment-variables-on-windows.html#GUID-DD6F9982-60D5-48F6-8270-A27EC53807D0)):
>     * To `JAVA_HOME` add the directory of OpenJDK
>     * To `PATH` add:
>         * `bin` directory of OpenJDK (i.e. `C:\Program Files\jdk-15\bin`)
>         * Maven `bin` directory (i.e. `C:\Program Files\apache-maven-3.6.3\bin`)
>


To start, clone this repo and navigate to *inner directory*:

`git clone https://github.com/dkostmii/sztuczne-zycie-projekt.git && cd sztuczne-zycie-projekt/sztuczne-zycie`

Compile:

`mvn install`

Run:

`mvn exec:java`

