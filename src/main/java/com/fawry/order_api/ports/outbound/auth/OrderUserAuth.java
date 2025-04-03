package com.fawry.order_api.ports.outbound.auth;

public interface OrderUserAuth {
    Long parseUserId ();
    String parseUserEmail();
}
