/*
 * This file is generated by jOOQ.
 */
package com.innovattic.medicinfo.dbschema;


import com.innovattic.medicinfo.dbschema.tables.ApiKey;
import com.innovattic.medicinfo.dbschema.tables.AppSelfTest;
import com.innovattic.medicinfo.dbschema.tables.CalendlyAppointment;
import com.innovattic.medicinfo.dbschema.tables.Conversation;
import com.innovattic.medicinfo.dbschema.tables.Label;
import com.innovattic.medicinfo.dbschema.tables.Message;
import com.innovattic.medicinfo.dbschema.tables.MessageAttachment;
import com.innovattic.medicinfo.dbschema.tables.MessageView;
import com.innovattic.medicinfo.dbschema.tables.OdataCustomerView;
import com.innovattic.medicinfo.dbschema.tables.OdataMessageView;
import com.innovattic.medicinfo.dbschema.tables.PrivacyStatement;
import com.innovattic.medicinfo.dbschema.tables.ReportingTriage;
import com.innovattic.medicinfo.dbschema.tables.ReportingTriageAnswer;
import com.innovattic.medicinfo.dbschema.tables.TriageAnswer;
import com.innovattic.medicinfo.dbschema.tables.TriageStatus;
import com.innovattic.medicinfo.dbschema.tables.User;
import com.innovattic.medicinfo.dbschema.tables.UserAppSelfTestResult;
import com.innovattic.medicinfo.dbschema.tables.UserView;

import java.util.Arrays;
import java.util.List;

import javax.annotation.Generated;

import org.jooq.Catalog;
import org.jooq.Table;
import org.jooq.impl.SchemaImpl;


/**
 * This class is generated by jOOQ.
 */
@Generated(
    value = {
        "https://www.jooq.org",
        "jOOQ version:3.14.16"
    },
    comments = "This class is generated by jOOQ"
)
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Public extends SchemaImpl {

    private static final long serialVersionUID = 1L;

    /**
     * The reference instance of <code>public</code>
     */
    public static final Public PUBLIC = new Public();

    /**
     * The table <code>public.api_key</code>.
     */
    public final ApiKey API_KEY = ApiKey.API_KEY;

    /**
     * The table <code>public.app_self_test</code>.
     */
    public final AppSelfTest APP_SELF_TEST = AppSelfTest.APP_SELF_TEST;

    /**
     * The table <code>public.calendly_appointment</code>.
     */
    public final CalendlyAppointment CALENDLY_APPOINTMENT = CalendlyAppointment.CALENDLY_APPOINTMENT;

    /**
     * The table <code>public.conversation</code>.
     */
    public final Conversation CONVERSATION = Conversation.CONVERSATION;

    /**
     * The table <code>public.label</code>.
     */
    public final Label LABEL = Label.LABEL;

    /**
     * The table <code>public.message</code>.
     */
    public final Message MESSAGE = Message.MESSAGE;

    /**
     * The table <code>public.message_attachment</code>.
     */
    public final MessageAttachment MESSAGE_ATTACHMENT = MessageAttachment.MESSAGE_ATTACHMENT;

    /**
     * The table <code>public.message_view</code>.
     */
    public final MessageView MESSAGE_VIEW = MessageView.MESSAGE_VIEW;

    /**
     * The table <code>public.odata_customer_view</code>.
     */
    public final OdataCustomerView ODATA_CUSTOMER_VIEW = OdataCustomerView.ODATA_CUSTOMER_VIEW;

    /**
     * The table <code>public.odata_message_view</code>.
     */
    public final OdataMessageView ODATA_MESSAGE_VIEW = OdataMessageView.ODATA_MESSAGE_VIEW;

    /**
     * The table <code>public.privacy_statement</code>.
     */
    public final PrivacyStatement PRIVACY_STATEMENT = PrivacyStatement.PRIVACY_STATEMENT;

    /**
     * The table <code>public.reporting_triage</code>.
     */
    public final ReportingTriage REPORTING_TRIAGE = ReportingTriage.REPORTING_TRIAGE;

    /**
     * The table <code>public.reporting_triage_answer</code>.
     */
    public final ReportingTriageAnswer REPORTING_TRIAGE_ANSWER = ReportingTriageAnswer.REPORTING_TRIAGE_ANSWER;

    /**
     * The table <code>public.triage_answer</code>.
     */
    public final TriageAnswer TRIAGE_ANSWER = TriageAnswer.TRIAGE_ANSWER;

    /**
     * The table <code>public.triage_status</code>.
     */
    public final TriageStatus TRIAGE_STATUS = TriageStatus.TRIAGE_STATUS;

    /**
     * The table <code>public.user</code>.
     */
    public final User USER = User.USER;

    /**
     * The table <code>public.user_app_self_test_result</code>.
     */
    public final UserAppSelfTestResult USER_APP_SELF_TEST_RESULT = UserAppSelfTestResult.USER_APP_SELF_TEST_RESULT;

    /**
     * The table <code>public.user_view</code>.
     */
    public final UserView USER_VIEW = UserView.USER_VIEW;

    /**
     * No further instances allowed
     */
    private Public() {
        super("public", null);
    }


    @Override
    public Catalog getCatalog() {
        return DefaultCatalog.DEFAULT_CATALOG;
    }

    @Override
    public final List<Table<?>> getTables() {
        return Arrays.<Table<?>>asList(
            ApiKey.API_KEY,
            AppSelfTest.APP_SELF_TEST,
            CalendlyAppointment.CALENDLY_APPOINTMENT,
            Conversation.CONVERSATION,
            Label.LABEL,
            Message.MESSAGE,
            MessageAttachment.MESSAGE_ATTACHMENT,
            MessageView.MESSAGE_VIEW,
            OdataCustomerView.ODATA_CUSTOMER_VIEW,
            OdataMessageView.ODATA_MESSAGE_VIEW,
            PrivacyStatement.PRIVACY_STATEMENT,
            ReportingTriage.REPORTING_TRIAGE,
            ReportingTriageAnswer.REPORTING_TRIAGE_ANSWER,
            TriageAnswer.TRIAGE_ANSWER,
            TriageStatus.TRIAGE_STATUS,
            User.USER,
            UserAppSelfTestResult.USER_APP_SELF_TEST_RESULT,
            UserView.USER_VIEW);
    }
}
