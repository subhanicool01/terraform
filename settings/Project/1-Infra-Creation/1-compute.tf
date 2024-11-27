resource "google_compute_instance" "tf-my-machines" {
    for_each = var.instances_list
    name = each.key
    machine_type = each.value
    zone = var.tf-zone
    tags = [ each.key ]

    boot_disk {
    initialize_params {
      image = "debian-cloud/debian-11"
    }
  }

    network_interface {
      network = "default"
      access_config {
        network_tier = "PREMIUM"
      }
    } 

    connection {
      type = "ssh"
      user = "subhanicool01"
      host = self.network_interface[0].access_config[0].nat_ip
      private_key = file("ssh-key")
      # Once we generate the public and private key using the above, make sure you copy the public key into GCP
      # Go to GCE > Metadata(project) > ssh-keys > edit > paste the public key 
      # this will make sure, all the instances under the project will be having the pbulic key
      # now, using the private key i can connect to all vm;s under the project
    } 

    provisioner "file" {
        source = each.key == "ansible" ? "ansible.sh" : "empty.sh" 
        destination = each.key == "ansible" ? "/home/subhanicool01/ansible.sh" : "/home/subhanicool01/empty.sh"
    
    } 

    provisioner "remote-exec" {
        inline = [ 
           each.key == "ansible" ? "chmod +x /home/subhnaicool01/ansible.sh && sh /home/subhanicool01/ansible.sh" : "chmod +x empty.sh" && "sh empty.sh" 
        ]
    }

    provisioner "file" {
      source = "ssh-key"
      destination = "/home/subhanicool01/ssh-key"
    }

    
}

# Implement data sources ,
<<comment 
data "google_compute_image" "ubuntu_image" {
  family  = "ubuntu-2004-lts"
  project = "ubuntu-os-cloud"
}
comment