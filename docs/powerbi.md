
# Introduction

MedicInfo uses Microsoft PowerBI to perform various reporting tasks on the database.

Previously, we would use OData for this: this backend had an OData endpoint that mapped directly on Postgres views. But this caused major performance problems as PowerBI would try to load the *entire* dataset with 1 request.

We are now using a Microsoft on-premises data gateway. This is a Windows machine within AWS, which can connect directly to the RDS database using a dedicated (read-only) Postgres user. The Windows machine, in turn, connects upstream to PowerBI. This setup is the [recommended configuration](https://docs.aws.amazon.com/whitepapers/latest/using-power-bi-with-aws-cloud/connecting-the-microsoft-power-bi-service-to-aws-data-sources.html) by AWS.

An alternative setup, to be investigated in the future, would be to set up a site-to-site vpn between Medicinfo's Azure environment and the AWS environment, and try to eliminate the need for a data gateway instance.

## Installing the data gateway machine

- This is a Windows machine on AWS
- Due to some Windows bug, you need to disable 'IE security model' before installing the data gateway. See https://superfarb.com/annoying-ie-and-power-bi-issue/
- When data gateway is installed, it needs to be linked to a Microsoft (work) account. This account is set-up and configured by Medicinfo.
- Install `npgsql` for the data gateway to talk to RDS (Postgres). At time of writing, PowerBI says it supports up to version 4.0.10. Download from: https://github.com/npgsql/npgsql/releases/download/v4.0.10/Npgsql-4.0.10.msi
- On Route53, we've created a record `bi-gateway.app.medicinfo.nl` that points to the EC2 instance.
- We have tried a t2.small instance, but during heavy querying from medicinfo, it seemed to crumble under the memory usage/cpu usage. Upgraded to t3.medium per 19/8/2022. (It appears that the gateway is doing some computations as well; "Microsoft Mashup Evaluation Container" was consuming most resources).    

### Enable auto-update

1. Click the windows icon in the left bottom corner of the screen and start typing `edit group policy`
2. Under *Computer Configuration\Administrative Templates\Windows Components\Windows update\Configure Automatic Updates*
3. Click `Enabled` in radio box
4. Select one of the four options. Refer to https://docs.microsoft.com/en-us/windows/deployment/update/waas-wu-settings#configure-automatic-updates or to the `Help` pane on the right side of the opened window
5. For now, we chose option `4 - Auto download and schedule the install` and default setting which are `0 - Every day`, `03:00` and `Every week` respectively


## Setting up RDS

For now, this is a manual process. Connect to the DB using an account with super privileges (the default postgres account will do), and
create the user:

```
CREATE USER "powerbi-gateway";
```

To set a password:

```
ALTER USER "powerbi-gateway" WITH PASSWORD '<insert-secret-password>'
```

When our Spring application starts up, the Flyway migration called `afterMigrate__reporting_grants.sql` will make sure the correct set of GRANTS is applied to this user.


## Configuring PowerBI

- In PowerBI, disable ssl - this related to the encryption between the gateway and rds, which does not support encryption. Communication between PowerBI and the gateway is always encrypted.
- For database name, use 'postgres'
- Use the credentials created in the RDS section above
- Hostname for gateway: `bi-gateway.app.medicinfo.nl`

# Connecting to the Windows machine

Make sure you are connected to the Innovattic VPN.

We can connect via RDP. Refer to AWS documentation for a compatible client. For Linux, Remmina does the job.
You'll need to configure the RDP session on first access. After that, just choose the existing config, fetch the password
for Lastpass and you're connected.

To get a RDP connection file to load in your RDP client:

1. Open AWS console
2. Go to EC2
3. Select `Instances` on the left pane
4. Select `powerbi-gateway` from the list by clicking on instance ID
5. Click `Connect` in the right upper corner
6. Choose appropriate connection option. RDP is the only tested connection option for now.
7. To make RDP connection to the instance just select `RDP Client` tab and download remote desktop file. Credentials are provided
underneath the button. The password is stored in Lastpass, under Shared-Medicinfo-PowerBI.

If we ever lose the administrator password, we can retrieve it from AWS by providing a private key. This key is also stored in Lastpass. 

# Maintaining the Windows machine

### Check for unauthorized logins

Because the machine access is IP-restricted, we don't expect failed login attempts, except maybe sometimes a mistake of our own.
We should check regularly that there are (almost) no failed login attempts. 

1. Open up `Computer Management`
2. Under System Tools, open `Event Viewer` on the left pane
3. Go to `Windows Logs/Security`
4. Look for 'Audit failure' in the Keywords column. The event id of a failed login is 4625.

For reference only, to check succeeded RDP connectionos: 
1. Go to `Application and Services Logs/Microsoft/Windows/TerminalServices-RemoteConnectionManager/Operational`
2. There you can see different events, ones that hold information about successful login have `Event ID=1149`
3. For convenience, we created custom view `LoginSuccessfulLog` under `Event Viewer/Custom Views` that aggregates only events with id=1149

### Windows updates

Updates should be installed and applied automatically, but we should check periodically if this still works.

1. Open Settings
2. Go to Update & Security
3. There may be pending updates; this is normal.
4. Click 'View update history'
5. Verify that updates are installed recently.

### Update the gateway software

1. Via start menu, open "On-premises data gateway"
2. The app will display whether updates are available. If available, download and install.

### Server reboot

Windows is able to reboot itself. We tried to do that out of the start menu, and it came back up after 3 minutes. We assume that when Windows Update
applies the updates it will bring itself back up again after process is finished

Data gateway service for PowerBI is started automatically on Windows startup. To enable automatic startup if it was ever changed:
1. Open up `Services` (you can do it from start menu)
2. Find `On-premises data gateway service` on the list
3. Right-click then choose `Properties`
4. There in `Startup type` box you can choose suitable option. You can also check current service status below
