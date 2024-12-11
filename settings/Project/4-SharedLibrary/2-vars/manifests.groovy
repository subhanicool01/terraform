 import com.builds.Docker
import com.builds.k8s.k8s

library ('com.i27acadamy.slb')

def call(Map pipelineParams) {
    Docker docker = new Docker(this)
    pipeline {
    agent {
        label 'k8s-slave'
    }
    parameters {
         string (name: 'Namespace_Name', description: 'enter the name of the k8s environment')
    }
    environment{
        SERVICE_NAME = "${pipelineParams.APP_NAME}""
        POM_VERSION = readMavenPom().getVersion()
        POM_PACKAGING = readMavenPom().getPackaging()
        DOCKER_HUB = "docker.io/subhanicool01"
        DOCKER_CREDS = credentials('subhanicool01_docker_creds')
        SONAR_URL = "http://34.68.126.198:9000"
        SONAR_TOKEN = credentials('sonar_creds')
        PUBLIC_IP = "34.41.246.17"
        HELM_PATH = "${WORKSPACE}/I27-CART/CHARTS"
        Docker_image_tag = sh(script: 'git log -1 --pretty=%h', returnStdot:true).trim()
    }
    tools {
        maven 'Maven-3.8.8'
        jdk 'JDK-17'
    }
    stages {
        stage("Checkout") {
            steps {
                println ("Checkout: Git Clone for selected repo")
                script {
                  k8sHelmChartsdeploy()
                }
            }
        }
        stage("Create s k8s namespaces") {
            steps {
                script {
                    k8s.namespace_creation(params."${Namespace_Name}")
                }
            }

        }
    
    }
}




}




//this helm pipeline
// and also using commit based builds pls mention the --set command --set -f image to replace