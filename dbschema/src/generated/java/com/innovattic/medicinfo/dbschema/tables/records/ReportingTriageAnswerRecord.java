/*
 * This file is generated by jOOQ.
 */
package com.innovattic.medicinfo.dbschema.tables.records;


import com.innovattic.medicinfo.dbschema.tables.ReportingTriageAnswer;

import javax.annotation.Generated;

import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.Record7;
import org.jooq.Row7;
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
public class ReportingTriageAnswerRecord extends UpdatableRecordImpl<ReportingTriageAnswerRecord> implements Record7<Integer, Integer, String, String, String, Boolean, String> {

    private static final long serialVersionUID = 1L;

    /**
     * Setter for <code>public.reporting_triage_answer.id</code>.
     */
    public void setId(Integer value) {
        set(0, value);
    }

    /**
     * Getter for <code>public.reporting_triage_answer.id</code>.
     */
    public Integer getId() {
        return (Integer) get(0);
    }

    /**
     * Setter for <code>public.reporting_triage_answer.triage_id</code>.
     */
    public void setTriageId(Integer value) {
        set(1, value);
    }

    /**
     * Getter for <code>public.reporting_triage_answer.triage_id</code>.
     */
    public Integer getTriageId() {
        return (Integer) get(1);
    }

    /**
     * Setter for <code>public.reporting_triage_answer.question_id</code>.
     */
    public void setQuestionId(String value) {
        set(2, value);
    }

    /**
     * Getter for <code>public.reporting_triage_answer.question_id</code>.
     */
    public String getQuestionId() {
        return (String) get(2);
    }

    /**
     * Setter for <code>public.reporting_triage_answer.question_text</code>.
     */
    public void setQuestionText(String value) {
        set(3, value);
    }

    /**
     * Getter for <code>public.reporting_triage_answer.question_text</code>.
     */
    public String getQuestionText() {
        return (String) get(3);
    }

    /**
     * Setter for <code>public.reporting_triage_answer.answer_text</code>.
     */
    public void setAnswerText(String value) {
        set(4, value);
    }

    /**
     * Getter for <code>public.reporting_triage_answer.answer_text</code>.
     */
    public String getAnswerText() {
        return (String) get(4);
    }

    /**
     * Setter for <code>public.reporting_triage_answer.divergent</code>.
     */
    public void setDivergent(Boolean value) {
        set(5, value);
    }

    /**
     * Getter for <code>public.reporting_triage_answer.divergent</code>.
     */
    public Boolean getDivergent() {
        return (Boolean) get(5);
    }

    /**
     * Setter for <code>public.reporting_triage_answer.possible_answers</code>.
     */
    public void setPossibleAnswers(String value) {
        set(6, value);
    }

    /**
     * Getter for <code>public.reporting_triage_answer.possible_answers</code>.
     */
    public String getPossibleAnswers() {
        return (String) get(6);
    }

    // -------------------------------------------------------------------------
    // Primary key information
    // -------------------------------------------------------------------------

    @Override
    public Record1<Integer> key() {
        return (Record1) super.key();
    }

    // -------------------------------------------------------------------------
    // Record7 type implementation
    // -------------------------------------------------------------------------

    @Override
    public Row7<Integer, Integer, String, String, String, Boolean, String> fieldsRow() {
        return (Row7) super.fieldsRow();
    }

    @Override
    public Row7<Integer, Integer, String, String, String, Boolean, String> valuesRow() {
        return (Row7) super.valuesRow();
    }

    @Override
    public Field<Integer> field1() {
        return ReportingTriageAnswer.REPORTING_TRIAGE_ANSWER.ID;
    }

    @Override
    public Field<Integer> field2() {
        return ReportingTriageAnswer.REPORTING_TRIAGE_ANSWER.TRIAGE_ID;
    }

    @Override
    public Field<String> field3() {
        return ReportingTriageAnswer.REPORTING_TRIAGE_ANSWER.QUESTION_ID;
    }

    @Override
    public Field<String> field4() {
        return ReportingTriageAnswer.REPORTING_TRIAGE_ANSWER.QUESTION_TEXT;
    }

    @Override
    public Field<String> field5() {
        return ReportingTriageAnswer.REPORTING_TRIAGE_ANSWER.ANSWER_TEXT;
    }

    @Override
    public Field<Boolean> field6() {
        return ReportingTriageAnswer.REPORTING_TRIAGE_ANSWER.DIVERGENT;
    }

    @Override
    public Field<String> field7() {
        return ReportingTriageAnswer.REPORTING_TRIAGE_ANSWER.POSSIBLE_ANSWERS;
    }

    @Override
    public Integer component1() {
        return getId();
    }

    @Override
    public Integer component2() {
        return getTriageId();
    }

    @Override
    public String component3() {
        return getQuestionId();
    }

    @Override
    public String component4() {
        return getQuestionText();
    }

    @Override
    public String component5() {
        return getAnswerText();
    }

    @Override
    public Boolean component6() {
        return getDivergent();
    }

    @Override
    public String component7() {
        return getPossibleAnswers();
    }

    @Override
    public Integer value1() {
        return getId();
    }

    @Override
    public Integer value2() {
        return getTriageId();
    }

    @Override
    public String value3() {
        return getQuestionId();
    }

    @Override
    public String value4() {
        return getQuestionText();
    }

    @Override
    public String value5() {
        return getAnswerText();
    }

    @Override
    public Boolean value6() {
        return getDivergent();
    }

    @Override
    public String value7() {
        return getPossibleAnswers();
    }

    @Override
    public ReportingTriageAnswerRecord value1(Integer value) {
        setId(value);
        return this;
    }

    @Override
    public ReportingTriageAnswerRecord value2(Integer value) {
        setTriageId(value);
        return this;
    }

    @Override
    public ReportingTriageAnswerRecord value3(String value) {
        setQuestionId(value);
        return this;
    }

    @Override
    public ReportingTriageAnswerRecord value4(String value) {
        setQuestionText(value);
        return this;
    }

    @Override
    public ReportingTriageAnswerRecord value5(String value) {
        setAnswerText(value);
        return this;
    }

    @Override
    public ReportingTriageAnswerRecord value6(Boolean value) {
        setDivergent(value);
        return this;
    }

    @Override
    public ReportingTriageAnswerRecord value7(String value) {
        setPossibleAnswers(value);
        return this;
    }

    @Override
    public ReportingTriageAnswerRecord values(Integer value1, Integer value2, String value3, String value4, String value5, Boolean value6, String value7) {
        value1(value1);
        value2(value2);
        value3(value3);
        value4(value4);
        value5(value5);
        value6(value6);
        value7(value7);
        return this;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Create a detached ReportingTriageAnswerRecord
     */
    public ReportingTriageAnswerRecord() {
        super(ReportingTriageAnswer.REPORTING_TRIAGE_ANSWER);
    }

    /**
     * Create a detached, initialised ReportingTriageAnswerRecord
     */
    public ReportingTriageAnswerRecord(Integer id, Integer triageId, String questionId, String questionText, String answerText, Boolean divergent, String possibleAnswers) {
        super(ReportingTriageAnswer.REPORTING_TRIAGE_ANSWER);

        setId(id);
        setTriageId(triageId);
        setQuestionId(questionId);
        setQuestionText(questionText);
        setAnswerText(answerText);
        setDivergent(divergent);
        setPossibleAnswers(possibleAnswers);
    }
}
