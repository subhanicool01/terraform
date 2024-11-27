pipeline {
    agent {
        label 'k8s-slave'
    }
    environment{
        SERVICE_NAME= "Admin-service"
    }
    stages {
        stage("Build") {
            steps {
                echo "Building the code for ${env.SERVICE_NAME}"
                sh "mvn clean package -DskipTests=True"
            }
        }
    }
}           