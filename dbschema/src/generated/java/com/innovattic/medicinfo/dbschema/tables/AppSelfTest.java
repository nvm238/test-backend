/*
 * This file is generated by jOOQ.
 */
package com.innovattic.medicinfo.dbschema.tables;


import com.innovattic.medicinfo.database.converter.JSONBConverter;
import com.innovattic.medicinfo.dbschema.Public;
import com.innovattic.medicinfo.dbschema.tables.records.AppSelfTestRecord;

import java.util.Arrays;
import java.util.List;

import javax.annotation.Generated;

import org.jooq.Field;
import org.jooq.Identity;
import org.jooq.Name;
import org.jooq.Row3;
import org.jooq.Schema;
import org.jooq.Table;
import org.jooq.TableField;
import org.jooq.TableOptions;
import org.jooq.UniqueKey;
import org.jooq.impl.DSL;
import org.jooq.impl.Internal;
import org.jooq.impl.SQLDataType;
import org.jooq.impl.TableImpl;


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
public class AppSelfTest extends TableImpl<AppSelfTestRecord> {

    private static final long serialVersionUID = 1L;

    /**
     * The reference instance of <code>public.app_self_test</code>
     */
    public static final AppSelfTest APP_SELF_TEST = new AppSelfTest();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<AppSelfTestRecord> getRecordType() {
        return AppSelfTestRecord.class;
    }

    /**
     * The column <code>public.app_self_test.id</code>.
     */
    public final TableField<AppSelfTestRecord, Integer> ID = createField(DSL.name("id"), SQLDataType.INTEGER.nullable(false).identity(true), this, "");

    /**
     * The column <code>public.app_self_test.label_id</code>.
     */
    public final TableField<AppSelfTestRecord, Integer> LABEL_ID = createField(DSL.name("label_id"), SQLDataType.INTEGER.nullable(false), this, "");

    /**
     * The column <code>public.app_self_test.data</code>.
     */
    public final TableField<AppSelfTestRecord, String> DATA = createField(DSL.name("data"), SQLDataType.JSONB.nullable(false), this, "", new JSONBConverter());

    private AppSelfTest(Name alias, Table<AppSelfTestRecord> aliased) {
        this(alias, aliased, null);
    }

    private AppSelfTest(Name alias, Table<AppSelfTestRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment(""), TableOptions.table());
    }

    /**
     * Create an aliased <code>public.app_self_test</code> table reference
     */
    public AppSelfTest(String alias) {
        this(DSL.name(alias), APP_SELF_TEST);
    }

    /**
     * Create an aliased <code>public.app_self_test</code> table reference
     */
    public AppSelfTest(Name alias) {
        this(alias, APP_SELF_TEST);
    }

    /**
     * Create a <code>public.app_self_test</code> table reference
     */
    public AppSelfTest() {
        this(DSL.name("app_self_test"), null);
    }

    @Override
    public Schema getSchema() {
        return Public.PUBLIC;
    }

    @Override
    public Identity<AppSelfTestRecord, Integer> getIdentity() {
        return (Identity<AppSelfTestRecord, Integer>) super.getIdentity();
    }

    @Override
    public UniqueKey<AppSelfTestRecord> getPrimaryKey() {
        return Internal.createUniqueKey(AppSelfTest.APP_SELF_TEST, DSL.name("app_self_test_pkey"), new TableField[] { AppSelfTest.APP_SELF_TEST.ID }, true);
    }

    @Override
    public List<UniqueKey<AppSelfTestRecord>> getKeys() {
        return Arrays.<UniqueKey<AppSelfTestRecord>>asList(
              Internal.createUniqueKey(AppSelfTest.APP_SELF_TEST, DSL.name("app_self_test_pkey"), new TableField[] { AppSelfTest.APP_SELF_TEST.ID }, true)
        );
    }

    @Override
    public AppSelfTest as(String alias) {
        return new AppSelfTest(DSL.name(alias), this);
    }

    @Override
    public AppSelfTest as(Name alias) {
        return new AppSelfTest(alias, this);
    }

    /**
     * Rename this table
     */
    @Override
    public AppSelfTest rename(String name) {
        return new AppSelfTest(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public AppSelfTest rename(Name name) {
        return new AppSelfTest(name, null);
    }

    // -------------------------------------------------------------------------
    // Row3 type methods
    // -------------------------------------------------------------------------

    @Override
    public Row3<Integer, Integer, String> fieldsRow() {
        return (Row3) super.fieldsRow();
    }
}
