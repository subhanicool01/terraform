# Lets Create a EC2 Instance
resource "aws_instance" "tf-ec2" {
  count = 2
  ami           = "ami-0005e0cfe09cc9050"
  instance_type = "t2.micro"
  subnet_id = aws_subnet.tf-prod-pub-subent-1.id
  vpc_security_group_ids = [aws_security_group.tf-sg.id]
  tags = {
    Name = "webserver-${count.index}" #its starts with 0
  } # Webserver-0, webserver-1
  # count = 0
  key_name = "newdevops"
  # File Function
  user_data = file("apache.sh")
}