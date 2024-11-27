
#this method was using multiple times to multiple users
resource "aws_iam_user" "tf-user-1" {
    name = "maha"  
}

resource "aws_iam_user" "tf-user-2" {
    name = "kartick"
}



# Requirement : I want to create some users in AWS 



resource "aws_iam_user" "tf-user-new" {
   for_each = toset([ "maha", "krishna" ])
  name = each.key
  #name = each.value
}



# If u are using for_each set of strings then each.key = each.value

