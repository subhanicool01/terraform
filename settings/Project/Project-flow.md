## Infra creation


## Configuration Management

* we need to configure `jenkins-master` and `jenkins-slave`. Lets same we are using ansible playbooks.

* Later on any package that needs to be installed on the `jenkins-slave` should be coming from `ansible-playbook` only.

* Lets starts wuth `jenkins-master` configurations:

1. so need to install jenkins-master in jenkins master machine

   #1-anisble-jenkins_master.yaml

2. after installation need to copy the key into jenkins at a first time in manully process, But we are using anisble playbook right so using another way