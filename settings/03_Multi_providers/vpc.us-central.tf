resource "aws_vp" "my-vpc" {
    cidr_block = "10.0.0.0/16"
    tags = {
    "Name" = "myvpc"
    }
  
}