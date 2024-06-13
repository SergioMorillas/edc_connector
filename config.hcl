storage "raft" {
    path         = "./vault/data"
    node_id      ="node1"
}
listener "tcp" {
    address      = "vault-server:8200"
}
api_addr         = "https://vault-server:8200"
cluster_addr     = "https://vault-server:8200"
ui               = true