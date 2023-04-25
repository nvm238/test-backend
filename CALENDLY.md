# Calendly

## Configure an api key
First of all you need an admin account.
- In Calendly go to your account -> integrations (https://calendly.com/integrations)
- Pick the "API and connectors" category, pick "API and webhooks".
- Under "Personal access tokens", press "Get a new token". Name the token after the environment you're generating the key for, so for prod it would be prod, dev would be dev.
- Copy this key
- Store this value in the LastPass
- Go to AWS -> CloudWatch -> medicinfo-server-prod (dev if you need dev etc, choose the right env)
- Press configuration, then by Software press edit
- Fill the value in for key: CALENDLY_API_KEY. (Overwrite if it already exists, if it doesnt exist create it)

NOTE: if the password of a Calendly account is reset, *all api keys are removed*. So this will break our integrations
until a new api key is provisioned.

## Configure a caretaker
1. Invite the caretaker to Calendly
2. Let the caretaker properly configure their name and avatar. (Can be done here: https://calendly.com/account/settings/profile)
   The welcome message is not relevant here, it's not being used currently
3. Configure the event at https://calendly.com/event_types/user/me, the description here is relevant as it will be shown in the app
4. Also on the event you see a settings button, click it and press Add internal note. (Or edit internal note)
   In here you should put the label code for where the calendly should be active. For example ADC