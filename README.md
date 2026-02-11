# ♕ BYU CS 240 Chess

This project demonstrates mastery of proper software design, client/server architecture, networking using HTTP and WebSocket, database persistence, unit testing, serialization, and security.

## 10k Architecture Overview

The application implements a multiplayer chess server and a command line chess client.

[![Sequence Diagram](10k-architecture.png)](https://sequencediagram.org/index.html#initialData=C4S2BsFMAIGEAtIGckCh0AcCGAnUBjEbAO2DnBElIEZVs8RCSzYKrgAmO3AorU6AGVIOAG4jUAEyzAsAIyxIYAERnzFkdKgrFIuaKlaUa0ALQA+ISPE4AXNABWAexDFoAcywBbTcLEizS1VZBSVbbVc9HGgnADNYiN19QzZSDkCrfztHFzdPH1Q-Gwzg9TDEqJj4iuSjdmoMopF7LywAaxgvJ3FC6wCLaFLQyHCdSriEseSm6NMBurT7AFcMaWAYOSdcSRTjTka+7NaO6C6emZK1YdHI-Qma6N6ss3nU4Gpl1ZkNrZwdhfeByy9hwyBA7mIT2KAyGGhuSWi9wuc0sAI49nyMG6ElQQA)

## Modules

The application has three modules.

- **Client**: The command line program used to play a game of chess over the network.
- **Server**: The command line program that listens for network requests from the client and manages users and games.
- **Shared**: Code that is used by both the client and the server. This includes the rules of chess and tracking the state of a game.

## Starter Code

As you create your chess application you will move through specific phases of development. This starts with implementing the moves of chess and finishes with sending game moves over the network between your client and server. You will start each phase by copying course provided [starter-code](starter-code/) for that phase into the source code of the project. Do not copy a phases' starter code before you are ready to begin work on that phase.

## IntelliJ Support

Open the project directory in IntelliJ in order to develop, run, and debug your code using an IDE.

## Maven Support

You can use the following commands to build, test, package, and run your code.

| Command                    | Description                                     |
| -------------------------- | ----------------------------------------------- |
| `mvn compile`              | Builds the code                                 |
| `mvn package`              | Run the tests and build an Uber jar file        |
| `mvn package -DskipTests`  | Build an Uber jar file                          |
| `mvn install`              | Installs the packages into the local repository |
| `mvn test`                 | Run all the tests                               |
| `mvn -pl shared test`      | Run all the shared tests                        |
| `mvn -pl client exec:java` | Build and run the client `Main`                 |
| `mvn -pl server exec:java` | Build and run the server `Main`                 |

These commands are configured by the `pom.xml` (Project Object Model) files. There is a POM file in the root of the project, and one in each of the modules. The root POM defines any global dependencies and references the module POM files.

## Running the program using Java

Once you have compiled your project into an uber jar, you can execute it with the following command.

```sh
java -jar client/target/client-jar-with-dependencies.jar

♕ 240 Chess Client: chess.ChessPiece@7852e922
```

## My Server Design
View server design [here](https://sequencediagram.org/index.html#initialData=IYYwLg9gTgBAwgGwJYFMB2YBQAHYUxIhK4YwDKKUAbpTngUSWDABLBoAmCtu+hx7ZhWqEUdPo0EwAIsDDAAgiBAoAzqswc5wAEbBVKGBx2ZM6MFACeq3ETQBzGAAYAdAE5M9qBACu2AMQALADMABwATG4gMP7I9gAWYDoIPoYASij2SKoWckgQaJiIqKQAtAB85JQ0UABcMADaAAoA8mQAKgC6MAD0PgZQADpoAN4ARP2UaMAAtihjtWMwYwA0y7jqAO7QHAtLq8soM8BICHvLAL6YwjUwFazsXJT145NQ03PnB2MbqttQu0WyzWYyOJzOQLGVzYnG4sHuN1E9SgmWyYEoAAoMlkcpQMgBHVI5ACU12qojulVk8iUKnU9XsKDAAFUBhi3h8UKTqYplGpVJSjDpagAxJCcGCsyg8mA6SwwDmzMQ6FHAADWkoGME2SDA8QVA05MGACFVHHlKAAHmiNDzafy7gjySp6lKoDyySIVI7KjdnjAFKaUMBze11egAKKWlTYAgFT23Ur3YrmeqBJzBYbjObqYCMhbLCNQbx1A1TJXGoMh+XyNXoKFmTiYO189Q+qpelD1NA+BAIBMU+4tumqWogVXot3sgY87nae1t+7GWoKDgcTXS7QD71D+et0fj4PohQ+PUY4Cn+Kz5t7keC5er9cnvUexE7+4wp6l7FovFqXtYJ+cLtn6pavIaSpLPU+wgheertBAdZoFByyXAmlDtimGD1OEThOFmEwQZ8MDQcCyxwfECFISh+xXOgHCmF4vgBNA7CMjEIpwBG0hwAoMAADIQFkhRYcwTrUP6zRtF0vQGOo+RoFmipzGsvz-BwVygYKQH+uB5afJCIJqTsXzQo8wHiVQSIwAgQnihignCQSRJgKSb6GLuNL7gyTJTspXI3l5d5LsKYoSm6MpymW7xKpgKrBhqbpGhwEBqDAaAQMwVpooFvLBZZ1k9n224eZZ-oAHIQElSoir4nBRjGcaFFpSaVKJaZOAAjAROaqHm8zQUWJb1D40yXtASAAF4oLsdFNsODqtR2Vkuhu7pbu57YLfSMDGQCqgALJyCA8QYntHCkvF6owE0+h-DsAopelmUwMcYAnblC4Ckt7n1AAZiaBglSB1T+rdWwPdIECVWAR3vfEDUoLGCnofCybIKmMDpt1oxjL1-UFmMQ3QCNY16hN02zY2DHbd9BWrRYqTA55eX8mOE4oM+p0UdetP3sKj4Bper7OqVlQ6aWjnihkqgAZgEsgxJYGEfp8wkah3wUVR9bq7RqOYRj2EwLh+G4-5NFkWMWuITrpENvRjHeH4-heCg6AxHEiSu+7jm+FgomCqB9QNNIEb8RG7QRt0PRyaoCnDNbSH6x+5m6Vbl7a8hkJmbCGH012NlCX7DlF6ezlqK5zNUrebMwIyYBc+eGc22gc5BYtlTLmFT7C9osryon6BxaqGqD2gRipQKGVZdaOSffuisrQXRX9ptP2g6WACSaBUCaSAcFzmeI8j8YtejJRgB1OPZvyBODcWJMKmT8QUzN9vzTXi75-UPMbaLgq01qBwFA3BjyXibi+bQbdWaLk7sKaQICmSGC5iLTs7YJb1F9mXf8CBAKpzRr6DeLxNIbwNhfHCeEsxzQYp4J2AQUTrn8NgcUGp+JohgAAcSVBoAOZVSwNA4RHaO9glQJ2bknFq4t8EvHTvBFupl5b4MDqLHyYAuE5gxBwpk6i1DlxJFXGQn9Rx1yZI3Me0Cvr81FOKHukD5D92NOIoeV1R5OPHilNK08YDZTnnzdenZuy9lXv-fxNR6jb13sgA+bjj5NWTufTG2Meq33zPfYaT8KKvypg7Px39HF2OAAYwBJi1HcIgZRFuFiF4hWsRKHRAoopj3nvlQhAS65Kh5Gwuea8U651LPXepMs5YK1CbpZYIicwFgaOMCZKBN7SALJ1cIwRAggk2PEXUKBqoGRgssZIoA1TbIGrssYszyqQWzjAToJClZkMxibLM4zuFTJmUqeZizlmrOWOszZRyvggn2SAQ5RFjmWzORc2iVzqGO2Yv4DgAB2NwTgUBOBiBGYIcAuIADZ4Ac04RWIohsxKtLCY0VoHRhGiOfpnLM4K5g3MTFIvpMi6WgobCMkl1lDxyBQDojEcAOY6L0ZXNe1d247XrmYtxVTgpwNqbYq8fdGluOHglfJFSkIT08S9HxWBcmctWivZmfDwk7z3tEuRSFYkozPm1IlV9km5lSYWB+pZRqZKgFNN+0L9XLWsr-eQRSjH1G5eiPlrKZUdyFCuNc+K5gel9b9dp8a-5oKWhgmAobeVKiGbgxRfTF5p1me8+oSyVkMrzgko2DzcbFoWaWz50LaGwssCA2ymwPZIASGAVtfYIAdoAFIQHFHGww-hAVqkJRfZRStg5NGZDJHosyxGWvQFmbACBgCtqgHACAtkoBrDrRW+ETKvwsrefWmAZavk-E3du3d+6FEcr9atAAVsOtAfKh3iiFSgQkFc3IhLFTA4xkrwHmOaVGruNihYFIcU0lx6rM5aqnjq2eeqjGL0KkE41JL-QRPNYfFu1rT6kKWu1LGXVHV9WdUTV1pMPVeuyR-cVdMDUFwDYU0VhjWOqPDReyNsDo3d1HZFeUdbVXXVmSh56M8cqJpUelHDPS8NbzNVEnR8ySPNTI1Wy+MAACslDcb41o8TUs4pIn72TYYd5PrMOhOsrM1BS8AHBpKQ+6AU4N1bsoJ5qAgnvpypE0chxPn717ugJJjU0mPGobk74hzeSIqptc6M0s-mwxWujEjOJtr4D2so5mEzKSBouvSSaM0NZwzIXs6xrDhrlNAZ4yBn+a5-PebvX5yLAXINCYfLGt0MBIC7S6zunruGHjMpgO+n9OacF4ILel4h8S7XkONsZkYTamLOy8Fuzt3a9vykQMGWAwBsAbsIHkAoMAp3mBnaSkOYcI5R16MYZOp64QrefUmkA3A8D8v+1AYVgG03Aa+kAxBk4BiqAxIFqxCDQGGBNAgNaLnBzg+8sApHjd4c1MR0gysqOUGpYxy1iH2OkH1Lh31oL0aCfoiJ6J0n75Pv+hO3gXNi2vyFrAseu51bNvQqAA)
