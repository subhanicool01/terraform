resource "google_compute_firewall" "tf-firewalls" {
  name = "freshwork-firewalls"
  network = "default"

  dynamic "allow" {
    for_each = [ var.ports ]
    content {
      protocol = "tcp"
      ports = [ allow.value ]
    }

  }
  source_ranges = ["0.0.0.0/0"]

}