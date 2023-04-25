---
layout: default
title:  "MedicInfo API Documentation"
---

## Introduction

This document describes the MedicInfo API, which can be used to create medical real-time chat and video-call applications.
The API is developed and maintained by [Innovattic](https://www.innovattic.com).

A complete list of endpoints and the input/output structures can be viewed through Swagger. The Swagger endpoints
are protected using an IP whitelist. Contact Medicinfo to add your IP address.

## Version history

This is version 0.3 of the documentation, which was released on {{ site.time }}.

Release history:

- (Unreleased) Version 0.3: Add more documentation and example on using the websockets. Fix text for image upload endpoint. 
- Version 0.2: Fix Swagger links, add section on 'Online Huisarts', add example request for message+image upload
- Version 0.1: Initial document release

## Environments

There's currently three environments, with separate hostnames.
- Development: <https://api.app.dev-medicinfo.nl/api/> ([Swagger](https://api.app.dev-medicinfo.nl/api/swagger-ui.html)). This environment is meant for
  internal development only.
- Acceptance: <https://api.app.acc-medicinfo.nl/api/> ([Swagger](https://api.app.acc-medicinfo.nl/api/swagger-ui.html)). This environment can be used
  to test your apps.
- Production: <https://api.app.medicinfo.nl/api/> ([Swagger](https://api.app.medicinfo.nl/api/swagger-ui.html))

## General information

The API is available as a REST API using JSON objects.

When a user is created, a one-time password ("api key") is created for that user, which will need to be stored by the client. This API key
cannot be changed or reset.

The API key serves as a refresh token, which can be used to obtain a JWT token. This token servers as a temporary access token for
all other endpoints. Note that this access token has a relatively short expiration time.

For web applications, there is also a 'create session' endpoint which uses HTTP-only cookies to provide a secure way of
storing the access token in the client's browser.

The REST endpoints can be used to retrieve customer data, like previous chat conversations. For real-time chat operation,
there is a websockets endpoint, described in more detail later in this document.

The REST API is versioned using a path-prefix. Currently, all endpoints are presented under the `/v1/` prefix.

Some of the endpoints accept [FIQL queries](https://datatracker.ietf.org/doc/html/draft-nottingham-atompub-fiql-00) 
to filter the output. Swagger describes which on fields of the data model you can perform filtering.  

## Push notifications
If the label has push notifications enabled, when registering or updating the user using the user endpoints, you can attach a `deviceToken` to enable push notifications for a user. Please note that push notifications need to be setup first before a `deviceToken` can be attached, otherwise you will get an error.

## Authentication
##### Register a user
You can register a user with the `registerCustomer` endpoint ([Swagger](https://api.app.acc-medicinfo.nl/api/swagger-ui/index.html?configUrl=/api/api-docs/swagger-config#/user-endpoint/registerCustomer)).

This endpoint requires a custom `Authorization` header, not for security purposes,
but to prevent people from spamming us with new user accounts.  
Its value should be `Digest <hash>` where `<hash>` is `sha256Hex("medicinfo-customer-registration;$labelId;$displayName")`.  

For example, when creating a user with name `John Doe` for the label with id `b1ceb6ac-1eb1-4de3-81cf-f0a5fc323db6`,
 the Authorization header should be `Digest eb58cf191b49cbc39f460c2b9032424f8deb978248030cf7916e5e4fb5c29728`.

It is important to store the API key and user id combination from the response. This combination will be used further to authenticate.

##### Obtain a JWT for the user
The JWT is used to access authenticated endpoints for the user. ([Swagger](https://api.app.acc-medicinfo.nl/api/swagger-ui/index.html?configUrl=/api/api-docs/swagger-config#/authentication-endpoint/getToken))

## Online Huisarts
If the app makes use of the Online Huisarts platform, it's obligatory to send extra user data before an appointment with the Online Huisarts (online GP) is confirmed. Once a chat message is received with the action type `confirm_appointment`, you need to present a form to the user where they can fill in some additional data. Once this data is submitted through the [POST /user/customer/{id}/confirm-id-data](https://api.app.dev-medicinfo.nl/api/swagger-ui/index.html?configUrl=/api/api-docs/swagger-config#/user-endpoint/confirmIdData) endpoint, you get a new chat message (action type `video_chat_message`) with the video link for the video chat.

The user data that needs to be send in this endpoint is:

```json
{
    "firstName": "string",
    "lastName": "string",
    // or "id", "alien_id" (for vreemdelingendocument), "drivers_license"
    "idType": "passport",
    // Number of the document. Use "123456789" for testing
    "idNumber": "string",
    // BSN number. Use "123456789" for testing
    "bsn": "string",
    "birthDate": "2021-10-28T08:23:58.839Z"
}
```

## Conversations

##### Create a new conversation
You can start a new conversation by using the 'create conversation' endpoint ([Swagger](https://api.app.acc-medicinfo.nl/api/swagger-ui/index.html?configUrl=/api/api-docs/swagger-config#/conversation-endpoint/create_3))

The response contains a conversation id. This can be further used in other endpoints as seen on Swagger.

##### Receiving a message with image
Receiving a message object with an attachment needs extra handling to properly show the image.
You can receive a message object through the websockets, or the 'get conversation' endpoint ([Swagger](https://api.app.acc-medicinfo.nl/api/swagger-ui/index.html?configUrl=/api/api-docs/swagger-config#/conversation-endpoint/get))

The following json blob is an example of a message with an image:

```json
{
 	"userId": "ebf7cce7-3e86-eb11-b80d-001dd8b73d4a",
 	"userName": "TestCustomer",
 	"userRole": "customer",
 	"id": "6d1d8f61-5e7a-4ad7-be65-e38a8eff39ff",
 	"created": "2021-03-22T12:51:35Z",
 	"attachment": {
 		"url": "conversation/<uuid>/image/<uuid>",
 		"type": "image"
 	}
 }
```

As you can see the `attachment#url` does not contain a full hostname. You’ll need to prefix the url with the proper environment. For example:

```
https://api.app.acc-medicinfo.nl/api/v1/conversation/<uuid>/image/<uuid>
```

The endpoint for getting an image is described here: [Swagger](https://api.app.acc-medicinfo.nl/api/swagger-ui/index.html?configUrl=/api/api-docs/swagger-config#/conversation-endpoint/getMessageImage)

##### Send a message
You can send a message with the `POST /conversation/{id}/message` endpoint ([Swagger](https://api.app.acc-medicinfo.nl/api/swagger-ui/index.html?configUrl=/api/api-docs/swagger-config#/conversation-endpoint/createMessage)).

##### Send a message with attachment
If you want to send a message with an attachment, there is a separate endpoint for that, which uses a `multipart/form-data` encoding ([Swagger](https://api.app.acc-medicinfo.nl/api/swagger-ui/index.html?configUrl=/api/api-docs/swagger-config#/conversation-endpoint/createMessage_1)).

The content type should set to `multipart/form-data; boundary={boundary}`. The request body is a form-data encoded messages containing these fields (no json!):

- `attachment`: the image file (required)
- `message`: the user message (optional)

An example body (where `B2D6DB9C-EDF8-4D2F-97F0-DE46681C1E2F` is the boundary) would look like this:

```
--B2D6DB9C-EDF8-4D2F-97F0-DE46681C1E2F
Content-Disposition: form-data; name="attachment"; filename="<uuid>"
Content-Type: image/jpeg

{binary image data}
--B2D6DB9C-EDF8-4D2F-97F0-DE46681C1E2F
Content-Disposition: form-data; name="message"

{message text}
--B2D6DB9C-EDF8-4D2F-97F0-DE46681C1E2F--
```

Please note that the filename in the attachment part is mandatory and can be filled with a random string. The body can be created manually or you can use an external library like Alamofire (iOS) or OkHttp (Android).

#### Archived conversation
If a conversation is archived, you'll receive an error (400 Bad Request) when trying to send a message:
```json
{
  "errorCode": "conversation_expired"
}
```
This means the conversation has been archived by MedicInfo. It is important to create a new conversation when this happens.
You could also detect if a conversation's archived by getting the conversation and checking the `status` field.
A conversation will usually be archived automatically after 48 hours, or when the caretaker decides to archive it.

## Conversation Websockets
The websockets are built on top of the sub protocol [STOMP](https://stomp.github.io/stomp-specification-1.2.html).
Be sure to use a websocket client which supports STOMP, or you will not be able to connect with the websockets.

- For Android (Kotlin) there is [Krossbow](https://github.com/joffrey-bion/krossbow)
- For Web there is [StompJS](https://www.npmjs.com/package/@stomp/stompjs)

After establishing the websockets connection, the STOMP protocol will send a `CONNECT` frame, which needs
to specify a JWT token for the connecting user. Pay attention to the expiration date of the JWT token. 

An example using StompJS:

```typescript
const SHOW_DEBUG_MESSAGES = true; // only in debug builds

const stompConfig: StompConfig = {
    brokerURL: SOCKET_URL,
    connectHeaders: {
        Authorization: `Bearer ${getStoredAccessToken()}`,
    },
    logRawCommunication: SHOW_DEBUG_MESSAGES,
    debug: function (str: string) {
      SHOW_DEBUG_MESSAGES && console.debug('STOMP: ' + str);
    },
    onConnect: onConnect,
    onStompError: onStompError,
};

const stompClient = new Client(stompConfig);
stompClient.activate();

function onConnect() {
    isClientConnected = true;
    stompClient.subscribe('/v1/topic/heartbeat', () => {});
    stompClient.subscribe(
        `/v1/topic/conversation/${conversationId}`,
        it => { onMessage(it, onEvent); }
    );
}
```

The websocket connection supports several topics you can subscribe to.

## Heartbeat
You should subscribe to the heartbeat using `/v1/topic/heartbeat`, to ensure your connection stays alive.

## Online employees
Changes in the number of employees will be published to the `/v1/topic/online-employees` topic.
When subscribed, you will receive events like:
```json
{
   "online": 42
}
```
Events will only be published when the value has actually changed.

## Conversation
Topic: `/v1/topic/conversation/{id}`

If you want to follow multiple conversations, you should subscribe to the topic for multiple ids.

### Receiving events
You can receive events on the websocket connection. You will receive one of the following events listed below.
Each event will have its own 'key', for example the message event is under the key 'message'. The read event under 'read' etc. The current events are:

#### Message

Sent when a message has been added to a conversation

```json
{
   "message": {
      "userId": "0be8c544-9518-4bce-8519-0eee0356989f",
      "userName": "c1",
      "userRole": "customer",
      "id": "385444d4-e1aa-4edc-a082-17507c781238",
      "created": "2021-07-26T12:05:22Z",
      "message": "Hello"
   }
}
```

#### Read

Sent when a user marks a conversation as read

```json
{
   "read": {
      "userId": "3d43c23b-c637-4a7a-a780-28c2c29f1194",
      "readAt": "2021-07-26T12:06:34.713856Z"
   }
}
```

#### Received

Sent when a user marks a conversation as received
```json
{
   "received": {
      "userId": "0be8c544-9518-4bce-8519-0eee0356989f",
      "receivedAt": "2021-07-26T12:05:22.935376Z"
   }
}
```

#### StopTyping

Sent when a user stops typing

```json
{
   "stopTyping": {
      "userId": "0be8c544-9518-4bce-8519-0eee0356989f"
   }
}
```

When a user disconnects we do not send a `stopTyping` event, instead you should listen to the `userDisconnected` event as well for this edge case.

#### StartTyping

Sent when a user starts typing

```json
{
   "startTyping": {
      "userId": "0be8c544-9518-4bce-8519-0eee0356989f"
   }
}
```

#### UserDisconnected

Sent when a user disconnects from a conversation

```json
{
   "userDisconnected": {
      "userId": "0be8c544-9518-4bce-8519-0eee0356989f"
   }
}
```

#### UserConnected

Sent when a user connects to a conversation

```json
{
   "userConnected": {
      "userId": "0be8c544-9518-4bce-8519-0eee0356989f"
   }
}
```

### Sending events
You can send a start typing event by sending an empty message over the websocket to:
`/v1/app/conversation/6b92c178-c22b-4bbb-8dec-8d497660b5bf/typing/start`

You can send a stop typing event by sending an empty message over the websocket to:
`/v1/app/conversation/6b92c178-c22b-4bbb-8dec-8d497660b5bf/typing/stop`

# Examples
WebSocket Customer Flow:
1. Authentication using the API key to obtain a JWT access token.
2. You can connect with the websocket server, authentication has to be done in the headers with `Authorization: Bearer <jwt-access-token>`
   The connection url: `wss://api.app.acc-medicinfo.nl/api/v1/chat`
3. You can create a conversation using the HTTP `POST /v1/conversation`. You will receive an id back in the response.
4. Once the conversation is created, you can subscribe to the conversation using websockets: `/v1/topic/conversation/28ccd489-f588-4d35-99a9-1e74ec2475b2`
   You will receive multiple ConversationEvents. See above for examples.
5. You can send messages to the conversation using the `POST /v1/conversation/28ccd489-f588-4d35-99a9-1e74ec2475b2/message` endpoint
6. You can mark a conversation as read using `POST /v1/conversation/28ccd489-f588-4d35-99a9-1e74ec2475b2/read`
   You could do this after you've received a message via websocket on the client and the chat was displayed to the end-user; 
   or when the end-user opens the chat display.
7. You can mark a conversation as received using `POST /v1/conversation/28ccd489-f588-4d35-99a9-1e74ec2475b2/received`
   If you're connected to the websocket and subscribed to the conversation this is not necessary, it will automatically mark the conversation as received  
   If you GET the conversation, the conversation will also be automatically marked as received. The purpose
   of this signal is to be sent when your application receives a push notification, to indicate the receival of the message. 
8. If you reopen the app, you will need to get the latest state of a conversation , you can do so by using `GET /v1/conversation/28ccd489-f588-4d35-99a9-1e74ec2475b2/?order=created&query=created=le=2021-07-15T11:44:52Z;created=ge=2021-07-14T13:56:35Z`
   If you do have a latest message in cache, grab the date from it and use it in the `=ge=` query to get the newest messages
   If you do not have a latest message in cache, don't give query parameters to receive all messages

Customer registration flow:
1. You can register a user with the `registerCustomer` endpoint ([Swagger](https://api.app.acc-medicinfo.nl/api/swagger-ui/index.html?configUrl=/api/api-docs/swagger-config#/user-endpoint/registerCustomer)).
2. You can generate a JWT for the created user ([Swagger](https://api.app.acc-medicinfo.nl/api/swagger-ui/index.html?configUrl=/api/api-docs/swagger-config#/authentication-endpoint/getToken))
3. You can start a conversation ([Swagger](https://api.app.acc-medicinfo.nl/api/swagger-ui/index.html?configUrl=/api/api-docs/swagger-config#/conversation-endpoint/create_3))
4. You can send a message ([Swagger](https://api.app.acc-medicinfo.nl/api/swagger-ui/index.html?configUrl=/api/api-docs/swagger-config#/conversation-endpoint/createMessage))
5. You can submit more ID data if required ([Swagger](https://api.app.acc-medicinfo.nl/api/swagger-ui/index.html?configUrl=/api/api-docs/swagger-config#/user-endpoint/confirmIdData))

## Migration from Gezondheidslijn API

Before the release of this API, MedicInfo was providing similar service using the Gezondheidslijn API.
The old and new API's are not connected, so messages sent to the old API will not appear in the new API.

There is however a manual migration in place to copy all data from the old API to the new API. Contact MedicInfo for
details on this process.
