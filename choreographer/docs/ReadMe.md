# Choreographer

<a name="choreographer_sdd" />

## System Design Description Overview

This supporting core system makes it possible to execute pre-defined workflows through orchestration and service consumption.

Each workflow can be divided into three segments:
* Plans,
* Actions,
* and Steps.

_Plans_ define the whole workflow by name and they contain _Actions_ which group coherent _Steps_ together for greater transparency and enabling sequentialization of these _Step_ groups.

Workflow execution in this generation can only be accomplished if the requested providers in each _Step_ are all available (they are registered with the same name in the service registry as in the plan description) and the requested services call back (notify) to the Choreographer through the Choreography service that the execution on their end is done. Only this way can the Choreographer continue the execution of the _Plan_.

<a name="choreographer_usecases" />

## Services and Use Cases

This Supporting Core System provides the Choreographer Service which only has one use-case scenario: notifying the Choreographer from the providers' side that the executed _Step_ is done.

<a name="choreographer_endpoints" />

## Endpoints

The Choreographer offers two types of endpoints: Client and Management.

Swagger API documentation is available on: `https://<host>:<port>` <br />
The base URL for the requests: `http://<host>:<port>/choreographer`

<a name="choreographer_endpoints_client" />

### Client endpoint description<br />

| Function | URL subpath | Method | Input | Output |
| -------- | ----------- | ------ | ----- | ------ |
| [Echo](#choreographer_endpoints_get_echo) | /echo | GET | - | OK |
| [Notify that a step is done](#choreographer_endpoints_post_nofity) | /notifyStepDone | POST | [SessionRunningStepData](#datastructures_choreographer_session_running_step_data) | OK |

<a name="choreographer_endpoints_mgmt" />

###  Management endpoint description<br />

These endpoints are mainly used by the Management Tool and Cloud Administrators.

| Function | URL subpath | Method | Input | Output |
| -------- | ----------- | ------ | ----- | ------ |
| [Get all plan entries](#choreographer_endpoints_get_mgmt_plan) | /mgmt/plan | GET | - | [ChoreographerPlanEntryList](#datastructures_choreographerplanentrylist) |
| [Add a plan entry](#choreographer_endpoints_post_mgmt_plan) | /mgmt/plan | POST | [ChoreographerPlanEntry](#datastructures_choreographer_addplanentry) | CREATED |
| [Get a plan entry by ID](#choreographer_endpoints_get_mgmt_plan_id) | /mgmt/plan/{id} | GET | ChoreographerPlanID | [ChoreographerPlanEntry](#datastructures_choreographerplanentry)
| [Delete a plan entry by ID](#choreographer_endpoints_delete_plan_id) | /mgmt/plan/{id} | DELETE | ChoreographerPlanID | NO CONTENT |
| [Start one or more sessions executing a plan](#choreographer_endpoints_start_session_id) | /mgmt/session/start | POST | [ChoreographerPlanIDList](#datastructures_choreographerplanidlist) | CREATED |
| [Change a running step to finished](#choreographer_endpoints_post_stepfinished) | /mgmt/session/stepFinished | POST | [SessionRunningStepData](#datastructures_choreographer_session_running_step_data_mgmt) | OK |


<a name="choreographer_endpoints_get_echo" />

### Echo
```
GET /choreographer/echo
```

Returns a "Got it!" message with the purpose of testing the core service availability.

<a name="choreographer_endpoints_post_nofity" />

### Notify that a step is done
```
POST /choreographer/notifyStepDone
```

Returns HTTP 200 - OK if the notification of the Choreographer is successful.

<a name="datastructures_choreographer_session_running_step_data" />

__SessionRunningStepData__ is the input
```json
{
  "runningStepId": 0,
  "sessionId": 0
}
```

| Field | Description | Mandatory |
| ----- | ----------- | --------- |
| `runningStepId` | The id of the running step which the provider gets from the Choreographer | yes |
| `sessionId` | The id of the session in which the step is running | yes |

<a name="choreographer_endpoints_get_mgmt_plan" />

### Get all plan entries
```
GET /choreographer/mgmt/plan
```

Returns a list of Choreographer Plan records. If `page` and `item_per_page` are not defined, returns all records.

Query params:

| Field | Description | Mandatory |
| ----- | ----------- | --------- |
| `page` | zero based page index | no |
| `item_per_page` | maximum number of items returned | no |
| `sort_field` | sorts by the given column | no |
| `direction` | direction of sorting | no |

> **Note:** Default value for `sort_field` is `id`. All possible values are: 
> * `id`
> * `createdAt`
> * `updatedAt`
> * `name`

> **Note:** Default value for `direction` is `ASC`. All possible values are:
> * `ASC`
> * `DESC` 

<a name="datastructures_choreographerplanentrylist" />

Returns a __ChoreographerPlanEntryList__
```json
[
  { 
    "id": 0,
    "name": "string",
    "firstActionName": "string",
    "createdAt": "string", 
    "updatedAt": "string",
    "actions": [
      {
	"id": 0,
        "name": "string",
        "createdAt": "string",
	"updatedAt": "string",
        "firstStepNames": [
          "string"
        ],
        "nextActionName": "string",
        "steps": [
          {
	    "id": 0,
	    "name": "string",
	    "serviceName": "string",
            "metadata": "string",
	    "parameters": "string",
            "quantity": 0,
	    "createdAt": "string",
            "updatedAt": "string",
            "nextSteps": [
              {
                "id": 0,
                "stepName": "string"
              }
            ]
          }
        ]
      }
    ]
  }
]
```

| Field | Description |
| ----- | ----------- |
| `id` | ID of the PlanEntry |
| `name` | Name of the PlanEntry |
| `firstActionName` | Name of the First Action |
| `createdAt` | Creation date of the entry |
| `updatedAt` | When the entry was last updated |
| `actions` | Array of the [ActionEntries](#datasturctures_choreographer_actionentry) in a Plan |

<a name="datasturctures_choreographer_actionentry" />

Contains a list of __ActionEntires__.
```json
{
  "id": 0,
  "name": "string",
  "createdAt": "string",
  "updatedAt": "string",
  "firstStepNames": [
    "string"
  ],
  "nextActionName": "string",
  "steps": [
    {
      "id": 0,
      "name": "string",
      "serviceName": "string",
      "metadata": "string",
      "parameters": "string",
      "quantity": 0,
      "createdAt": "string",
      "updatedAt": "string",
      "nextSteps": [
        {
          "id": 0,
          "stepName": "string"
        }
      ]
    }
  ]
}
```

| Field | Description |
| ----- | ----------- |
| `id` | ID of an Action entry |
| `name` | Name of an Action entry |
| `createdAt` | Creation date of the entry |
| `updatedAt` | When the entry was last updated |
| `firstStepNames` | The names of the first steps within this Action |
| `nextActionName` | The name of the Action which follows the current Action |
| `steps` | Array of [StepEntries](#datasturctures_choreographer_stepentry) in an Action |

<a name="datasturctures_choreographer_stepentry" />

Contains a list of __StepEntries__.

```json
{
  "id": 0,
  "name": "string",
  "serviceName": "string",
  "metadata": "string",
  "parameters": "string",
  "quantity": 0,
  "createdAt": "string",
  "updatedAt": "string",
  "nextSteps": [
    {
      "id": 0,
      "stepName": "string"
    }
  ]
}
```

| Field | Description |
| ----- | ----------- |
| `id` | ID of a Step entry |
| `name` | Name of a Step entry |
| `serviceName` | Name of the service which the step uses |
| `metadata` | Additional metadata needed for step execution |
| `parameters` | Parameters needed by the device to run this step |
| `quantity` |  How many times should the step run in its current position |
| `createdAt` | Creation date of the entry |
| `updatedAt` | When the entry was last updated |
| `nextSteps` | Array of next steps following the Step entry |

> **Note:**  In the current version the `parameters`, the `metadata` and the `quantity` fields cannot be used when executing plans. These will be implemented in future versions.

<a name="choreographer_endpoints_post_mgmt_plan" />

### Add a plan entry
```
POST choreographer/mgmt/plan
```

Creates a plan record and returns HTTP 201 - CREATED if the entry creation is successful or the proper error message if the entry creation failed.

<a name="datastructures_choreographer_addplanentry" />

__ChoreographerPlanEntry__ is the input
```json
{
  "actions": [
    {
      "firstStepNames": [
        "string"
      ],
      "name": "string",
      "nextActionName": "string",
      "steps": [
        {
          "metadata": "string",
          "name": "string",
          "nextStepNames": [
            "string"
          ],
          "parameters": "string",
          "quantity": 0,
          "serviceName": "string"
        }
      ]
    }
  ],
  "firstActionName": "string",
  "name": "string"
}
```

| Field | Description | Mandatory |
| ----- | ----------- | --------- |
| `name` | Name of the PlanEntry | yes |
| `firstActionName` | Name of the First Action | yes |
| `actions` | Array of the [ActionEntries](#datasturctures_choreographer_actionentry) in a Plan | yes |

<a name="choreographer_endpoints_get_mgmt_plan_id" />

### Get a plan entry by ID
```
GET /choreographer/mgmt/plan/{id}
```

Returns the Choreographer Plan Entry specified by the ID path parameter.

<a name="datastructures_choreographerplanentry" />

Returns a ChoreographerPlanEntry
```json
{
  "actions": [
    {
      "createdAt": "string",
      "firstStepNames": [
        "string"
      ],
      "id": 0,
      "name": "string",
      "nextActionName": "string",
      "steps": [
        {
          "createdAt": "string",
          "id": 0,
          "metadata": "string",
          "name": "string",
          "nextSteps": [
            {
              "id": 0,
              "stepName": "string"
            }
          ],
          "parameters": "string",
          "quantity": 0,
          "serviceName": "string",
          "updatedAt": "string"
        }
      ],
      "updatedAt": "string"
    }
  ],
  "createdAt": "string",
  "firstActionName": "string",
  "id": 0,
  "name": "string",
  "updatedAt": "string"
}
```

| Field | Description |
| ----- | ----------- |
| `id` | ID of the PlanEntry |
| `name` | Name of the PlanEntry |
| `firstActionName` | Name of the First Action |
| `createdAt` | Creation date of the entry |
| `updatedAt` | When the entry was last updated |
| `actions` | Array of the [ActionEntries](#datasturctures_choreographer_actionentry) in a Plan |

<a name="choreographer_endpoints_delete_plan_id" />

### Delete a plan entry by id
```
DELETE /choreographer/plan/{id}
```

Remove the Choreographer Plan record specified by the id path parameter.

<a name="choreographer_endpoints_start_session_id" />

### Start one or more sessions executing a plan
```
POST /choreographer/mgmt/session/start
```

Starts one or more plans in sessions.

<a name="datastructures_choreographerplanidlist" />

ChoreographerPlanIDList is the input
```json
[
  {
    "id": 0
  }
]
```

| Field | Description | Mandatory |
| ----- | ----------- | --------- |
| id | ID of a plan to be executed | yes |

<a name="choreographer_endpoints_post_stepfinished" />

### Change a running step to finished
```
POST /choreographer/mgmt/session/stepFinished
```

Returns HTTP 200 - OK if the notification of the Choreographer is successful.

<a name="datastructures_choreographer_session_running_step_data_mgmt" />

__SessionRunningStepData__ is the input
```json
{
  "runningStepId": 0,
  "sessionId": 0
}
```

