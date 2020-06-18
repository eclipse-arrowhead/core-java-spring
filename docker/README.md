# Docker Compose Example Files

This folder contains various files for setting up a network of Arrowhead Core systems, each located in its own Docker container.
Using the files requires you to follow the below steps:

## Create Container Volumes

In order for data to persist after shutting down your containers, you must use a so-called _Docker Volume_.
The one needed by the [`docker-compose.yml`](docker-compose.yml) file in this folder requires one to exist with the name `mysql`.
The following terminal command may be used to create such a volume:

```sh
$ docker volume create --name=mysql
```

## Copy Initialization Scripts for the MySQL Database

When the database container of the [`docker-compose.yml`](docker-compose.yml) starts, its database will be empty.
As the Arrowhead Core systems do not attempt to create the database tables themselves if they do not exist, you must make sure that they are created by the database container when it is started for the first time.
The following terminal command may be used, given that the root directory of this repository is the current working directory, to copy database initialization scripts to a location from which they will be read by the [`docker-compose.yml`](docker-compose.yml) file:

```sh
$ cp scripts/create_empty_arrowhead_db.sql docker/
```

## Configuring the Core Systems

The [`core_system_config`](core_system_config) folder contains a collection of `.properties` files, each of which is passed to an Arrowhead Core system when the network described in [`docker-compose.yml`](docker-compose.yml) is brought up.
You may want to look at, or perhaps even edit, some of those files to get a better understanding of how the Arrowhead Core systems can be configured.
Editing them is not required, however, for the example network to run.

## Running the Compose Script

Having completed the above steps, you are ready to execute the following command from this directory:

```sh
$ docker-compose up --build
```

To shut down the `docker_default` network you just started, use `CTRL-C`, or whatever keyboard combination is used to send the interrupt signal to Docker.
Afterwards, you may use the following command to clean up any remaining resources:

```sh
$ docker-compose down
```
