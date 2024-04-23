# How to use

# Commands to reproduce the errors

1. ``git clone  https://github.com/SergioMorillas/edc_connector.git``
2. ``cd edc_connector/docker``
3. ``docker compose -f 'docker-compose.yml'``
4. Create the dataplane
```sh
curl -H 'Content-Type: application/json' \
     -d '{  "@context": {    "@vocab": "https://w3id.org/edc/v0.0.1/ns/"  },  "@id": "http-pull-provider-dataplane",  "url": "http://provider:19192/control/transfer",  "allowedSourceTypes": [    "HttpData"  ],  "allowedDestTypes": [    "HttpProxy",    "HttpData"  ],  "properties": {    "https://w3id.org/edc/v0.0.1/ns/publicApiUrl": "http://provider:19291/public/"  }}' \
     -X POST "http://localhost:19193/management/v2/dataplanes" -s | jq
```
5. Create the asset, policy and contract
```sh
curl -d '{  "@context": {    "@vocab": "https://w3id.org/edc/v0.0.1/ns/"  },  "@id": "assetId",  "properties": {    "name": "product description",    "contenttype": "application/json"  },  "dataAddress": {    "type": "HttpData",    "name": "Test asset",    "baseUrl": "https://jsonplaceholder.typicode.com/users",    "proxyPath": "true"  }}' \
  -H 'content-type: application/json' http://localhost:19193/management/v3/assets \
  -s | jq
curl -d '{  "@context": {    "@vocab": "https://w3id.org/edc/v0.0.1/ns/",    "odrl": "http://www.w3.org/ns/odrl/2/"  },  "@id": "aPolicy",  "policy": {    "@context": "http://www.w3.org/ns/odrl.jsonld",    "@type": "Set",    "permission": [],    "prohibition": [],    "obligation": []  }}' \
  -H 'content-type: application/json' http://localhost:19193/management/v2/policydefinitions \
  -s | jq

curl -d '{  "@context": {    "@vocab": "https://w3id.org/edc/v0.0.1/ns/"  },  "@id": "1",  "accessPolicyId": "aPolicy",  "contractPolicyId": "aPolicy",  "assetsSelector": []}' \
  -H 'content-type: application/json' http://localhost:19193/management/v2/contractdefinitions \
  -s | jq
curl -X POST "http://localhost:29193/management/v2/catalog/request" \
    -H 'Content-Type: application/json' \
    -d '{  "@context": {    "@vocab": "https://w3id.org/edc/v0.0.1/ns/"  },  "counterPartyAddress": "http://provider:19194/protocol",  "protocol": "dataspace-protocol-http"}' -s | jq
```
6. Negotiate the contract
```sh
curl -d '{  "@context": {    "@vocab": "https://w3id.org/edc/v0.0.1/ns/"  },  "@type": "ContractRequest",  "counterPartyAddress": "http://provider:19194/protocol",  "protocol": "dataspace-protocol-http",  "policy": {    "@context": "http://www.w3.org/ns/odrl.jsonld",    "@id": "{{odrl:hasPolicy.@id}}",    "@type": "Offer",    "assigner": "provider",    "target": "assetId"  }}' \
  -X POST -H 'content-type: application/json' http://localhost:29193/management/v2/contractnegotiations \
  -s | jq
```
