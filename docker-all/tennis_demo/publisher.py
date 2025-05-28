import requests
import json
import time

# --- Arrowhead Core Settings ---
SR_ADDRESS = "http://localhost:8443"
EH_ADDRESS = "http://localhost:8455"

# --- Publisher System Details ---
SYSTEM_NAME = "TennisBallPublisher"
ADDRESS = "127.0.0.1"
PORT = 5000
INTERFACE_NAME = "HTTP-INSECURE-JSON"
PUBLISHER_SERVICE_DEFINITION = "tennis-ball-publisher-service"
PUBLISHER_SERVICE_URI = "/tennis-publisher-events"

# --- Event Details ---
EVENT_TYPE = "BallMovement"
EVENT_SOURCE_SYSTEM = SYSTEM_NAME
EVENT_DESCRIPTION = "A tennis ball moved on the court."

def unregister_system(system_name, address, port):
    """Unregisters a system from the Service Registry using DELETE /serviceregistry/unregister-system."""
    print(f"Unregistering system '{system_name}'...")
    params = {
        "system_name": system_name,
        "address": address,
        "port": port
    }
    try:
        response = requests.delete(f"{SR_ADDRESS}/serviceregistry/unregister-system", params=params)
        response.raise_for_status()
        print(f"System '{system_name}' unregistered successfully.")
    except requests.exceptions.HTTPError as e:
        if e.response.status_code == 404: # Not Found, meaning system was not registered
            print(f"System '{system_name}' not found/already unregistered. Continuing...")
        else:
            print(f"Error unregistering system: {e}")
            if e.response:
                print(f"Response content: {e.response.text}")
    except requests.exceptions.ConnectionError as e:
        print(f"Connection error to Service Registry during system unregister: {e}")

def register_system():
    """Registers the system in the Service Registry using POST /serviceregistry/register-system."""
    # First, try to unregister the system in case it's still there
    unregister_system(SYSTEM_NAME, ADDRESS, PORT)

    print(f"Registering system '{SYSTEM_NAME}' in Service Registry...")
    payload = {
        "systemName": SYSTEM_NAME,
        "address": ADDRESS,
        "port": PORT,
        "authenticationInfo": ""
    }
    try:
        response = requests.post(f"{SR_ADDRESS}/serviceregistry/register-system", json=payload)
        response.raise_for_status()
        print(f"System '{SYSTEM_NAME}' registered successfully in SR.")
    except requests.exceptions.HTTPError as e:
        if e.response.status_code == 409: # Conflict, meaning system already registered
            print(f"System '{SYSTEM_NAME}' already registered in SR. Continuing...")
        else:
            print(f"Error registering system: {e}")
            if e.response:
                print(f"Response content: {e.response.text}")
            exit(1)
    except requests.exceptions.ConnectionError as e:
        print(f"Connection error to Service Registry: {e}. Is Arrowhead Core running?")
        exit(1)

def register_publisher_service_offering():
    """Registers the publisher's actual service offering in Service Registry."""
    print(f"Registering service offering '{PUBLISHER_SERVICE_DEFINITION}' for system '{SYSTEM_NAME}'...")
    payload = {
        "serviceDefinition": PUBLISHER_SERVICE_DEFINITION,
        "interfaces": [INTERFACE_NAME],
        "providerSystem": {
            "systemName": SYSTEM_NAME,
            "address": ADDRESS,
            "port": PORT,
            "authenticationInfo": ""
        },
        "serviceUri": PUBLISHER_SERVICE_URI,
        "secure": "NOT_SECURE",
        "version": 1
    }
    try:
        response = requests.post(f"{SR_ADDRESS}/serviceregistry/register", json=payload)
        response.raise_for_status()
        print(f"Service offering '{PUBLISHER_SERVICE_DEFINITION}' registered successfully.")
    except requests.exceptions.HTTPError as e:
        if e.response.status_code == 409:
            print(f"Service offering '{PUBLISHER_SERVICE_DEFINITION}' already registered. Continuing...")
        else:
            print(f"Error registering service offering: {e}")
            if e.response:
                print(f"Response content: {e.response.text}")
            exit(1)
    except requests.exceptions.ConnectionError as e:
        print(f"Connection error to Service Registry during service offering registration: {e}")
        exit(1)

def publish_event(event_data):
    """Publishes an event to the Event Handler."""
    print(f"Publishing event '{EVENT_TYPE}' to Event Handler...")
    
    payload = {
        "eventType": EVENT_TYPE,
        "source": {
            "systemName": EVENT_SOURCE_SYSTEM,
            "address": ADDRESS,
            "port": PORT,
            "authenticationInfo": "",
            "metadata": {} # Keep this as an empty object
        },
        "payload": json.dumps(event_data), # Your custom event data as a JSON string
        "timeStamp": time.strftime("%Y-%m-%dT%H:%M:%SZ") # Ensure ISO 8601 format including Z for UTC
    }
    
    try:
        response = requests.post(f"{EH_ADDRESS}/eventhandler/publish", json=payload)
        response.raise_for_status() # This will raise an HTTPError for 4xx/5xx responses
        print(f"Event '{EVENT_TYPE}' published successfully. Status: {response.status_code}")
    except requests.exceptions.HTTPError as e: # Catch HTTPError specifically
        print(f"Error publishing event: {e}")
        if e.response is not None: # Check if response object exists
            print(f"Response status: {e.response.status_code}")
            print(f"Response content: {e.response.text}") # Print the actual response content
        else:
            print(f"No detailed HTTP response available.")
    except requests.exceptions.ConnectionError as e: # Catch connection errors
        print(f"Connection error to Event Handler: {e}. Is Arrowhead Core running?")
    except requests.exceptions.RequestException as e: # Catch any other request exceptions
        print(f"An unexpected request error occurred: {e}")

if __name__ == "__main__":
    print("--- Starting Tennis Ball Publisher ---")
    register_system()
    register_publisher_service_offering()

    event_counter = 0
    while True:
        event_counter += 1
        current_time_for_ball_data = time.strftime("%Y-%m-%d %H:%M:%S")
        ball_position = {"x": event_counter * 10 % 100, "y": event_counter * 5 % 50}

        event_data = {
            "timestamp": current_time_for_ball_data,
            "ballId": "tennis_ball_001",
            "movementDirection": "forward",
            "position": ball_position,
            "eventCounter": event_counter
        }
        
        publish_event(event_data)
        time.sleep(3)