import requests
import json
from flask import Flask, request

# --- Arrowhead Core Settings ---
SR_ADDRESS = "http://localhost:8443"
EH_ADDRESS = "http://localhost:8455"

# --- Subscriber System Details ---
SYSTEM_NAME = "TennisBallSubscriber"
ADDRESS = "127.0.0.1"
PORT = 8080
INTERFACE_NAME = "HTTP-INSECURE-JSON"
SUBSCRIBER_SERVICE_DEFINITION = "tennis-ball-subscriber-service"
SUBSCRIBER_SERVICE_URI = "/tennis-subscriber-events"

# --- Event Details ---
EVENT_TYPE = "BallMovement"

app = Flask(__name__)

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

def register_subscriber_service_offering():
    """Registers the subscriber's actual service offering in Service Registry."""
    print(f"Registering service offering '{SUBSCRIBER_SERVICE_DEFINITION}' for system '{SYSTEM_NAME}'...")
    payload = {
        "serviceDefinition": SUBSCRIBER_SERVICE_DEFINITION,
        "interfaces": [INTERFACE_NAME],
        "providerSystem": {
            "systemName": SYSTEM_NAME,
            "address": ADDRESS,
            "port": PORT,
            "authenticationInfo": ""
        },
        "serviceUri": SUBSCRIBER_SERVICE_URI,
        "secure": "NOT_SECURE",
        "version": 1
    }
    try:
        response = requests.post(f"{SR_ADDRESS}/serviceregistry/register", json=payload)
        response.raise_for_status()
        print(f"Service offering '{SUBSCRIBER_SERVICE_DEFINITION}' registered successfully.")
    except requests.exceptions.HTTPError as e:
        if e.response.status_code == 409:
            print(f"Service offering '{SUBSCRIBER_SERVICE_DEFINITION}' already registered. Continuing...")
        else:
            print(f"Error registering service offering: {e}")
            if e.response:
                print(f"Response content: {e.response.text}")
            exit(1)
    except requests.exceptions.ConnectionError as e:
        print(f"Connection error to Service Registry during service offering registration: {e}")
        exit(1)

def subscribe_to_event():
    """Subscribes to 'BallMovement' events from the Event Handler."""
    print(f"Subscribing to event '{EVENT_TYPE}' from Event Handler...")
    
    filter_payload = {
        "subscriberSystem": {
            "systemName": SYSTEM_NAME,
            "address": ADDRESS,
            "port": PORT,
            "authenticationInfo": "",
            "metadata": {} # Keep this as an empty object
        },
        "eventType": EVENT_TYPE,
        "sources": [], # Empty list if not filtering by specific sources
        "interfaceIds": [], # Empty list if not using specific interfaces for subscription
        "matchMetaData": False,
        "filterMetaData": {}, # Keep this as an empty object
        "startDate": None, # Use None for no start date filter
        "endDate": None,   # Use None for no end date filter
        "notifyUri": "/event_callback"
    }

    try:
        response = requests.post(f"{EH_ADDRESS}/eventhandler/subscribe", json=filter_payload)
        response.raise_for_status() # This will raise an HTTPError for 4xx/5xx responses
        
        # Try to parse JSON. If response is empty or not JSON, this will raise a JSONDecodeError
        try:
            subscription_id = response.json().get('id')
            print(f"Successfully subscribed to event '{EVENT_TYPE}'. Subscription ID: {subscription_id}")
        except json.JSONDecodeError as json_e:
            print(f"Error decoding JSON from Event Handler subscription response: {json_e}")
            print(f"Raw response content: '{response.text}'") # Print raw response for debugging
            exit(1)

    except requests.exceptions.HTTPError as e: # Catch HTTPError specifically
        print(f"Error subscribing to event: {e}")
        if e.response is not None:
            print(f"Response status: {e.response.status_code}")
            print(f"Response content: {e.response.text}") # Print the actual response content
        else:
            print(f"No detailed HTTP response available.")
        exit(1)
    except requests.exceptions.ConnectionError as e: # Catch connection errors
        print(f"Connection error to Event Handler: {e}. Is Arrowhead Core running?")
        exit(1)
    except requests.exceptions.RequestException as e: # Catch any other request exceptions
        print(f"An unexpected request error occurred: {e}")
        exit(1)
        
@app.route("/event_callback", methods=["POST"])
def event_callback():
    """Endpoint for Event Handler to post events to."""
    if request.is_json:
        event_data = request.get_json()
        print(f"\n--- Received Event ---")
        print(f"Event Type: {event_data.get('eventType')}")
        print(f"Source System: {event_data.get('source', {}).get('systemName')}")
        try:
            # Event payload is already a JSON string, so load it
            payload_content = json.loads(event_data.get('payload', '{}'))
            print(f"Payload: {json.dumps(payload_content, indent=2)}")
        except json.JSONDecodeError as e:
            print(f"Error decoding payload JSON: {e}")
            print(f"Payload (raw): {event_data.get('payload')}")
        print(f"----------------------")
        return "Event received", 200
    else:
        return "Invalid request, expected JSON", 400

if __name__ == "__main__":
    print("--- Starting Tennis Ball Subscriber ---")
    register_system()
    register_subscriber_service_offering()
    subscribe_to_event()

    print(f"Subscriber running on http://{ADDRESS}:{PORT}. Waiting for events...")
    app.run(host=ADDRESS, port=PORT, debug=True, use_reloader=False)