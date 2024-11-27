pipeline {
    agent {
        label 'k8s-slave'
    }
    environment{
        SERVICE_NAME = "eureka-server"
        POM_VERSION = readMavenPom().getVersion()
        POM_PACKAGING = readMavenPom().getPackaging()
        DOCKER_HUB = "docker.io/subhanicool01"
        DOCKER_CREDS = credentials('subhanicool01_docker_creds')
        SONAR_URL = "http://34.68.126.198:9000"
        SONAR_TOKEN = credentials('sonar_creds')
    }
    tools {
        maven 'Maven-3.8.8'
        jdk 'JDK-17'
    }
    stages {
        stage("Build") {
            steps {
                echo "Starting the build"
                echo "Building the code for ${env.SERVICE_NAME}"
                echo "Now Starting the mvn packages"
                sh "mvn clean package -DskipTests=True"
                echo "Build is done"             
            }
        }
        stage('Unit tests') {
            steps {
                echo "performing the unit test cases"
                sh "mvn test"
            }
            post {
                always {
                    junit 'target/surefire-reports/*.xml'
                }
            }
        }
        stage('sonar scan') {
            steps {
               echo " sonarqube with quality gates"
               withSonarQubeEnv('SonarQube'){  //SonarQube name mentioned in manage-jenkins sonar section
               sh"""
                 mvn clean verify sonar:sonar \
                   -Dsonar.projectKey=freshwork-eureka-server \
                   -Dsonar.host.url=${env.SONAR_URL} \
                   -Dsonar.login=${env.SONAR_TOKEN}
               """
               }
               timeout (time: 2, unit: 'MINUTES') {
                  script {
                     waitForQualityGate abortPipeline: true
                  }
                }
            }
        }
        stage('Docker format') {
            steps {
                echo "actual format: ${env.SERVICE_NAME}-${env.POM_VERSION}-${env.POM_PACKAGING}"
                //Need to have below formating
                //service-name-buildnumber-branchname.packing
                //pricecur-service-5-main.jar
                echo "custom format: ${env.SERVICE_NAME}-${currentBuild.number}-${BRANCH_NAME}-${env.POM_PACKAGING}"
            }
        }
        stage('Docker build') {
            steps {
                sh """
                  ls -la
                  cp -f ${workspace}/target/${env.SERVICE_NAME}-${env.POM_VERSION}.${env.POM_PACKAGING} ./.cicd
                  ls -la ./.cicd
                  echo "********************** Build docker image ************************"
                  docker build --force-rm --no-cache --pull --rm=true --build-arg JAR_SOURCE=${env.SERVICE_NAME}-${env.POM_VERSION}.${env.POM_PACKAGING} -t ${env.DOCKER_HUB}/${env.SERVICE_NAME}:${GIT_COMMIT} ./.cicd
                  docker images
                  echo "*********************** Docker push *****************************"
                  docker login  -u ${env.DOCKER_CREDS_USR} -p ${env.DOCKER_CREDS_PSW}
                  docker push  ${env.DOCKER_HUB}/${env.SERVICE_NAME}:${GIT_COMMIT}
                """
            }
        }
    }
}

//  /home/subhanicool01/jenkins/workspace/pricecut-service_main/target/price-cut-service-0.0.1-SNAPSHOT.jar
//   git remote set-url origin https://{subhanicool01}:{Subhani@5736}@github.com/{subhanicool01}/project.git
//   after install the docker we need to fix one issue for permission denied. To add user for docker group so we write a module on ansible playbook.

//docker build --force-rm --no-cache --pull --rm=true 
// --force-rm - forcefully remove
// --no-cache - no cacheing 
// --pullm --rm=true - to pull the remote directory

// soanr
  //  1. sonar scan is ready for selected service.
  //  2. soanr is working, but dev pushes the code in multiple times so we need to findout the how many bucks are there.
  //  3. so we are implement quality-gates for project to verify how many code smells are there. if cross fail, if not crosses then pass.
  //  4. if build is done, but the code is some codesmiles are not proper way.
  //  5. So we are using sonar quality gates in jenkins.
  //  6. using some methods to that sonar stage is populating some colours.
  //  7. so used one pluging sonarqube scanner && install
  //  8. after that add sonarqube details in manage-jenkins-system-