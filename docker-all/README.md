# Docker Image of All Core Systems

In certain scenarios, such as for development, demonstration or testing, it might be preferred to have one Docker image containing all Arrowhead Core systems instead of having one image for each system.
Most significantly, having a single container reduces memory overhead, both in terms of RAM and disk usage.
Note, however, that using this image is _NOT_ recommended for most kinds of production scenarios.

## Evaluating Image

To try this image, first make sure that the [`target`](target) folder, located in this directory, contains the `.jar` files of all core systems.
If not, they can be assembled and collected via the following Maven command:

```sh
$ mvn -DskipTests=true package -pl docker-all -am
```

If you are using an IDE, running the maven `package` goal should also cause the [`target`](target) folder to be populated.

As a second step, retrieve all necessary .sql files. For this run the following command:

```sh
[docker-all/]$ chmod u+x initSQL.sh
[docker-all/]$ ./initSQL.sh
```
The script downloads all required files to setup the database.

Thirdly, change the current directory to the [`example`](example) folder located in this directory, and then execute the following terminal commands:

```sh
$ docker volume create arrowhead_core_mysql
$ docker-compose up --build
```

This should start up a Docker network called `example_default` with both a MySQL database and the image containing the Arrowhead Core systems.
Please refer to the [`example/docker-compose.yml`](example/docker-compose.yml) file for more details about how the latter image can be configured.

## Building Image

To build the image for use in other projects, the following command could be used:

```sh
$ docker build . -t arrowhead-core-all
```

Please make sure the folder in which this file is located is the current working directory before executing the above command.
