pipeline {
    agent {
        label 'k8s-slave'
    }
    environment{
        SERVICE_NAME = "Admin-service"
        POM_VERSION = readMavenPom().getVersion()
        POM_PACKAGING = readMavenPom().getPacking()
        DOCKER_HUB = "docker.io/mehaboobshaik5736"   #docker hub details username

    }
    stages {
        stage("Build") {
            steps {
                echo "Building the code for ${env.SERVICE_NAME}"
                sh "mvn clean package -DskipTests=True"
            }
        }
        stage('Unit tests') {
            steps {
                echo "Permorning unit test cases ${env.SERVICE_NAME}"
                sh "mvn test"
            
            }
            post {
                always {
                  junit 'target/surfice-reports/*.xml'  #this is test cases we are shows in the dashboard and under the target folder we choose

                }
            }            
        }
        stage('Docker Build format') {
            // tell me how can i read pom.xml file form jenkinsfile
            steps {
              echo "Actual format ${env.SERVICE_NAME}-${env.POM_VERSION}-${env.POM_PACKIMG}"
              // need to have below formating
              // service-name-buildnumber-branchname.packing
               echo "Custom formate ${env.SERVICE_NAME}-${currentBuild.number}-${BRANCH_NAME}-${env.POM_VERSION}-${env.POM_PACKING}"


            }            

        }
        stage('Docker build') {
            steps {
               sh """ 
                ls -la
                cp ${workspace}/target/i27-${env.APPLICATION_NAME}-${env.POM_VERSION}.${env.POM_PACKING} ./.cicd
                ls -la ./.cicd

                echo "*****Building the docker code **********"
                # docker build -t name:tag
                docker build --force-rm --no-cache --pull --rm=true -t ${env.DOCKER_HUB}/${env.APPLICATION_NAME}:${GIT_COMMIT} ./.cicd
            """
            }
        }
    }
}