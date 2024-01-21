package com.hangout.core.hangoutauthservice.service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.hangout.core.hangoutauthservice.entity.User;
import com.hangout.core.hangoutauthservice.exceptions.JwtNotValidException;
import com.hangout.core.hangoutauthservice.repository.UserRepo;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {
	@Autowired
	private UserRepo userRepo;
	@Value("${jwt.accessSecret}")
	private String accessTokenSecret = "";
	@Value("${jwt.refreshSecret}")
	private String refreshTokenSecret = "";

	/**
	 * @param userDetails - This accepts the user object
	 * @param typeOfToken - This variable has two valid values `access` for access
	 *                    token
	 *                    and `refresh` for refresh token
	 * @return It returns the specified jwt token.
	 */
	public String generateToken(User userDetails, String typeOfToken) {
		Map<String, Object> extraClaims = new HashMap<>();
		extraClaims.put("name", userDetails.getName());
		if (Objects.equals(typeOfToken, "access")) {
			return generateJWT(extraClaims, userDetails, true);
		} else if (Objects.equals(typeOfToken, "refresh")) {
			return generateJWT(extraClaims, userDetails, false);
		} else {
			return null;
		}
	}

	public Boolean isTokenValid(String token, UserDetails userDetails, Boolean isAcessToken) {
		final String userName = extractUsername(token, isAcessToken);
		return userName.equals(userDetails.getUsername()) && !isTokenExpired(token, isAcessToken);
	}

	/**
	 * @param token JWT token to be validated
	 * @return user email if token validates null if it is expired or damaged
	 * @throws JwtNotValidException when the given jwt does not match with given
	 *                              signature
	 */
	public String checkIfTokenValid(String token, String tokenType) {
		System.out.println("Incoming token: " + token);
		try {
			if (Objects.equals(tokenType, "access")) {
				final String userName = extractUsername(token, true);
				System.out.println("extracted email: " + userName);
				User user = userRepo.findByEmail(userName).orElse(null);
				System.out.println("user found from db: " + user);
				if (user != null && !isTokenExpired(token, true)) {
					return user.getEmail();
				} else {
					return null;
				}
			} else if (Objects.equals(tokenType, "refresh")) {
				final String userName = extractUsername(token, false);
				System.out.println("extracted email: " + userName);
				User user = userRepo.findByEmail(userName).orElse(null);
				System.out.println("user found from db: " + user);
				if (user != null && !isTokenExpired(token, false)) {
					return user.getEmail();
				} else {
					return null;
				}
			} else {
				return null;
			}

		} catch (Exception e) {
			throw new JwtNotValidException("the given jwt string is not valid");
		}
	}

	public String extractUsername(String jwt, Boolean isAcessToken) {
		// getSubject is email of user
		return extractClaim(jwt, Claims::getSubject, isAcessToken);
	}

	private <T> T extractClaim(String token, Function<Claims, T> claimsResolver, Boolean isAccessToken) {
		final Claims claims = extractAllClaims(token, isAccessToken);
		return claimsResolver.apply(claims);
	}

	private String generateJWT(Map<String, Object> extraClaims, UserDetails userDetails, Boolean isAccessToken) {
		return Jwts.builder().setClaims(extraClaims).setSubject(userDetails.getUsername())
				.setIssuedAt(new Date(System.currentTimeMillis()))
				.setExpiration(isAccessToken ? new Date(System.currentTimeMillis() + 3600000)
						: new Date(System.currentTimeMillis() + 259200000)) // if it is access token it expires in 1 hr
																			// or if it is a refresh token it expires in
																			// 3 days or 72 hours
				.signWith(getSigningKey(isAccessToken), SignatureAlgorithm.HS256)
				.compact();
	}

	private Claims extractAllClaims(String token, Boolean isAccessToken) {
		return Jwts.parserBuilder().setSigningKey(getSigningKey(isAccessToken)).build().parseClaimsJws(token).getBody();
	}

	private Key getSigningKey(Boolean isAccessToken) {
		if (isAccessToken) {
			byte[] keyBytes = Decoders.BASE64.decode(accessTokenSecret);
			return Keys.hmacShaKeyFor(keyBytes);
		} else {
			byte[] keyBytes = Decoders.BASE64.decode(refreshTokenSecret);
			return Keys.hmacShaKeyFor(keyBytes);
		}
	}

	private Boolean isTokenExpired(String token, Boolean isAccessToken) {
		return extractExpiration(token, isAccessToken).before(new Date());
	}

	private Date extractExpiration(String token, Boolean isAccessToken) {
		return extractClaim(token, Claims::getExpiration, isAccessToken);
	}
}
