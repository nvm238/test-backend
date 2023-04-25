# Salesforce link
Many interactions with this backend will cause information passed on to the MedicInfo Logic Apps, 
 which are their connection to Salesforce.

These endpoints are documented [here](https://medicinfo-mw-ont-api.developer.azure-api.net/api-details#api=medicinfo-mw-la-ont-process-chat-message).
 For details on the implementation, see [RealSalesforceService](../businesslogic/src/main/kotlin/com/innovattic/medicinfo/logic/salesforce/RealSalesforceService.kt).

## Add message
If a `customer` type user adds a message:
* If the message has text, we call `process-chat-message`
* If the message has an attached image, we call `add-attachment-to-case` (NYI, see MED-448)

If an `employee` type user adds a message, we call `update-case-status`.

## Message action types with action context
Messages created using `POST conversation/{id}/message` or retrieved using `GET conversation/{id}` have a type and 
can contain a context. Below the types along with their context is listed:

* confirm_appointment:
  * buttonText: String
* video_chat_message:
  * buttonText: String
  * url: String
  * startDateTime: String
  * appointmentId: String? *(When **receiving** messages from the server, this indicates the external identifier of the
    linked appointment. This id is not usable by customer apps; use the appointmentPublicId instead. 
    When **creating** a message, this property should be set with the linked appointment (employee-only).)*
  * appointmentPublicId: String? *(When receiving messages from the server, this property indicates the id of the
    linked appointment. Do not set this field when creating a message.)*
* create_appointment:
  * buttonText: String
* open_face_talk
* confirm_id_data
* survey
* triage:
  * buttonText: String
  * complaintAreaTriage: String

## ID data
When a customer submits their ID data (`POST user/customer/$id/confirm-id-data`),
 we don't save it in our backend at all, but only pass it on to `receive-id-data`.

## User profile
If a `customer` type user is updated (but not when created!), we call `add-profile`.

## Selftest
If a customer submits the final selftest results (so on `POST selftest/problem-area`),
 we call `add-selftest`, with a combination of the `POST selftest` and `POST selftest/problem-area` data.
 (NYI, but almost. See MED-200)
