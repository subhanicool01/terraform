#terraform serring block mainly used three things
#1.required_version 2.required_providers 3. backend
# terraform settings block called as terrafoam also

terraform{
    required_version = "~>3.2.5"  #versions are constarins


    #required_providers
    required_providers {
       aws = {
        version = ">= 1.2.5"
        source  = "hashicorp/aws"
       }
    }

    #remote backend  for storing your terraform configuration

    backend "s3" {
        bucket = "test-ss"
        region = "us-east-1"
    }

}

