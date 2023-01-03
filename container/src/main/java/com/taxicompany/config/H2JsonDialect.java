package com.taxicompany.config;

import java.sql.Types;

public class H2JsonDialect extends org.hibernate.dialect.H2Dialect {

    public H2JsonDialect() {

        super();
        this.registerHibernateType(Types.OTHER, "json");

    }
    
    @Override
    public String toString() {

        return H2JsonDialect.class.getName();

    }

}
