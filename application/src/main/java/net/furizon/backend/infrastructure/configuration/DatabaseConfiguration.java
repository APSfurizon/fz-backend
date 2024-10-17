package net.furizon.backend.infrastructure.configuration;

import net.furizon.jooq.infrastructure.command.JooqSqlCommand;
import net.furizon.jooq.infrastructure.command.SqlCommand;
import net.furizon.jooq.infrastructure.query.JooqSqlQuery;
import net.furizon.jooq.infrastructure.query.SqlQuery;
import org.jooq.DSLContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DatabaseConfiguration {
    @Bean
    SqlCommand sqlCommand(DSLContext dslContext) {
        return new JooqSqlCommand(dslContext);
    }

    @Bean
    SqlQuery sqlQuery(DSLContext dslContext) {
        return new JooqSqlQuery(dslContext);
    }
}
