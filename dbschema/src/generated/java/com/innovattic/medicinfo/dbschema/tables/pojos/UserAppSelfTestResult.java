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
public class UserAppSelfTestResult implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer id;
    private Integer labelId;
    private Integer customerId;
    private String  data;

    public UserAppSelfTestResult() {}

    public UserAppSelfTestResult(UserAppSelfTestResult value) {
        this.id = value.id;
        this.labelId = value.labelId;
        this.customerId = value.customerId;
        this.data = value.data;
    }

    public UserAppSelfTestResult(
        Integer id,
        Integer labelId,
        Integer customerId,
        String  data
    ) {
        this.id = id;
        this.labelId = labelId;
        this.customerId = customerId;
        this.data = data;
    }

    /**
     * Getter for <code>public.user_app_self_test_result.id</code>.
     */
    public Integer getId() {
        return this.id;
    }

    /**
     * Setter for <code>public.user_app_self_test_result.id</code>.
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * Getter for <code>public.user_app_self_test_result.label_id</code>.
     */
    public Integer getLabelId() {
        return this.labelId;
    }

    /**
     * Setter for <code>public.user_app_self_test_result.label_id</code>.
     */
    public void setLabelId(Integer labelId) {
        this.labelId = labelId;
    }

    /**
     * Getter for <code>public.user_app_self_test_result.customer_id</code>.
     */
    public Integer getCustomerId() {
        return this.customerId;
    }

    /**
     * Setter for <code>public.user_app_self_test_result.customer_id</code>.
     */
    public void setCustomerId(Integer customerId) {
        this.customerId = customerId;
    }

    /**
     * Getter for <code>public.user_app_self_test_result.data</code>.
     */
    public String getData() {
        return this.data;
    }

    /**
     * Setter for <code>public.user_app_self_test_result.data</code>.
     */
    public void setData(String data) {
        this.data = data;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("UserAppSelfTestResult (");

        sb.append(id);
        sb.append(", ").append(labelId);
        sb.append(", ").append(customerId);
        sb.append(", ").append(data);

        sb.append(")");
        return sb.toString();
    }
}