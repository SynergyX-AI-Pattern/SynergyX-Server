package com.synergyx.trading.apiPayload.exception.handler;

import com.synergyx.trading.apiPayload.code.BaseErrorCode;
import com.synergyx.trading.apiPayload.exception.GeneralException;

public class TempHandler extends GeneralException {

    public TempHandler(BaseErrorCode errorCode) {
        super(errorCode);
    }
}