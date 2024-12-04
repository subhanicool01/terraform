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
        choice(name: 'buildOnly',
          choices: 'no\nyes',
          description: 'this will only build the application'
        )
        choice(name: 'scanOnly',
          choices: 'no\nyes',
          description: 'this will scan the application'
        )
        choice(name: 'dockerPush',
          choices: 'no\nyes',
          description: 'this will push the docker image'
        )
        choice(name: 'deployToDev',
          choices: 'no\nyes',
          description: 'this will deploy to the dev env'
        )
        choice(name: 'deployToStage',
          choices: 'no\nyes',
          description: 'this will deploy to the stage env'
        )
        choice(name: 'deployToProd',
          choices: 'no\nyes',
          description: 'this will deploy to the Prod env'
        )

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
        stage("Build") {
          when {
            anyOf {
                expression {
                    params.buildOnly == 'yes'
                    params.dockerPush == 'yes'
                }
            }
          }
            steps {
               script {
                //buildApp().call()
                echo "printing app_name"
                println docker.buildApp("${env. SERVICE_NAME}")
               }            
            }
        }
        stage('Unit tests') {
            when {
              anyOf {
                expression {
                    params.buildOnly == 'yes'
                    params.dockerPush == 'yes'
                }
              }
            }  
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
        // For these application not implemented the sonar
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
            when {
                expression {
                    params.dockerPush == 'yes'
                }
            }
            steps {
               script {
                 dockerBuildandPush().call()
               }
            }
        }
        stage('Deploy to dev') {
            when {
                expression {
                    params.deployToDev == 'yes'
                }
            }
            steps {
                script {
                    imageValidation().call()
                    #Dockerdeploy('dev', '5761').call()
                    k8s.k8sHelmChartsdeploy("${env.SERVICE_NAME}", "${env.ENV_NAME}", "${env.HELM_PATH}")
                }
            }
        }
        stage('Deploy to stage') {
             when {
                expression {
                    params.deployToStage == 'yes'
                }
            }
            steps {
                script {
                    imageValidation().call()
                    Dockerdeploy('stage', '6761').call()
                }
            }
        }
        stage('Deploy to Prod') {
            when {
                allOf {
                    anyOf {
                        expression {
                            params.deployToProd == 'yes'
                        }
                    }
                    anyOf {
                        branch 'release/*'
                    }
                }
            }
            steps {
                timeout(time: 100, unit: 'SECONDS') {
                    input message: "Deploying ${env.SERVICE_NAME} to Prod ???", ok: 'yes', submitter: 'Krishna'
                }
                script {
                    imageValidation().call()
                    Dockerdeploy('Prod', '8761').call()
                }
            }
        }
    }
}



def dockerBuildandPush() {
    return {
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

//this method is for if devloper was directly deploy the any env, there is no images are avaible.
def imageValidation() {
    return {
       println ("Pulling the docker image")
       try {
         sh "docker pull ${env.DOCKER_HUB}/${env.SERVICE_NAME}:${GIT_COMMIT}"
       } 
       catch (error) {
          println ("Oops docker image is not available")
          buildApp().call()
          dockerBuildandPush().call()
        }
    }
}

// this method is devloped for deploying a multiple environmnets.
def Dockerdeploy(env_Name, host_Port) {
    return {
    echo "***** deploying docker in $env_Name env *****"
            withCredentials([usernamePassword(credentialsId: 'docker_dev_vm_creds', passwordVariable: 'PASSWORD', usernameVariable: 'USERNAME')]) {
                // some block
                // with the help of these block, this slave will be connecting to docker vm machine to executing containers.
                //sh "sshpass -p ${PASSWORD} -v ssh -o StrictHostKeyChecking=no ${USERNAME}@34.42.116.84 hostname -i"
                // docker login and pull

            script {
                // pulling the container
                echo "pulling the container"

                sh "sshpass -p ${PASSWORD} -v ssh -o StrictHostKeyChecking=no ${USERNAME}@${env.PUBLIC_IP} docker pull ${env.DOCKER_HUB}/${env.SERVICE_NAME}:${GIT_COMMIT}"
                try {
                    // stop the container 
                    echo "stopping the container"
                    sh "sshpass -p ${PASSWORD} -v ssh -o StrictHostKeyChecking=no ${USERNAME}@${env.PUBLIC_IP} docker stop ${env.SERVICE_NAME}-$env_Name"

                    // remove the container
                    echo "removing the container"
                    sh "sshpass -p ${PASSWORD} -v ssh -o StrictHostKeyChecking=no ${USERNAME}@${env.PUBLIC_IP} docker rm ${env.SERVICE_NAME}-$env_Name"

                } catch(err) {
                    echo "Caught the error: $err"
                }
                // docker create a conatainer
                   echo "creating a new-container"
                   sh "sshpass -p ${PASSWORD} -v ssh -o StrictHostKeyChecking=no ${USERNAME}@${env.PUBLIC_IP} docker run -d -p $host_Port:8761 --name ${env.SERVICE_NAME}-$env_Name ${env.DOCKER_HUB}/${env.SERVICE_NAME}:${GIT_COMMIT}"


            } 
            }
    }

}

}




//this helm pipeline