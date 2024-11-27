
#before that mwntioned two profiles on .aws folder
#profile means keys

terraform{
  required_version = "~>1.5.5" # version constaints

  required_providers {
    aws = { # local provider name
        source = "hashicorp/aws" # https://registry.terraform.io/hashicorp/aws
        version = "~> 5.32.1" # Version of the Provider
     }
  }   
}


provider "aws" {
    profile = "default"
    region = "us-west-1"
    alias = "us-west1"
  
}

# this is extra provider added for other resources
provider "aws" {
    region = "us-central1"
    #alias = "us-central1" #this can be user frindly name
  
}