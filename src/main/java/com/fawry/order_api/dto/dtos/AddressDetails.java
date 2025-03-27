package com.fawry.order_api.dto.dtos;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class AddressDetails {
    private String governorate;
    private String city;
    private String address;
}
