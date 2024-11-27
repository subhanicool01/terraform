
# this file sholud be use the resource and thay type resource will use particular provider


resource "aws_vpc" "my-vpc-2" {
    cidr_block = "10.1.0.0/16"
    tags = {
    "Name" = "us-west-1-vpc"
    }

  provider = aws.us-west1
}
