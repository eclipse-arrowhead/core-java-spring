
## MQTT Setup

### Installation
On Debian-based systems, install the Mosquitto MQTT broker with the following command:
```
> sudo apt-get install mosquitto mosquitto-clients
```

### Configuration
To setup username and password for all clients, first create a password file:
```
> sudo touch /etc/mosquitto/passwords.dat
```

Then add user(s) for each core system:
```
> sudo mosquitto_password /etc/mosquitto/passwords.dat serviceregistry secretpassword
```

Remember to use strong username and password combinations.
Then add a custom configuration to Mosquitto to disable anonymous logins. Edit /etc/mosquitto/conf.d/broker.conf and add the following lines:
```
allow_anonymous false
password_file /etc/mosquitto/passwords.dat
```

Restart the broker with:
```
> sudo service mosquitto restart
```

Edit application.properties in the target/ folder and make sure these lines are there (edit username and password to match prev. steps):

```
# MQTT broker parameters
mqtt.broker.enabled=true
mqtt.broker.address=127.0.0.1
mqtt.broker.port=1883
mqtt.broker.username=serviceregistry
mqtt.broker.password=secretpassword
```

## Testing
The Mosquitto broker stores log entries in /var/log/mosquitto/mosquitto.log
Here a user can see login attempts etc.

