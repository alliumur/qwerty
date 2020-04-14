#!/usr/bin/env bash
mvn clean package

echo '========== copy files to server =========='
scp -i "$IDE\\testappsserver.pem" \
target/qwerty-0.0.1-SNAPSHOT.jar \
ubuntu@ec2-18-216-9-101.us-east-2.compute.amazonaws.com:~/dev/qwerty

echo '============= restart server ============='
ssh -i "$IDE\\testappsserver.pem" ubuntu@ec2-18-216-9-101.us-east-2.compute.amazonaws.com << EOA
killall -9 java
nohup java -jar ~/dev/qwerty/qwerty-0.0.1-SNAPSHOT.jar > log.txt &
EOA
echo '=============== successful ==============='