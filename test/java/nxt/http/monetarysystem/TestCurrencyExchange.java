/*
 * Copyright © 2013-2016 The Nxt Core Developers.
 * Copyright © 2016-2019 Jelurida IP B.V.
 *
 * See the LICENSE.txt file at the top-level directory of this distribution
 * for licensing information.
 *
 * Unless otherwise agreed in a custom licensing agreement with Jelurida B.V.,
 * no part of this software, including this file, may be copied, modified,
 * propagated, or distributed except according to the terms contained in the
 * LICENSE.txt file.
 *
 * Removal or modification of this copyright notice is prohibited.
 *
 */

package nxt.http.monetarysystem;

import nxt.AccountCurrencyBalance;
import nxt.BlockchainTest;
import nxt.Tester;
import nxt.blockchain.ChildChain;
import nxt.http.APICall;
import nxt.ms.CurrencyType;
import nxt.util.Convert;
import nxt.util.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.junit.Assert;
import org.junit.Test;

public class TestCurrencyExchange extends BlockchainTest {

    @Test
    public void buyCurrency() {
        APICall apiCall1 = new TestCurrencyIssuance.Builder().type(CurrencyType.EXCHANGEABLE.getCode()).build();
        String currencyId = TestCurrencyIssuance.issueCurrencyApi(apiCall1);
        AccountCurrencyBalance initialSellerBalance = new AccountCurrencyBalance(ALICE.getSecretPhrase(), currencyId, ChildChain.IGNIS);
        AccountCurrencyBalance initialBuyerBalance = new AccountCurrencyBalance(BOB.getSecretPhrase(), currencyId, ChildChain.IGNIS);

        Assert.assertEquals(100000, initialSellerBalance.getCurrencyUnits());
        Assert.assertEquals(100000, initialSellerBalance.getUnconfirmedCurrencyUnits());

        JSONObject publishExchangeOfferResponse = publishExchangeOffer(currencyId);

        generateBlock();

        APICall apiCall = new APICall.Builder("getBuyOffers").param("currency", currencyId).build();
        JSONObject getAllOffersResponse = apiCall.invoke();
        Logger.logDebugMessage("getAllOffersResponse:" + getAllOffersResponse.toJSONString());
        JSONArray offer = (JSONArray)getAllOffersResponse.get("offers");
        Assert.assertEquals(Tester.responseToStringId(publishExchangeOfferResponse), ((JSONObject)offer.get(0)).get("offer"));

        // The buy offer reduces the unconfirmed balance but does not change the confirmed balance
        // The sell offer reduces the unconfirmed currency units and confirmed units
        AccountCurrencyBalance afterOfferSellerBalance = new AccountCurrencyBalance(ALICE.getSecretPhrase(), currencyId, ChildChain.IGNIS);
        Assert.assertEquals(new AccountCurrencyBalance(-1000*95 - ChildChain.IGNIS.ONE_COIN, -ChildChain.IGNIS.ONE_COIN, -500, 0),
                afterOfferSellerBalance.diff(initialSellerBalance));

        // buy at rate higher than sell offer results in selling at sell offer
        apiCall = new APICall.Builder("currencyBuy").
                secretPhrase(BOB.getSecretPhrase()).feeNQT(ChildChain.IGNIS.ONE_COIN).
                param("currency", currencyId).
                param("rateNQTPerUnit", "" + 106).
                param("unitsQNT", "200").
                build();
        JSONObject currencyExchangeResponse = apiCall.invoke();
        Logger.logDebugMessage("currencyExchangeResponse:" + currencyExchangeResponse);
        generateBlock();

        AccountCurrencyBalance afterBuySellerBalance = new AccountCurrencyBalance(ALICE.getSecretPhrase(), currencyId, ChildChain.IGNIS);
        Assert.assertEquals(new AccountCurrencyBalance(2000, 200 * 105, 0, -200),
                afterBuySellerBalance.diff(afterOfferSellerBalance));

        AccountCurrencyBalance afterBuyBuyerBalance = new AccountCurrencyBalance(BOB.getSecretPhrase(), currencyId, ChildChain.IGNIS);
        Assert.assertEquals(new AccountCurrencyBalance(-200*105 - ChildChain.IGNIS.ONE_COIN, -200*105 - ChildChain.IGNIS.ONE_COIN, 200, 200),
                afterBuyBuyerBalance.diff(initialBuyerBalance));

        apiCall = new APICall.Builder("getAllExchanges").build();
        JSONObject getAllExchangesResponse = apiCall.invoke();
        Logger.logDebugMessage("getAllExchangesResponse: " + getAllExchangesResponse);
        JSONArray exchanges = (JSONArray)getAllExchangesResponse.get("exchanges");
        JSONObject exchange = (JSONObject) exchanges.get(0);
        Assert.assertEquals("105", exchange.get("rateNQTPerUnit"));
        Assert.assertEquals("200", exchange.get("unitsQNT"));
        Assert.assertEquals(currencyId, exchange.get("currency"));
        Assert.assertEquals(initialSellerBalance.getAccountId(), Convert.parseUnsignedLong((String)exchange.get("seller")));
        Assert.assertEquals(initialBuyerBalance.getAccountId(), Convert.parseUnsignedLong((String)exchange.get("buyer")));
    }

    @Test
    public void sellCurrency() {
        APICall apiCall1 = new TestCurrencyIssuance.Builder().type(CurrencyType.EXCHANGEABLE.getCode()).build();
        String currencyId = TestCurrencyIssuance.issueCurrencyApi(apiCall1);
        AccountCurrencyBalance initialBuyerBalance = new AccountCurrencyBalance(ALICE.getSecretPhrase(), currencyId, ChildChain.IGNIS);
        AccountCurrencyBalance initialSellerBalance = new AccountCurrencyBalance(BOB.getSecretPhrase(), currencyId, ChildChain.IGNIS);

        Assert.assertEquals(100000, initialBuyerBalance.getCurrencyUnits());
        Assert.assertEquals(100000, initialBuyerBalance.getUnconfirmedCurrencyUnits());

        JSONObject publishExchangeOfferResponse = publishExchangeOffer(currencyId);

        generateBlock();

        APICall apiCall = new APICall.Builder("getSellOffers").param("currency", currencyId).build();
        JSONObject getAllOffersResponse = apiCall.invoke();
        Logger.logDebugMessage("getAllOffersResponse:" + getAllOffersResponse.toJSONString());
        JSONArray offer = (JSONArray)getAllOffersResponse.get("offers");
        Assert.assertEquals(Tester.responseToStringId(publishExchangeOfferResponse), ((JSONObject)offer.get(0)).get("offer"));

        // The buy offer reduces the unconfirmed balance but does not change the confirmed balance
        // The sell offer reduces the unconfirmed currency units and confirmed units
        AccountCurrencyBalance afterOfferBuyerBalance = new AccountCurrencyBalance(ALICE.getSecretPhrase(), currencyId, ChildChain.IGNIS);
        Assert.assertEquals(new AccountCurrencyBalance(-1000 * 95 - ChildChain.IGNIS.ONE_COIN, -ChildChain.IGNIS.ONE_COIN, -500, 0),
                afterOfferBuyerBalance.diff(initialBuyerBalance));

        // We now transfer 2000 units to the 2nd account so that this account can sell them for NXT
        apiCall = new APICall.Builder("transferCurrency").
                secretPhrase(ALICE.getSecretPhrase()).feeNQT(ChildChain.IGNIS.ONE_COIN).
                param("currency", currencyId).
                param("recipient", Long.toUnsignedString(initialSellerBalance.getAccountId())).
                param("unitsQNT", "2000").
                build();
        apiCall.invoke();
        generateBlock();

        AccountCurrencyBalance afterTransferBuyerBalance = new AccountCurrencyBalance(ALICE.getSecretPhrase(), currencyId, ChildChain.IGNIS);
        Assert.assertEquals(new AccountCurrencyBalance(-ChildChain.IGNIS.ONE_COIN, -ChildChain.IGNIS.ONE_COIN, -2000, -2000),
                afterTransferBuyerBalance.diff(afterOfferBuyerBalance));

        AccountCurrencyBalance afterTransferSellerBalance = new AccountCurrencyBalance(BOB.getSecretPhrase(), currencyId, ChildChain.IGNIS);
        Assert.assertEquals(new AccountCurrencyBalance(0, 0, 2000, 2000),
                afterTransferSellerBalance.diff(initialSellerBalance));

        // sell at rate lower than buy offer results in selling at buy offer rate (95)
        apiCall = new APICall.Builder("currencySell").
                secretPhrase(BOB.getSecretPhrase()).feeNQT(ChildChain.IGNIS.ONE_COIN).
                param("currency", currencyId).
                param("rateNQTPerUnit", "" + 90).
                param("unitsQNT", "200").
                build();
        JSONObject currencyExchangeResponse = apiCall.invoke();
        Logger.logDebugMessage("currencyExchangeResponse:" + currencyExchangeResponse);
        generateBlock();

        // the seller receives 200*95=19000 for 200 units
        AccountCurrencyBalance afterBuyBuyerBalance = new AccountCurrencyBalance(ALICE.getSecretPhrase(), currencyId, ChildChain.IGNIS);
        Assert.assertEquals(new AccountCurrencyBalance(0, -19000, 0, 200),
                afterBuyBuyerBalance.diff(afterTransferBuyerBalance));

        AccountCurrencyBalance afterBuySellerBalance = new AccountCurrencyBalance(BOB.getSecretPhrase(), currencyId, ChildChain.IGNIS);
        Assert.assertEquals(new AccountCurrencyBalance(19000- ChildChain.IGNIS.ONE_COIN, 19000- ChildChain.IGNIS.ONE_COIN, -200, -200),
                afterBuySellerBalance.diff(afterTransferSellerBalance));

        apiCall = new APICall.Builder("getAllExchanges").build();
        JSONObject getAllExchangesResponse = apiCall.invoke();
        Logger.logDebugMessage("getAllExchangesResponse: " + getAllExchangesResponse);
        JSONArray exchanges = (JSONArray)getAllExchangesResponse.get("exchanges");
        JSONObject exchange = (JSONObject) exchanges.get(0);
        Assert.assertEquals("95", exchange.get("rateNQTPerUnit"));
        Assert.assertEquals("200", exchange.get("unitsQNT"));
        Assert.assertEquals(currencyId, exchange.get("currency"));
        Assert.assertEquals(initialSellerBalance.getAccountId(), Convert.parseUnsignedLong((String) exchange.get("seller")));
        Assert.assertEquals(initialBuyerBalance.getAccountId(), Convert.parseUnsignedLong((String)exchange.get("buyer")));
    }

    private JSONObject publishExchangeOffer(String currencyId) {
        APICall apiCall = new APICall.Builder("publishExchangeOffer").
                secretPhrase(ALICE.getSecretPhrase()).feeNQT(ChildChain.IGNIS.ONE_COIN).
                param("deadline", "1440").
                param("currency", currencyId).
                param("buyRateNQTPerUnit", "" + 95). // buy currency for NXT
                param("sellRateNQTPerUnit", "" + 105). // sell currency for NXT
                param("totalBuyLimitQNT", "10000").
                param("totalSellLimitQNT", "5000").
                param("initialBuySupplyQNT", "1000").
                param("initialSellSupplyQNT", "500").
                param("expirationHeight", "" + Integer.MAX_VALUE).
                build();

        JSONObject publishExchangeOfferResponse = apiCall.invoke();
        Logger.logDebugMessage("publishExchangeOfferResponse: " + publishExchangeOfferResponse.toJSONString());
        return publishExchangeOfferResponse;
    }


}
