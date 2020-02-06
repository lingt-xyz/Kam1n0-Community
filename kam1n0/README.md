## Changes

- Update `kam1n0-resources/bin/kam1n0.propertiess`
- Update `kam1n0-resources/bin/ExtractBinaryViaIDA.py`
- Add `Hadoop/bin/winutils.exe`
- Update `ca/mcgill/sis/dmas/kam1n0/utils/executor/SparkInstance.java` for `Hadoop/bin/winutils.exe`, see line 205
- Add `kam1n0-rep\src\main\java\ca\concordia\Printer.java` to output log.
- Update `optm_parallelism` to `1` in `ca/mcgill/sis/dmas/kam1n0/problem/clone/detector/rep/Asm2VecCloneDetectorIntegration.java`, see line 181
- Update `inline`

## Development

### Install Oracle JDK-8

- Download [Oracle JDK 8](https://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html).

- Unzip JDK: `sudo tar xvf jdk-8xxxx-linux-x64.tar.gz -C /usr/lib/jvm`
- Add Oracle JDK to Java alternatives: `sudo update-alternatives --install /usr/bin/java java /usr/lib/jvm/jdk1.8.x_xxx/bin/java 1`
- Config Java to Oracle JDK 8: `sudo update-alternatives --config java`

### Install Apache Maven

- See [Installing Apache Maven](http://maven.apache.org/install.html)

### Import project

- `git clone git@github.com:lingt-xyz/Kam1n0-Community.git`
- `git submodule update --init --recursive`
- Import project as a Maven project
- Setup Java as Oracle Java 8
- Click `build project`

### Build project

- `cd $PROJECT_GIT_ROOT/kam1n0`
- `mvn -DskipTests clean package`
- `mvn -DskipTests package`

### Run

- `cd build-bins/`
- `java -jar kam1n0-server-workbench.jar`
