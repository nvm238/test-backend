/*
 * This file is generated by jOOQ.
 */
package com.innovattic.medicinfo.dbschema.tables.pojos;


import java.io.Serializable;
import java.time.LocalDateTime;

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
public class PrivacyStatement implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer       id;
    private LocalDateTime acceptedAt;
    private Integer       userId;
    private String        version;

    public PrivacyStatement() {}

    public PrivacyStatement(PrivacyStatement value) {
        this.id = value.id;
        this.acceptedAt = value.acceptedAt;
        this.userId = value.userId;
        this.version = value.version;
    }

    public PrivacyStatement(
        Integer       id,
        LocalDateTime acceptedAt,
        Integer       userId,
        String        version
    ) {
        this.id = id;
        this.acceptedAt = acceptedAt;
        this.userId = userId;
        this.version = version;
    }

    /**
     * Getter for <code>public.privacy_statement.id</code>.
     */
    public Integer getId() {
        return this.id;
    }

    /**
     * Setter for <code>public.privacy_statement.id</code>.
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * Getter for <code>public.privacy_statement.accepted_at</code>.
     */
    public LocalDateTime getAcceptedAt() {
        return this.acceptedAt;
    }

    /**
     * Setter for <code>public.privacy_statement.accepted_at</code>.
     */
    public void setAcceptedAt(LocalDateTime acceptedAt) {
        this.acceptedAt = acceptedAt;
    }

    /**
     * Getter for <code>public.privacy_statement.user_id</code>.
     */
    public Integer getUserId() {
        return this.userId;
    }

    /**
     * Setter for <code>public.privacy_statement.user_id</code>.
     */
    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    /**
     * Getter for <code>public.privacy_statement.version</code>.
     */
    public String getVersion() {
        return this.version;
    }

    /**
     * Setter for <code>public.privacy_statement.version</code>.
     */
    public void setVersion(String version) {
        this.version = version;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("PrivacyStatement (");

        sb.append(id);
        sb.append(", ").append(acceptedAt);
        sb.append(", ").append(userId);
        sb.append(", ").append(version);

        sb.append(")");
        return sb.toString();
    }
}
