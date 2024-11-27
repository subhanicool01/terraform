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
					mkdir -p ${NFS_FRONTEND_REPO}${PT_BUILD_DIR_BASE}/${FEATURE_NAME}/${PENV}/new/
					cd ${NFS_FRONTEND_REPO}${PT_BUILD_DIR_BASE}/${PENV}/new/
					#Delete folders 'buyer-app' if exist and create new one
					if [ -d "buyer-app" ] 
					then
					echo "Directory buyer-app exists."
					rm -rf buyer-app
					mkdir buyer-app
					else
					echo "Directory buyer-app does not exists."
					mkdir buyer-app
					fi
					'''
                sh '''
					cd ${NFS_FRONTEND_REPO}${PT_BUILD_DIR_BASE}/${FEATURE_NAME}/${PENV}/nginx/docker/www
					#Delete folders 'buyer-app' if exist and create new one
					if [ -d "buyer-app" ] 
					then
					echo "Directory buyer-app exists."
					rm -rf buyer-app
					mkdir buyer-app
					else
					echo "Directory buyer-app does not exists."
					mkdir buyer-app
					fi
					'''
            }
        }
        stage('Pull from SCM') {
            steps {
                git branch: '${PT_GIT_BRANCH}',
                    credentialsId: 'bitbucket',
                    url: 'https://jenkins-procurant@bitbucket.org/procurant/buyer-app.git'
            }
        }
        stage('job-execution') {
            environment {
                GIT_COMMIT="${sh(returnStdout: true, script: 'git rev-parse HEAD').trim()}"
            }
            steps {
                sh 'printenv'
                sh '''
					echo "INFO - Build Buyer App"
					
					echo -e "USER: $(echo $USER)"
					echo -e "npm: $(which npm)"
					echo -e "npm-version: $(npm -v)"
					
					echo -e "node: $(which node)"
					echo -e "node-version: $(node -v)"
					
					echo -e "ng: $(which ng)"
					echo -e "ng-version: $(ng -v)"
					echo -e "ng-version: $(ng version)"
					
					cp -r ${NFS_FRONTEND_REPO}${PT_BUILD_DIR_BASE}/${FEATURE_NAME}/${PENV}/new/common-lib ${JENKINS_WORKSPACE}/
					cp -r ${NFS_FRONTEND_REPO}${PT_BUILD_DIR_BASE}/${FEATURE_NAME}/${PENV}/new/one-authlib ${JENKINS_WORKSPACE}/
					
					echo "INFO - Run NPM install"
					npm install
					
					echo "INFO - Install Library"
					npm install ${JENKINS_WORKSPACE}/${FEATURE_NAME}/one-authlib/one-auth-3.0.0.tgz

					echo "INFO - Install Common Lib"
					npm install ${JENKINS_WORKSPACE}/${FEATURE_NAME}/common-lib/common-lib-3.0.0.tgz
					
					echo "INFO - Build Buyer App"
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
					
					echo "INFO - Buyer App Completed"
					
					cd ${NFS_FRONTEND_REPO}${PT_BUILD_DIR_BASE}/${FEATURE_NAME}/${PENV}/nginx/docker/www
					
					cp -r ${JENKINS_WORKSPACE}/${JOB_BASE_NAME}/dist/buyer-app/* buyer-app/
					
					MANIFEST_FILE=${NFS_FRONTEND_REPO}${PT_BUILD_DIR_BASE}/${FEATURE_NAME}/${PENV}/manifest
					echo "buyer-app,${PT_GIT_BRANCH},${GIT_COMMIT}" >> ${MANIFEST_FILE}
					
					echo $UITAG > buyer-app/version.html
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



AUTH_ODY_BRANCH = "${env.AUTH_ODY_BRANCH == "" ? "${PT_GIT_BRANCH}" : "${env.AUTH_ODY_BRANCH}"}".trim()
BRANCH_NAME = "${env.BRANCH_NAME == ""? "${PT_GIT_BRANCH} : "${env.BRANCH_NAME}"}".trim()

 CONNECT_APP_BRANCH = "${env.CONNECT_APP_BRANCH == "" ? "${PT_GIT_BRANCH}" : "${env.CONNECT_APP_BRANCH}"}".trim()
build job: 'connect-app', parameters: [string(name: 'PT_GIT_BRANCH', value: String.valueOf(CONNECT_APP_BRANCH)),string(name: 'PT_BUILD_DIR_BASE', value: String.valueOf(PT_BUILD_DIR_BASE)),string(name: 'PENV', value: String.valueOf(BUILD_ENV)),string(name: 'NFS_FRONTEND_REPO', value: String.valueOf(NFS_FRONTEND_REPO)),string(name: 'NODE_VERSION', value: String.valueOf(NODE_VERSION))]




if [ ! -d "$BRANCH_NAME" ]; then
    echo "INFO - Install Library"
    cd ${NFS_FRONTEND_REPO}${PT_BUILD_DIR_BASE}/${PENV}/${BRANCH_NAME}/new/one-authlib/${BRANCH_NAME} || exit
    npm install one-auth-3.0.0.tgz

    echo "INFO - Install Common Lib"
    cd ${NFS_FRONTEND_REPO}${PT_BUILD_DIR_BASE}/${PENV}/${BRANCH_NAME}/new/common-lib/${BRANCH_NAME} || exit
    npm install common-lib-3.0.0.tgz
else
    echo "INFO - Install Library"
    npm install ${JENKINS_WORKSPACE}/one-authlib/one-auth-3.0.0.tgz
    
    echo "INFO - Install Common Lib"
    npm install ${JENKINS_WORKSPACE}/common-lib/common-lib-3.0.0.tgz
fi


if [ ! -d "${NFS_FRONTEND_REPO}${PT_BUILD_DIR_BASE}/${PENV}/${BRANCH_NAME}" ]; then
    echo "Directory ${NFS_FRONTEND_REPO}${PT_BUILD_DIR_BASE}/${PENV}/${BRANCH_NAME} does not exist. Skipping copy."
else
    echo "Directory ${NFS_FRONTEND_REPO}${PT_BUILD_DIR_BASE}/${PENV}/${BRANCH_NAME} exists. Performing copy operations."
    cp -r "${NFS_FRONTEND_REPO}${PT_BUILD_DIR_BASE}/${PENV}/new/common-lib" "${JENKINS_WORKSPACE}/"
    cp -r "${NFS_FRONTEND_REPO}${PT_BUILD_DIR_BASE}/${PENV}/new/one-authlib" "${JENKINS_WORKSPACE}/"
fi

cp -r ${NFS_FRONTEND_REPO}${PT_BUILD_DIR_BASE}/${PENV}/new/common-lib ${JENKINS_WORKSPACE}/
cp -r ${NFS_FRONTEND_REPO}${PT_BUILD_DIR_BASE}/${PENV}/new/one-authlib ${JENKINS_WORKSPACE}/