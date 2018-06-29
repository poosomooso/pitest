mvn package -Dcheckstyle.skip -DskipTests
scp pitest/target/pitest-1.4.1-SNAPSHOT.jar feature@feature10.andrew.cmu.edu:serena
scp pitest-entry/target/pitest-entry-1.4.1-SNAPSHOT.jar feature@feature10.andrew.cmu.edu:serena
scp pitest-command-line/target/pitest-command-line-1.4.1-SNAPSHOT.jar  feature@feature10.andrew.cmu.edu:serena