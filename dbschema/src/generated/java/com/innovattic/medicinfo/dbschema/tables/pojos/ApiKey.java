/*
 * This file is generated by jOOQ.
 */
package com.innovattic.medicinfo.dbschema.tables.pojos;


import java.io.Serializable;

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
public class ApiKey implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer id;
    private Integer userId;
    private String  apiKey;

    public ApiKey() {}

    public ApiKey(ApiKey value) {
        this.id = value.id;
        this.userId = value.userId;
        this.apiKey = value.apiKey;
    }

    public ApiKey(
        Integer id,
        Integer userId,
        String  apiKey
    ) {
        this.id = id;
        this.userId = userId;
        this.apiKey = apiKey;
    }

    /**
     * Getter for <code>public.api_key.id</code>.
     */
    public Integer getId() {
        return this.id;
    }

    /**
     * Setter for <code>public.api_key.id</code>.
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * Getter for <code>public.api_key.user_id</code>.
     */
    public Integer getUserId() {
        return this.userId;
    }

    /**
     * Setter for <code>public.api_key.user_id</code>.
     */
    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    /**
     * Getter for <code>public.api_key.api_key</code>.
     */
    public String getApiKey() {
        return this.apiKey;
    }

    /**
     * Setter for <code>public.api_key.api_key</code>.
     */
    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("ApiKey (");

        sb.append(id);
        sb.append(", ").append(userId);
        sb.append(", ").append(apiKey);

        sb.append(")");
        return sb.toString();
    }
}
