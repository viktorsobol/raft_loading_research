
# Cluster with N nodes communicating by RAFT protocol

## Build
`mvn clean package` to produce jar file


`mvn clean install --batch-mode -V -DskipTests=true -Dmaven.test.skip=true -Dmaven.javadoc.skip=true` to build atomix library

`unzip -q -c target/raft_server-1.0-SNAPSHOT-jar-with-dependencies.jar META-INF/MANIFEST.MF` see manifest of jar
