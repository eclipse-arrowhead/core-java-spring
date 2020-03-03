# How to include certificate into docker

## Prerequisites
1. You have certificates for **all** your core systems. If not, follow the [Certificate guide](/README.md#certificates) first.


## Steps
1. [Check you have all the necessary certificates](#step01)
2. [Edit your property file](#step02)
3. [Overwrite the Secure Mode properties](#step03) 
4. [Edit your docker-compose.yml](#step04)
5. [Mount the certificates as a volume to the containers](#step04)

<a name="step01" />

## 1. Check you have all the necessary certificates

Please make sure, you have already generated the certificates you wish to use.

![Step 1](/documentation/images/images_docker_certificate_guide/step01.png)


<a name="step02" />

## 2. Edit your property file

Open the Core Systems `application.properties` file with your favorite editor.

![Step 2](/documentation/images/images_docker_certificate_guide/step02.png)

<a name="step03" />

## 3. Overwrite the Secure Mode properties

Scroll down to the **Secure Mode** part of the properties file and edit:
 * `server.ssl-key-store` property
 
 By default its value is: `classpath:certificates/service_registry.p12`, it means it will search for the certificate **inside** the JAR file, we have to change it to `file:<your_certificate_name.extension` (ex.: `file:service_registry.p12`). This means, when the application starts up, it will load the certificate from **the same folder** as the JAR.
 
 * `server.ssl.key-store-type` property
  
  If your file is not a p12 file, change it accordingly
  
 * `server.ssl.key-store-password` property
 
  You should **really not** use the default password. Hopefully you used a different password when generating the certificate.
  
![Step 3](/documentation/images/images_docker_certificate_guide/step03.png)

# Repeat [Step 2](#step02) and [Step 3](#step03) for every property file

<a name="step04" />

## 4. Edit your docker-compose.yml    

Open the `docker-compose.yml` file with your favorite editor.

![Step 4](/documentation/images/images_docker_certificate_guide/step04.png)

<a name="step05" />

## 5. Mount the certificates as a volume to the containers

Mount the certificate file as a volume inside the container.

The syntax is: - outside_path/outside_certificate.name:inside_path/inside_certificate.name

![Step 5](/documentation/images/images_docker_certificate_guide/step05.png)

In the image you can see two examples. I'll explain the first one.

`- ./tmit_certs/service_registry.p12:/serviceregistry/service_registry.p12`

In [Step 1](#step1) we saw that our certificates were inside a folder called `tmit_certs`. We are mounting the `service_registry.p12` file from the `tmit_certs` folder on the host machine inside the Docker Container into the `serviceregistry` folder under the name of `service_registry.p12`

> **Note:** The certificate name you specified in [Step 3](#step03) **MUST** match the `inside_certificate.name` specified in [Step 5](#step05), otherwise it won't work. 


## Congratulations, you are all set! You can start your containers :)


