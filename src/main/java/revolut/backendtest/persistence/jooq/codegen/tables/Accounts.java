/*
 * This file is generated by jOOQ.
 */
package revolut.backendtest.persistence.jooq.codegen.tables;


import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Generated;

import org.jooq.Field;
import org.jooq.ForeignKey;
import org.jooq.Identity;
import org.jooq.Index;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.Row3;
import org.jooq.Schema;
import org.jooq.Table;
import org.jooq.TableField;
import org.jooq.UniqueKey;
import org.jooq.impl.DSL;
import org.jooq.impl.TableImpl;

import revolut.backendtest.persistence.jooq.codegen.Indexes;
import revolut.backendtest.persistence.jooq.codegen.Keys;
import revolut.backendtest.persistence.jooq.codegen.Public;
import revolut.backendtest.persistence.jooq.codegen.tables.records.AccountsRecord;


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
public class Accounts extends TableImpl<AccountsRecord> {

    private static final long serialVersionUID = -1205564649;

    /**
     * The reference instance of <code>PUBLIC.ACCOUNTS</code>
     */
    public static final Accounts ACCOUNTS = new Accounts();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<AccountsRecord> getRecordType() {
        return AccountsRecord.class;
    }

    /**
     * The column <code>PUBLIC.ACCOUNTS.ACCOUNT_ID</code>.
     */
    public final TableField<AccountsRecord, Long> ACCOUNT_ID = createField(DSL.name("ACCOUNT_ID"), org.jooq.impl.SQLDataType.BIGINT.nullable(false).identity(true), this, "");

    /**
     * The column <code>PUBLIC.ACCOUNTS.CREATED_AT</code>.
     */
    public final TableField<AccountsRecord, Timestamp> CREATED_AT = createField(DSL.name("CREATED_AT"), org.jooq.impl.SQLDataType.TIMESTAMP.precision(6).defaultValue(org.jooq.impl.DSL.field("CURRENT_TIMESTAMP", org.jooq.impl.SQLDataType.TIMESTAMP)), this, "");

    /**
     * The column <code>PUBLIC.ACCOUNTS.BALANCE</code>.
     */
    public final TableField<AccountsRecord, BigDecimal> BALANCE = createField(DSL.name("BALANCE"), org.jooq.impl.SQLDataType.DECIMAL(20, 2).defaultValue(org.jooq.impl.DSL.field("0", org.jooq.impl.SQLDataType.DECIMAL)), this, "");

    /**
     * Create a <code>PUBLIC.ACCOUNTS</code> table reference
     */
    public Accounts() {
        this(DSL.name("ACCOUNTS"), null);
    }

    /**
     * Create an aliased <code>PUBLIC.ACCOUNTS</code> table reference
     */
    public Accounts(String alias) {
        this(DSL.name(alias), ACCOUNTS);
    }

    /**
     * Create an aliased <code>PUBLIC.ACCOUNTS</code> table reference
     */
    public Accounts(Name alias) {
        this(alias, ACCOUNTS);
    }

    private Accounts(Name alias, Table<AccountsRecord> aliased) {
        this(alias, aliased, null);
    }

    private Accounts(Name alias, Table<AccountsRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment(""));
    }

    public <O extends Record> Accounts(Table<O> child, ForeignKey<O, AccountsRecord> key) {
        super(child, key, ACCOUNTS);
    }

    @Override
    public Schema getSchema() {
        return Public.PUBLIC;
    }

    @Override
    public List<Index> getIndexes() {
        return Arrays.<Index>asList(Indexes.PRIMARY_KEY_A);
    }

    @Override
    public Identity<AccountsRecord, Long> getIdentity() {
        return Keys.IDENTITY_ACCOUNTS;
    }

    @Override
    public UniqueKey<AccountsRecord> getPrimaryKey() {
        return Keys.CONSTRAINT_A;
    }

    @Override
    public List<UniqueKey<AccountsRecord>> getKeys() {
        return Arrays.<UniqueKey<AccountsRecord>>asList(Keys.CONSTRAINT_A);
    }

    @Override
    public Accounts as(String alias) {
        return new Accounts(DSL.name(alias), this);
    }

    @Override
    public Accounts as(Name alias) {
        return new Accounts(alias, this);
    }

    /**
     * Rename this table
     */
    @Override
    public Accounts rename(String name) {
        return new Accounts(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public Accounts rename(Name name) {
        return new Accounts(name, null);
    }

    // -------------------------------------------------------------------------
    // Row3 type methods
    // -------------------------------------------------------------------------

    @Override
    public Row3<Long, Timestamp, BigDecimal> fieldsRow() {
        return (Row3) super.fieldsRow();
    }
}
