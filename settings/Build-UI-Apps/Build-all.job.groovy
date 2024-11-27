pipeline {
    agent { label 'master' }
    environment {
        PT_BUILD_DIR_BASE = "/opt/procurant"
        NFS_FRONTEND_REPO = "/opt/frontend-service-repo"
        BRANCH_NAME = "${env.BRANCH_NAME == "" ? "${PT_GIT_BRANCH}" : "${env.BRANCH_NAME}"}".trim()
        AUTH_ODY_BRANCH = "${env.AUTH_ODY_BRANCH == "" ? "${PT_GIT_BRANCH}" : "${env.AUTH_ODY_BRANCH}"}".trim()
        ONE_SSO_BRANCH = "${env.ONE_SSO_BRANCH == "" ? "${PT_GIT_BRANCH}" : "${env.ONE_SSO_BRANCH}"}".trim()
        COMMON_UI_LIB_BRANCH = "${env.COMMON_UI_LIB_BRANCH == "" ? "${PT_GIT_BRANCH}" : "${env.COMMON_UI_LIB_BRANCH}"}".trim()
        COMMON_APP_BRANCH = "${env.COMMON_APP_BRANCH == "" ? "${PT_GIT_BRANCH}" : "${env.COMMON_APP_BRANCH}"}".trim()
        INSPECT_APP_BRANCH = "${env.INSPECT_APP_BRANCH == "" ? "${PT_GIT_BRANCH}" : "${env.INSPECT_APP_BRANCH}"}".trim()
        BUYER_APP_BRANCH = "${env.BUYER_APP_BRANCH == "" ? "${PT_GIT_BRANCH}" : "${env.BUYER_APP_BRANCH}"}".trim()
        VENDOR_APP_BRANCH = "${env.VENDOR_APP_BRANCH == "" ? "${PT_GIT_BRANCH}" : "${env.VENDOR_APP_BRANCH}"}".trim()
        CARRIER_APP_BRANCH = "${env.CARRIER_APP_BRANCH == "" ? "${PT_GIT_BRANCH}" : "${env.CARRIER_APP_BRANCH}"}".trim()
        LINK_APP_BRANCH = "${env.LINK_APP_BRANCH == "" ? "${PT_GIT_BRANCH}" : "${env.LINK_APP_BRANCH}"}".trim()
        CONNECT_APP_BRANCH = "${env.CONNECT_APP_BRANCH == "" ? "${PT_GIT_BRANCH}" : "${env.CONNECT_APP_BRANCH}"}".trim()
        SHARE_APP_BRANCH = "${env.SHARE_APP_BRANCH == "" ? "${PT_GIT_BRANCH}" : "${env.SHARE_APP_BRANCH}"}".trim()
        TRACE_APP_BRANCH = "${env.TRACE_APP_BRANCH == "" ? "${PT_GIT_BRANCH}" : "${env.TRACE_APP_BRANCH}"}".trim()
    }
    stages {
        stage("set job build name") {
            steps {
                script {
                    currentBuild.displayName = "#${BUILD_NUMBER}:${BUILD_ENV}"
                }
            }
        }
        stage('cleanup_nginx') {
            when {
                expression { env.Cleanup_Nginx_Folder.toBoolean() }
            }
            steps {
                build job: 'cleanup_nginx', parameters: [string(name: 'PENV', value: String.valueOf(BUILD_ENV))]
            }
        }
        stage('one-authlib-package') {
            when {
                expression { env.Build_One_Auth_Lib.toBoolean() }
            }
            steps {
                build job: 'one-authlib-package', parameters: [string(name: 'PT_GIT_BRANCH', value: String.valueOf(AUTH_ODY_BRANCH)),string(name: 'PT_BUILD_DIR_BASE', value: String.valueOf(PT_BUILD_DIR_BASE)),string(name: 'PENV', value: String.valueOf(BUILD_ENV)),string(name: 'NFS_FRONTEND_REPO', value: String.valueOf(NFS_FRONTEND_REPO)),string(name: 'NODE_VERSION', value: String.valueOf(NODE_VERSION)),string(name: 'BRANCH_NAME', value: String.valueOf(BRANCH_NAME))]
            }
        }
        stage('common-ui-lib') {
            when {
                expression { env.Build_Common_UI_Lib.toBoolean() }
            }
            steps {
                build job: 'common-ui-lib', parameters: [string(name: 'PT_GIT_BRANCH', value: String.valueOf(COMMON_UI_LIB_BRANCH)),string(name: 'PT_BUILD_DIR_BASE', value: String.valueOf(PT_BUILD_DIR_BASE)),string(name: 'PENV', value: String.valueOf(BUILD_ENV)),string(name: 'NFS_FRONTEND_REPO', value: String.valueOf(NFS_FRONTEND_REPO)),string(name: 'NODE_VERSION', value: String.valueOf(NODE_VERSION)),string(name: 'BRANCH_NAME', value: String.valueOf(BRANCH_NAME))]
            }
        }
        stage('Parallel execution-onesso-common-vendor-carrier-connect-share-start-buyer') {
            parallel {
                stage('one-sso') {
                    when {
                        expression { env.Build_One_SSO.toBoolean() }
                    }
                    steps {
                        build job: 'one-sso', parameters: [string(name: 'PT_GIT_BRANCH', value: String.valueOf(ONE_SSO_BRANCH)),string(name: 'PT_BUILD_DIR_BASE', value: String.valueOf(PT_BUILD_DIR_BASE)),string(name: 'PENV', value: String.valueOf(BUILD_ENV)),string(name: 'NFS_FRONTEND_REPO', value: String.valueOf(NFS_FRONTEND_REPO)),string(name: 'NODE_VERSION', value: String.valueOf(NODE_VERSION)),string(name: 'BRANCH_NAME', value: String.valueOf(BRANCH_NAME))]
                    }
                }
                stage('inspect-app') {
                    when {
                        expression { env.Build_Inspect_App.toBoolean() }
                    }
                    steps {
                        build job: 'inspect-app', parameters: [string(name: 'PT_GIT_BRANCH', value: String.valueOf(INSPECT_APP_BRANCH)),string(name: 'PT_BUILD_DIR_BASE', value: String.valueOf(PT_BUILD_DIR_BASE)),string(name: 'PENV', value: String.valueOf(BUILD_ENV)),string(name: 'NFS_FRONTEND_REPO', value: String.valueOf(NFS_FRONTEND_REPO)),string(name: 'NODE_VERSION', value: String.valueOf(NODE_VERSION))]
                    }
                }
                stage('common-app') {
                    when {
                        expression { env.Build_Common_App.toBoolean() }
                    }
                    steps {
                        build job: 'common-app', parameters: [string(name: 'PT_GIT_BRANCH', value: String.valueOf(COMMON_APP_BRANCH)),string(name: 'PT_BUILD_DIR_BASE', value: String.valueOf(PT_BUILD_DIR_BASE)),string(name: 'PENV', value: String.valueOf(BUILD_ENV)),string(name: 'NFS_FRONTEND_REPO', value: String.valueOf(NFS_FRONTEND_REPO)),string(name: 'NODE_VERSION', value: String.valueOf(NODE_VERSION))]
                    }
                }
                stage('buyer-app') {
                    when {
                        expression { env.Build_Buyer_App.toBoolean() }
                    }
                    steps {
                        build job: 'buyer-app', parameters: [string(name: 'PT_GIT_BRANCH', value: String.valueOf(BUYER_APP_BRANCH)),string(name: 'PT_BUILD_DIR_BASE', value: String.valueOf(PT_BUILD_DIR_BASE)),string(name: 'PENV', value: String.valueOf(BUILD_ENV)),string(name: 'NFS_FRONTEND_REPO', value: String.valueOf(NFS_FRONTEND_REPO)),string(name: 'NODE_VERSION', value: String.valueOf(NODE_VERSION)),string(name: 'BRANCH_NAME', value: String.valueOf(BRANCH_NAME))]
                    }
                }
                stage('vendor-app') {
                    when {
                        expression { env.Build_Vendor_App.toBoolean() }
                    }
                    steps {
                        build job: 'vendor-app', parameters: [string(name: 'PT_GIT_BRANCH', value: String.valueOf(VENDOR_APP_BRANCH)),string(name: 'PT_BUILD_DIR_BASE', value: String.valueOf(PT_BUILD_DIR_BASE)),string(name: 'PENV', value: String.valueOf(BUILD_ENV)),string(name: 'NFS_FRONTEND_REPO', value: String.valueOf(NFS_FRONTEND_REPO)),string(name: 'NODE_VERSION', value: String.valueOf(NODE_VERSION)),string(name: 'BRANCH_NAME', value: String.valueOf(BRANCH_NAME))]
                    }
                }
                stage('carrier-app') {
                    when {
                        expression { env.Build_Carrier_App.toBoolean() }
                    }
                    steps {
                        build job: 'carrier-app', parameters: [string(name: 'PT_GIT_BRANCH', value: String.valueOf(CARRIER_APP_BRANCH)),string(name: 'PT_BUILD_DIR_BASE', value: String.valueOf(PT_BUILD_DIR_BASE)),string(name: 'PENV', value: String.valueOf(BUILD_ENV)),string(name: 'NFS_FRONTEND_REPO', value: String.valueOf(NFS_FRONTEND_REPO)),string(name: 'NODE_VERSION', value: String.valueOf(NODE_VERSION)),string(name: 'BRANCH_NAME', value: String.valueOf(BRANCH_NAME))]
                    }
                }
                stage('link-app') {
                    when {
                        expression { env.Build_Link_App.toBoolean() }
                    }
                    steps {
                        build job: 'link-app',parameters: [string(name: 'PT_GIT_BRANCH', value: String.valueOf(LINK_APP_BRANCH)),string(name: 'PT_BUILD_DIR_BASE', value: String.valueOf(PT_BUILD_DIR_BASE)),string(name: 'PENV', value: String.valueOf(BUILD_ENV)),string(name: 'NFS_FRONTEND_REPO', value: String.valueOf(NFS_FRONTEND_REPO)),string(name: 'NODE_VERSION', value: String.valueOf(NODE_VERSION)),string(name: 'BRANCH_NAME', value: String.valueOf(BRANCH_NAME))]
                    }
                }
                stage('connect-app') {
                    when {
                        expression { env.Build_Connect_App.toBoolean() }
                    }
                    steps {
                        build job: 'connect-app', parameters: [string(name: 'PT_GIT_BRANCH', value: String.valueOf(CONNECT_APP_BRANCH)),string(name: 'PT_BUILD_DIR_BASE', value: String.valueOf(PT_BUILD_DIR_BASE)),string(name: 'PENV', value: String.valueOf(BUILD_ENV)),string(name: 'NFS_FRONTEND_REPO', value: String.valueOf(NFS_FRONTEND_REPO)),string(name: 'NODE_VERSION', value: String.valueOf(NODE_VERSION)),string(name: 'BRANCH_NAME', value: String.valueOf(BRANCH_NAME))]
                    }
                }
                stage('share-app') {
                    when {
                        expression { env.Build_Share_App.toBoolean() }
                    }
                    steps {
                        build job: 'share-app', parameters: [string(name: 'PT_GIT_BRANCH', value: String.valueOf(SHARE_APP_BRANCH)),string(name: 'PT_BUILD_DIR_BASE', value: String.valueOf(PT_BUILD_DIR_BASE)),string(name: 'PENV', value: String.valueOf(BUILD_ENV)),string(name: 'NFS_FRONTEND_REPO', value: String.valueOf(NFS_FRONTEND_REPO)),string(name: 'NODE_VERSION', value: String.valueOf(NODE_VERSION)),string(name: 'BRANCH_NAME', value: String.valueOf(BRANCH_NAME))]
                    }
                }
                stage('trace-app') {
                    when {
                        expression { env.Build_Trace_App.toBoolean() }
                    }
                    steps {
                        build job: 'trace-app', parameters: [string(name: 'PT_GIT_BRANCH', value: String.valueOf(TRACE_APP_BRANCH)),string(name: 'PT_BUILD_DIR_BASE', value: String.valueOf(PT_BUILD_DIR_BASE)),string(name: 'PENV', value: String.valueOf(BUILD_ENV)),string(name: 'NFS_FRONTEND_REPO', value: String.valueOf(NFS_FRONTEND_REPO)),string(name: 'NODE_VERSION', value: String.valueOf(NODE_VERSION)),string(name: 'BRANCH_NAME', value: String.valueOf(BRANCH_NAME))]
                    }
                }
            }
        }
        stage('build-nginx-container') {
            when {
                expression { env.Build_Nginx_Container.toBoolean() }
            }
            steps {
                build job: 'build-nginx-container', parameters: [string(name: 'BUILD_ENV', value: String.valueOf(BUILD_ENV)),string(name: 'TAG', value: String.valueOf(TAG))]
            }
        }
    }
}
