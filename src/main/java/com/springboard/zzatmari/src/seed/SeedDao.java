package com.springboard.zzatmari.src.seed;

import com.springboard.zzatmari.src.seed.model.GetSeedDetailRes;
import com.springboard.zzatmari.src.seed.model.GetSeedsRes;
import com.springboard.zzatmari.src.seed.model.Seed;
import com.springboard.zzatmari.src.seed.model.SeedStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import javax.transaction.Transactional;
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
        String selectSeedsQuery = "SELECT U.sunlight mySunlight FROM User U WHERE idx=?";
        int selectSeedsParams = userIdx;

        String selectSeedListQuery = "SELECT S.idx seedIdx, seedName, seedImgUrl,\n" +
                "                          needSunlight sunlight, floweringTime,\n" +
                "                          growthTime, rewardSunlight reward, ifnull(quantity,0) quantity\n" +
                "                    FROM SeedInfo S\n" +
                "                    LEFT JOIN (SELECT count(*) quantity, seedIdx FROM UserSeed WHERE seedIdx=? AND userIdx=? AND status=0) US ON US.seedIdx=S.idx\n" +
                "                    WHERE S.idx=?";

        Object[] selectSeedListParams = new Object[]{seedIdx, userIdx, seedIdx};

        int mySunlight =  this.jdbcTemplate.queryForObject(selectSeedsQuery,
                int.class,
                selectSeedsParams
        );

        return this.jdbcTemplate.queryForObject(selectSeedListQuery,
                (rs,rowNum)-> new GetSeedDetailRes(
                        mySunlight,
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

    }

    //씨앗상점 조회
    public GetSeedsRes selectSeeds(int userIdx){
        String selectSeedsQuery = "SELECT U.sunlight mySunlight FROM User U WHERE idx=?";
        String selectSeedListQuery = "SELECT idx seedIdx, seedName, seedImgUrl, needSunlight sunlight\n" +
                "FROM SeedInfo\n" +
                "WHERE status=0\n" +
                "ORDER BY needSunlight";

        int selectSeedsParams = userIdx;


        List<SeedStore> seedList = this.jdbcTemplate.query(selectSeedListQuery,
                (rs,rowNum)-> new SeedStore(
                        rs.getInt("seedIdx"),
                        rs.getString("seedName"),
                        rs.getString("seedImgUrl"),
                        rs.getInt("sunlight"))
        );

        return this.jdbcTemplate.queryForObject(selectSeedsQuery,
                (rs,rowNum)-> new GetSeedsRes(
                        rs.getInt("mySunlight"),
                        seedList
                ),
                selectSeedsParams
        );
    }

    //씨앗구매 - 사용자 보유씨앗 추가
    public int insertUserSeed(int userIdx, int seedIdx){
        String insertUserSeedQuery = "INSERT INTO UserSeed(userIdx, seedIdx) VALUES(?,?)";

        Object[] insertUserSeedParams = new Object[]{userIdx, seedIdx};

        return this.jdbcTemplate.update(insertUserSeedQuery, insertUserSeedParams);

    }

    //씨앗구매 - 사용자 햇살 사용
    public int updateUserSunlight(int userIdx, int sunlight){
        String updateUserSunlightQuery = "UPDATE User SET sunlight=? WHERE idx=?";

        Object[] updateUserSunlightParams = new Object[]{sunlight, userIdx};

        return this.jdbcTemplate.update(updateUserSunlightQuery, updateUserSunlightParams);

    }

    //씨앗가격 조회
    public int selectSeedSunlight(int seedIdx){
        String selectSeedSunlightQuery = "SELECT needSunlight sunlight FROM SeedInfo S WHERE S.idx=?";

        Object[] selectSeedSunlightParams = new Object[]{seedIdx};

        return this.jdbcTemplate.queryForObject(selectSeedSunlightQuery, int.class, selectSeedSunlightParams);

    }

    //사용자 햇살 조회
    public int selectUserSunlight(int userIdx){
        String selectUserSunlightQuery = "SELECT U.sunlight FROM User U WHERE U.idx=?";

        Object[] selectUserSunlightParams = new Object[]{userIdx};

        return this.jdbcTemplate.queryForObject(selectUserSunlightQuery, int.class, selectUserSunlightParams);

    }

    //씨앗 기본 세팅
    public int insertSeedDefault(int userIdx){
        String insertUserSeedQuery = "INSERT INTO UserSeed(userIdx, seedIdx) VALUES(?,1)";

        Object[] insertUserSeedParams = new Object[]{userIdx};

        return this.jdbcTemplate.update(insertUserSeedQuery, insertUserSeedParams);

    }
}
