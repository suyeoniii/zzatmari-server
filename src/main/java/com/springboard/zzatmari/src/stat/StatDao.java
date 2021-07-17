package com.springboard.zzatmari.src.stat;

import com.springboard.zzatmari.src.goal.model.GoalLists;
import com.springboard.zzatmari.src.stat.model.GetStatsListRes;
import com.springboard.zzatmari.src.stat.model.GetStatsRes;
import com.springboard.zzatmari.src.user.model.GetUserRes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.List;

@Repository
public class StatDao {

    private JdbcTemplate jdbcTemplate;

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);

    }

    public List<GetStatsRes> selectStats(int userIdx, int year, int month){
        String selectStatsQuery = "SELECT DAY(executionDate) day, case when time>100 then 100 else time end percent\n" +
                "FROM (SELECT TRUNCATE((SUM(E.min)/E.goalTime)*100,0) time, executionDate, listIdx FROM Execution E WHERE status=2 GROUP BY executionDate) E\n" +
                "JOIN List L on L.idx=E.listIdx\n" +
                "JOIN User U on U.idx=L.userIdx\n" +
                "WHERE U.idx=? AND YEAR(executionDate)=? AND MONTH(executionDate)=? AND time>0 ORDER BY day";
        Object[] selectStatsParams = new Object[]{userIdx, year, month};
        return this.jdbcTemplate.query(selectStatsQuery,
                (rs, rowNum) -> new GetStatsRes(
                        rs.getInt("day"),
                        rs.getInt("percent")),
                        selectStatsParams);
    }

    public GetStatsListRes selectStatsList(int userIdx, String type, int year, int month, int day){

        String selectStatsInfoQuery = "SELECT ifnull(ROUND((digitalDetoxTime/(digitalDetoxTime+selfDevelopmentTime)*100),0),0) digitalDetoxPercent,\n" +
                "       ifnull(ROUND((selfDevelopmentTime/(digitalDetoxTime+selfDevelopmentTime)*100),0),0) selfDevelopmentPercent,\n" +
                "       digitalDetoxTime,\n" +
                "       selfDevelopmentTime,\n" +
                "       0 continuousDay\n" +
                "FROM (SELECT ifnull(SUM(case when L.listType=0 then E.min end),0) digitalDetoxTime,\n" +
                "             ifnull(SUM(case when L.listType=1 then E.min end),0) selfDevelopmentTime\n" +
                "FROM Execution E\n" +
                "    JOIN List L on L.idx=E.listIdx\n" +
                "    JOIN User U on U.idx=L.userIdx\n" +
                "    WHERE L.userIdx=? AND YEAR(executionDate)=? AND MONTH(executionDate)=?";

        if(type.equals("daily")){
            selectStatsInfoQuery += " AND DAY(executionDate)=?) E";
        }else{
            selectStatsInfoQuery += " OR -1=?) E";
        }

        String selectStatsListQuery = "SELECT E.listIdx, L.listItem, SUM(E.min) time\n" +
                "FROM Execution E\n" +
                "    JOIN List L on L.idx=E.listIdx\n" +
                "    JOIN User U on U.idx=L.userIdx\n" +
                "WHERE U.idx=? AND L.listType=? AND E.min>0 AND YEAR(executionDate)=? AND MONTH(executionDate)=?";

        if(type.equals("daily")){
            selectStatsListQuery += " AND DAY(executionDate)=? GROUP BY L.idx";
        }else{
            selectStatsListQuery += " OR -1=? GROUP BY L.idx";
        }

        Object[] selectStatsParams = new Object[]{userIdx, year, month, day};
        Object[] selectStatsListParams1 = new Object[]{userIdx, 0, year, month, day};
        Object[] selectStatsListParams2 = new Object[]{userIdx, 1, year, month, day};

        //디지털디톡스 리스트
        List<GoalLists> result1 = this.jdbcTemplate.query(selectStatsListQuery,
                (rs,rowNum)-> new GoalLists(
                        rs.getInt("listIdx"),
                        rs.getString("listItem"),
                        rs.getInt("time")
                ),selectStatsListParams1
        );

        //자기개발 리스트
        List<GoalLists> result2 = this.jdbcTemplate.query(selectStatsListQuery,
                (rs,rowNum)-> new GoalLists(
                        rs.getInt("listIdx"),
                        rs.getString("listItem"),
                        rs.getInt("time")
                ),selectStatsListParams2
        );

        return this.jdbcTemplate.queryForObject(selectStatsInfoQuery,
                (rs, rowNum) -> new GetStatsListRes(
                        rs.getInt("digitalDetoxPercent"),
                        rs.getInt("selfDevelopmentPercent"),
                        rs.getInt("digitalDetoxTime"),
                        rs.getInt("selfDevelopmentTime"),
                        rs.getInt("continuousDay"),
                        result1,
                        result2),
                selectStatsParams);
    }
}