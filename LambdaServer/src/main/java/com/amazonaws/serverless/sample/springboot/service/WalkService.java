package com.amazonaws.serverless.sample.springboot.service;

import com.amazonaws.serverless.sample.springboot.db.Data;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;


@Service
public class WalkService {

    public Integer getDayCount(Long userId,int day) throws SQLException {
        Integer res = null;

        Connection conn = Data.getConn();
        String sql = "select step_count from walk where user_id = ? and day = ? limit 1";
        PreparedStatement pstmt;
        try {
            pstmt = (PreparedStatement)conn.prepareStatement(sql);
            pstmt.setLong(1,userId);
            pstmt.setInt(2,day);
            ResultSet rows = pstmt.executeQuery();
            if(rows.next()){
                res = rows.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return res;
    }

    public Integer addStepCount(Long userId,int day,int timeInterval,int stepCount) throws ExecutionException, InterruptedException, SQLException {
        Connection conn = Data.getConn();

        Integer res = null;
        String sql = "insert into walk (`user_id`,`day`,`time_interval`,`step_count`) values(?,?,?,?)";
        PreparedStatement pstmt;

        try {
            pstmt = (PreparedStatement) conn.prepareStatement(sql);
            pstmt.setLong(1, userId);
            pstmt.setInt(2, day);
            pstmt.setInt(3, timeInterval);
            pstmt.setInt(4,stepCount);
            res = pstmt.executeUpdate();
            pstmt.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return res;
    }

    public List<Integer> getDayRangeCount(Long userId, int startDay, int numDays) throws ExecutionException, InterruptedException, SQLException {
        List<Integer> res = new ArrayList<>();

        Connection conn = Data.getConn();
        String sql = "select step_count from walk where user_id = ? and day >= ? and day <= ?";
        PreparedStatement pstmt;

        try {
            pstmt = (PreparedStatement)conn.prepareStatement(sql);

            pstmt.setLong(1,userId);
            pstmt.setInt(2,startDay);
            pstmt.setInt(3,startDay + numDays - 1);

            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                res.add(rs.getInt(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return res;
    }

}
