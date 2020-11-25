![DIF Logo](https://raw.githubusercontent.com/decentralized-identity/universal-resolver/master/docs/logo-dif.png)

# Universal Resolver Driver: did:vaa

This is a [Universal Resolver](https://github.com/decentralized-identity/universal-resolver/) driver for Caict **did:vaa** identifiers.

## Specifications

* [W3C Decentralized Identifiers](https://w3c.github.io/did-core/)
* [VAA DID Method Specification](https://github.com/caict-develop-zhangbo/vaa-method)

## Example DIDs

```
did:vaa:3wJVWDQWtDFx27FqvSqyo5xsTsxC
```
## Build and Run (Docker)

```
docker build -f ./docker/Dockerfile . -t caict-develop-zhangbo/driver-did-vaa
docker run -p 8080:8080 caict-develop-zhangbo/driver-did-vaa
curl -X GET http://localhost:8080/1.0/identifiers/did:vaa:3wJVWDQWtDFx27FqvSqyo5xsTsxC
```

## Build (native Java)

Maven build:

	mvn --settings settings.xml clean install


## Driver Metadata

The driver returns the following metadata in addition to a DID document:

* `proof`: Some proof info about the DID document.
* `created`: The DID create time.
* `updated`: The DID document last update time.
