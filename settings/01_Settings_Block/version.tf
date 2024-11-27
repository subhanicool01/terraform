terraform {
  # Required Version, is the underlying terraform version which is in your machine
  # https://developer.hashicorp.com/terraform/language/expressions/version-constraints#version-constraint-syntax
  # Allows only the rightmost version component to increment.
  # Constraints
  required_version = "~> 1.2.1"

  required_providers {
    # What provider i want to go with 
    aws = {
      version = "~> 5.43.0"
      source  = "hashicorp/aws"
    }
  }

}  

provider "aws" {
  region = "us-east-1"
  // region = "us-central1" , google
  # Static Credentials
  # Never Mention Credetntials in you source code
  # Even if you are using for testing, make sure you wont commit them in the SCM
  access_key = ""
  secret_key = ""
}

resource "s3" "mybucket" {
    
  
}