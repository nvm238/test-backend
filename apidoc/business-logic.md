# Triage
Triage is basically a questionnaire that allows Medicinfo employees to assess what is the condition of the patient and how urgently help is needed. Triage is only available to users that have their phone set to Dutch.

## How does the triage setup work
### What is the flow between Excel sheets, the parser, the backend?
**_Excel file_** is created by Medicinfo. It has specific structure that they need to follow, so it is cleaner to read it by parser. It contains all the data that defines the flow of the questionnaire, what actions to take, on what condition to show question, which questions to go to, what are the details of the question. We wanted to generify the code as much as possible and delegate all control to the Excel file.

We have 4 conditions to check if question can be asked:
- _**Adult/child/both**_ - this condition is not actively used as AGE condition is more granular
- _**Sex**_ - sex of the user. Possible values: `M`, `F`, `Beide`
- **_Age_** - age of the user. You can use operators like: `<`,`<=`,`=`,`=>`,`>` or just `Alle` to indicate no age condition
- **_Label_** - label of the user
When Excel file is parsed, and condition covers all cases(Alle, Beide), then condition will not end up in the JSON model

As for triage answer actions possible formats are:
- `<answer number>. <instruction>` ex. 1. Ga naar vraag KEELK9
- `<answer numbers>. <instruction>` ex. 1-5. Ga naar vraag KEELK9
- `<answer number>. <urgency>. <instruction>` ex. 4. U4, Ga naar vraag KEELK9
- `<answer numbers>. <urgency>. <instruction>` ex. 4-9. U4, Ga naar vraag KEELK9
 
Answers with no explicitly defined actions are considered 'go to next question'. Every action should be on separate line, ex.
```
2-5. Sla volgende vraag over. 
7. Sla volgende vraag over.
```
 
For detailed view what instructions are possible in the spreadsheet check out `question.ActionParser#parseActionString` in parser project.

Excel file contains sheet called `_medarea` which contains instructions to parse `C-MEDAREA` question. It has some additional features:
- in answer column it contains questionnaires ids ordered by number. Those numbers indicate the answer
- answer visibility column defines if answer should be show to user with certain sex. Number preceding condition refers to answer column
- synonyms of for the medical area that are useful for frontend. Number preceding condition refers to answer column

**_Parser_** is a project that reads the Excel file and generates the JSON model of the questionnaire. Here we enforce some constraints, like uniqueness of the question ID and translate the Excel data in the common concise format. Parser repo is here: [Medicinfo parser repo](https://gitlab.innovattic.com/medicinfo/medicinfo-triage-parser)

_**Backend**_ is a name for Spring webapp. JSON model from parser is added to the resources as a file <version_number>.json (ex. 68.json) and the loaded on application startup. On load backend translates the model to its own internal structure that makes it easy to use in the code. Model is kept in memory and all triage requests make use of the model. There can be multiple triage model versions loaded at once.
For a documentation on what is the effect of every action refer to `com.innovattic.medicinfo.logic.triage.model.ActionType` in this repository

### What steps are involved for updating the triage definitions
Whole flow of adding new triage model is:
1. parse the Excel file using parser
2. commit Excel file to triage Git repository with pattern `<version_number>.xlsx` (ex. 68.xlsx)
3. copy generated JSON model to backend resources with new version number. Pattern: `<version_number>.json` (ex. 68.json)
4. startup the server

### How does triage versioning work in the backend?
Triage JSON model is saved in backend project resources folder with pattern `<version_number>.json` (ex. 68.json). File name is parsed on startup and the number is taken as a version number. Version number of started triage is saved in the database. New triages are always started with the newest version loaded. 
When a triage is in progress, and we deploy application with new triage version, code will check for in progress triages and also load those versions, so previously started triages can finish uninterrupted with version that it was started on.

### How does the web demo fit in (even though it hasn't been updated in a while)?
Web demo is a react application written to visualize JSON questionnaire model. It renders a tree of questions. [Webdemo git repo](https://gitlab.innovattic.com/medicinfo/medicinfo-triage-webdemo)

### What's the role of the Ktor server in the triage-parser project?
Ktor server is used to expose an endpoint that allows to upload Excel files and responds with JSON model. Basically it is the web interface for parser. It is used by Medicinfo to check validity of the Excel file before sending it to Innovattic. It is automatically deployed to AWS by our gitlab CI when we update 'main' branch.

### What are the different states when a triage is finished / stopped, or when going to the next answer?
There are four states that are sent to frontend to indicate triage state.
- _**NEW**_ - triage that was started as a response to the start triage endpoint call
- _**IN_PROGRESS**_ - indicates that triage is already in progress. You can see it when endpoint to start triage is called for triage that was started in the past. If you call start triage endpoint twice with the same parameters, you will first get NEW state and on the second call you will see IN_PROGRESS
- _**FINISHED**_ - triage is already finished
- _**NOT_STARTED**_ - when triage is started outside of service hours

### How and when are answers sent to Salesforce?
Answers are sent with JSON through the endpoint exposed by Salesforce. On Salesforce side it will initiate the case. For when answers are sent to Salesforce, check out `com/innovattic/medicinfo/database/dto/TriageProgress.kt` there is a field `shouldBeSentToSalesforce` and comments that explain the meaning behind constants

### What is special about the PRO questions (and how are those answers used later on, especially PRO1)
PRO(Profile) questions define basic personal data that influence the flow of the questionnaire. In Excel file they are in the sheet called `PRO Gezondheidsprofiel`. Exception is `C-MEDAREA` question which is stored in `_medarea` sheet

- _**PRO0**_ - instruction/informative question. There is no real answer to that question, answer just allows to proceed further.
- _**PRO1**_ - answer to this question defines if triage is being done for registered user or on behalf of someone else. In case that `myself` answer is given, then data that we need to control the flow of the questionnaire will be taken from registered user properties. In cast `for someone else` is given, then we have to ask additional set of questions to determine authorization, age, gender etc.
- _**TRIAGEOTHER_AUTHORIZED**_ - (asked when PRO1 answered for someone else) - this question allows to determine if person that triage is filled for gave their consent to fill the triage on their behalf
- _**TRIAGEOTHER_RELATION**_ - (asked when PRO1 answered for someone else) - asked to determine a relation of person filling the questionnaire to the person that questionnaire is filled for
- _**TRIAGEOTHER_FIRSTNAME**_ - (asked when PRO1 answered for someone else) - first name of the person
- _**TRIAGEOTHER_LASTNAME**_ - (asked when PRO1 answered for someone else) - last name of the person
- _**TRIAGEOTHER_GENDER**_ - (asked when PRO1 answered for someone else) - gender of the person
- _**TRIAGEOTHER_BIRTHDATE**_ - (asked when PRO1 answered for someone else) - birthdate of the person
- _**TRIAGEOTHER_BSN**_ - (asked when PRO1 answered for someone else) - BSN(burgerservicenummer - personal identification number) of the person
- _**C-MEDAREA**_ - question that requires user to pick a 'pain' area. Every medical area is associated with separate questionnaire. There is also an option to pick `I don't know` answer. We save that user finished a triage on medical area, then user is redirected to chat with a nurse that can help user pick medical area by sending him a special chat message. That special message contains a button, which clicked calls an endpoint to start triage with medical area parameter. It allows user to continue filling the triage for medical area chosen by nurse. Nurse can also just continue chat without sending user back to triage.

### Is there anything interesting about the going back feature?
Endpoint that allows to go back and see answers already given takes current question as a parameter. Current refers to question that is currently visible on the users screen. Although action that user went back is not saved on the backend, it is purely a request to get a question before question given as parameter. If there is a wish to go back, system looks in the database for previous answers and sends it back to the client along with question definition. User then can go to the next question, if nothing was changed in previous question then next question is sent along with answer that was previously given. Although if user changed the previous answer we discard every answer given to questions that are further down in the tree and send the next question pointed by that answer.
User cannot go back to profile questions, except medical area question, although if medical area question was answered by nurse(user was sent back to triage from the chat) then going back to medical area is also not allowed.

### How do ‘skip’ and ‘I don’t know’ behave?
Question can be skipped, this behavior is defined in the Excel file when question is marked as not required. Possibility to choose 'I don't know' is also defined in the Excel. When question is skipped(or 'I don't know'), we pick the answer among its answers that points to the question with the highest position, because some questions can be asked as a follow-up to previous one, so we have to skip over that also. Although there is a case when we break that behavior: when question with the highest position has some conditions that do not apply for the user, then we take the question on the '+1' position and we check the user against the conditions, until we find question we can ask.
In order to make this behavior more robust and explicit we would have to add field to every question, that will define which question should we jump to in case that question is not applicable for the user.

### How do questions with uploading images work?
There is an endpoint that allow users to add image to the triage. It means that when image is uploaded we do not tie it specifically to a question, but to a triage. Endpoint responds with UUID of uploaded image, then frontend adds that UUID to the request to save answer. This way we tie images to question. When triage is finished, we gather all UUID's from the answers in database, and we remove all images with UUID's that are not in there.

### Why do some users have birthdates and some have age?
Some labels are not allowed to store user birthdate, instead they store age

## What are the various flows between apps/backend/salesforce/chat console (chat socket, endpoints)
_apps -> backend_  
There are multiple rest endpoints that allow apps to manipulate backend data. Additionally, apps use websockets(with STOMP) to receive messages from the backend. Note that messages are posted by invoking HTTP endpoint, they are only broadcast using websockets.

_backend -> salesforce_  
All operations that send data to salesforce are contained in `SalesforceService`. All calls to Salesforce are done asynchronously except `sendOnboardingData`, there are no retries, so if the call fails, there is no way to retry it. Some responses are cached for faster reuse. Be aware that we had cases were salesforce endpoint calls can take 15 minutes to return or not return at all, there is a timeout on the HTTP client to account for that.

_salesforce -> backend_  
When case in salesforce is being closed, endpoint is called to archive conversation on our side. Typically, conversation is archived automatically 48h after last message was received

_backend -> chatconsole_  
Chat console uses HTTP endpoints and websockets to send and receive messages. On chat console side there is an API connected that automatically translates messages in foreign language to Dutch, and messages in Dutch to other language. [Chatconsole git repo](https://gitlab.innovattic.com/medicinfo/medicinfo-chat-console-frontend)

_salesforce -> chatconsole_  
Chatconsole is embedded in Salesforece window, when JS application is initialized Salesforce framework sets a set of variables, like userId, conversationId, caseId, that are useful to determine which conversation we should send messages to.

## What is Eloqua, what does it do, how do we connect to it
Eloqua is an email automation tool that is a part of Marketing Automation by Oracle. We use it for labels that support a trial period. This is a simple API we connect to it using values defined in `.properties` files.

## What is Calendly, what does it do, how do we connect to it
Calendly is a scheduling platform that allow users to pick time of a meeting in a calendar. It is used in our system to allow nurses to make appointments with users for a video call. There are two type of appointments:
1. REGULAR - appointment that is made to assess and clarify condition of the user
2. INTAKE - appointment that is made to verify users identity, it is required by Dutch law
We connect to calendly using values defined in `.properties` files.
Backend also exposes an endpoint(`/v1/appoinment/callback`) that can be called by callendly(webhook) when there is a change to the appointment(rescheduled, cancelled etc.)

## What is a ‘self test’ and mirro, what is used for and how does it relate to triage (it doesn’t ;-))
Selftest is a questionnaire that was a simpler triage version. You get some questions to answer, and then you can get an advice or be redirected to mirro to get more advices. Mirro is an external provider of 'self-help' modules. It is basically a questionnaire that results in advice.
Mirro is integrated with our system in following way:
- Self test has a mirro module id(sometimes called blog id) included in `link` property aftert the last `/`. In case of the example below it is 42. Links can be different, but module id is always at the end
```
"mirro": {
          "link": "https://test.mirro/module/42",
          "title": "mirro-title",
          "longDescription": "mirro-long-description",
          "shortDescription": "mirro-description"
        }
```
- we parse that link on backend side and send it to apps as a response to `GET /v1/selftest`
- if user chooses to go to Mirro, apps send that module id as a path variable to `GET /v1/mirro/login/{blogId}`
- endpoint call returns a mirro module link with JWT token signed with mirro certificate appended
- if link is called by the app, user is automatically authenticated to mirro

Self test JSON is kept in git here: [Self test JSON repo](https://gitlab.innovattic.com/medicinfo/medicinfo-config-json)

## How does the chat socket work
Chat sockets are using STOMP protocol to communicate https://stomp.github.io/
Almost all operations are initiated using HTTP and then broadcast to connected clients using websockets. Exceptions are start typing event and stop typing event, which work only through web sockets.
For example to send a message call `POST /v1/conversation/{id}/message`. It will be broadcast to connected clients over websocket. Additionally, if app(iOS or Android) user is not connected via websocket we send notification.

## How do DB transactions work
Every call to HTTP endpoint spawns a transaction. We are using `TransactionFilter` class that is inside innolib project.

## Overview of DB tables, what are they used for
_**api_key**_ - contains api keys for every user in the app. Api keys are used to obtain JWT tokens
_**app_self_test**_ - contains per label configuration for self test. JSON that is in `data` column is stored in git(https://gitlab.innovattic.com/medicinfo/medicinfo-config-json) and it is manually inserted to db
_**calendly_appointment**_ - appointment data who with who and when, video meeting links, callback urls etc.
_**conversation**_ - table containing data about conversation(chat)
_**flyway_schema_history**_ - flyway is a database versioning tool. This table contains data about migrations, name of the file, checksums etc.
_**label**_ - holds data about label
_**message**_ - holds data about messages in a conversation
_**message_attachment**_ - data about media files attached to the message and their location
_**privacy_statement**_ - data about version of privacy statement that was accepted by the user. Made event log style to keep track of previous accepted versions
_**reporting_triage**_ - table that is used by PowerBI, and it contains information about triage and its state. Data here is inserted at the end of a triage
_**reporting_triage_answer**_ - table that is used by PowerBI, and it contains data about all the answers give to triage. Data here is inserted at the end of a triage
_**triage_answer**_ - table containing answers to triage questions
_**triage_status**_ - table containing information about triage, ex. user id, conversation id, status
_**user**_ - data about users in the app
_**user_app_self_test_result**_ - keeps an answers to self test questions as JSON

## How does authentication work?
When user is created we create an api key. That api key is used in `Authorization` header to obtain JWT token. You can prefix api key in the header with `Digest`, but it is not required. When JWT token is obtained it is used in `Authorization: Bearer <token>` header and allows a user to access endpoints.

There is a special `Authorization` header case when we want to register a customer using `/v1/user/register/customer`. The Authorization header has to consist of a Digestion of the label and name combination: `Digest + DigestUtils.sha256Hex("medicinfo-customer-registration;labelId;name")`

## What are the general practice endpoints? (general practice, general practice center and general practitioner)
Endpoints that return cached data about general practices, centers and practitioners. Data is cached for later reuse using Guava cache
