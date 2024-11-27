pipeline {
    agent { label 'jenkins-worker-all' }
    environment {
        PT_BUILD_DIR_BASE = "/opt/procurant"
        NFS_FRONTEND_REPO = "/opt/frontend-service-repo"
        ANGULAR_BUILD_NAME = "${PENV}"
        JENKINS_WORKSPACE = "/home/jenkins/agent/workspace"
        PVERSION = "${env.PT_GIT_BRANCH == "master" ? "master" : sh(returnStdout: true, script: "echo ${env.PT_GIT_BRANCH} | cut -d '/' -f2")}".trim()
    }
    tools { nodejs "${NODE_VERSION}"}
    stages {
        stage('Creating Directories') {
            steps {
                sh '''
					cd ${NFS_FRONTEND_REPO}${PT_BUILD_DIR_BASE}/${PENV}/nginx/docker/www
					if [ -d "sso" ]; then
                        echo "Directory sso exists."
                        rm -rf sso
				        mkdir sso
                    else
                        echo "Directory sso does not exists."
					    mkdir sso
                    fi
			    '''
            }
        }
        stage('Pull from SCM') {
            steps {
                git branch: '${PT_GIT_BRANCH}',
                        credentialsId: 'bitbucket',
                        url: 'https://jenkins-procurant@bitbucket.org/procurant/one-sso.git'
            }
        }
        stage('job-execution') {
            environment {
                GIT_COMMIT="${sh(returnStdout: true, script: 'git rev-parse HEAD').trim()}"
            }
            steps {
                sh '''
				   	#cp -r ${NFS_FRONTEND_REPO}${PT_BUILD_DIR_BASE}/${PENV}/new/one-authlib ${JENKINS_WORKSPACE}/
					
					echo "INFO - Run NPM install"
					npm install
					
					echo "INFO - Install Library"
					npm install cd ${NFS_FRONTEND_REPO}${PT_BUILD_DIR_BASE}/${PENV}/${BRANCH_NAME}/new/one-authlib/${BRANCH_NAME}/one-auth-3.0.0.tgz
					
					echo "INFO - Build one-sso"
					
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
					
					echo "SSO App Completed"
					
					cd ${NFS_FRONTEND_REPO}${PT_BUILD_DIR_BASE}/${PENV}/nginx/docker/www
					
					cp -r ${JENKINS_WORKSPACE}/one-sso/dist/one-sso/* sso/
					
					MANIFEST_FILE=${NFS_FRONTEND_REPO}${PT_BUILD_DIR_BASE}/${PENV}/manifest
                    echo "one-sso,${PT_GIT_BRANCH},${GIT_COMMIT}" >> ${MANIFEST_FILE}
					
					echo $UITAG > sso/version.html
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
