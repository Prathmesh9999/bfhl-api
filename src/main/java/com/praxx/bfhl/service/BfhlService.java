package com.praxx.bfhl.service;

import com.praxx.bfhl.dto.BfhlRequest;
import com.praxx.bfhl.dto.BfhlResponseContract;

public interface BfhlService {

    BfhlResponseContract process(BfhlRequest request, String requestId);
}
