def k8sdeploy(env_Name.yaml, docker_image) {
    jenkins.sh """#!bin/bash
    echo "executing k8sdeploy method"
    echo " final image name is $docker_image"
    // DIT or yaml file image name should be not same, so replaced with current image
    sed -i "s|DIT|$docker_image" ./.cicd/$env_Name.yaml|g
    kubectl apply -f ./.cicd/$env_Name.yaml
    """
}