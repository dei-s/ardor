// Auto generated code, do not modify
package nxt.http.callers;

import java.lang.String;
import nxt.http.APICall;

public class GetCoinExchangeOrderIdsCall extends APICall.Builder<GetCoinExchangeOrderIdsCall> {
    private GetCoinExchangeOrderIdsCall() {
        super("getCoinExchangeOrderIds");
    }

    public static GetCoinExchangeOrderIdsCall create(int chain) {
        GetCoinExchangeOrderIdsCall instance = new GetCoinExchangeOrderIdsCall();
        instance.param("chain", chain);
        return instance;
    }

    public GetCoinExchangeOrderIdsCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetCoinExchangeOrderIdsCall chain(String chain) {
        return param("chain", chain);
    }

    public GetCoinExchangeOrderIdsCall chain(int chain) {
        return param("chain", chain);
    }

    public GetCoinExchangeOrderIdsCall firstIndex(int firstIndex) {
        return param("firstIndex", firstIndex);
    }

    public GetCoinExchangeOrderIdsCall exchange(String exchange) {
        return param("exchange", exchange);
    }

    public GetCoinExchangeOrderIdsCall exchange(int exchange) {
        return param("exchange", exchange);
    }

    public GetCoinExchangeOrderIdsCall lastIndex(int lastIndex) {
        return param("lastIndex", lastIndex);
    }

    public GetCoinExchangeOrderIdsCall account(String account) {
        return param("account", account);
    }

    public GetCoinExchangeOrderIdsCall account(long account) {
        return unsignedLongParam("account", account);
    }

    public GetCoinExchangeOrderIdsCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }

    public GetCoinExchangeOrderIdsCall adminPassword(String adminPassword) {
        return param("adminPassword", adminPassword);
    }
}
