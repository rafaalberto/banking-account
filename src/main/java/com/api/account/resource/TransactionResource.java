package com.api.account.resource;

import com.api.account.exception.BusinessException;
import com.api.account.model.Message;
import com.api.account.model.Transaction;
import com.api.account.service.TransactionFactory;
import com.fasterxml.jackson.core.type.TypeReference;
import io.undertow.server.HttpServerExchange;

import static com.api.account.utils.HttpUtils.*;
import static com.api.account.utils.JsonConverter.convertToJson;
import static com.api.account.utils.JsonConverter.readFromJson;

public class TransactionResource {

    public static void execute(HttpServerExchange exchange) {
        handleStatusAndHeaders(exchange, HTTP_CREATED_STATUS);
        exchange.getRequestReceiver().receiveFullString((serverExchange, message) -> {
            try {
                Transaction transaction = readFromJson(message, new TypeReference<>() {});
                if (transaction != null) {
                    var transactionType = transaction.getType();
                    TransactionFactory.getService(transactionType).execute(transaction);
                    Message messageToSend = new Message(true, transactionType.getDescription() + " executed successfully");
                    exchange.getResponseSender().send(convertToJson(messageToSend));
                }
            } catch(BusinessException e) {
                handleApplicationException(exchange, e);
            } catch(Exception e) {
                handleApplicationException(exchange, e);
            }
        });
    }
}
