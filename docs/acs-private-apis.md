# ACS Private REST APIs for Hx Insight Integration

## Purpose

## Endpoints

Base path for all endpoints mentioned below is `{acs_host}/alfresco/api/-default-/private/hxi/versions/1`

### Agents API

#### Get Agents
List of agents that are available for Hx Insights.
* Method: `GET`
* Path: `/agents`
* Response:
    * Status: 200
    * Content-Type: `application/json`
    * Entry object (wrapped with list/pagination information):
        * `name` [string]
        * `description` [string]
        * `id` [string]
    * Sample
    ```json
    {
      "list": {
        "pagination": {
          "count": 2,
          "hasMoreItems": false,
          "totalItems": 2,
          "skipCount": 0,
          "maxItems": 100
        },
        "entries": [
          {
            "entry": {
              "name": "HR Policy Agent",
              "description": "This agent is responsible for HR policy predictions",
              "id": "5fca2c77-cdc0-4118-9373-e75f53177ff8",
              "avatarUrl": "https://dummy-host.xyz/avatars/agent2.png"
            }
          },
          {
            "entry": {
              "name": "Knowledge Base Agent",
              "description": "Very smart about product knowledge",
              "id": "b999ee14-3974-41b2-bef8-70ab38c9e642",
              "avatarUrl": "https://dummy-host.xyz/avatars/agent2.png"
            }
          }
        ]
      }
    }
    ```

### Questions API

#### Ask Question
Ask a question for Hx Insights.
* Method: `POST`
* Path: `/agents/{agentId}/questions`
* Request:
    * Content-Type: `application/json`
    * Body:
        * `question` [string]
        * `restrictionQuery` [object]:
            * `nodeIds` [array of strings]
    * Sample
    ```json
    {
      "question": "What is the policy for maternity leave?",
      "restrictionQuery": {
        "nodeIds": ["nodeId1", "nodeId2"]
      }
    }
    ```
* Response:
  * Status: 201
  * Content-Type: `application/json`
  * Body (entry of):
      * `question` [string]
      * `restrictionQuery` [object]:
          * `nodeIds` [array of strings]
      * `questionId` [string]
  * Sample
    ```json
    {
        "entry": {
            "questionId": "5fca2c77-cdc0-4118-9373-e75f53177ff8",
            "question": "What is the meaning of life?",
            "restrictionQuery": {
                "nodesIds": [
                    "5fa74ad3-9b5b-461b-9df5-de407f1f4fe7"
                ]
            }
        }
    }
    ```

#### Retry Question
Ask a question for Hx Insights (not ready on Hx Insight side yet).
* Method: `POST`
* Path: `/agents/{agentId}/questions/{questionId}/retry`
* Request:
    * Content-Type: `application/json`
    * Body:
        * `comments` [string]
        * `originalQuestion` [object]:
          * `question` [string]
          * `restrictionQuery` [object]:
              * `nodeIds` [array of strings]
    * Sample
    ```json
    {
      "comments": "I need more details about the answer.",
      "originalQuestion": {
        "question": "What is the meaning of life?",
        "restrictionQuery": {
          "nodesIds": [
            "nodeId1"
          ]
        }
      }
    }
    ```


#### Send feedback on a question
TBD - not ready on Hx Insight side yet.


### Answers API
Get answer for a question.
* Method: `GET`
* Path: `questions/{questionId}/answers`
* Response:
    * Status: 200
    * Content-Type: `application/json`
    * Entry object (wrapped with list/pagination information):
        * `question` [string]
        * `answer` [string]
        * `references` [object]:
            * `referenceId` [string]
            * `referenceText` [string]
    * Sample
```json
{
    "list": {
        "pagination": {
            "count": 1,
            "hasMoreItems": false,
            "totalItems": 1,
            "skipCount": 0,
            "maxItems": 100
        },
        "entries": [
            {
                "entry": {
                    "question": "This is the question",
                    "answer": "This is the answer to the question",
                    "references": [
                        {
                            "referenceId": "276718b0-c3ab-4e11-81d5-96dbbb540269",
                            "referenceText": "This is the text reference which led to generating the answer"
                        }
                    ]
                }
            }
        ]
    }
}
```
### Hx Insights Configuration API
Get Hx Insights configuration.
* Method: `GET`
* Path: `config`
* Response:
    * Status: 200
    * Content-Type: `application/json`
    * Body (entry of):
        * `knowledgeRetrievalUrl` [string]
    * Sample
```json
{
    "entry": {
        "knowledgeRetrievalUrl": "http://dummy-host.xyz/knowledge-retrieval/bots"
    }
}
```
### Nodes API extension
* Path: `nodes/{nodeId}`
* Method: `GET`
* Response:
    * Status: 200
    * Content-Type: `application/json`
    * Body (entry of):
        * `id` [string]
        * `latestPredictionDateTime` [timestamp]
        * `predictedProperties` [collection of strings]
    * Sample
```json
{
  "entry": {
    "predictedProperties": [
      "cm:description"
    ],
    "id": "5fa74ad3-9b5b-461b-9df5-de407f1f4fe7",
    "latestPredictionDateTime": "2024-08-30T10:18:42.389+0000"
  }
}
```

### Predictions API

TBD
