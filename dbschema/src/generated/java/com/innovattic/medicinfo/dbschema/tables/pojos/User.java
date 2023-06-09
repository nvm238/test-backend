/*
 * This file is generated by jOOQ.
 */
package com.innovattic.medicinfo.dbschema.tables.pojos;


import com.innovattic.medicinfo.database.dto.Gender;
import com.innovattic.medicinfo.database.dto.UserRole;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

import javax.annotation.Generated;


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
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer       id;
    private UUID          publicId;
    private LocalDateTime created;
    private Integer       labelId;
    private String        salesforceId;
    private UserRole      role;
    private String        name;
    private Gender        gender;
    private Integer       age;
    private String        email;
    private Boolean       isInsured;
    private String        deviceToken;
    private String        snsEndpointArn;
    private String        privacyVersion;
    private LocalDateTime privacyVersionAcceptedAt;
    private LocalDateTime birthdate;
    private String        migratedFrom;
    private String        phoneNumber;
    private String        postalCode;
    private String        houseNumber;
    private String        entryType;
    private String        generalPractice;
    private String        generalPracticeAgbCode;
    private String        generalPracticeCenter;
    private String        generalPracticeCenterAgbCode;
    private String        holidayDestination;
    private Boolean       onboardingDetailsAdded;
    private String        shelterLocationId;
    private String        shelterLocationName;

    public User() {}

    public User(User value) {
        this.id = value.id;
        this.publicId = value.publicId;
        this.created = value.created;
        this.labelId = value.labelId;
        this.salesforceId = value.salesforceId;
        this.role = value.role;
        this.name = value.name;
        this.gender = value.gender;
        this.age = value.age;
        this.email = value.email;
        this.isInsured = value.isInsured;
        this.deviceToken = value.deviceToken;
        this.snsEndpointArn = value.snsEndpointArn;
        this.privacyVersion = value.privacyVersion;
        this.privacyVersionAcceptedAt = value.privacyVersionAcceptedAt;
        this.birthdate = value.birthdate;
        this.migratedFrom = value.migratedFrom;
        this.phoneNumber = value.phoneNumber;
        this.postalCode = value.postalCode;
        this.houseNumber = value.houseNumber;
        this.entryType = value.entryType;
        this.generalPractice = value.generalPractice;
        this.generalPracticeAgbCode = value.generalPracticeAgbCode;
        this.generalPracticeCenter = value.generalPracticeCenter;
        this.generalPracticeCenterAgbCode = value.generalPracticeCenterAgbCode;
        this.holidayDestination = value.holidayDestination;
        this.onboardingDetailsAdded = value.onboardingDetailsAdded;
        this.shelterLocationId = value.shelterLocationId;
        this.shelterLocationName = value.shelterLocationName;
    }

    public User(
        Integer       id,
        UUID          publicId,
        LocalDateTime created,
        Integer       labelId,
        String        salesforceId,
        UserRole      role,
        String        name,
        Gender        gender,
        Integer       age,
        String        email,
        Boolean       isInsured,
        String        deviceToken,
        String        snsEndpointArn,
        String        privacyVersion,
        LocalDateTime privacyVersionAcceptedAt,
        LocalDateTime birthdate,
        String        migratedFrom,
        String        phoneNumber,
        String        postalCode,
        String        houseNumber,
        String        entryType,
        String        generalPractice,
        String        generalPracticeAgbCode,
        String        generalPracticeCenter,
        String        generalPracticeCenterAgbCode,
        String        holidayDestination,
        Boolean       onboardingDetailsAdded,
        String        shelterLocationId,
        String        shelterLocationName
    ) {
        this.id = id;
        this.publicId = publicId;
        this.created = created;
        this.labelId = labelId;
        this.salesforceId = salesforceId;
        this.role = role;
        this.name = name;
        this.gender = gender;
        this.age = age;
        this.email = email;
        this.isInsured = isInsured;
        this.deviceToken = deviceToken;
        this.snsEndpointArn = snsEndpointArn;
        this.privacyVersion = privacyVersion;
        this.privacyVersionAcceptedAt = privacyVersionAcceptedAt;
        this.birthdate = birthdate;
        this.migratedFrom = migratedFrom;
        this.phoneNumber = phoneNumber;
        this.postalCode = postalCode;
        this.houseNumber = houseNumber;
        this.entryType = entryType;
        this.generalPractice = generalPractice;
        this.generalPracticeAgbCode = generalPracticeAgbCode;
        this.generalPracticeCenter = generalPracticeCenter;
        this.generalPracticeCenterAgbCode = generalPracticeCenterAgbCode;
        this.holidayDestination = holidayDestination;
        this.onboardingDetailsAdded = onboardingDetailsAdded;
        this.shelterLocationId = shelterLocationId;
        this.shelterLocationName = shelterLocationName;
    }

    /**
     * Getter for <code>public.user.id</code>.
     */
    public Integer getId() {
        return this.id;
    }

    /**
     * Setter for <code>public.user.id</code>.
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * Getter for <code>public.user.public_id</code>.
     */
    public UUID getPublicId() {
        return this.publicId;
    }

    /**
     * Setter for <code>public.user.public_id</code>.
     */
    public void setPublicId(UUID publicId) {
        this.publicId = publicId;
    }

    /**
     * Getter for <code>public.user.created</code>.
     */
    public LocalDateTime getCreated() {
        return this.created;
    }

    /**
     * Setter for <code>public.user.created</code>.
     */
    public void setCreated(LocalDateTime created) {
        this.created = created;
    }

    /**
     * Getter for <code>public.user.label_id</code>.
     */
    public Integer getLabelId() {
        return this.labelId;
    }

    /**
     * Setter for <code>public.user.label_id</code>.
     */
    public void setLabelId(Integer labelId) {
        this.labelId = labelId;
    }

    /**
     * Getter for <code>public.user.salesforce_id</code>.
     */
    public String getSalesforceId() {
        return this.salesforceId;
    }

    /**
     * Setter for <code>public.user.salesforce_id</code>.
     */
    public void setSalesforceId(String salesforceId) {
        this.salesforceId = salesforceId;
    }

    /**
     * Getter for <code>public.user.role</code>.
     */
    public UserRole getRole() {
        return this.role;
    }

    /**
     * Setter for <code>public.user.role</code>.
     */
    public void setRole(UserRole role) {
        this.role = role;
    }

    /**
     * Getter for <code>public.user.name</code>.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Setter for <code>public.user.name</code>.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Getter for <code>public.user.gender</code>.
     */
    public Gender getGender() {
        return this.gender;
    }

    /**
     * Setter for <code>public.user.gender</code>.
     */
    public void setGender(Gender gender) {
        this.gender = gender;
    }

    /**
     * Getter for <code>public.user.age</code>.
     */
    public Integer getAge() {
        return this.age;
    }

    /**
     * Setter for <code>public.user.age</code>.
     */
    public void setAge(Integer age) {
        this.age = age;
    }

    /**
     * Getter for <code>public.user.email</code>.
     */
    public String getEmail() {
        return this.email;
    }

    /**
     * Setter for <code>public.user.email</code>.
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Getter for <code>public.user.is_insured</code>.
     */
    public Boolean getIsInsured() {
        return this.isInsured;
    }

    /**
     * Setter for <code>public.user.is_insured</code>.
     */
    public void setIsInsured(Boolean isInsured) {
        this.isInsured = isInsured;
    }

    /**
     * Getter for <code>public.user.device_token</code>.
     */
    public String getDeviceToken() {
        return this.deviceToken;
    }

    /**
     * Setter for <code>public.user.device_token</code>.
     */
    public void setDeviceToken(String deviceToken) {
        this.deviceToken = deviceToken;
    }

    /**
     * Getter for <code>public.user.sns_endpoint_arn</code>.
     */
    public String getSnsEndpointArn() {
        return this.snsEndpointArn;
    }

    /**
     * Setter for <code>public.user.sns_endpoint_arn</code>.
     */
    public void setSnsEndpointArn(String snsEndpointArn) {
        this.snsEndpointArn = snsEndpointArn;
    }

    /**
     * Getter for <code>public.user.privacy_version</code>.
     */
    public String getPrivacyVersion() {
        return this.privacyVersion;
    }

    /**
     * Setter for <code>public.user.privacy_version</code>.
     */
    public void setPrivacyVersion(String privacyVersion) {
        this.privacyVersion = privacyVersion;
    }

    /**
     * Getter for <code>public.user.privacy_version_accepted_at</code>.
     */
    public LocalDateTime getPrivacyVersionAcceptedAt() {
        return this.privacyVersionAcceptedAt;
    }

    /**
     * Setter for <code>public.user.privacy_version_accepted_at</code>.
     */
    public void setPrivacyVersionAcceptedAt(LocalDateTime privacyVersionAcceptedAt) {
        this.privacyVersionAcceptedAt = privacyVersionAcceptedAt;
    }

    /**
     * Getter for <code>public.user.birthdate</code>.
     */
    public LocalDateTime getBirthdate() {
        return this.birthdate;
    }

    /**
     * Setter for <code>public.user.birthdate</code>.
     */
    public void setBirthdate(LocalDateTime birthdate) {
        this.birthdate = birthdate;
    }

    /**
     * Getter for <code>public.user.migrated_from</code>.
     */
    public String getMigratedFrom() {
        return this.migratedFrom;
    }

    /**
     * Setter for <code>public.user.migrated_from</code>.
     */
    public void setMigratedFrom(String migratedFrom) {
        this.migratedFrom = migratedFrom;
    }

    /**
     * Getter for <code>public.user.phone_number</code>.
     */
    public String getPhoneNumber() {
        return this.phoneNumber;
    }

    /**
     * Setter for <code>public.user.phone_number</code>.
     */
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    /**
     * Getter for <code>public.user.postal_code</code>.
     */
    public String getPostalCode() {
        return this.postalCode;
    }

    /**
     * Setter for <code>public.user.postal_code</code>.
     */
    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    /**
     * Getter for <code>public.user.house_number</code>.
     */
    public String getHouseNumber() {
        return this.houseNumber;
    }

    /**
     * Setter for <code>public.user.house_number</code>.
     */
    public void setHouseNumber(String houseNumber) {
        this.houseNumber = houseNumber;
    }

    /**
     * Getter for <code>public.user.entry_type</code>.
     */
    public String getEntryType() {
        return this.entryType;
    }

    /**
     * Setter for <code>public.user.entry_type</code>.
     */
    public void setEntryType(String entryType) {
        this.entryType = entryType;
    }

    /**
     * Getter for <code>public.user.general_practice</code>.
     */
    public String getGeneralPractice() {
        return this.generalPractice;
    }

    /**
     * Setter for <code>public.user.general_practice</code>.
     */
    public void setGeneralPractice(String generalPractice) {
        this.generalPractice = generalPractice;
    }

    /**
     * Getter for <code>public.user.general_practice_agb_code</code>.
     */
    public String getGeneralPracticeAgbCode() {
        return this.generalPracticeAgbCode;
    }

    /**
     * Setter for <code>public.user.general_practice_agb_code</code>.
     */
    public void setGeneralPracticeAgbCode(String generalPracticeAgbCode) {
        this.generalPracticeAgbCode = generalPracticeAgbCode;
    }

    /**
     * Getter for <code>public.user.general_practice_center</code>.
     */
    public String getGeneralPracticeCenter() {
        return this.generalPracticeCenter;
    }

    /**
     * Setter for <code>public.user.general_practice_center</code>.
     */
    public void setGeneralPracticeCenter(String generalPracticeCenter) {
        this.generalPracticeCenter = generalPracticeCenter;
    }

    /**
     * Getter for <code>public.user.general_practice_center_agb_code</code>.
     */
    public String getGeneralPracticeCenterAgbCode() {
        return this.generalPracticeCenterAgbCode;
    }

    /**
     * Setter for <code>public.user.general_practice_center_agb_code</code>.
     */
    public void setGeneralPracticeCenterAgbCode(String generalPracticeCenterAgbCode) {
        this.generalPracticeCenterAgbCode = generalPracticeCenterAgbCode;
    }

    /**
     * Getter for <code>public.user.holiday_destination</code>.
     */
    public String getHolidayDestination() {
        return this.holidayDestination;
    }

    /**
     * Setter for <code>public.user.holiday_destination</code>.
     */
    public void setHolidayDestination(String holidayDestination) {
        this.holidayDestination = holidayDestination;
    }

    /**
     * Getter for <code>public.user.onboarding_details_added</code>.
     */
    public Boolean getOnboardingDetailsAdded() {
        return this.onboardingDetailsAdded;
    }

    /**
     * Setter for <code>public.user.onboarding_details_added</code>.
     */
    public void setOnboardingDetailsAdded(Boolean onboardingDetailsAdded) {
        this.onboardingDetailsAdded = onboardingDetailsAdded;
    }

    /**
     * Getter for <code>public.user.shelter_location_id</code>.
     */
    public String getShelterLocationId() {
        return this.shelterLocationId;
    }

    /**
     * Setter for <code>public.user.shelter_location_id</code>.
     */
    public void setShelterLocationId(String shelterLocationId) {
        this.shelterLocationId = shelterLocationId;
    }

    /**
     * Getter for <code>public.user.shelter_location_name</code>.
     */
    public String getShelterLocationName() {
        return this.shelterLocationName;
    }

    /**
     * Setter for <code>public.user.shelter_location_name</code>.
     */
    public void setShelterLocationName(String shelterLocationName) {
        this.shelterLocationName = shelterLocationName;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("User (");

        sb.append(id);
        sb.append(", ").append(publicId);
        sb.append(", ").append(created);
        sb.append(", ").append(labelId);
        sb.append(", ").append(salesforceId);
        sb.append(", ").append(role);
        sb.append(", ").append(name);
        sb.append(", ").append(gender);
        sb.append(", ").append(age);
        sb.append(", ").append(email);
        sb.append(", ").append(isInsured);
        sb.append(", ").append(deviceToken);
        sb.append(", ").append(snsEndpointArn);
        sb.append(", ").append(privacyVersion);
        sb.append(", ").append(privacyVersionAcceptedAt);
        sb.append(", ").append(birthdate);
        sb.append(", ").append(migratedFrom);
        sb.append(", ").append(phoneNumber);
        sb.append(", ").append(postalCode);
        sb.append(", ").append(houseNumber);
        sb.append(", ").append(entryType);
        sb.append(", ").append(generalPractice);
        sb.append(", ").append(generalPracticeAgbCode);
        sb.append(", ").append(generalPracticeCenter);
        sb.append(", ").append(generalPracticeCenterAgbCode);
        sb.append(", ").append(holidayDestination);
        sb.append(", ").append(onboardingDetailsAdded);
        sb.append(", ").append(shelterLocationId);
        sb.append(", ").append(shelterLocationName);

        sb.append(")");
        return sb.toString();
    }
}
