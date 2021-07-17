package com.springboard.zzatmari.src.seed;

import com.springboard.zzatmari.src.seed.model.GetSeedDetailRes;
import com.springboard.zzatmari.src.seed.model.Seed;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.List;

@Repository
public class SeedDao {

    private JdbcTemplate jdbcTemplate;

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    //씨앗정보 조회
    public GetSeedDetailRes selectSeedDetail(int userIdx, int seedIdx){
        String selectSeedDetailQuery = "SELECT U.sunlight mySunlight FROM User U WHERE idx=?";

        String selectSeedListQuery = "SELECT S.idx seedIdx, seedName, seedImgUrl,\n" +
                "       needSunlight sunlight, floweringTime,\n" +
                "       growthTime, rewardSunlight reward, quantity\n" +
                "FROM SeedInfo S\n" +
                "JOIN (SELECT count(*) quantity FROM UserSeed WHERE seedIdx=? AND userIdx=?) US\n" +
                "WHERE S.idx=?";

        Object[] selectSeedDetailParams = new Object[]{userIdx};
        Object[] selectSeedListParams = new Object[]{seedIdx, userIdx, seedIdx};

        List<Seed> seedList = this.jdbcTemplate.query(selectSeedListQuery,
                (rs,rowNum)-> new Seed(
                        rs.getInt("seedIdx"),
                        rs.getString("seedName"),
                        rs.getString("seedImgUrl"),
                        rs.getInt("sunlight"),
                        rs.getInt("floweringTime"),
                        rs.getInt("growthTime"),
                        rs.getInt("reward"),
                        rs.getInt("quantity")),
                selectSeedListParams
        );

        return this.jdbcTemplate.queryForObject(selectSeedDetailQuery,
                (rs,rowNum)-> new GetSeedDetailRes(
                        rs.getInt("mySunlight"),
                        seedList
                ),
                selectSeedDetailParams
        );
    }
}