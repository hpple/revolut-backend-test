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
import org.jooq.Row5;
import org.jooq.Schema;
import org.jooq.Table;
import org.jooq.TableField;
import org.jooq.UniqueKey;
import org.jooq.impl.DSL;
import org.jooq.impl.TableImpl;

import revolut.backendtest.persistence.jooq.codegen.Indexes;
import revolut.backendtest.persistence.jooq.codegen.Keys;
import revolut.backendtest.persistence.jooq.codegen.Public;
import revolut.backendtest.persistence.jooq.codegen.tables.records.TransfersRecord;


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
public class Transfers extends TableImpl<TransfersRecord> {

    private static final long serialVersionUID = -1539253622;

    /**
     * The reference instance of <code>PUBLIC.TRANSFERS</code>
     */
    public static final Transfers TRANSFERS = new Transfers();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<TransfersRecord> getRecordType() {
        return TransfersRecord.class;
    }

    /**
     * The column <code>PUBLIC.TRANSFERS.TRANSFER_ID</code>.
     */
    public final TableField<TransfersRecord, Long> TRANSFER_ID = createField(DSL.name("TRANSFER_ID"), org.jooq.impl.SQLDataType.BIGINT.nullable(false).identity(true), this, "");

    /**
     * The column <code>PUBLIC.TRANSFERS.FROM_ACCOUNT_ID</code>.
     */
    public final TableField<TransfersRecord, Long> FROM_ACCOUNT_ID = createField(DSL.name("FROM_ACCOUNT_ID"), org.jooq.impl.SQLDataType.BIGINT.nullable(false), this, "");

    /**
     * The column <code>PUBLIC.TRANSFERS.TO_ACCOUNT_ID</code>.
     */
    public final TableField<TransfersRecord, Long> TO_ACCOUNT_ID = createField(DSL.name("TO_ACCOUNT_ID"), org.jooq.impl.SQLDataType.BIGINT.nullable(false), this, "");

    /**
     * The column <code>PUBLIC.TRANSFERS.TRANSFERED_AT</code>.
     */
    public final TableField<TransfersRecord, Timestamp> TRANSFERED_AT = createField(DSL.name("TRANSFERED_AT"), org.jooq.impl.SQLDataType.TIMESTAMP.precision(6).defaultValue(org.jooq.impl.DSL.field("CURRENT_TIMESTAMP", org.jooq.impl.SQLDataType.TIMESTAMP)), this, "");

    /**
     * The column <code>PUBLIC.TRANSFERS.AMOUNT</code>.
     */
    public final TableField<TransfersRecord, BigDecimal> AMOUNT = createField(DSL.name("AMOUNT"), org.jooq.impl.SQLDataType.DECIMAL(20, 2), this, "");

    /**
     * Create a <code>PUBLIC.TRANSFERS</code> table reference
     */
    public Transfers() {
        this(DSL.name("TRANSFERS"), null);
    }

    /**
     * Create an aliased <code>PUBLIC.TRANSFERS</code> table reference
     */
    public Transfers(String alias) {
        this(DSL.name(alias), TRANSFERS);
    }

    /**
     * Create an aliased <code>PUBLIC.TRANSFERS</code> table reference
     */
    public Transfers(Name alias) {
        this(alias, TRANSFERS);
    }

    private Transfers(Name alias, Table<TransfersRecord> aliased) {
        this(alias, aliased, null);
    }

    private Transfers(Name alias, Table<TransfersRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment(""));
    }

    public <O extends Record> Transfers(Table<O> child, ForeignKey<O, TransfersRecord> key) {
        super(child, key, TRANSFERS);
    }

    @Override
    public Schema getSchema() {
        return Public.PUBLIC;
    }

    @Override
    public List<Index> getIndexes() {
        return Arrays.<Index>asList(Indexes.CONSTRAINT_INDEX_E, Indexes.CONSTRAINT_INDEX_E4, Indexes.PRIMARY_KEY_E);
    }

    @Override
    public Identity<TransfersRecord, Long> getIdentity() {
        return Keys.IDENTITY_TRANSFERS;
    }

    @Override
    public UniqueKey<TransfersRecord> getPrimaryKey() {
        return Keys.CONSTRAINT_E;
    }

    @Override
    public List<UniqueKey<TransfersRecord>> getKeys() {
        return Arrays.<UniqueKey<TransfersRecord>>asList(Keys.CONSTRAINT_E);
    }

    @Override
    public List<ForeignKey<TransfersRecord, ?>> getReferences() {
        return Arrays.<ForeignKey<TransfersRecord, ?>>asList(Keys.CONSTRAINT_E4, Keys.CONSTRAINT_E4D);
    }

    public Accounts constraintE4() {
        return new Accounts(this, Keys.CONSTRAINT_E4);
    }

    public Accounts constraintE4d() {
        return new Accounts(this, Keys.CONSTRAINT_E4D);
    }

    @Override
    public Transfers as(String alias) {
        return new Transfers(DSL.name(alias), this);
    }

    @Override
    public Transfers as(Name alias) {
        return new Transfers(alias, this);
    }

    /**
     * Rename this table
     */
    @Override
    public Transfers rename(String name) {
        return new Transfers(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public Transfers rename(Name name) {
        return new Transfers(name, null);
    }

    // -------------------------------------------------------------------------
    // Row5 type methods
    // -------------------------------------------------------------------------

    @Override
    public Row5<Long, Long, Long, Timestamp, BigDecimal> fieldsRow() {
        return (Row5) super.fieldsRow();
    }
}