package moe.ofs.backend.dao;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import moe.ofs.backend.domain.TokenInfo;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface TokenInfoDao extends BaseMapper<TokenInfo> {

    @Select("SELECT token_info.id id,token_info.access_token,token_info.access_token_expire_time,token_info.user_id," +
            "token_info.refresh_token,token_info.refresh_token_expire_time,admin_info.name,admin_info.password " +
            "FROM token_info inner join admin_info WHERE admin_info.id = token_info.user_id;")
    List<TokenInfo> selectTokens();

    TokenInfo selectOneByAccessToken(@Param("accessToken") String accessToken);

    TokenInfo selectOneByRefreshToken(@Param("refreshToken") String refreshToken);
}
