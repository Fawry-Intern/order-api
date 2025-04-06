package com.fawry.order_api.dto.dtos;

import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;

@Builder
@Getter
public class AddressDetails implements Serializable {
    private String governorate;
    private String city;
    private String address;
}
