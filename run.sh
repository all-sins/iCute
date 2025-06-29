#!/bin/bash
mvn clean install package && sudo java -jar target/iCute-1.0-SNAPSHOT.jar
