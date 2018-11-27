package com.amazonaws.serverless.sample.springboot.db;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.SQLException;

public class Data {
    private static final HikariDataSource dataSource;

    static {
        String driver = "com.mysql.jdbc.Driver";
        String url = "jdbc:mysql://walkdatabase.crbiix4pux8c.us-west-2.rds.amazonaws.com/datacenter?useSSL=false&autoReconnect=true";
        String username = "tianqingche";
        String password = "Ok*64818503";

        HikariConfig config = new HikariConfig();
        config.setDriverClassName(driver);
        config.setJdbcUrl(url);
        config.setUsername(username);
        config.setPassword(password);

        dataSource = new HikariDataSource(config);
    }

    public static java.sql.Connection getConn() throws SQLException {
        return dataSource.getConnection();
    }
}
