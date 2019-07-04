echo This will configure Maven. Make sure maven is on path

mvn install:install-file -Dfile=aws-java-sdk-1.7.5.jar -DgroupId=coppe -DartifactId=aws-java-sdk -Dversion=1.7.5 -D packaging=jar -DgeneratePom=true
mvn install:install-file -Dfile=ganymed-ssh2-build210.jar -DgroupId=coppe -DartifactId=ganymed-ssh2-build210 -Dversion=1.0 -D packaging=jar -DgeneratePom=true

echo done