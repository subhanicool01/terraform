resource "aws_s3_bucket" "tf.s3" {
    for_each = {
      "dev" = "dev-boutique"
      "stage" = "stage-boutique"
      "prod" = "prod-boutique"
    }
    bucket = "${each.key}-${each.value}"
    tags = {
        environmet = each.key
    }
    lifecycle {
      prevent_destroy =true  #bydefault is false make it true
    }

}    


#this is not destroy untill you mentioned extra aregument

#terraform state file - to get the resources list
#terraform destroy --target <resource name>