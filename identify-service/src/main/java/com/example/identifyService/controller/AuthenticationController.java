package com.example.identifyService.controller;

import com.example.identifyService.dto.request.AuthenticationRequest;
import com.example.identifyService.dto.request.IntrospectRequest;
import com.example.identifyService.dto.response.ApiResponse;
import com.example.identifyService.dto.response.AuthenticationResponse;
import com.example.identifyService.dto.response.IntrospectResponse;
import com.example.identifyService.service.AuthenticationService;
import com.example.identifyService.service.UserService;
import com.nimbusds.jose.JOSEException;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.text.ParseException;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthenticationController {
    private final AuthenticationService authenticationService;
    private final UserService userService;

    @PostMapping
    ApiResponse<AuthenticationResponse> login(@RequestBody AuthenticationRequest request, HttpServletResponse response) {
        AuthenticationResponse authenticationResponse = authenticationService.authenticate(request);
        ResponseCookie cookie = ResponseCookie.from("token", authenticationResponse.getToken())
                .secure(false)
                .httpOnly(true)
                .path("/")
                .maxAge(30*24*60*60)
                .build();
        response.setHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        return ApiResponse.<AuthenticationResponse>builder()
                .result(authenticationResponse)
                .build();
    }

    @PostMapping("/introspect")
    ApiResponse<IntrospectResponse> introspect(@RequestBody IntrospectRequest request)
            throws ParseException, JOSEException {
        var result = authenticationService.introspect(request);
        return ApiResponse.<IntrospectResponse>builder()
                .result(result)
                .build();
    }

    @GetMapping("/oauth2/success")
    public void oauth2Success(
            Authentication authentication,
            HttpServletResponse response
    ) throws IOException {
        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
        Map<String, Object> attributes = oauthToken.getPrincipal().getAttributes();

        String email = (String) attributes.get("email");
        String fullName = (String) attributes.get("name");
        String avatar = (String) attributes.get("picture");
        String sub = (String) attributes.get("sub");

        AuthenticationResponse authResponse =
                authenticationService.authenticateOAuth2(email, fullName, avatar, sub);

        ResponseCookie cookie = ResponseCookie.from("token", authResponse.getToken())
                .secure(false)
                .httpOnly(false)
                .path("/")
                .sameSite("Lax")
                .maxAge(30 * 24 * 60 * 60)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        response.sendRedirect("http://localhost:5173/chat");

    }

    @GetMapping("/oauth2/failure")
    public ApiResponse<String> oauth2Failure() {
        return ApiResponse.<String>builder()
                .code(200)
                .result("OAuth2 login failed")
                .build();
    }


    @PostMapping("/logout")
    ApiResponse<Void> logout(@RequestBody IntrospectRequest request) throws ParseException, JOSEException {
        authenticationService.Logout(request);
        return ApiResponse.<Void>builder()
                .build();
    }

    @GetMapping("/active")
    public void active(@RequestParam("token") String token, HttpServletResponse response) throws ParseException, JOSEException, IOException {
        var signToken = authenticationService.verifyToken(token,false);
        if(signToken != null){
            userService.activateUser(token);
            response.sendRedirect("http://localhost:5173/");
        }
    }
}
