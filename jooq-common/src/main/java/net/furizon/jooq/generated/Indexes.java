/*
 * This file is generated by jOOQ.
 */
package net.furizon.jooq.generated;


import javax.annotation.processing.Generated;

import net.furizon.jooq.generated.tables.Authentications;
import net.furizon.jooq.generated.tables.Sessions;

import org.jooq.Index;
import org.jooq.OrderField;
import org.jooq.impl.DSL;
import org.jooq.impl.Internal;


/**
 * A class modelling indexes of tables in public.
 */
@Generated(
    value = {
        "https://www.jooq.org",
        "jOOQ version:3.19.13"
    },
    comments = "This class is generated by jOOQ"
)
@SuppressWarnings({ "all", "unchecked", "rawtypes", "this-escape" })
public class Indexes {

    // -------------------------------------------------------------------------
    // INDEX definitions
    // -------------------------------------------------------------------------

    public static final Index AUTHENTICATION_HASHED_PASSWORD = Internal.createIndex(DSL.name("authentication_hashed_password"), Authentications.AUTHENTICATIONS, new OrderField[] { Authentications.AUTHENTICATIONS.AUTHENTICATION_HASHED_PASSWORD }, false);
    public static final Index AUTHENTICATIONS_EMAIL_IDX = Internal.createIndex(DSL.name("authentications_email_idx"), Authentications.AUTHENTICATIONS, new OrderField[] { Authentications.AUTHENTICATIONS.AUTHENTICATION_EMAIL }, false);
    public static final Index SESSIONS_CREATED_AT_IDX = Internal.createIndex(DSL.name("sessions_created_at_idx"), Sessions.SESSIONS, new OrderField[] { Sessions.SESSIONS.CREATED_AT }, false);
    public static final Index SESSIONS_EXPIRES_AT_IDX = Internal.createIndex(DSL.name("sessions_expires_at_idx"), Sessions.SESSIONS, new OrderField[] { Sessions.SESSIONS.EXPIRES_AT }, false);
    public static final Index SESSIONS_USER_ID_IDX = Internal.createIndex(DSL.name("sessions_user_id_idx"), Sessions.SESSIONS, new OrderField[] { Sessions.SESSIONS.USER_ID }, false);
}
