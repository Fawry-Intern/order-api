package com.fawry.order_api.exception;

import com.fawry.order_api.domain.model.Money;

public class InvalidTotalAmountException extends OrderApiException{
    public InvalidTotalAmountException(Money expected, Money calculated) {
        super(String.format("Total amount does not match calculated total: expected %s, but calculated %s",
                expected.getAmount(), calculated.getAmount()));
    }
}
