terraform {
  required_version = "~> 1.5.5" # Most Commonly Used

  required_providers {
    aws = {
      source = "hashicorp/aws" # https://registry.terraform.io/hashicorp/aws
      version = "~> 5.32.1" # Version of the Provider
    }
  }
}
provider "aws" {
    region = "us-central-1a"

}