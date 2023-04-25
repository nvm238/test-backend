# Websockets
The websockets are built on top of the sub protocol [STOMP](https://stomp.github.io/stomp-specification-1.2.html).
Be sure to use a websocket client which supports STOMP, or you will not be able to connect with the websockets.
For Android (Kotlin)/web there is [Krossbow](https://github.com/joffrey-bion/krossbow)

The websocket supports several topics you can subscribe to.

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
You can receive events on the websocket connection. You will always receive one of N events.
Each event will have its own 'key', for example the message event is under the key 'message'. The read event under 'read' etc. The current events are:

#### Message
when a user has sent a message in the chat
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
when a user marks a conversation as read
```json
{
   "read": {
      "userId": "3d43c23b-c637-4a7a-a780-28c2c29f1194",
      "readAt": "2021-07-26T12:06:34.713856Z"
   }
}
```

#### Received
when a user marks a conversation as received
```json
  {
   "received": {
      "userId": "0be8c544-9518-4bce-8519-0eee0356989f",
      "receivedAt": "2021-07-26T12:05:22.935376Z"
   }
}
```

#### StopTyping
when a user stops typing
```json
  {
   "stopTyping": {
      "userId": "0be8c544-9518-4bce-8519-0eee0356989f"
   }
}
```
When a user disconnects we do not send a `stopTyping` event, instead you should listen to the `userDisconnected` event as well for this edge case.

#### StartTyping
when a user starts typing
```json
  {
   "startTyping": {
      "userId": "0be8c544-9518-4bce-8519-0eee0356989f"
   }
}
```

#### UserDisconnected
when a user disconnects from a conversation
```json
  {
   "userDisconnected": {
      "userId": "0be8c544-9518-4bce-8519-0eee0356989f"
   }
}
```

#### UserConnected
when a user connects to a conversation
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
1. You can connect with the websocket server, authentication has to be done in the headers with `Authorization: Bearer <jwt>`
   The connection url: `wss://api.app.dev-medicinfo.nl/api/v1/chat`
2. You can create a conversation using the HTTP `POST /v1/conversation`. You will receive an id back in the response.
3. Once the conversation is created, you can subscribe to the conversation using websockets: `/v1/topic/conversation/28ccd489-f588-4d35-99a9-1e74ec2475b2`
   You will receive multiple ConversationEvents. See General for the events and examples
4. You can send messages to the conversation using the `POST /v1/conversation/28ccd489-f588-4d35-99a9-1e74ec2475b2/message` endpoint
5. You can mark a conversation as read using `POST /v1/conversation/28ccd489-f588-4d35-99a9-1e74ec2475b2/read`
   You could do this after you've received a message via websocket on the client and you had the chat open
   You could do this after opening the chat
6. You can mark a conversation as received using `POST /v1/conversation/28ccd489-f588-4d35-99a9-1e74ec2475b2/received`
   If you're connected to the websocket and subscribed to the conversation this is not necessary, it will automatically mark the conversation as received  
   If you GET the conversation, the conversation will also be automatically marked as received
   This is relevant for when you receive a push notification that you have a new message, we should then call this endpoint to mark it as received
7. If you reopen the app, you will need to get the latest state of a conversation , you can do so by using `GET /v1/conversation/28ccd489-f588-4d35-99a9-1e74ec2475b2/?order=created&query=created=le=2021-07-15T11:44:52Z;created=ge=2021-07-14T13:56:35Z`
   If you do have a latest message in cache, grab the date from it and use it in the =ge= query to get the newest messages
   If you do not have a latest message in cache, don't give query parameters to receive all messages

WebSocket Employee Flow:
1. You can connect with the websocket server, authentication has to be done with cookies.
   The connection url: `wss://api.app.dev-medicinfo.nl/api/v1/chat`
2. You can subscribe to the conversation using websockets: `/v1/topic/conversation/28ccd489-f588-4d35-99a9-1e74ec2475b2`
   You will receive multiple ConversationEvents. See General for the events and examples
3. You can send messages to the conversation using the `POST /v1/conversation/28ccd489-f588-4d35-99a9-1e74ec2475b2/message` endpoint
4. You can mark a conversation as receive manually, but I assume it won't be necessary. Look at Customer #6 to see the implementation use case there.
5. You can mark a conversation as read using `POST /v1/conversation/28ccd489-f588-4d35-99a9-1e74ec2475b2/read`
   You could do this after you've received a message via websocket on the client and you had the chat open
   You could do this after opening the chat
6. You can fetch messages, in case you were not connected to the websocket for a conversation and get the latest conversation state using: `GET /v1/conversation/28ccd489-f588-4d35-99a9-1e74ec2475b2/?order=created&query=created=le=2021-07-15T11:44:52Z;created=ge=2021-07-14T13:56:35Z`
