/*
 * This file is generated by jOOQ.
 */
package com.innovattic.medicinfo.dbschema.tables.records;


import com.innovattic.medicinfo.database.dto.TriageProgress;
import com.innovattic.medicinfo.dbschema.tables.TriageStatus;

import java.time.LocalDateTime;

import javax.annotation.Generated;

import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.Record9;
import org.jooq.Row9;
import org.jooq.impl.UpdatableRecordImpl;


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
public class TriageStatusRecord extends UpdatableRecordImpl<TriageStatusRecord> implements Record9<Integer, LocalDateTime, LocalDateTime, TriageProgress, Integer, Integer, Boolean, Integer, String> {

    private static final long serialVersionUID = 1L;

    /**
     * Setter for <code>public.triage_status.id</code>.
     */
    public void setId(Integer value) {
        set(0, value);
    }

    /**
     * Getter for <code>public.triage_status.id</code>.
     */
    public Integer getId() {
        return (Integer) get(0);
    }

    /**
     * Setter for <code>public.triage_status.created</code>.
     */
    public void setCreated(LocalDateTime value) {
        set(1, value);
    }

    /**
     * Getter for <code>public.triage_status.created</code>.
     */
    public LocalDateTime getCreated() {
        return (LocalDateTime) get(1);
    }

    /**
     * Setter for <code>public.triage_status.ended</code>.
     */
    public void setEnded(LocalDateTime value) {
        set(2, value);
    }

    /**
     * Getter for <code>public.triage_status.ended</code>.
     */
    public LocalDateTime getEnded() {
        return (LocalDateTime) get(2);
    }

    /**
     * Setter for <code>public.triage_status.status</code>.
     */
    public void setStatus(TriageProgress value) {
        set(3, value);
    }

    /**
     * Getter for <code>public.triage_status.status</code>.
     */
    public TriageProgress getStatus() {
        return (TriageProgress) get(3);
    }

    /**
     * Setter for <code>public.triage_status.user_id</code>.
     */
    public void setUserId(Integer value) {
        set(4, value);
    }

    /**
     * Getter for <code>public.triage_status.user_id</code>.
     */
    public Integer getUserId() {
        return (Integer) get(4);
    }

    /**
     * Setter for <code>public.triage_status.conversation_id</code>.
     */
    public void setConversationId(Integer value) {
        set(5, value);
    }

    /**
     * Getter for <code>public.triage_status.conversation_id</code>.
     */
    public Integer getConversationId() {
        return (Integer) get(5);
    }

    /**
     * Setter for <code>public.triage_status.active</code>.
     */
    public void setActive(Boolean value) {
        set(6, value);
    }

    /**
     * Getter for <code>public.triage_status.active</code>.
     */
    public Boolean getActive() {
        return (Boolean) get(6);
    }

    /**
     * Setter for <code>public.triage_status.schema_version</code>.
     */
    public void setSchemaVersion(Integer value) {
        set(7, value);
    }

    /**
     * Getter for <code>public.triage_status.schema_version</code>.
     */
    public Integer getSchemaVersion() {
        return (Integer) get(7);
    }

    /**
     * Setter for <code>public.triage_status.stop_reason</code>.
     */
    public void setStopReason(String value) {
        set(8, value);
    }

    /**
     * Getter for <code>public.triage_status.stop_reason</code>.
     */
    public String getStopReason() {
        return (String) get(8);
    }

    // -------------------------------------------------------------------------
    // Primary key information
    // -------------------------------------------------------------------------

    @Override
    public Record1<Integer> key() {
        return (Record1) super.key();
    }

    // -------------------------------------------------------------------------
    // Record9 type implementation
    // -------------------------------------------------------------------------

    @Override
    public Row9<Integer, LocalDateTime, LocalDateTime, TriageProgress, Integer, Integer, Boolean, Integer, String> fieldsRow() {
        return (Row9) super.fieldsRow();
    }

    @Override
    public Row9<Integer, LocalDateTime, LocalDateTime, TriageProgress, Integer, Integer, Boolean, Integer, String> valuesRow() {
        return (Row9) super.valuesRow();
    }

    @Override
    public Field<Integer> field1() {
        return TriageStatus.TRIAGE_STATUS.ID;
    }

    @Override
    public Field<LocalDateTime> field2() {
        return TriageStatus.TRIAGE_STATUS.CREATED;
    }

    @Override
    public Field<LocalDateTime> field3() {
        return TriageStatus.TRIAGE_STATUS.ENDED;
    }

    @Override
    public Field<TriageProgress> field4() {
        return TriageStatus.TRIAGE_STATUS.STATUS;
    }

    @Override
    public Field<Integer> field5() {
        return TriageStatus.TRIAGE_STATUS.USER_ID;
    }

    @Override
    public Field<Integer> field6() {
        return TriageStatus.TRIAGE_STATUS.CONVERSATION_ID;
    }

    @Override
    public Field<Boolean> field7() {
        return TriageStatus.TRIAGE_STATUS.ACTIVE;
    }

    @Override
    public Field<Integer> field8() {
        return TriageStatus.TRIAGE_STATUS.SCHEMA_VERSION;
    }

    @Override
    public Field<String> field9() {
        return TriageStatus.TRIAGE_STATUS.STOP_REASON;
    }

    @Override
    public Integer component1() {
        return getId();
    }

    @Override
    public LocalDateTime component2() {
        return getCreated();
    }

    @Override
    public LocalDateTime component3() {
        return getEnded();
    }

    @Override
    public TriageProgress component4() {
        return getStatus();
    }

    @Override
    public Integer component5() {
        return getUserId();
    }

    @Override
    public Integer component6() {
        return getConversationId();
    }

    @Override
    public Boolean component7() {
        return getActive();
    }

    @Override
    public Integer component8() {
        return getSchemaVersion();
    }

    @Override
    public String component9() {
        return getStopReason();
    }

    @Override
    public Integer value1() {
        return getId();
    }

    @Override
    public LocalDateTime value2() {
        return getCreated();
    }

    @Override
    public LocalDateTime value3() {
        return getEnded();
    }

    @Override
    public TriageProgress value4() {
        return getStatus();
    }

    @Override
    public Integer value5() {
        return getUserId();
    }

    @Override
    public Integer value6() {
        return getConversationId();
    }

    @Override
    public Boolean value7() {
        return getActive();
    }

    @Override
    public Integer value8() {
        return getSchemaVersion();
    }

    @Override
    public String value9() {
        return getStopReason();
    }

    @Override
    public TriageStatusRecord value1(Integer value) {
        setId(value);
        return this;
    }

    @Override
    public TriageStatusRecord value2(LocalDateTime value) {
        setCreated(value);
        return this;
    }

    @Override
    public TriageStatusRecord value3(LocalDateTime value) {
        setEnded(value);
        return this;
    }

    @Override
    public TriageStatusRecord value4(TriageProgress value) {
        setStatus(value);
        return this;
    }

    @Override
    public TriageStatusRecord value5(Integer value) {
        setUserId(value);
        return this;
    }

    @Override
    public TriageStatusRecord value6(Integer value) {
        setConversationId(value);
        return this;
    }

    @Override
    public TriageStatusRecord value7(Boolean value) {
        setActive(value);
        return this;
    }

    @Override
    public TriageStatusRecord value8(Integer value) {
        setSchemaVersion(value);
        return this;
    }

    @Override
    public TriageStatusRecord value9(String value) {
        setStopReason(value);
        return this;
    }

    @Override
    public TriageStatusRecord values(Integer value1, LocalDateTime value2, LocalDateTime value3, TriageProgress value4, Integer value5, Integer value6, Boolean value7, Integer value8, String value9) {
        value1(value1);
        value2(value2);
        value3(value3);
        value4(value4);
        value5(value5);
        value6(value6);
        value7(value7);
        value8(value8);
        value9(value9);
        return this;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Create a detached TriageStatusRecord
     */
    public TriageStatusRecord() {
        super(TriageStatus.TRIAGE_STATUS);
    }

    /**
     * Create a detached, initialised TriageStatusRecord
     */
    public TriageStatusRecord(Integer id, LocalDateTime created, LocalDateTime ended, TriageProgress status, Integer userId, Integer conversationId, Boolean active, Integer schemaVersion, String stopReason) {
        super(TriageStatus.TRIAGE_STATUS);

        setId(id);
        setCreated(created);
        setEnded(ended);
        setStatus(status);
        setUserId(userId);
        setConversationId(conversationId);
        setActive(active);
        setSchemaVersion(schemaVersion);
        setStopReason(stopReason);
    }
}
