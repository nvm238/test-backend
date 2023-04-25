/*
 * This file is generated by jOOQ.
 */
package com.innovattic.medicinfo.dbschema.tables.records;


import com.innovattic.medicinfo.dbschema.tables.OdataCustomerView;

import java.time.LocalDateTime;

import javax.annotation.Generated;

import org.jooq.Field;
import org.jooq.Record17;
import org.jooq.Row17;
import org.jooq.impl.TableRecordImpl;


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
public class OdataCustomerViewRecord extends TableRecordImpl<OdataCustomerViewRecord> implements Record17<String, String, LocalDateTime, Integer, String, Boolean, String, LocalDateTime, String, String, String, String, String, String, String, String, String> {

    private static final long serialVersionUID = 1L;

    /**
     * Setter for <code>public.odata_customer_view.conversation_id</code>.
     */
    public void setConversationId(String value) {
        set(0, value);
    }

    /**
     * Getter for <code>public.odata_customer_view.conversation_id</code>.
     */
    public String getConversationId() {
        return (String) get(0);
    }

    /**
     * Setter for <code>public.odata_customer_view.customer_id</code>.
     */
    public void setCustomerId(String value) {
        set(1, value);
    }

    /**
     * Getter for <code>public.odata_customer_view.customer_id</code>.
     */
    public String getCustomerId() {
        return (String) get(1);
    }

    /**
     * Setter for <code>public.odata_customer_view.registered_at</code>.
     */
    public void setRegisteredAt(LocalDateTime value) {
        set(2, value);
    }

    /**
     * Getter for <code>public.odata_customer_view.registered_at</code>.
     */
    public LocalDateTime getRegisteredAt() {
        return (LocalDateTime) get(2);
    }

    /**
     * Setter for <code>public.odata_customer_view.age</code>.
     */
    public void setAge(Integer value) {
        set(3, value);
    }

    /**
     * Getter for <code>public.odata_customer_view.age</code>.
     */
    public Integer getAge() {
        return (Integer) get(3);
    }

    /**
     * Setter for <code>public.odata_customer_view.gender</code>.
     */
    public void setGender(String value) {
        set(4, value);
    }

    /**
     * Getter for <code>public.odata_customer_view.gender</code>.
     */
    public String getGender() {
        return (String) get(4);
    }

    /**
     * Setter for <code>public.odata_customer_view.is_insured</code>.
     */
    public void setIsInsured(Boolean value) {
        set(5, value);
    }

    /**
     * Getter for <code>public.odata_customer_view.is_insured</code>.
     */
    public Boolean getIsInsured() {
        return (Boolean) get(5);
    }

    /**
     * Setter for <code>public.odata_customer_view.privacy_version</code>.
     */
    public void setPrivacyVersion(String value) {
        set(6, value);
    }

    /**
     * Getter for <code>public.odata_customer_view.privacy_version</code>.
     */
    public String getPrivacyVersion() {
        return (String) get(6);
    }

    /**
     * Setter for <code>public.odata_customer_view.privacy_version_accepted_at</code>.
     */
    public void setPrivacyVersionAcceptedAt(LocalDateTime value) {
        set(7, value);
    }

    /**
     * Getter for <code>public.odata_customer_view.privacy_version_accepted_at</code>.
     */
    public LocalDateTime getPrivacyVersionAcceptedAt() {
        return (LocalDateTime) get(7);
    }

    /**
     * Setter for <code>public.odata_customer_view.label_code</code>.
     */
    public void setLabelCode(String value) {
        set(8, value);
    }

    /**
     * Getter for <code>public.odata_customer_view.label_code</code>.
     */
    public String getLabelCode() {
        return (String) get(8);
    }

    /**
     * Setter for <code>public.odata_customer_view.entry_type</code>.
     */
    public void setEntryType(String value) {
        set(9, value);
    }

    /**
     * Getter for <code>public.odata_customer_view.entry_type</code>.
     */
    public String getEntryType() {
        return (String) get(9);
    }

    /**
     * Setter for <code>public.odata_customer_view.general_practice</code>.
     */
    public void setGeneralPractice(String value) {
        set(10, value);
    }

    /**
     * Getter for <code>public.odata_customer_view.general_practice</code>.
     */
    public String getGeneralPractice() {
        return (String) get(10);
    }

    /**
     * Setter for <code>public.odata_customer_view.general_practice_agb_code</code>.
     */
    public void setGeneralPracticeAgbCode(String value) {
        set(11, value);
    }

    /**
     * Getter for <code>public.odata_customer_view.general_practice_agb_code</code>.
     */
    public String getGeneralPracticeAgbCode() {
        return (String) get(11);
    }

    /**
     * Setter for <code>public.odata_customer_view.general_practice_center</code>.
     */
    public void setGeneralPracticeCenter(String value) {
        set(12, value);
    }

    /**
     * Getter for <code>public.odata_customer_view.general_practice_center</code>.
     */
    public String getGeneralPracticeCenter() {
        return (String) get(12);
    }

    /**
     * Setter for <code>public.odata_customer_view.general_practice_center_agb_code</code>.
     */
    public void setGeneralPracticeCenterAgbCode(String value) {
        set(13, value);
    }

    /**
     * Getter for <code>public.odata_customer_view.general_practice_center_agb_code</code>.
     */
    public String getGeneralPracticeCenterAgbCode() {
        return (String) get(13);
    }

    /**
     * Setter for <code>public.odata_customer_view.holiday_destination</code>.
     */
    public void setHolidayDestination(String value) {
        set(14, value);
    }

    /**
     * Getter for <code>public.odata_customer_view.holiday_destination</code>.
     */
    public String getHolidayDestination() {
        return (String) get(14);
    }

    /**
     * Setter for <code>public.odata_customer_view.shelter_location_id</code>.
     */
    public void setShelterLocationId(String value) {
        set(15, value);
    }

    /**
     * Getter for <code>public.odata_customer_view.shelter_location_id</code>.
     */
    public String getShelterLocationId() {
        return (String) get(15);
    }

    /**
     * Setter for <code>public.odata_customer_view.shelter_location_name</code>.
     */
    public void setShelterLocationName(String value) {
        set(16, value);
    }

    /**
     * Getter for <code>public.odata_customer_view.shelter_location_name</code>.
     */
    public String getShelterLocationName() {
        return (String) get(16);
    }

    // -------------------------------------------------------------------------
    // Record17 type implementation
    // -------------------------------------------------------------------------

    @Override
    public Row17<String, String, LocalDateTime, Integer, String, Boolean, String, LocalDateTime, String, String, String, String, String, String, String, String, String> fieldsRow() {
        return (Row17) super.fieldsRow();
    }

    @Override
    public Row17<String, String, LocalDateTime, Integer, String, Boolean, String, LocalDateTime, String, String, String, String, String, String, String, String, String> valuesRow() {
        return (Row17) super.valuesRow();
    }

    @Override
    public Field<String> field1() {
        return OdataCustomerView.ODATA_CUSTOMER_VIEW.CONVERSATION_ID;
    }

    @Override
    public Field<String> field2() {
        return OdataCustomerView.ODATA_CUSTOMER_VIEW.CUSTOMER_ID;
    }

    @Override
    public Field<LocalDateTime> field3() {
        return OdataCustomerView.ODATA_CUSTOMER_VIEW.REGISTERED_AT;
    }

    @Override
    public Field<Integer> field4() {
        return OdataCustomerView.ODATA_CUSTOMER_VIEW.AGE;
    }

    @Override
    public Field<String> field5() {
        return OdataCustomerView.ODATA_CUSTOMER_VIEW.GENDER;
    }

    @Override
    public Field<Boolean> field6() {
        return OdataCustomerView.ODATA_CUSTOMER_VIEW.IS_INSURED;
    }

    @Override
    public Field<String> field7() {
        return OdataCustomerView.ODATA_CUSTOMER_VIEW.PRIVACY_VERSION;
    }

    @Override
    public Field<LocalDateTime> field8() {
        return OdataCustomerView.ODATA_CUSTOMER_VIEW.PRIVACY_VERSION_ACCEPTED_AT;
    }

    @Override
    public Field<String> field9() {
        return OdataCustomerView.ODATA_CUSTOMER_VIEW.LABEL_CODE;
    }

    @Override
    public Field<String> field10() {
        return OdataCustomerView.ODATA_CUSTOMER_VIEW.ENTRY_TYPE;
    }

    @Override
    public Field<String> field11() {
        return OdataCustomerView.ODATA_CUSTOMER_VIEW.GENERAL_PRACTICE;
    }

    @Override
    public Field<String> field12() {
        return OdataCustomerView.ODATA_CUSTOMER_VIEW.GENERAL_PRACTICE_AGB_CODE;
    }

    @Override
    public Field<String> field13() {
        return OdataCustomerView.ODATA_CUSTOMER_VIEW.GENERAL_PRACTICE_CENTER;
    }

    @Override
    public Field<String> field14() {
        return OdataCustomerView.ODATA_CUSTOMER_VIEW.GENERAL_PRACTICE_CENTER_AGB_CODE;
    }

    @Override
    public Field<String> field15() {
        return OdataCustomerView.ODATA_CUSTOMER_VIEW.HOLIDAY_DESTINATION;
    }

    @Override
    public Field<String> field16() {
        return OdataCustomerView.ODATA_CUSTOMER_VIEW.SHELTER_LOCATION_ID;
    }

    @Override
    public Field<String> field17() {
        return OdataCustomerView.ODATA_CUSTOMER_VIEW.SHELTER_LOCATION_NAME;
    }

    @Override
    public String component1() {
        return getConversationId();
    }

    @Override
    public String component2() {
        return getCustomerId();
    }

    @Override
    public LocalDateTime component3() {
        return getRegisteredAt();
    }

    @Override
    public Integer component4() {
        return getAge();
    }

    @Override
    public String component5() {
        return getGender();
    }

    @Override
    public Boolean component6() {
        return getIsInsured();
    }

    @Override
    public String component7() {
        return getPrivacyVersion();
    }

    @Override
    public LocalDateTime component8() {
        return getPrivacyVersionAcceptedAt();
    }

    @Override
    public String component9() {
        return getLabelCode();
    }

    @Override
    public String component10() {
        return getEntryType();
    }

    @Override
    public String component11() {
        return getGeneralPractice();
    }

    @Override
    public String component12() {
        return getGeneralPracticeAgbCode();
    }

    @Override
    public String component13() {
        return getGeneralPracticeCenter();
    }

    @Override
    public String component14() {
        return getGeneralPracticeCenterAgbCode();
    }

    @Override
    public String component15() {
        return getHolidayDestination();
    }

    @Override
    public String component16() {
        return getShelterLocationId();
    }

    @Override
    public String component17() {
        return getShelterLocationName();
    }

    @Override
    public String value1() {
        return getConversationId();
    }

    @Override
    public String value2() {
        return getCustomerId();
    }

    @Override
    public LocalDateTime value3() {
        return getRegisteredAt();
    }

    @Override
    public Integer value4() {
        return getAge();
    }

    @Override
    public String value5() {
        return getGender();
    }

    @Override
    public Boolean value6() {
        return getIsInsured();
    }

    @Override
    public String value7() {
        return getPrivacyVersion();
    }

    @Override
    public LocalDateTime value8() {
        return getPrivacyVersionAcceptedAt();
    }

    @Override
    public String value9() {
        return getLabelCode();
    }

    @Override
    public String value10() {
        return getEntryType();
    }

    @Override
    public String value11() {
        return getGeneralPractice();
    }

    @Override
    public String value12() {
        return getGeneralPracticeAgbCode();
    }

    @Override
    public String value13() {
        return getGeneralPracticeCenter();
    }

    @Override
    public String value14() {
        return getGeneralPracticeCenterAgbCode();
    }

    @Override
    public String value15() {
        return getHolidayDestination();
    }

    @Override
    public String value16() {
        return getShelterLocationId();
    }

    @Override
    public String value17() {
        return getShelterLocationName();
    }

    @Override
    public OdataCustomerViewRecord value1(String value) {
        setConversationId(value);
        return this;
    }

    @Override
    public OdataCustomerViewRecord value2(String value) {
        setCustomerId(value);
        return this;
    }

    @Override
    public OdataCustomerViewRecord value3(LocalDateTime value) {
        setRegisteredAt(value);
        return this;
    }

    @Override
    public OdataCustomerViewRecord value4(Integer value) {
        setAge(value);
        return this;
    }

    @Override
    public OdataCustomerViewRecord value5(String value) {
        setGender(value);
        return this;
    }

    @Override
    public OdataCustomerViewRecord value6(Boolean value) {
        setIsInsured(value);
        return this;
    }

    @Override
    public OdataCustomerViewRecord value7(String value) {
        setPrivacyVersion(value);
        return this;
    }

    @Override
    public OdataCustomerViewRecord value8(LocalDateTime value) {
        setPrivacyVersionAcceptedAt(value);
        return this;
    }

    @Override
    public OdataCustomerViewRecord value9(String value) {
        setLabelCode(value);
        return this;
    }

    @Override
    public OdataCustomerViewRecord value10(String value) {
        setEntryType(value);
        return this;
    }

    @Override
    public OdataCustomerViewRecord value11(String value) {
        setGeneralPractice(value);
        return this;
    }

    @Override
    public OdataCustomerViewRecord value12(String value) {
        setGeneralPracticeAgbCode(value);
        return this;
    }

    @Override
    public OdataCustomerViewRecord value13(String value) {
        setGeneralPracticeCenter(value);
        return this;
    }

    @Override
    public OdataCustomerViewRecord value14(String value) {
        setGeneralPracticeCenterAgbCode(value);
        return this;
    }

    @Override
    public OdataCustomerViewRecord value15(String value) {
        setHolidayDestination(value);
        return this;
    }

    @Override
    public OdataCustomerViewRecord value16(String value) {
        setShelterLocationId(value);
        return this;
    }

    @Override
    public OdataCustomerViewRecord value17(String value) {
        setShelterLocationName(value);
        return this;
    }

    @Override
    public OdataCustomerViewRecord values(String value1, String value2, LocalDateTime value3, Integer value4, String value5, Boolean value6, String value7, LocalDateTime value8, String value9, String value10, String value11, String value12, String value13, String value14, String value15, String value16, String value17) {
        value1(value1);
        value2(value2);
        value3(value3);
        value4(value4);
        value5(value5);
        value6(value6);
        value7(value7);
        value8(value8);
        value9(value9);
        value10(value10);
        value11(value11);
        value12(value12);
        value13(value13);
        value14(value14);
        value15(value15);
        value16(value16);
        value17(value17);
        return this;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Create a detached OdataCustomerViewRecord
     */
    public OdataCustomerViewRecord() {
        super(OdataCustomerView.ODATA_CUSTOMER_VIEW);
    }

    /**
     * Create a detached, initialised OdataCustomerViewRecord
     */
    public OdataCustomerViewRecord(String conversationId, String customerId, LocalDateTime registeredAt, Integer age, String gender, Boolean isInsured, String privacyVersion, LocalDateTime privacyVersionAcceptedAt, String labelCode, String entryType, String generalPractice, String generalPracticeAgbCode, String generalPracticeCenter, String generalPracticeCenterAgbCode, String holidayDestination, String shelterLocationId, String shelterLocationName) {
        super(OdataCustomerView.ODATA_CUSTOMER_VIEW);

        setConversationId(conversationId);
        setCustomerId(customerId);
        setRegisteredAt(registeredAt);
        setAge(age);
        setGender(gender);
        setIsInsured(isInsured);
        setPrivacyVersion(privacyVersion);
        setPrivacyVersionAcceptedAt(privacyVersionAcceptedAt);
        setLabelCode(labelCode);
        setEntryType(entryType);
        setGeneralPractice(generalPractice);
        setGeneralPracticeAgbCode(generalPracticeAgbCode);
        setGeneralPracticeCenter(generalPracticeCenter);
        setGeneralPracticeCenterAgbCode(generalPracticeCenterAgbCode);
        setHolidayDestination(holidayDestination);
        setShelterLocationId(shelterLocationId);
        setShelterLocationName(shelterLocationName);
    }
}