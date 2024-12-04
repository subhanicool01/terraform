package com.builds

class Docker {  // class name
    def jenkins
    Docker(jenkins) {
        this.jenkins = jenkins
    }

    // now we added the actual methods 

    def buildApp (APP_NAME) {
        //shared library formate
        jenkins.sh """#!/bin/bash                 
        echo "Building the ${APP_NAME} Application"
        mvn clean package -DskipTests=True
        echo "Build is done"
        """
    }
}


// Now These is method defining
// And these is source folder, source folder having olny method
// actual code is having on "VARS" folder


// now we configure the jenkins for use the shared library
// manage-jenkins----system-- add shared library thinks.

// after these configuration we can call the actual service to use these shared library and the method
// @Library("com.builds.slb@master") _
// dockerPipeline(
//    APP_NAME: 'eureka_server'
// ) 
// docker.methodName