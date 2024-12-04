package com.build.k8s

class k8s {
    def jenkins
    k8s (jenkoins) {
        this.jenkins = jenkins
    }



    def k8sdeploy(env_Name.yaml, docker_image) {
        jenkins.sh """#!/bin/bash
        echo "executing k8sdeploy method"
        echo " final image name is $docker_image"
        // DIT or yaml file image name should be not same, so replaced with current image
        sed -i "s|DIT|$docker_image" ./.cicd/$env_Name.yaml|g
        kubectl apply -f ./.cicd/$env_Name.yaml
        """
    }

    def GitClone() {
        jenkins.sh """#!/bin/bash
        echo "**************Entering into git clone method *********"
        git clone -b master <repo-url>
        echo "listing the files"
        ls -la
        echo "showing the files"
        ls -la eureka-repo(under the cicd files)
        """
    }
    def k8sHelmChartsdeploy(app_name, env, helmchartpath) {
        jenkins.sh """#!/bin/bash
        echo "Helm groovy method starts"
        echo "Installing helm charts"
        helm install ${app_name}-${env}-chart -f ./.cicd/k8s/values_${env}.yaml ${helmchartpath}
        # app_name-env-chart ${app_name}-${env}-chart -f ./.cicd/k8s/values_${env}.yaml ${helmchartpath}
        # helm intall chartname -f valuesfilepath chartpath
        # helm upgrade chartname -f valuesfilepath chartpath
        """

    }

}


// if we are using helm through deployment mostly two commands are used
// helm install chats
// if we are placed values.yaml file on charts level no need to mention any other parameters.
// if we want to placed another location for values.yaml file so we mentioned particular place 
     // helm install chartname --f <location>

// same helm upgrade are wirking in helm deployments
// values.yaml file should be an server


