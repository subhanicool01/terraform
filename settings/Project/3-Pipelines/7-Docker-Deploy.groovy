
 stage('Deploy to dev') {
    steps {
       script {
          Dockerdeploy('dev', '5761').call()
       }
    }
  }
  stage('Deploy to stage') {
    steps {
      script {
         Dockerdeploy('stage', '6761').call()
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
                //sh "sshpass -p ${PASSWORD} -v ssh -o StrictHostKeyChecking=no ${USERNAME}@34.41.49.154 hostname -i"
                // docker login and pull

            script {
                // pulling the container
                echo "pulling the container"

                sh "sshpass -p ${PASSWORD} -v ssh -o StrictHostKeyChecking=no ${USERNAME}@35.232.210.113 docker pull ${env.DOCKER_HUB}/${env.SERVICE_NAME}:${GIT_COMMIT}"
                try {
                    // stop the container 
                    echo "stopping the container"
                    sh "sshpass -p ${PASSWORD} -v ssh -o StrictHostKeyChecking=no ${USERNAME}@35.232.210.113 docker stop ${env.SERVICE_NAME}-$env_Name"

                    // remove the container
                    echo "removing the container"
                    sh "sshpass -p ${PASSWORD} -v ssh -o StrictHostKeyChecking=no ${USERNAME}@35.232.210.113 docker rm ${env.SERVICE_NAME}-$env_Name"

                } catch(err) {
                    echo "Caught the error: $err"
                }
                // docker create a conatainer
                echo "creating a new-container"
                sh "sshpass -p ${PASSWORD} -v ssh -o StrictHostKeyChecking=no ${USERNAME}@35.232.210.113 docker run -d -p $host_Port:8761 --name ${env.SERVICE_NAME}-$env_Name ${env.DOCKER_HUB}/${env.SERVICE_NAME}:${GIT_COMMIT}"


            } 
            }
    }
}

// this method is for build the code
def buildApp() {
    return {
        echo "Building the code for ${env.SERVICE_NAME}"
        echo "Now Starting the mvn packages"
        sh "mvn clean package -DskipTests=True"
        echo "Build is done" 
    }
}

//this method is for if devloper was directly deploy the any env, there is no images are avaible.
def imageValidation(){
    println ("Pulling the docker image")
    try {
        sh "docker pull ${env.DOCKER_HUB}/${env.SERVICE_NAME}:${GIT_COMMIT}"
    } 
    catch (error) {
       println ("Oops docker image is not available")
       buildApp().call()
    }
}

// Docker
  // now code is avaible how to deploy in real time used k8s through nginx 
  // now we are using another ways to deploy.
  // 1. deploy to docker container  2. using shared libraries 3. k8s- deployment   4. deploy thorough helm charts
  // 2. now we are following docker way right. In these way using dev-env deploy, stage-env deply, prod-env-deploy.
  // 3. So these way docker is aviable in slave do you want to execute all env in slave machine? "NO"
  // 4. Each Env is saparate machine and having docker. In real time to deploy k8s way.

  // slave is communicate with docker env machine.
  //  Add a credentials on manage-jenkins through one particular user
  // use pipeline syntax for use with-credential formate
  // install sshpass to communicate to another machine.
  // sh "sshpass -p ${PASSWORD} -v ssh -o StrictHostKeyChecking=no ${USERNAME}@34.41.49.154 hostname -i" these command should use added user only
  // these user have public and private-keys correctly.
  // these method using withcredentials of syatemgenerated syntax.
  // docker hub repository has a public.
  // 