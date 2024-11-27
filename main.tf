//terraform maily divied in five types

1. Init 2. validate 3. paln 4. Apply 5. Destroy

1. init - initiazing the providers to your current workdirs.

if you configure the first with yor provider

provider "aws" {
    region: "us-central-1a"
}


provider "google" {
    region: "us-central-1a"
}

provider "azurerm" {
    region: "us-central-1b"
}


resource "aws_instance" "newtf"{
     ami  = "ami-456122"
     instance_type = "t2.micro"
     region = "south-east"
     bucket_name = "my_bucket"
     vpc         = "test_vpc-1"
}

2.Validate -

Validate is a one type block which is the manifest files are validate or not 

it means that .tf files are written by right fomat.

resource "aws_instance" "newtf"{
     ami  = "ami-456122"
     instance_type = "t2.micro"
     region = "south-east"
     bucket_name = "my_bucket"
     vpc         = "test_vpc-1"
}


3. Plan -
   
   The terraform plan commands creates execution plan, 
   which let's you preview the changes that terraform plans to make to your infrasture
