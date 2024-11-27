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
        stage("set job build name") {
            steps {
                script {
                    currentBuild.displayName = "#${BUILD_NUMBER}:${PENV}"
                }
            }
        }
        stage('Clean up old directories') {
            steps {
                sh '''
                    mkdir -p ${NFS_FRONTEND_REPO}${PT_BUILD_DIR_BASE}/${PENV}/new/
                    cd 	${NFS_FRONTEND_REPO}${PT_BUILD_DIR_BASE}/${PENV}/new/
                    if [ -d "common-app" ]; then
                        echo "Directory common-app exists."
                        rm -rf common-app
                        mkdir common-app
                    else
                        echo "Directory common-app does not exists."
                        mkdir common-app
                    fi
                '''
                sh '''
                    cd ${NFS_FRONTEND_REPO}${PT_BUILD_DIR_BASE}/${PENV}/nginx/docker/www
                    if [ -d "common-app"  ]; then
                        echo "Directory common-app exists."
                        rm -rf common-app
                        mkdir common-app
                    else
                        echo "Directory common-app does not exists."
                        mkdir common-app
                    fi
                '''
            }
        }
        stage('Pull from SCM') {
            steps {
                git branch: '${PT_GIT_BRANCH}',
                    credentialsId: 'bitbucket',
                    url: 'https://jenkins-procurant@bitbucket.org/procurant/common-app.git'
            }
        }
        stage('job-execution') {
            environment {
                GIT_COMMIT="${sh(returnStdout: true, script: 'git rev-parse HEAD').trim()}"
            }
            steps {
                sh '''
                    echo "INFO - Build Common App"
                    echo $PATH
                    cp -r ${NFS_FRONTEND_REPO}${PT_BUILD_DIR_BASE}/${PENV}/new/common-lib ${JENKINS_WORKSPACE}/
                    cp -r ${NFS_FRONTEND_REPO}${PT_BUILD_DIR_BASE}/${PENV}/new/one-authlib ${JENKINS_WORKSPACE}/
                    rm -rf package-lock.json
                    echo "INFO - Run NPM install"
                    npm install
                    
					echo "INFO - Install Library"
					npm install ${JENKINS_WORKSPACE}/one-authlib/one-auth-3.0.0.tgz

					echo "INFO - Install Common Lib"
					npm install ${JENKINS_WORKSPACE}/common-lib/common-lib-3.0.0.tgz
					
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
					
                    echo "INFO - Common App Completed"
                    
                    cd ${NFS_FRONTEND_REPO}${PT_BUILD_DIR_BASE}/${PENV}/nginx/docker/www
                    
                    cp -r ${JENKINS_WORKSPACE}/${JOB_BASE_NAME}/dist/common-app/* common-app/
                    
                    MANIFEST_FILE=${NFS_FRONTEND_REPO}${PT_BUILD_DIR_BASE}/${PENV}/manifest
                    echo "common-app,${PT_GIT_BRANCH},${GIT_COMMIT}" >> ${MANIFEST_FILE}
                    
                    echo $UITAG > common-app/version.html
                    echo  " expected versions : Angular CLI: 14.2.10  ;  Node: 14.16.0  ;  npm: 6.14.11  "
                    npm -v
                    node -v
                    ng --version || ng version
                    echo "Service built successfully"
                '''
            }
        }
    }
}