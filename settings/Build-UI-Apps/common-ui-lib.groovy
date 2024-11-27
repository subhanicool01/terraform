pipeline {
    agent { label 'jenkins-worker-all' }
    environment {
        PT_BUILD_DIR_BASE = "/opt/procurant"
        NFS_FRONTEND_REPO = "/opt/frontend-service-repo"
        JENKINS_WORKSPACE = "/home/jenkins/agent/workspace"
        PVERSION = "${env.PT_GIT_BRANCH == "master" ? "master" : sh(returnStdout: true, script: "echo ${env.PT_GIT_BRANCH} | cut -d '/' -f2")}".trim()
    }
    tools { nodejs "${NODE_VERSION}"}
    stages {
        stage('Creating Directories') {
            steps {
                sh '''
					mkdir -p ${NFS_FRONTEND_REPO}${PT_BUILD_DIR_BASE}/${PENV}/${BRANCH_NAME}/new/
					cd ${NFS_FRONTEND_REPO}${PT_BUILD_DIR_BASE}/${PENV}/${BRANCH_NAME}/new/
					#Delete folders 'common-lib/${BRANCH_NAME}' if exist and create new one
					if [ -d "common-lib/${BRANCH_NAME}" ]; then
					    echo "Directory common-lib/${BRANCH_NAME} exists."
					    rm -rf common-lib/${BRANCH_NAME}
					    mkdir -p common-lib/${BRANCH_NAME}
					else
					    echo "Directory common-lib does not exists."
					    mkdir -p common-lib/${BRANCH_NAME}
					fi
			    '''
            }
        }
        stage('Pull from SCM') {
            steps {
                git branch: '${PT_GIT_BRANCH}',
                        credentialsId: 'bitbucket',
                        url: 'https://jenkins-procurant@bitbucket.org/procurant/common.ui.lib.git'
            }
        }
        stage('job-execution') {
            environment {
                GIT_COMMIT="${sh(returnStdout: true, script: 'git rev-parse HEAD').trim()}"
            }
            steps {
                sh '''
					echo "INFO - Build Auth Lib"
					echo $PATH

					if [ ! $BRANCH_NAME ]; then
                      cp -r ${NFS_FRONTEND_REPO}${PT_BUILD_DIR_BASE}/${PENV}/${BRANCH_NAME}/new/one-authlib ${JENKINS_WORKSPACE}/
                    fi

					echo "INFO - Running NPM Install"
					npm install
					
					if [ -n "$BRANCH_NAME" ]; then
				       echo "INFO - Install Library"
					   npm install cd ${NFS_FRONTEND_REPO}${PT_BUILD_DIR_BASE}/${PENV}/${BRANCH_NAME}/new/one-authlib/${BRANCH_NAME}/one-auth-3.0.0.tgz
                    else 
					   echo "Branch was not mentioned, executing on jenkins workspace"
					   npm install ${JENKINS_WORKSPACE}/one-authlib/one-auth-3.0.0.tgz
					fi

					echo "INFO - Build and package library"
					npm run package
					pwd
					ls dist
					ls dist/common-lib
					echo "INFO - common Lib Completed"
					cd ${NFS_FRONTEND_REPO}${PT_BUILD_DIR_BASE}/${PENV}/${BRANCH_NAME}/new/
					
					MANIFEST_FILE=${NFS_FRONTEND_REPO}${PT_BUILD_DIR_BASE}/${PENV}/${BRANCH_NAME}/manifest
                    echo "common-lib,${PT_GIT_BRANCH},${GIT_COMMIT}" >> ${MANIFEST_FILE}
					cp -r ${JENKINS_WORKSPACE}/common-ui-lib/dist/common-lib/* ./common-lib/${BRANCH_NAME}
                    

					echo  " expected versions : Angular CLI: 14.2.10  ;  Node: 14.16.0  ;  npm: 6.14.11  "
					npm -v
					node -v
					ng version
					ls common-lib
				'''
            }
        }
    }
}
