package com.api.account.utils;

import com.api.account.exception.BusinessException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

import static com.api.account.utils.HttpUtils.*;

public final class JsonConverter {

    public static String convertToJson(Object data) throws Exception {
        var mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(data);
        } catch (BusinessException e) {
            throw  new BusinessException(HTTP_BAD_REQUEST_STATUS, e.getMessage());
        } catch (JsonProcessingException e) {
            throw new Exception(e);
        }
    }

    public static <T> T readFromJson(String json, TypeReference<T> typeReference) throws Exception {
        var mapper = new ObjectMapper();
        try {
            return mapper.readValue(json, typeReference);
        } catch (BusinessException e) {
            throw  new BusinessException(HTTP_BAD_REQUEST_STATUS, e.getMessage());
        } catch (IOException e) {
            throw new Exception(e);
        }
    }
}
