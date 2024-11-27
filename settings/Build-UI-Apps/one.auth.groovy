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
					#Delete folders 'one-authlib/${BRANCH_NAME}' if exist and create new one
					if [ -d "one-authlib/${BRANCH_NAME}" ]; then
					    echo "Directory one-authlib/${BRANCH_NAME} exists."
					    rm -rf one-authlib/${BRANCH_NAME}
					    mkdir one-authlib/${BRANCH_NAME}
					else
					    echo "Directory one-authlib does not exists."
					    mkdir one-authlib/${BRANCH_NAME}
					fi
			    '''
            }
        }
        stage('Pull from SCM') {
            steps {
                git branch: '${PT_GIT_BRANCH}',
                        credentialsId: 'bitbucket',
                        url: 'https://jenkins-procurant@bitbucket.org/procurant/auth-odyssey.git'
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
					echo "INFO - Running NPM Install"
					npm install
					echo "INFO - Build and package library"
					npm run package
					pwd
					ls dist
					ls dist/one-auth
					echo "INFO - One Auth Lib Completed"
					cd ${NFS_FRONTEND_REPO}${PT_BUILD_DIR_BASE}/${PENV}/${BRANCH_NAME}/new/
					cp -r ${JENKINS_WORKSPACE}/one-authlib-package/dist/one-auth/* ./one-authlib/${BRANCH_NAME}
					
					MANIFEST_FILE=${NFS_FRONTEND_REPO}${PT_BUILD_DIR_BASE}/${PENV}/${BRANCH_NAME}/manifest
					echo "one-authlib,${PT_GIT_BRANCH},${GIT_COMMIT}" >> ${MANIFEST_FILE}
					
					echo  " expected versions : Angular CLI: 14.2.10  ;  Node: 14.16.0  ;  npm: 6.14.11  "
					npm -v
					node -v
					ng version
					ls one-authlib
				'''
            }
        }
    }
}
