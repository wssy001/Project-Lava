package moe.ofs.backend.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import moe.ofs.backend.domain.dcs.BaseEntity;

import java.util.Date;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class LavaUserToken extends BaseEntity {
    @JsonIgnore
    private Object userInfoToken;
    private String accessToken;
    private String refreshToken;
    private Date accessTokenExpireTime;
    private Date refreshTokenExpireTime;
}
