/*
 * This file is generated by jOOQ.
 */
package revolut.backendtest.persistence.jooq.codegen.tables.records;


import java.math.BigDecimal;
import java.sql.Timestamp;

import javax.annotation.Generated;

import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.Record3;
import org.jooq.Row3;
import org.jooq.impl.UpdatableRecordImpl;

import revolut.backendtest.persistence.jooq.codegen.tables.Accounts;


/**
 * This class is generated by jOOQ.
 */
@Generated(
    value = {
        "http://www.jooq.org",
        "jOOQ version:3.12.1"
    },
    comments = "This class is generated by jOOQ"
)
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class AccountsRecord extends UpdatableRecordImpl<AccountsRecord> implements Record3<Long, Timestamp, BigDecimal> {

    private static final long serialVersionUID = -360847644;

    /**
     * Setter for <code>PUBLIC.ACCOUNTS.ACCOUNT_ID</code>.
     */
    public void setAccountId(Long value) {
        set(0, value);
    }

    /**
     * Getter for <code>PUBLIC.ACCOUNTS.ACCOUNT_ID</code>.
     */
    public Long getAccountId() {
        return (Long) get(0);
    }

    /**
     * Setter for <code>PUBLIC.ACCOUNTS.CREATED_AT</code>.
     */
    public void setCreatedAt(Timestamp value) {
        set(1, value);
    }

    /**
     * Getter for <code>PUBLIC.ACCOUNTS.CREATED_AT</code>.
     */
    public Timestamp getCreatedAt() {
        return (Timestamp) get(1);
    }

    /**
     * Setter for <code>PUBLIC.ACCOUNTS.BALANCE</code>.
     */
    public void setBalance(BigDecimal value) {
        set(2, value);
    }

    /**
     * Getter for <code>PUBLIC.ACCOUNTS.BALANCE</code>.
     */
    public BigDecimal getBalance() {
        return (BigDecimal) get(2);
    }

    // -------------------------------------------------------------------------
    // Primary key information
    // -------------------------------------------------------------------------

    @Override
    public Record1<Long> key() {
        return (Record1) super.key();
    }

    // -------------------------------------------------------------------------
    // Record3 type implementation
    // -------------------------------------------------------------------------

    @Override
    public Row3<Long, Timestamp, BigDecimal> fieldsRow() {
        return (Row3) super.fieldsRow();
    }

    @Override
    public Row3<Long, Timestamp, BigDecimal> valuesRow() {
        return (Row3) super.valuesRow();
    }

    @Override
    public Field<Long> field1() {
        return Accounts.ACCOUNTS.ACCOUNT_ID;
    }

    @Override
    public Field<Timestamp> field2() {
        return Accounts.ACCOUNTS.CREATED_AT;
    }

    @Override
    public Field<BigDecimal> field3() {
        return Accounts.ACCOUNTS.BALANCE;
    }

    @Override
    public Long component1() {
        return getAccountId();
    }

    @Override
    public Timestamp component2() {
        return getCreatedAt();
    }

    @Override
    public BigDecimal component3() {
        return getBalance();
    }

    @Override
    public Long value1() {
        return getAccountId();
    }

    @Override
    public Timestamp value2() {
        return getCreatedAt();
    }

    @Override
    public BigDecimal value3() {
        return getBalance();
    }

    @Override
    public AccountsRecord value1(Long value) {
        setAccountId(value);
        return this;
    }

    @Override
    public AccountsRecord value2(Timestamp value) {
        setCreatedAt(value);
        return this;
    }

    @Override
    public AccountsRecord value3(BigDecimal value) {
        setBalance(value);
        return this;
    }

    @Override
    public AccountsRecord values(Long value1, Timestamp value2, BigDecimal value3) {
        value1(value1);
        value2(value2);
        value3(value3);
        return this;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Create a detached AccountsRecord
     */
    public AccountsRecord() {
        super(Accounts.ACCOUNTS);
    }

    /**
     * Create a detached, initialised AccountsRecord
     */
    public AccountsRecord(Long accountId, Timestamp createdAt, BigDecimal balance) {
        super(Accounts.ACCOUNTS);

        set(0, accountId);
        set(1, createdAt);
        set(2, balance);
    }
}
