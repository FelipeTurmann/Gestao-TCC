package com.example.gestaotcc.config;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
@Slf4j
public class JwtTokenProvider {

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.expiration}")
    private int jwtExpirationInMs;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    // Método para gerar o token JWT
    public String generateToken(Authentication authentication) {
        // Obtendo o usuário autenticado
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationInMs);

        return Jwts.builder()
                .setSubject(Long.toString(userPrincipal.getId()))  // ID do usuário autenticado
                .setIssuedAt(new Date())  // Data de emissão
                .setExpiration(expiryDate)  // Data de expiração
                .claim("tipoUsuario", userPrincipal.getTipoUsuario().name())  // Adiciona um claim para o tipo de usuário
                .signWith(getSigningKey())  // Usando a chave secreta para assinar o token
                .compact();  // Retorna o JWT compactado
    }

    // Método para extrair o ID do usuário do token JWT
    public Long getUserIdFromJWT(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(getSigningKey())  // Usando a mesma chave para validação
                .parseClaimsJws(token)
                .getBody();

        return Long.parseLong(claims.getSubject());
    }

    // Método para validar o token JWT
    public boolean validateToken(String authToken) {
        try {
            Jwts.parser().setSigningKey(getSigningKey()).parseClaimsJws(authToken);  // Usando a chave secreta para validação
            return true;  // Token válido
        } catch (SignatureException ex) {
            log.error("Assinatura JWT inválida");
        } catch (MalformedJwtException ex) {
            log.error("Token JWT inválido");
        } catch (ExpiredJwtException ex) {
            log.error("Token JWT expirado");
        } catch (UnsupportedJwtException ex) {
            log.error("Token JWT não suportado");
        } catch (IllegalArgumentException ex) {
            log.error("String claims JWT vazia");
        }
        return false;  // Token inválido ou expirado
    }
}