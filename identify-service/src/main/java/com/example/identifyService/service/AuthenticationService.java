package com.example.identifyService.service;

import com.example.identifyService.dto.request.AuthenticationRequest;
import com.example.identifyService.dto.request.IntrospectRequest;
import com.example.identifyService.dto.response.AuthenticationResponse;
import com.example.identifyService.dto.response.IntrospectResponse;
import com.example.identifyService.entity.InvalidatedToken;
import com.example.identifyService.entity.User;
import com.example.identifyService.exception.AppException;
import com.example.identifyService.exception.ErrorCode;
import com.example.identifyService.repository.InvalidatedTokenRepository;
import com.example.identifyService.repository.UserRepository;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.RequiredArgsConstructor;
import lombok.experimental.NonFinal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository userRepository;
    private final InvalidatedTokenRepository invalidatedTokenRepository;
    private final IdGeneratorService idGeneratorService;

    @NonFinal
    @Value("${jwt.signerKey}")
    protected String SIGNER_KEY;


    public AuthenticationResponse authenticate(AuthenticationRequest request){
        User user = userRepository.findByUsername(request.getUsername()).orElseThrow(()-> new AppException(ErrorCode.USER_NOT_FOUND));
        if(user.getIsActive() == false){
            throw new AppException(ErrorCode.NOT_ACTIVATE_YET);
        }
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        if(!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new AppException(ErrorCode.WRONG_PASSWORD);
        }
        return AuthenticationResponse.builder()
                .token(generateToken(user))
                .authenticated(true)
                .build();

    }

    public void Logout(IntrospectRequest request) throws ParseException, JOSEException {
        var signToken = verifyToken(request.getToken(),false);
        String accessTokenId = signToken.getJWTClaimsSet().getJWTID();
        Date accessTokenExpiryTime = signToken.getJWTClaimsSet().getExpirationTime();
        InvalidatedToken invalidatedToken= InvalidatedToken.builder()
                .id(accessTokenId)
                .expiryTime(accessTokenExpiryTime)
                .build();
        invalidatedTokenRepository.save(invalidatedToken);
    }

    public IntrospectResponse introspect(IntrospectRequest request) throws JOSEException, ParseException {
        var token = request.getToken();
        boolean isValid = true;

        try {
            verifyToken(token,false);
        } catch (AppException e) {
            isValid = false;
        }
        return IntrospectResponse.builder()
                .valid(isValid)
                .build();
    }

    public SignedJWT verifyToken(String token, boolean isRefresh) throws ParseException, JOSEException {
        JWSVerifier verifier = new MACVerifier(SIGNER_KEY.getBytes());
        SignedJWT signedJWT = SignedJWT.parse(token);

        boolean verified = signedJWT.verify(verifier);
        if (!verified) throw new AppException(ErrorCode.UNAUTHENTICATED);

        Date expiryTime = signedJWT.getJWTClaimsSet().getExpirationTime();
        if (expiryTime.before(new Date())) throw new AppException(ErrorCode.UNAUTHENTICATED);

        if (invalidatedTokenRepository.existsById(signedJWT.getJWTClaimsSet().getJWTID())) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        return signedJWT;
    }

    public String generateToken(User user) {
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);
        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .subject(user.getUsername())
                .jwtID(UUID.randomUUID().toString())
                .issuer("hoangvandien.id.vn")
                .issueTime(new Date())
                .expirationTime(Date.from(Instant.now().plus(1, ChronoUnit.HOURS)))
                .claim("scope",buildScope(user))
                .claim("type","access_token")
                .claim("userId",user.getUserId())
                .build();
        Payload payload = new Payload(claimsSet.toJSONObject());
        JWSObject jwsObject = new JWSObject(header, payload);
        try {
            jwsObject.sign(new MACSigner(SIGNER_KEY.getBytes()));
        } catch (JOSEException e) {
            throw new RuntimeException(e);
        }
        return jwsObject.serialize();
    }



    private String buildScope(User user){
        StringJoiner scope = new StringJoiner(" ");
        String role = user.getRole();
        if(role != null && !role.trim().isEmpty()){
            scope.add("ROLE_"+role.trim());
        }
        return scope.toString();
    }

    public AuthenticationResponse authenticateOAuth2(String email, String fullName, String avatar,String sub) {
        Optional<User> userOptional = userRepository.findByEmail(email);

        User user = userOptional.orElseGet(() -> {
            User newUser = User.builder()
                    .userId(idGeneratorService.generateRandomId("US", userRepository::existsById))
                    .role("USER")
                    .email(email)
                    .username(email)
                    .fullName(fullName)
                    .avatarUrl(avatar)
                    .googleId(sub)
                    .isActive(true)
                    .build();
            return userRepository.save(newUser);
        });

        if (user.getAvatarUrl() == null && avatar != null) {
            user.setAvatarUrl(avatar);
            userRepository.save(user);
        }

        return AuthenticationResponse.builder()
                .token(generateToken(user))
                .authenticated(true)
                .build();
    }
}
