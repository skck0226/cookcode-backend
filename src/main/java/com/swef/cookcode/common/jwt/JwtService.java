package com.swef.cookcode.common.jwt;

import static com.swef.cookcode.common.ErrorCode.BLACKLIST_TOKEN_REQUEST;
import static com.swef.cookcode.common.ErrorCode.INVALID_REFRESH_TOKEN_REQUEST;
import static com.swef.cookcode.common.ErrorCode.REDIS_TOKEN_NOT_FOUND;
import static com.swef.cookcode.common.ErrorCode.TOKEN_EXPIRED;
import static com.swef.cookcode.common.ErrorCode.TOKEN_NOT_EXPIRED;
import static com.swef.cookcode.common.ErrorCode.TOKEN_USER_ID_NOT_MATCHED;
import static java.util.Objects.isNull;

import com.auth0.jwt.exceptions.TokenExpiredException;
import com.swef.cookcode.common.config.JwtConfig;
import com.swef.cookcode.common.error.exception.AuthErrorException;
import com.swef.cookcode.common.error.exception.InvalidRequestException;
import com.swef.cookcode.common.jwt.claims.AccessClaim;
import com.swef.cookcode.common.jwt.claims.RefreshClaim;
import com.swef.cookcode.common.service.RedisService;
import com.swef.cookcode.user.domain.User;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

  private final Jwt accessJwt;

  private final Jwt refreshJwt;

  private final JwtConfig jwtConfig;

  private final RedisService redisService;

  public JwtService(@Qualifier("accessJwt") Jwt accessJwt, @Qualifier("refreshJwt") Jwt refreshJwt, JwtConfig jwtConfig,
                    RedisService redisService) {
    this.accessJwt = accessJwt;
    this.refreshJwt = refreshJwt;
    this.jwtConfig = jwtConfig;
    this.redisService = redisService;
  }

  public int getRefreshExpiry() {
    return refreshJwt.getExpirySeconds();
  }

  public String createAccessToken(Long userId, String email, List<GrantedAuthority> authorities) {
    String[] roles = authorities.stream()
        .map(GrantedAuthority::getAuthority)
        .toArray(String[]::new);
    return accessJwt.sign(new AccessClaim(userId, email, roles));
  }

  public String createRefreshToken(String email) {
    String refreshToken = refreshJwt.sign(new RefreshClaim(email));
    redisService.setValues(email, refreshToken, Duration.ofSeconds(
        refreshJwt.getExpirySeconds()));
    return refreshToken;
  }

  public void checkRefreshToken(String email, String refreshToken) {
    try{
      refreshJwt.verifyRefreshToken(refreshToken);
    } catch (TokenExpiredException e) {
      throw new AuthErrorException(TOKEN_EXPIRED);
    }
    String redisToken = (String) redisService.getValues(email);
    if(isNull(redisToken)) throw new AuthErrorException(REDIS_TOKEN_NOT_FOUND);
    if(!redisToken.equals(refreshToken)) {
      throw new AuthErrorException(INVALID_REFRESH_TOKEN_REQUEST);
    }
  }

  public String reissueAccessToken(User user, String expiredAccessToken, String refreshToken) {
    Date now = new Date();
    try {
      AccessClaim claims = verifyAccessToken(expiredAccessToken);
      if (!claims.getUserId().equals(user.getId())) throw new InvalidRequestException(TOKEN_USER_ID_NOT_MATCHED);
      if (claims.getExp().getTime() - now.getTime() >= 1000 * 60 * 5) {
        throw new InvalidRequestException(TOKEN_NOT_EXPIRED);
      } else {
        throw new TokenExpiredException(TOKEN_EXPIRED.getMessage(), Instant.now());
      }
    } catch (TokenExpiredException e) {
      checkRefreshToken(user.getEmail(), refreshToken);
      List<GrantedAuthority> authorities = List.of(user.getAuthority().toGrantedAuthority());
      return createAccessToken(user.getId(), user.getEmail(), authorities);
    }
  }

  public void signOut(String token) {
    AccessClaim claim = accessJwt.verifyAccessToken(token);
    long expiredAccessTokenTime = claim.getExp().getTime() - new Date().getTime();
    redisService.setValues(jwtConfig.getBlackListPrefix() + token, claim.getEmail(), Duration.ofMillis(expiredAccessTokenTime));
    redisService.deleteValues(claim.getEmail());
  }

  public AccessClaim verifyAccessToken(String token) {
    String expiredAt = (String) redisService.getValues(jwtConfig.getBlackListPrefix() + token);
    if (expiredAt != null) throw new AuthErrorException(BLACKLIST_TOKEN_REQUEST);
    return accessJwt.verifyAccessToken(token);
  }
}
