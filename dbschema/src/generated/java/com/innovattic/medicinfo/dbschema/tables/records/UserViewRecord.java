/*
 * This file is generated by jOOQ.
 */
package com.innovattic.medicinfo.dbschema.tables.records;


import com.innovattic.medicinfo.database.dto.Gender;
import com.innovattic.medicinfo.database.dto.UserRole;
import com.innovattic.medicinfo.dbschema.tables.UserView;

import java.time.LocalDateTime;
import java.util.UUID;

import javax.annotation.Generated;

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
public class UserViewRecord extends TableRecordImpl<UserViewRecord> {

    private static final long serialVersionUID = 1L;

    /**
     * Setter for <code>public.user_view.id</code>.
     */
    public void setId(Integer value) {
        set(0, value);
    }

    /**
     * Getter for <code>public.user_view.id</code>.
     */
    public Integer getId() {
        return (Integer) get(0);
    }

    /**
     * Setter for <code>public.user_view.public_id</code>.
     */
    public void setPublicId(UUID value) {
        set(1, value);
    }

    /**
     * Getter for <code>public.user_view.public_id</code>.
     */
    public UUID getPublicId() {
        return (UUID) get(1);
    }

    /**
     * Setter for <code>public.user_view.created</code>.
     */
    public void setCreated(LocalDateTime value) {
        set(2, value);
    }

    /**
     * Getter for <code>public.user_view.created</code>.
     */
    public LocalDateTime getCreated() {
        return (LocalDateTime) get(2);
    }

    /**
     * Setter for <code>public.user_view.label_id</code>.
     */
    public void setLabelId(Integer value) {
        set(3, value);
    }

    /**
     * Getter for <code>public.user_view.label_id</code>.
     */
    public Integer getLabelId() {
        return (Integer) get(3);
    }

    /**
     * Setter for <code>public.user_view.salesforce_id</code>.
     */
    public void setSalesforceId(String value) {
        set(4, value);
    }

    /**
     * Getter for <code>public.user_view.salesforce_id</code>.
     */
    public String getSalesforceId() {
        return (String) get(4);
    }

    /**
     * Setter for <code>public.user_view.role</code>.
     */
    public void setRole(UserRole value) {
        set(5, value);
    }

    /**
     * Getter for <code>public.user_view.role</code>.
     */
    public UserRole getRole() {
        return (UserRole) get(5);
    }

    /**
     * Setter for <code>public.user_view.name</code>.
     */
    public void setName(String value) {
        set(6, value);
    }

    /**
     * Getter for <code>public.user_view.name</code>.
     */
    public String getName() {
        return (String) get(6);
    }

    /**
     * Setter for <code>public.user_view.gender</code>.
     */
    public void setGender(Gender value) {
        set(7, value);
    }

    /**
     * Getter for <code>public.user_view.gender</code>.
     */
    public Gender getGender() {
        return (Gender) get(7);
    }

    /**
     * Setter for <code>public.user_view.age</code>.
     */
    public void setAge(Integer value) {
        set(8, value);
    }

    /**
     * Getter for <code>public.user_view.age</code>.
     */
    public Integer getAge() {
        return (Integer) get(8);
    }

    /**
     * Setter for <code>public.user_view.email</code>.
     */
    public void setEmail(String value) {
        set(9, value);
    }

    /**
     * Getter for <code>public.user_view.email</code>.
     */
    public String getEmail() {
        return (String) get(9);
    }

    /**
     * Setter for <code>public.user_view.is_insured</code>.
     */
    public void setIsInsured(Boolean value) {
        set(10, value);
    }

    /**
     * Getter for <code>public.user_view.is_insured</code>.
     */
    public Boolean getIsInsured() {
        return (Boolean) get(10);
    }

    /**
     * Setter for <code>public.user_view.device_token</code>.
     */
    public void setDeviceToken(String value) {
        set(11, value);
    }

    /**
     * Getter for <code>public.user_view.device_token</code>.
     */
    public String getDeviceToken() {
        return (String) get(11);
    }

    /**
     * Setter for <code>public.user_view.sns_endpoint_arn</code>.
     */
    public void setSnsEndpointArn(String value) {
        set(12, value);
    }

    /**
     * Getter for <code>public.user_view.sns_endpoint_arn</code>.
     */
    public String getSnsEndpointArn() {
        return (String) get(12);
    }

    /**
     * Setter for <code>public.user_view.privacy_version</code>.
     */
    public void setPrivacyVersion(String value) {
        set(13, value);
    }

    /**
     * Getter for <code>public.user_view.privacy_version</code>.
     */
    public String getPrivacyVersion() {
        return (String) get(13);
    }

    /**
     * Setter for <code>public.user_view.privacy_version_accepted_at</code>.
     */
    public void setPrivacyVersionAcceptedAt(LocalDateTime value) {
        set(14, value);
    }

    /**
     * Getter for <code>public.user_view.privacy_version_accepted_at</code>.
     */
    public LocalDateTime getPrivacyVersionAcceptedAt() {
        return (LocalDateTime) get(14);
    }

    /**
     * Setter for <code>public.user_view.birthdate</code>.
     */
    public void setBirthdate(LocalDateTime value) {
        set(15, value);
    }

    /**
     * Getter for <code>public.user_view.birthdate</code>.
     */
    public LocalDateTime getBirthdate() {
        return (LocalDateTime) get(15);
    }

    /**
     * Setter for <code>public.user_view.migrated_from</code>.
     */
    public void setMigratedFrom(String value) {
        set(16, value);
    }

    /**
     * Getter for <code>public.user_view.migrated_from</code>.
     */
    public String getMigratedFrom() {
        return (String) get(16);
    }

    /**
     * Setter for <code>public.user_view.label_public_id</code>.
     */
    public void setLabelPublicId(UUID value) {
        set(17, value);
    }

    /**
     * Getter for <code>public.user_view.label_public_id</code>.
     */
    public UUID getLabelPublicId() {
        return (UUID) get(17);
    }

    /**
     * Setter for <code>public.user_view.phone_number</code>.
     */
    public void setPhoneNumber(String value) {
        set(18, value);
    }

    /**
     * Getter for <code>public.user_view.phone_number</code>.
     */
    public String getPhoneNumber() {
        return (String) get(18);
    }

    /**
     * Setter for <code>public.user_view.postal_code</code>.
     */
    public void setPostalCode(String value) {
        set(19, value);
    }

    /**
     * Getter for <code>public.user_view.postal_code</code>.
     */
    public String getPostalCode() {
        return (String) get(19);
    }

    /**
     * Setter for <code>public.user_view.house_number</code>.
     */
    public void setHouseNumber(String value) {
        set(20, value);
    }

    /**
     * Getter for <code>public.user_view.house_number</code>.
     */
    public String getHouseNumber() {
        return (String) get(20);
    }

    /**
     * Setter for <code>public.user_view.entry_type</code>.
     */
    public void setEntryType(String value) {
        set(21, value);
    }

    /**
     * Getter for <code>public.user_view.entry_type</code>.
     */
    public String getEntryType() {
        return (String) get(21);
    }

    /**
     * Setter for <code>public.user_view.general_practice</code>.
     */
    public void setGeneralPractice(String value) {
        set(22, value);
    }

    /**
     * Getter for <code>public.user_view.general_practice</code>.
     */
    public String getGeneralPractice() {
        return (String) get(22);
    }

    /**
     * Setter for <code>public.user_view.general_practice_agb_code</code>.
     */
    public void setGeneralPracticeAgbCode(String value) {
        set(23, value);
    }

    /**
     * Getter for <code>public.user_view.general_practice_agb_code</code>.
     */
    public String getGeneralPracticeAgbCode() {
        return (String) get(23);
    }

    /**
     * Setter for <code>public.user_view.general_practice_center</code>.
     */
    public void setGeneralPracticeCenter(String value) {
        set(24, value);
    }

    /**
     * Getter for <code>public.user_view.general_practice_center</code>.
     */
    public String getGeneralPracticeCenter() {
        return (String) get(24);
    }

    /**
     * Setter for <code>public.user_view.general_practice_center_agb_code</code>.
     */
    public void setGeneralPracticeCenterAgbCode(String value) {
        set(25, value);
    }

    /**
     * Getter for <code>public.user_view.general_practice_center_agb_code</code>.
     */
    public String getGeneralPracticeCenterAgbCode() {
        return (String) get(25);
    }

    /**
     * Setter for <code>public.user_view.holiday_destination</code>.
     */
    public void setHolidayDestination(String value) {
        set(26, value);
    }

    /**
     * Getter for <code>public.user_view.holiday_destination</code>.
     */
    public String getHolidayDestination() {
        return (String) get(26);
    }

    /**
     * Setter for <code>public.user_view.shelter_location_id</code>.
     */
    public void setShelterLocationId(String value) {
        set(27, value);
    }

    /**
     * Getter for <code>public.user_view.shelter_location_id</code>.
     */
    public String getShelterLocationId() {
        return (String) get(27);
    }

    /**
     * Setter for <code>public.user_view.shelter_location_name</code>.
     */
    public void setShelterLocationName(String value) {
        set(28, value);
    }

    /**
     * Getter for <code>public.user_view.shelter_location_name</code>.
     */
    public String getShelterLocationName() {
        return (String) get(28);
    }

    /**
     * Setter for <code>public.user_view.onboarding_details_added</code>.
     */
    public void setOnboardingDetailsAdded(Boolean value) {
        set(29, value);
    }

    /**
     * Getter for <code>public.user_view.onboarding_details_added</code>.
     */
    public Boolean getOnboardingDetailsAdded() {
        return (Boolean) get(29);
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Create a detached UserViewRecord
     */
    public UserViewRecord() {
        super(UserView.USER_VIEW);
    }

    /**
     * Create a detached, initialised UserViewRecord
     */
    public UserViewRecord(Integer id, UUID publicId, LocalDateTime created, Integer labelId, String salesforceId, UserRole role, String name, Gender gender, Integer age, String email, Boolean isInsured, String deviceToken, String snsEndpointArn, String privacyVersion, LocalDateTime privacyVersionAcceptedAt, LocalDateTime birthdate, String migratedFrom, UUID labelPublicId, String phoneNumber, String postalCode, String houseNumber, String entryType, String generalPractice, String generalPracticeAgbCode, String generalPracticeCenter, String generalPracticeCenterAgbCode, String holidayDestination, String shelterLocationId, String shelterLocationName, Boolean onboardingDetailsAdded) {
        super(UserView.USER_VIEW);

        setId(id);
        setPublicId(publicId);
        setCreated(created);
        setLabelId(labelId);
        setSalesforceId(salesforceId);
        setRole(role);
        setName(name);
        setGender(gender);
        setAge(age);
        setEmail(email);
        setIsInsured(isInsured);
        setDeviceToken(deviceToken);
        setSnsEndpointArn(snsEndpointArn);
        setPrivacyVersion(privacyVersion);
        setPrivacyVersionAcceptedAt(privacyVersionAcceptedAt);
        setBirthdate(birthdate);
        setMigratedFrom(migratedFrom);
        setLabelPublicId(labelPublicId);
        setPhoneNumber(phoneNumber);
        setPostalCode(postalCode);
        setHouseNumber(houseNumber);
        setEntryType(entryType);
        setGeneralPractice(generalPractice);
        setGeneralPracticeAgbCode(generalPracticeAgbCode);
        setGeneralPracticeCenter(generalPracticeCenter);
        setGeneralPracticeCenterAgbCode(generalPracticeCenterAgbCode);
        setHolidayDestination(holidayDestination);
        setShelterLocationId(shelterLocationId);
        setShelterLocationName(shelterLocationName);
        setOnboardingDetailsAdded(onboardingDetailsAdded);
    }
}
