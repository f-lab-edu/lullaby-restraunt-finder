package com.lullaby.flab.restrauntfinderapi.security

import io.jsonwebtoken.Claims
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.MalformedJwtException
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.UnsupportedJwtException
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.security.Key
import java.util.Date

@Component
class TokenProvider {
    private val logger: Logger = LoggerFactory.getLogger(TokenProvider::class.java)
    private val secret: String = "Y2hvcHBhLWRvbnQtYml0ZS1tZS1zcHJpbmctYm9vdC1qd3QtdGVzdC1zZWNyZXQta2V5LWNob3BwYS1kb250LWJpdGUtbWUtc3ByaW5nLWJvb3Qtand0LXRlc3Qtc2VjcmV0LWtleQo="
    private val accessTokenExpireInMillis: Long = 1000 * 60 * 60L // 1시간
    private val refreshTokenExpireInMillis: Long = 1000 * 60 * 60 * 24 * 7L // 1주일
    private var key: Key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret))

    fun accessToken(userId: Long): String {
        // 토큰의 expire 시간을 설정
        val now: Long = Date().time
        val validity = Date(now + accessTokenExpireInMillis)

        return Jwts.builder()
            .setSubject(userId.toString())
            .signWith(key, SignatureAlgorithm.HS512) // 사용할 암호화 알고리즘과 , signature 에 들어갈 secret값 세팅
            .setExpiration(validity) // set Expire Time 해당 옵션 안넣으면 expire안함
            .compact()
    }

    fun refreshToken(userId: Long): String {
        // 토큰의 expire 시간을 설정
        val now: Long = Date().time
        val validity = Date(now + refreshTokenExpireInMillis)

        return Jwts.builder()
            .setSubject(userId.toString())
            .signWith(key, SignatureAlgorithm.HS512)
            .setExpiration(validity)
            .compact()
    }

    fun parseToken(token: String): Claims = Jwts
        .parserBuilder()
        .setSigningKey(key)
        .build()
        .parseClaimsJws(token)
        .body

    // 토큰의 유효성 검증을 수행
    fun validateToken(token: String?): Boolean {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token)
            return true
        } catch (e: SecurityException) {
            logger.error("토큰 서명이 올바르지 않습니다.", e)
        } catch (e: MalformedJwtException) {
            logger.error("토큰 서명이 올바르지 않습니다.", e)
        } catch (e: ExpiredJwtException) {
            logger.error("만료된 토큰 입니다.", e)
        } catch (e: UnsupportedJwtException) {
            logger.error("지원 하지 않는 형식의 토큰 입니다..", e)
        } catch (e: IllegalArgumentException) {
            logger.error("올바르지 않은 토큰 입니다.", e)
        }
        return false
    }

}
