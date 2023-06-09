/*
 * This file is generated by jOOQ.
 */
package com.innovattic.medicinfo.dbschema.tables;


import com.innovattic.medicinfo.dbschema.Public;
import com.innovattic.medicinfo.dbschema.tables.records.ReportingTriageAnswerRecord;

import java.util.Arrays;
import java.util.List;

import javax.annotation.Generated;

import org.jooq.Field;
import org.jooq.Identity;
import org.jooq.Name;
import org.jooq.Row7;
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
public class ReportingTriageAnswer extends TableImpl<ReportingTriageAnswerRecord> {

    private static final long serialVersionUID = 1L;

    /**
     * The reference instance of <code>public.reporting_triage_answer</code>
     */
    public static final ReportingTriageAnswer REPORTING_TRIAGE_ANSWER = new ReportingTriageAnswer();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<ReportingTriageAnswerRecord> getRecordType() {
        return ReportingTriageAnswerRecord.class;
    }

    /**
     * The column <code>public.reporting_triage_answer.id</code>.
     */
    public final TableField<ReportingTriageAnswerRecord, Integer> ID = createField(DSL.name("id"), SQLDataType.INTEGER.nullable(false).identity(true), this, "");

    /**
     * The column <code>public.reporting_triage_answer.triage_id</code>.
     */
    public final TableField<ReportingTriageAnswerRecord, Integer> TRIAGE_ID = createField(DSL.name("triage_id"), SQLDataType.INTEGER.nullable(false), this, "");

    /**
     * The column <code>public.reporting_triage_answer.question_id</code>.
     */
    public final TableField<ReportingTriageAnswerRecord, String> QUESTION_ID = createField(DSL.name("question_id"), SQLDataType.VARCHAR.nullable(false), this, "");

    /**
     * The column <code>public.reporting_triage_answer.question_text</code>.
     */
    public final TableField<ReportingTriageAnswerRecord, String> QUESTION_TEXT = createField(DSL.name("question_text"), SQLDataType.VARCHAR.nullable(false), this, "");

    /**
     * The column <code>public.reporting_triage_answer.answer_text</code>.
     */
    public final TableField<ReportingTriageAnswerRecord, String> ANSWER_TEXT = createField(DSL.name("answer_text"), SQLDataType.VARCHAR.nullable(false), this, "");

    /**
     * The column <code>public.reporting_triage_answer.divergent</code>.
     */
    public final TableField<ReportingTriageAnswerRecord, Boolean> DIVERGENT = createField(DSL.name("divergent"), SQLDataType.BOOLEAN.nullable(false), this, "");

    /**
     * The column <code>public.reporting_triage_answer.possible_answers</code>.
     */
    public final TableField<ReportingTriageAnswerRecord, String> POSSIBLE_ANSWERS = createField(DSL.name("possible_answers"), SQLDataType.CLOB, this, "");

    private ReportingTriageAnswer(Name alias, Table<ReportingTriageAnswerRecord> aliased) {
        this(alias, aliased, null);
    }

    private ReportingTriageAnswer(Name alias, Table<ReportingTriageAnswerRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment(""), TableOptions.table());
    }

    /**
     * Create an aliased <code>public.reporting_triage_answer</code> table reference
     */
    public ReportingTriageAnswer(String alias) {
        this(DSL.name(alias), REPORTING_TRIAGE_ANSWER);
    }

    /**
     * Create an aliased <code>public.reporting_triage_answer</code> table reference
     */
    public ReportingTriageAnswer(Name alias) {
        this(alias, REPORTING_TRIAGE_ANSWER);
    }

    /**
     * Create a <code>public.reporting_triage_answer</code> table reference
     */
    public ReportingTriageAnswer() {
        this(DSL.name("reporting_triage_answer"), null);
    }

    @Override
    public Schema getSchema() {
        return Public.PUBLIC;
    }

    @Override
    public Identity<ReportingTriageAnswerRecord, Integer> getIdentity() {
        return (Identity<ReportingTriageAnswerRecord, Integer>) super.getIdentity();
    }

    @Override
    public UniqueKey<ReportingTriageAnswerRecord> getPrimaryKey() {
        return Internal.createUniqueKey(ReportingTriageAnswer.REPORTING_TRIAGE_ANSWER, DSL.name("reporting_triage_answer_pkey"), new TableField[] { ReportingTriageAnswer.REPORTING_TRIAGE_ANSWER.ID }, true);
    }

    @Override
    public List<UniqueKey<ReportingTriageAnswerRecord>> getKeys() {
        return Arrays.<UniqueKey<ReportingTriageAnswerRecord>>asList(
              Internal.createUniqueKey(ReportingTriageAnswer.REPORTING_TRIAGE_ANSWER, DSL.name("reporting_triage_answer_pkey"), new TableField[] { ReportingTriageAnswer.REPORTING_TRIAGE_ANSWER.ID }, true)
        );
    }

    @Override
    public ReportingTriageAnswer as(String alias) {
        return new ReportingTriageAnswer(DSL.name(alias), this);
    }

    @Override
    public ReportingTriageAnswer as(Name alias) {
        return new ReportingTriageAnswer(alias, this);
    }

    /**
     * Rename this table
     */
    @Override
    public ReportingTriageAnswer rename(String name) {
        return new ReportingTriageAnswer(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public ReportingTriageAnswer rename(Name name) {
        return new ReportingTriageAnswer(name, null);
    }

    // -------------------------------------------------------------------------
    // Row7 type methods
    // -------------------------------------------------------------------------

    @Override
    public Row7<Integer, Integer, String, String, String, Boolean, String> fieldsRow() {
        return (Row7) super.fieldsRow();
    }
}
