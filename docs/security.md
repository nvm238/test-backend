This document is a checklist for security-related aspects of server-side software.

# System administration

- Where is the VM/server hosted?

Amazon AWS

- On what OS does the server run?

Elasticbeans (Amazon Linux 2)

- Who is responsible for OS/package updates? What is the update policy?

Automated by Elasticbeans.

- Who is responsible for backups? What is the backup policy?

RDS has automatic backups - see cloudformation template for details.

- Is the data on the machine encrypted? How?

The RDS database is encrypted by AWS.

- Are backups encrypted?

The backups are encrypted by AWS.

- How is the system accessible?

Via AWS tooling (AWS console, command line tools).

- Where is the access matrix for the server?

The Innovattic-wide AWS access matrix.

- Is the server protected with some kind of two-factor authentication?

Yes, AWS is accessed using Innovattic's Google credentials, which are forced to have 2FA. 

# Software development

- How is the software deployed on the machine?

Via AWS tooling, see hosting docs.

- How is the availability of the service configured?

One instance. Elasticbeans can be configured to scale up when necessary.

- How does the software scale?

Horizontally, all state is kept in the RDS database.

- How is the software accessible for end-users?

Using API keys mostly, but specifics are described in apidocs folder.

- How is the software accessible for admin users?

There is no admin panel at the moment.

Admin-like access is provided through Salesforce. The security of this needs to be checked:
https://innovattic.atlassian.net/browse/MED-1095

- Does the software log important user events?

Login attempts are logged.

- Does the software log important admin events?

Login attempts (successful and failed) are logged with timestamps.

For personal information, keep track of who views/modifies which data - this should be checked and improved on: 
https://innovattic.atlassian.net/browse/MED-1096

- How long are the log files retained?

The log files are retained for 1 month (Cloudwatch default).

# Health-care specifics

- If the server returns medical personal information, is the user's identity contained in the response?

The conversations endpoint return customer details.

- If the client software asks for consent, is the user's consent archived on the server?

No consent involved.

- If the server contains publicly available health-care information, how do we check it's validity and does the software reference the source of information?

N/A

- Does the software inform the user of confidentiality when accessing personal health information?

N/A, the frontend application should take care of that.

- Does the software mark any 'paper materials' as being confidential?

N/A, no exports that are meant for human viewing (pdfs), just an OData API.
