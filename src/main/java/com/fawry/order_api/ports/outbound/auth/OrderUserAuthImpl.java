package com.fawry.order_api.ports.outbound.auth;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class OrderUserAuthImpl implements OrderUserAuth {

    private final HttpServletRequest httpServletRequest;

    @Override
    public Long parseUserId() {
//        String userId=httpServletRequest.getHeader("UserId");
//
//        if (userId == null) {
//            throw new IllegalActionException("UserId header is missing");
//        }
//
//        Long authUserId;
//
//        try {
//            authUserId = Long.parseLong(userId);
//        } catch (NumberFormatException e) {
//            throw new IllegalActionException("Invalid UserId format");
//        }
//        return authUserId;
        return 1L;
    }

    @Override
    public String parseUserEmail() {
//        String userEmail = httpServletRequest.getHeader("Email");
//
//        if (Objects.equals(userEmail, null))
//            throw new AuthenticationUserException("Email header is missing");
//
//        return userEmail;
        return "muhammadhussein2312@gmail.com";
    }
}
