variable "project-ID" {
  default = "freshworks-435005"
}


variable "instances_list" {
    default = {
        "jenkins-master" = "e2-medium"
        "jenkins-slave"  = "e2-medium"
        "ansible"        = "e2-medium"
        "sonar"          = "e2-medium"
 
    }  
}

variable "tf-zone" {
  default = "us-central1-c"
}

variable "ports" {
    default = [ 80, 8080, 8081, 9000]
  
}