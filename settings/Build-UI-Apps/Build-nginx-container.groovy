pipeline {
    agent { label 'jenkins-worker-all' }
    environment {
        PT_BUILD_DIR_BASE = "/opt/procurant"
        GCP_PROJECT_ID = "preprod-221918"
        NFS_FRONTEND_REPO = "/opt/frontend-service-repo"
        JENKINS_WORKSPACE = "/home/jenkins/agent/workspace"
    }
    stages {
        stage("set job build name") {
            steps {
                script {
                    currentBuild.displayName = "#${BUILD_NUMBER}:nginx"
                }
            }
        }
        stage('Cloning') {
            steps {
                git branch: 'devoped',
                    credentialsId: 'bitbucket',
                    url: 'https://jenkins-procurant@bitbucket.org/procurant/procurant-jenkins.git'
            }
        }
        stage('Move Docker Artifacts') {
            steps {
                sh '''
					cp ${JENKINS_WORKSPACE}/${JOB_BASE_NAME}/frontend-service/nginx-artifacts/default.conf ${NFS_FRONTEND_REPO}${PT_BUILD_DIR_BASE}/${BUILD_ENV}/nginx/docker/
					cp ${JENKINS_WORKSPACE}/${JOB_BASE_NAME}/frontend-service/nginx-artifacts/nginx.conf ${NFS_FRONTEND_REPO}${PT_BUILD_DIR_BASE}/${BUILD_ENV}/nginx/docker/
					cp ${JENKINS_WORKSPACE}/${JOB_BASE_NAME}/frontend-service/dockerfile/frontend-nginx/Dockerfile ${NFS_FRONTEND_REPO}${PT_BUILD_DIR_BASE}/${BUILD_ENV}/nginx/docker/
				'''
            }
        }
        stage('Ngnix docker image build') {
            steps {
                sh '''
					TSTAMP=$(date "+%Y%m%d_%H%M")
					if [ "${BUILD_ENV}" = optimized ]; then
					    UITAG="optimized-${TSTAMP}-${TAG}"
					else
					    UITAG="${BUILD_ENV}-${TSTAMP}-${TAG}"
					fi
					echo $UITAG > ${NFS_FRONTEND_REPO}${PT_BUILD_DIR_BASE}/${BUILD_ENV}/UITAG
                    
					echo "INFO - Build Docker Image"
					NGINX_HOME="${NFS_FRONTEND_REPO}${PT_BUILD_DIR_BASE}/${BUILD_ENV}/nginx/docker"
					cp ${NFS_FRONTEND_REPO}${PT_BUILD_DIR_BASE}/${BUILD_ENV}/0 ${NGINX_HOME}/manifest
					cat ${NGINX_HOME}/manifest
					echo "NGINX_HOME - " $NGINX_HOME
					echo " $UITAG - " $UITAG
					cd $NGINX_HOME
					docker build -t gcr.io/${GCP_PROJECT_ID}/nginx-1.17:$UITAG .
					echo "INFO - Push Docker Image"
					#cd /var/secrets/google/
					#docker login -u _json_key -p "$(cat key.json)" https://gcr.io
					gcloud auth print-access-token | docker login -u oauth2accesstoken --password-stdin https://gcr.io
					docker push gcr.io/${GCP_PROJECT_ID}/nginx-1.17:$UITAG
					echo "INFO - Docker Image pushed successfully"
					echo "INFO - The tag of the container to deploy is: $UITAG"
				'''
            }
        }
    }
}