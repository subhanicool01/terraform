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
      create_before_destroy = true  # bydefault its faluse make it true
    }
}
