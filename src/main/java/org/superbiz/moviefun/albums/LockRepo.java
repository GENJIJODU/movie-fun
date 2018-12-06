package org.superbiz.moviefun.albums;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class LockRepo {

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Transactional
    public Boolean acquireLock() {

        int affectedRows = jdbcTemplate.update("UPDATE locker" +
                " SET lst_updt_dt = now()" +
                " WHERE lst_updt_dt IS NULL" +
                " OR lst_updt_dt < date_sub(now(), INTERVAL 20 second)");

        return affectedRows > 0;
    }
}
