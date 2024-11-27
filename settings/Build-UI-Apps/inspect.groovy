pipeline {
    agent { label 'jenkins-worker-all' }
    environment {
        PT_BUILD_DIR_BASE = "/opt/procurant"
        NFS_FRONTEND_REPO = "/opt/frontend-service-repo"
        ANGULAR_BUILD_NAME = "${PENV}"
        JENKINS_WORKSPACE = "/home/jenkins/agent/workspace"
        PVERSION = "${env.PT_GIT_BRANCH == "master" ? "master" : sh(returnStdout: true, script: "echo ${env.PT_GIT_BRANCH} | cut -d '/' -f2")}".trim()
    }
    tools {nodejs "${NODE_VERSION}"}
    stages {
        stage("set job build name"){
            steps {
                script {
                    currentBuild.displayName = "#${BUILD_NUMBER}:${PENV}"

                }
            }
        }
        stage('Creating Directories') {
            steps {
                sh '''
					mkdir -p ${NFS_FRONTEND_REPO}${PT_BUILD_DIR_BASE}/${PENV}/new/
					cd ${NFS_FRONTEND_REPO}${PT_BUILD_DIR_BASE}/${PENV}/new/
					#Delete folders 'inspect-app' if exist and create new one
					if [ -d "inspect-app" ] 
					then
					echo "Directory inspect-app exists."
					rm -rf inspect-app
					mkdir inspect-app
					else
					echo "Directory inspect-app does not exists."
					mkdir inspect-app
					fi
					'''
                sh '''
					cd ${NFS_FRONTEND_REPO}${PT_BUILD_DIR_BASE}/${PENV}/nginx/docker/www
					#Delete folders 'inspect-app' if exist and create new one
					if [ -d "inspect-app" ] 
					then
					echo "Directory inspect-app exists."
					rm -rf inspect-app
					mkdir inspect-app
					else
					echo "Directory inspect-app does not exists."
					mkdir inspect-app
					fi
					'''
            }
        }
        stage('Pull from SCM') {
            steps {

                git branch: '${PT_GIT_BRANCH}',
                        credentialsId: 'bitbucket',
                        url: 'https://jenkins-procurant@bitbucket.org/procurant/inspect-app.git'
            }
        }

        stage('job-execution') {
            environment {
                GIT_COMMIT="${sh(returnStdout: true, script: 'git rev-parse HEAD').trim()}"
            }
            steps {
                sh '''
					echo "INFO - Build Procurant Inspect App"
					
					cp -r ${NFS_FRONTEND_REPO}${PT_BUILD_DIR_BASE}/${PENV}/new/common-lib ${JENKINS_WORKSPACE}/
					cp -r ${NFS_FRONTEND_REPO}${PT_BUILD_DIR_BASE}/${PENV}/new/one-authlib ${JENKINS_WORKSPACE}/
					
					echo "INFO - Run NPM install"
					npm install
					
					echo "INFO - Install Common Lib"
					npm install cd ${NFS_FRONTEND_REPO}${PT_BUILD_DIR_BASE}/${PENV}/${BRANCH_NAME}/new/common-lib/${BRANCH_NAME}/common-lib-3.0.0.tgz
					
					echo "INFO - Install Library"
					npm install cd ${NFS_FRONTEND_REPO}${PT_BUILD_DIR_BASE}/${PENV}/${BRANCH_NAME}/new/one-authlib/${BRANCH_NAME}/one-auth-3.0.0.tgz

					echo "Building Inspect App"
                    if [ "$PENV" = optimized ]; then
    		        	ng build \
    					--aot=true \
    					--build-optimizer=true \
    					--common-chunk=true \
    					--optimization=true \
    					--source-map=false \
    					--vendor-chunk=true \
    					--output-hashing=all \
    					--extract-licenses=true \
    					--base-href /
					else
					    ng build \
					    --source-map=true \
					    --named-chunks=true \
					    --vendor-chunk=true \
					    --extract-licenses=true \
					    --base-href /
					fi
					
					echo "INFO - Inspect App Completed"
					
					cd ${NFS_FRONTEND_REPO}${PT_BUILD_DIR_BASE}/${PENV}/nginx/docker/www
					
					cp -r ${JENKINS_WORKSPACE}/${JOB_BASE_NAME}/dist/inspect-app/* inspect-app/
					
					MANIFEST_FILE=${NFS_FRONTEND_REPO}${PT_BUILD_DIR_BASE}/${PENV}/manifest
                    echo "inspect-app,${PT_GIT_BRANCH},${GIT_COMMIT}" >> ${MANIFEST_FILE}
					
					echo "Service built successfully"
					echo  " expected versions : Angular CLI: 11.2.4  ;  Node: 14.16.0  ;  npm: 6.14.11  "
					npm -v
					node -v
					ng --version || ng version
					'''
            }
        }
    }
}

