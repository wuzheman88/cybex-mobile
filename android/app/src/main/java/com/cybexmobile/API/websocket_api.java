package com.cybexmobile.API;


import android.text.TextUtils;
import android.util.Log;

import com.cybexmobile.Exception.NetworkStatusException;
import com.cybexmobile.graphene.chain.FullNodeServerSelect;
import com.cybexmobile.graphene.chain.account_object;
import com.cybexmobile.graphene.chain.asset;
import com.cybexmobile.graphene.chain.asset_object;
import com.cybexmobile.graphene.chain.bucket_object;
import com.cybexmobile.graphene.chain.full_account_object;
import com.cybexmobile.graphene.chain.global_config_object;
import com.cybexmobile.graphene.chain.limit_order_object;
import com.cybexmobile.graphene.chain.object_id;
import com.cybexmobile.Market.MarketStat;
import com.cybexmobile.Market.MarketTicker;
import com.cybexmobile.Market.MarketTrade;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

import static com.cybexmobile.Constant.ErrorCode.ERROR_CONNECT_SERVER_FAILD;

public class websocket_api extends WebSocketListener {

    private int _nDatabaseId = -1;
    private int _nHistoryId = -1;
    private int _nBroadcastId = -1;

    private OkHttpClient mOkHttpClient;
    private WebSocket mWebsocket;

    private int mnConnectStatus = WEBSOCKET_CONNECT_INVALID;
    private static int WEBSOCKET_CONNECT_INVALID = -1;
    private static int WEBSOCKET_CONNECT_SUCCESS = 0;
    private static int WEBSOCKET_ALL_READY = 0;
    private static int WEBSOCKET_CONNECT_FAIL = 1;

    private AtomicInteger mnCallId = new AtomicInteger(1);
    private HashMap<Integer, IReplyObjectProcess> mHashMapIdToProcess = new HashMap<>();

    /*
     WS_NODE_LIST: [
        {url: "wss://fake.automatic-selection.com", location: {translate: "settings.api_closest"}},
        {url: "ws://127.0.0.1:8090", location: "Locally hosted"},
        {url: "wss://bitshares.openledger.info/ws", location: "Nuremberg, Germany"},
        {url: "wss://eu.openledger.info/ws", location: "Berlin, Germany"},
        {url: "wss://bit.btsabc.org/ws", location: "Hong Kong"},
        {url: "wss://bts.transwiser.com/ws", location: "Hangzhou, China"},
        {url: "wss://bitshares.dacplay.org/ws", location:  "Hangzhou, China"},
        {url: "wss://bitshares-api.wancloud.io/ws", location:  "China"},
        {url: "wss://openledger.hk/ws", location: "Hong Kong"},
        {url: "wss://secure.freedomledger.com/ws", location: "Toronto, Canada"},
        {url: "wss://dexnode.net/ws", location: "Dallas, USA"},
        {url: "wss://altcap.io/ws", location: "Paris, France"},
        {url: "wss://bitshares.crypto.fans/ws", location: "Munich, Germany"},
        {url: "wss://node.testnet.bitshares.eu", location: "Public Testnet Server (Frankfurt, Germany)"}
         */

    private List<String> mListNode = Arrays.asList(
            "wss://bitshares.openledger.info/ws",
            "wss://eu.openledger.info/ws",
            "wss://bit.btsabc.org/ws",
            "wss://bts.transwiser.com/ws",
            "wss://bitshares.dacplay.org/ws",
            "wss://bitshares-api.wancloud.io/ws",
            "wss://openledger.hk/ws",
            "wss://secure.freedomledger.com/ws",
            "wss://dexnode.net/ws",
            "wss://altcap.io/ws",
            "wss://bitshares.crypto.fans/ws"
    );

    class WebsocketError {
        int code;
        String message;
        Object data;
    }

    class Call {
        int id;
        String method;
        List<Object> params;
    }

    class Reply<T> {
        String id;
        String jsonrpc;
        T result;
        WebsocketError error;
    }

    class ReplyBase {
        int id;
        String jsonrpc;
    }

    private interface IReplyObjectProcess<T> {
        void processTextToObject(String strText);

        T getReplyObject();

        String getError();

        void notifyFailure(Throwable t);

        Throwable getException();

        String getResponse();
    }

    private class ReplyObjectProcess<T> implements IReplyObjectProcess<T> {
        private String strError;
        private T mT;
        private Type mType;
        private Throwable exception;
        private String strResponse;

        public ReplyObjectProcess(Type type) {
            mType = type;
        }

        public void processTextToObject(String strText) {
            try {
                Gson gson = global_config_object.getInstance().getGsonBuilder().create();
                mT = gson.fromJson(strText, mType);
            } catch (JsonSyntaxException e) {
                e.printStackTrace();
                strError = e.getMessage();
                strResponse = strText;
            } catch (Exception e) {
                e.printStackTrace();
                strError = e.getMessage();
                strResponse = strText;
            }
            synchronized (this) {
                notify();
            }
        }

        @Override
        public T getReplyObject() {
            return mT;
        }

        @Override
        public String getError() {
            return strError;
        }

        @Override
        public void notifyFailure(Throwable t) {
            exception = t;
            synchronized (this) {
                notify();
            }
        }

        @Override
        public Throwable getException() {
            return exception;
        }

        @Override
        public String getResponse() {
            return strResponse;
        }
    }

    @Override
    public void onClosed(WebSocket webSocket, int code, String reason) {
        Log.e("Shefeng", "socket is disconnected");
        super.onClosed(webSocket, code, reason);
    }

    @Override
    public void onClosing(WebSocket webSocket, int code, String reason) {
        Log.e("Shefeng", "socket is disconnecting");
        super.onClosing(webSocket, code, reason);
    }

    @Override
    public void onMessage(WebSocket webSocket, ByteString bytes) {
        super.onMessage(webSocket, bytes);
    }

    @Override
    public void onOpen(WebSocket webSocket, Response response) {
        Log.e("Shefeng", "socket is connected");
        synchronized (mWebsocket) {
            mnConnectStatus = WEBSOCKET_CONNECT_SUCCESS;
            mWebsocket.notify();
        }
    }

    @Override
    public void onFailure(WebSocket webSocket, Throwable t, Response response) {
        Log.e("onFailure", "Failure");
        if (t instanceof IOException) {  // 出现io错误
            synchronized (mWebsocket) {
                mnConnectStatus = WEBSOCKET_CONNECT_FAIL;
                mWebsocket.notify();
            }
            synchronized (mHashMapIdToProcess) {
                for (Map.Entry<Integer, IReplyObjectProcess> entry : mHashMapIdToProcess.entrySet()) {
                    entry.getValue().notifyFailure(t);
                }
                mHashMapIdToProcess.clear();
            }
            connect();
            //EventBus.getDefault().post("onReconnect");
        }
    }

    @Override
    public void onMessage(WebSocket webSocket, String text) {
        //super.onMessage(webSocket, text);
        Log.d("webSocket_Response", text);
        Log.e("receive", "receive Message");

        try {
            Gson gson = new Gson();
            ReplyBase replyObjectBase = gson.fromJson(text, ReplyBase.class);

            IReplyObjectProcess iReplyObjectProcess = null;
            synchronized (mHashMapIdToProcess) {
                if (mHashMapIdToProcess.containsKey(replyObjectBase.id)) {
                    iReplyObjectProcess = mHashMapIdToProcess.get(replyObjectBase.id);
                }
            }

            if (iReplyObjectProcess != null) {
                iReplyObjectProcess.processTextToObject(text);
            } else {
                try {
                    JSONObject noticeObject = new JSONObject(text);
                    JSONArray params = noticeObject.getJSONArray("params");
                    int id = params.getInt(0);
                    EventBus.getDefault().post(String.valueOf(id));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
        }
    }

    public synchronized int connect() {
        if (mnConnectStatus == WEBSOCKET_ALL_READY) {
            return 0;
        }

        FullNodeServerSelect fullNodeServerSelect = new FullNodeServerSelect();
        String strServer = fullNodeServerSelect.getServer();
        Log.e("shefeng", strServer);
        if (TextUtils.isEmpty(strServer)) {
            return ERROR_CONNECT_SERVER_FAILD;
        }

        Request request = new Request.Builder().url(strServer).build();
        mOkHttpClient = new OkHttpClient();
        mWebsocket = mOkHttpClient.newWebSocket(request, this);
        synchronized (mWebsocket) {
            if (mnConnectStatus == WEBSOCKET_CONNECT_INVALID) {
                try {
                    mWebsocket.wait(10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                if (mnConnectStatus != WEBSOCKET_CONNECT_SUCCESS) {
                    return ERROR_CONNECT_SERVER_FAILD;
                }
            }
        }

        int nRet = 0;
        try {
            boolean bLogin = login("", "");
            if (bLogin == true) {
                _nDatabaseId = get_websocket_bitshares_api_id("database");
                _nHistoryId = get_websocket_bitshares_api_id("history");
                _nBroadcastId = get_websocket_bitshares_api_id("network_broadcast");
            } else {
                nRet = ERROR_CONNECT_SERVER_FAILD;
            }
        } catch (NetworkStatusException e) {
            e.printStackTrace();
            nRet = ERROR_CONNECT_SERVER_FAILD;
        }

//        try {
//            asset_object base = lookup_asset_symbols("BTS");
//            asset_object quote = lookup_asset_symbols("CNY");
//            subscribe_to_market(base.id, quote.id);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

        if (nRet != 0) {
            mWebsocket.close(1000, "");
            mWebsocket = null;
            mnConnectStatus = WEBSOCKET_CONNECT_INVALID;
        } else {
            mnConnectStatus = WEBSOCKET_ALL_READY;
        }

        return nRet;
    }

    public synchronized int close() {
        synchronized (mHashMapIdToProcess) {
            for (Map.Entry<Integer, IReplyObjectProcess> entry : mHashMapIdToProcess.entrySet()) {
                synchronized (entry.getValue()) {
                    entry.getValue().notify();
                }
            }
        }


        mWebsocket.close(1000, "Close");
        mOkHttpClient = null;
        mWebsocket = null;
        mnConnectStatus = WEBSOCKET_CONNECT_INVALID;

        _nDatabaseId = -1;
        _nBroadcastId = -1;
        _nHistoryId = -1;

        return 0;
    }

    private boolean login(String strUserName, String strPassword) throws NetworkStatusException {
        Call callObject = new Call();

        callObject.id = mnCallId.getAndIncrement();
        ;
        callObject.method = "call";
        callObject.params = new ArrayList<>();
        callObject.params.add(1);
        callObject.params.add("login");

        List<Object> listLoginParams = new ArrayList<>();
        listLoginParams.add(strUserName);
        listLoginParams.add(strPassword);
        callObject.params.add(listLoginParams);

        ReplyObjectProcess<Reply<Boolean>> replyObject =
                new ReplyObjectProcess<>(new TypeToken<Reply<Boolean>>() {
                }.getType());
        Reply<Boolean> replyLogin = sendForReplyImpl(callObject, replyObject);


        return replyLogin.result;
    }

    private int get_websocket_bitshares_api_id(String strApiName) throws NetworkStatusException {
        Call callObject = new Call();
        callObject.id = mnCallId.getAndIncrement();
        callObject.method = "call";
        callObject.params = new ArrayList<>();
        callObject.params.add(1);
        callObject.params.add(strApiName);

        List<Object> listDatabaseParams = new ArrayList<>();
        callObject.params.add(listDatabaseParams);

        ReplyObjectProcess<Reply<Integer>> replyObject =
                new ReplyObjectProcess<>(new TypeToken<Reply<Integer>>() {
                }.getType());
        Reply<Integer> replyApiId = sendForReplyImpl(callObject, replyObject);

        return replyApiId.result;
    }

    private int get_database_api_id() throws NetworkStatusException {
        _nDatabaseId = get_websocket_bitshares_api_id("database");
        return _nDatabaseId;
    }

    private int get_history_api_id() throws NetworkStatusException {
        _nHistoryId = get_websocket_bitshares_api_id("history");
        return _nHistoryId;
    }

    private int get_broadcast_api_id() throws NetworkStatusException {
        _nBroadcastId = get_websocket_bitshares_api_id("network_broadcast");
        return _nBroadcastId;
    }

//    public sha256_object get_chain_id() throws NetworkStatusException {
//        Call callObject = new Call();
//        callObject.id = mnCallId.getAndIncrement();
//        callObject.method = "call";
//        callObject.params = new ArrayList<>();
//        callObject.params.add(_nDatabaseId);
//        callObject.params.add("get_chain_id");
//
//        List<Object> listDatabaseParams = new ArrayList<>();
//
//        callObject.params.add(listDatabaseParams);
//
//        ReplyObjectProcess<Reply<sha256_object>> replyObject =
//                new ReplyObjectProcess<>(new TypeToken<Reply<sha256_object>>(){}.getType());
//        Reply<sha256_object> replyDatabase = sendForReply(callObject, replyObject);
//
//        return replyDatabase.result;
//    }

    public List<account_object> lookup_account_names(String strAccountName) throws NetworkStatusException {
        Call callObject = new Call();
        callObject.id = mnCallId.getAndIncrement();
        callObject.method = "call";
        callObject.params = new ArrayList<>();
        callObject.params.add(_nDatabaseId);
        callObject.params.add("lookup_account_names");

        List<Object> listAccountNames = new ArrayList<>();
        listAccountNames.add(strAccountName);

        List<Object> listAccountNamesParams = new ArrayList<>();
        listAccountNamesParams.add(listAccountNames);

        callObject.params.add(listAccountNamesParams);

        ReplyObjectProcess<Reply<List<account_object>>> replyObject =
                new ReplyObjectProcess<>(new TypeToken<Reply<List<account_object>>>() {
                }.getType());
        Reply<List<account_object>> replyAccountObjectList = sendForReply(callObject, replyObject);

        return replyAccountObjectList.result;
    }

    public List<account_object> get_accounts(List<object_id<account_object>> listAccountObjectId) throws NetworkStatusException {
        Call callObject = new Call();
        callObject.id = mnCallId.getAndIncrement();
        callObject.method = "call";
        callObject.params = new ArrayList<>();
        callObject.params.add(_nDatabaseId);
        callObject.params.add("get_accounts");

        List<Object> listAccountIds = new ArrayList<>();
        listAccountIds.add(listAccountObjectId);

        List<Object> listAccountNamesParams = new ArrayList<>();
        listAccountNamesParams.add(listAccountIds);

        callObject.params.add(listAccountIds);
        ReplyObjectProcess<Reply<List<account_object>>> replyObject =
                new ReplyObjectProcess<>(new TypeToken<Reply<List<account_object>>>() {
                }.getType());
        Reply<List<account_object>> replyAccountObjectList = sendForReply(callObject, replyObject);

        return replyAccountObjectList.result;
    }

    public List<asset> list_account_balances(object_id<account_object> accountId) throws NetworkStatusException {
        Call callObject = new Call();
        callObject.id = mnCallId.getAndIncrement();
        callObject.method = "call";
        callObject.params = new ArrayList<>();
        callObject.params.add(_nDatabaseId);
        callObject.params.add("get_account_balances");

        List<Object> listAccountBalancesParam = new ArrayList<>();
        listAccountBalancesParam.add(accountId);
        listAccountBalancesParam.add(new ArrayList<Object>());
        callObject.params.add(listAccountBalancesParam);


        ReplyObjectProcess<Reply<List<asset>>> replyObject =
                new ReplyObjectProcess<>(new TypeToken<Reply<List<asset>>>() {
                }.getType());
        Reply<List<asset>> replyLookupAccountNames = sendForReply(callObject, replyObject);

        return replyLookupAccountNames.result;
    }

//    public List<operation_history_object> get_account_history(object_id<account_object> accountId, int nLimit) throws NetworkStatusException {
//        Call callObject = new Call();
//        callObject.id = mnCallId.getAndIncrement();
//        callObject.method = "call";
//        callObject.params = new ArrayList<>();
//        callObject.params.add(_nHistoryId);
//        callObject.params.add("get_account_history");
//
//        List<Object> listAccountHistoryParam = new ArrayList<>();
//        listAccountHistoryParam.add(accountId);
//        listAccountHistoryParam.add("1.11.0");
//        listAccountHistoryParam.add(nLimit);
//        listAccountHistoryParam.add("1.11.0");
//        callObject.params.add(listAccountHistoryParam);
//
//        ReplyObjectProcess<Reply<List<operation_history_object>>> replyObject =
//                new ReplyObjectProcess<>(new TypeToken<Reply<List<operation_history_object>>>(){}.getType());
//        Reply<List<operation_history_object>> replyAccountHistory = sendForReply(callObject, replyObject);
//
//        return replyAccountHistory.result;
//    }

//    public global_property_object get_global_properties() throws NetworkStatusException {
//        Call callObject = new Call();
//        callObject.id = mnCallId.getAndIncrement();
//        callObject.method = "call";
//        callObject.params = new ArrayList<>();
//        callObject.params.add(_nDatabaseId);
//        callObject.params.add("get_global_properties");
//
//        callObject.params.add(new ArrayList<>());
//
//        ReplyObjectProcess<Reply<global_property_object>> replyObjectProcess =
//                new ReplyObjectProcess<>(new TypeToken<Reply<global_property_object>>(){}.getType());
//        Reply<global_property_object> replyObject = sendForReply(callObject, replyObjectProcess);
//
//        return replyObject.result;
//    }

//    public dynamic_global_property_object get_dynamic_global_properties() throws NetworkStatusException {
//        Call callObject = new Call();
//        callObject.id = mnCallId.getAndIncrement();
//        callObject.method = "call";
//        callObject.params = new ArrayList<>();
//        callObject.params.add(_nDatabaseId);
//        callObject.params.add("get_dynamic_global_properties");
//
//        callObject.params.add(new ArrayList<Object>());
//
//        ReplyObjectProcess<Reply<dynamic_global_property_object>> replyObjectProcess =
//                new ReplyObjectProcess<>(new TypeToken<Reply<dynamic_global_property_object>>(){}.getType());
//        Reply<dynamic_global_property_object> replyObject = sendForReply(callObject, replyObjectProcess);
//
//        return replyObject.result;
//
//    }

    public List<asset_object> list_assets(String strLowerBound, int nLimit) throws NetworkStatusException {
        Call callObject = new Call();
        callObject.id = mnCallId.getAndIncrement();
        callObject.method = "call";
        callObject.params = new ArrayList<>();
        callObject.params.add(_nDatabaseId);
        callObject.params.add("list_assets");

        List<Object> listAssetsParam = new ArrayList<>();
        listAssetsParam.add(strLowerBound);
        listAssetsParam.add(nLimit);
        callObject.params.add(listAssetsParam);

        ReplyObjectProcess<Reply<List<asset_object>>> replyObjectProcess =
                new ReplyObjectProcess<>(new TypeToken<Reply<List<asset_object>>>() {
                }.getType());
        Reply<List<asset_object>> replyObject = sendForReply(callObject, replyObjectProcess);

        return replyObject.result;
    }

    public List<asset_object> get_assets(List<object_id<asset_object>> listAssetObjectId) throws NetworkStatusException {
        Call callObject = new Call();
        callObject.id = mnCallId.getAndIncrement();
        callObject.method = "call";
        callObject.params = new ArrayList<>();
        callObject.params.add(_nDatabaseId);
        callObject.params.add("get_assets");

        List<Object> listAssetsParam = new ArrayList<>();

        List<Object> listObjectId = new ArrayList<>();
        listObjectId.addAll(listAssetObjectId);

        listAssetsParam.add(listObjectId);
        callObject.params.add(listAssetsParam);

        ReplyObjectProcess<Reply<List<asset_object>>> replyObjectProcess =
                new ReplyObjectProcess<>(new TypeToken<Reply<List<asset_object>>>() {
                }.getType());
        Reply<List<asset_object>> replyObject = sendForReply(callObject, replyObjectProcess);

        return replyObject.result;
    }

    public asset_object lookup_asset_symbols(String strAssetSymbol) throws NetworkStatusException {
        Call callObject = new Call();
        callObject.id = mnCallId.getAndIncrement();
        callObject.method = "call";
        callObject.params = new ArrayList<>();
        callObject.params.add(2);
        callObject.params.add("lookup_asset_symbols");

        List<Object> listAssetsParam = new ArrayList<>();

        List<Object> listAssetSysmbols = new ArrayList<>();
        listAssetSysmbols.add(strAssetSymbol);

        listAssetsParam.add(listAssetSysmbols);
        callObject.params.add(listAssetsParam);

        ReplyObjectProcess<Reply<List<asset_object>>> replyObjectProcess =
                new ReplyObjectProcess<>(new TypeToken<Reply<List<asset_object>>>() {
                }.getType());
        Reply<List<asset_object>> replyObject = sendForReply(callObject, replyObjectProcess);

        return replyObject.result.get(0);
    }

    public asset_object get_object(String object_id) throws NetworkStatusException {
        Call callObject = new Call();
        callObject.id = mnCallId.getAndIncrement();
        callObject.method = "call";
        callObject.params = new ArrayList<>();
        callObject.params.add(2);
        callObject.params.add("get_objects");

        List<Object> listObjectParams = new ArrayList<>();
        List<Object> listObjectIds = new ArrayList<>();
        listObjectIds.add(object_id);
        listObjectParams.add(listObjectIds);
        callObject.params.add(listObjectParams);
        ReplyObjectProcess<Reply<List<asset_object>>> replyObjectProcess =
                new ReplyObjectProcess<>(new TypeToken<Reply<List<asset_object>>>() {
                }.getType());
        Reply<List<asset_object>> replyObject = sendForReply(callObject, replyObjectProcess);

        return replyObject.result.get(0);
    }

//    public block_header get_block_header(int nBlockNumber) throws NetworkStatusException {
//        Call callObject = new Call();
//        callObject.id = mnCallId.getAndIncrement();
//        callObject.method = "call";
//        callObject.params = new ArrayList<>();
//        callObject.params.add(_nDatabaseId);
//        callObject.params.add("get_block_header");
//        List<Object> listBlockNumber = new ArrayList<>();
//        listBlockNumber.add(nBlockNumber);
//        callObject.params.add(listBlockNumber);
//
//        ReplyObjectProcess<Reply<block_header>> replyObjectProcess =
//                new ReplyObjectProcess<>(new TypeToken<Reply<block_header>>(){}.getType());
//        Reply<block_header> replyObject = sendForReply(callObject, replyObjectProcess);
//
//        return replyObject.result;
//
//    }


//    public int broadcast_transaction(signed_transaction tx) throws NetworkStatusException {
//        Call callObject = new Call();
//        callObject.id = mnCallId.getAndIncrement();
//        callObject.method = "call";
//        callObject.params = new ArrayList<>();
//        callObject.params.add(_nBroadcastId);
//        callObject.params.add("broadcast_transaction");
//        List<Object> listTransaction = new ArrayList<>();
//        listTransaction.add(tx);
//        callObject.params.add(listTransaction);
//
//        ReplyObjectProcess<Reply<Object>> replyObjectProcess =
//                new ReplyObjectProcess<>(new TypeToken<Reply<Integer>>(){}.getType());
//        Reply<Object> replyObject = sendForReply(callObject, replyObjectProcess);
//        if (replyObject.error != null) {
//            return -1;
//        } else {
//            return 0;
//        }
//    }

    public List<bucket_object> get_market_history(object_id<asset_object> assetObjectId1,
                                                  object_id<asset_object> assetObjectId2,
                                                  int nBucket,
                                                  Date dateStart,
                                                  Date dateEnd) throws NetworkStatusException {
        Call callObject = new Call();
        callObject.id = mnCallId.getAndIncrement();
        callObject.method = "call";
        callObject.params = new ArrayList<>();
        callObject.params.add(3);
        callObject.params.add("get_market_history");

        List<Object> listParams = new ArrayList<>();
        listParams.add(assetObjectId1);
        listParams.add(assetObjectId2);
        listParams.add(nBucket);
        listParams.add(dateStart);
        listParams.add(dateEnd);
        callObject.params.add(listParams);

        ReplyObjectProcess<Reply<List<bucket_object>>> replyObjectProcess =
                new ReplyObjectProcess<>(new TypeToken<Reply<List<bucket_object>>>() {
                }.getType());
        Reply<List<bucket_object>> replyObject = sendForReply(callObject, replyObjectProcess);

        return replyObject.result;

    }


    public List<limit_order_object> get_limit_orders(object_id<asset_object> base,
                                                     object_id<asset_object> quote,
                                                     int limit) throws NetworkStatusException {
        Call callObject = new Call();
        callObject.id = mnCallId.getAndIncrement();
        callObject.method = "call";
        callObject.params = new ArrayList<>();
        callObject.params.add(_nDatabaseId);
        callObject.params.add("get_limit_orders");

        List<Object> listParams = new ArrayList<>();
        listParams.add(base);
        listParams.add(quote);
        listParams.add(limit);
        callObject.params.add(listParams);

        ReplyObjectProcess<Reply<List<limit_order_object>>> replyObjectProcess =
                new ReplyObjectProcess<>(new TypeToken<Reply<List<limit_order_object>>>() {
                }.getType());
        Reply<List<limit_order_object>> replyObject = sendForReply(callObject, replyObjectProcess);

        return replyObject.result;
    }

    public String subscribe_to_market(String base, String quote)
            throws NetworkStatusException {
        Call callObject = new Call();
        callObject.id = mnCallId.getAndIncrement();
        callObject.method = "call";
        callObject.params = new ArrayList<>();
        callObject.params.add(_nDatabaseId);
        callObject.params.add("subscribe_to_market");

        List<Object> listParams = new ArrayList<>();
        listParams.add(callObject.id);
        listParams.add(base);
        listParams.add(quote);
        callObject.params.add(listParams);

        ReplyObjectProcess<Reply<Object>> replyObjectProcess =
                new ReplyObjectProcess<>(new TypeToken<Reply<Object>>() {
                }.getType());
        Reply<Object> replyObject = sendForReply(callObject, replyObjectProcess);
        return replyObject.id;
    }

    public void set_subscribe_callback(boolean filter) throws NetworkStatusException {
        Call callObject = new Call();
        callObject.id = mnCallId.getAndIncrement();
        callObject.method = "call";
        callObject.params = new ArrayList<>();
        callObject.params.add(_nDatabaseId);
        callObject.params.add("set_subscribe_callback");

        List<Object> listParams = new ArrayList<>();
        listParams.add(callObject.id);
        listParams.add(filter);
        callObject.params.add(listParams);

        ReplyObjectProcess<Reply<String>> replyObject =
                new ReplyObjectProcess<>(new TypeToken<Reply<String>>() {
                }.getType());
        sendForReplyImpl(callObject, replyObject);

    }

    public MarketTicker get_ticker(String base, String quote) throws NetworkStatusException {
        Call callObject = new Call();
        callObject.id = mnCallId.getAndIncrement();
        callObject.method = "call";
        callObject.params = new ArrayList<>();
        callObject.params.add(_nDatabaseId);
        callObject.params.add("get_ticker");

        List<Object> listParams = new ArrayList<>();
        listParams.add(base);
        listParams.add(quote);
        callObject.params.add(listParams);

        ReplyObjectProcess<Reply<MarketTicker>> replyObject =
                new ReplyObjectProcess<>(new TypeToken<Reply<MarketTicker>>() {
                }.getType());
        Reply<MarketTicker> reply = sendForReply(callObject, replyObject);

        return reply.result;
    }

    public List<MarketTrade> get_trade_history(String base, String quote, Date start, Date end, int limit)
            throws NetworkStatusException {
        Call callObject = new Call();
        callObject.id = mnCallId.getAndIncrement();
        callObject.method = "call";
        callObject.params = new ArrayList<>();
        callObject.params.add(_nDatabaseId);
        callObject.params.add("get_trade_history");

        List<Object> listParams = new ArrayList<>();
        listParams.add(base);
        listParams.add(quote);
        listParams.add(start);
        listParams.add(end);
        listParams.add(limit);
        callObject.params.add(listParams);

        ReplyObjectProcess<Reply<List<MarketTrade>>> replyObject =
                new ReplyObjectProcess<>(new TypeToken<Reply<List<MarketTrade>>>() {
                }.getType());
        Reply<List<MarketTrade>> reply = sendForReply(callObject, replyObject);

        return reply.result;
    }

    public List<HashMap<String, Object>> get_fill_order_history(object_id<asset_object> assetObjectId1,
                                                                object_id<asset_object> assetObjectId2, int limit) throws NetworkStatusException {
        Call callObject = new Call();
        callObject.id = mnCallId.getAndIncrement();
        callObject.method = "call";
        callObject.params = new ArrayList<>();
        callObject.params.add(_nHistoryId);
        callObject.params.add("get_fill_order_history");

        List<Object> listParams = new ArrayList<>();
        listParams.add(assetObjectId1);
        listParams.add(assetObjectId2);
        listParams.add(limit);
        callObject.params.add(listParams);

        ReplyObjectProcess<Reply<List<HashMap<String, Object>>>> replyObject =
                new ReplyObjectProcess<>(new TypeToken<Reply<List<HashMap<String, Object>>>>() {
                }.getType());
        Reply<List<HashMap<String, Object>>> reply = sendForReply(callObject, replyObject);

        return reply.result;

    }

    public List<full_account_object> get_full_accounts(List<String> names, boolean subscribe)
            throws NetworkStatusException {
        Call callObject = new Call();
        callObject.id = mnCallId.getAndIncrement();
        callObject.method = "call";
        callObject.params = new ArrayList<>();
        callObject.params.add(_nDatabaseId);
        callObject.params.add("get_full_accounts");

        List<Object> listParams = new ArrayList<>();
        listParams.add(names);
        listParams.add(subscribe);
        callObject.params.add(listParams);

        ReplyObjectProcess<Reply<List<full_account_object>>> replyObject =
                new ReplyObjectProcess<>(new TypeToken<Reply<List<full_account_object>>>() {
                }.getType());
        Reply<List<full_account_object>> reply = sendForReply(callObject, replyObject);

        return reply.result;
    }

    public List<limit_order_object> get_limit_orders(List<object_id<limit_order_object>> ids)
            throws NetworkStatusException {
        Call callObject = new Call();
        callObject.id = mnCallId.getAndIncrement();
        callObject.method = "call";
        callObject.params = new ArrayList<>();
        callObject.params.add(_nDatabaseId);
        callObject.params.add("get_objects");

        List<Object> listParams = new ArrayList<>();
        listParams.add(ids);
        callObject.params.add(listParams);

        ReplyObjectProcess<Reply<List<limit_order_object>>> replyObject =
                new ReplyObjectProcess<>(new TypeToken<Reply<List<limit_order_object>>>() {
                }.getType());
        Reply<List<limit_order_object>> reply = sendForReply(callObject, replyObject);

        return reply.result;
    }

    public limit_order_object get_limit_order(object_id<limit_order_object> id)
            throws NetworkStatusException {
        return get_limit_orders(Collections.singletonList(id)).get(0);
    }

    private <T> Reply<T> sendForReply(Call callObject,
                                      ReplyObjectProcess<Reply<T>> replyObjectProcess) throws NetworkStatusException {
        if (mWebsocket == null || mnConnectStatus != WEBSOCKET_CONNECT_SUCCESS) {
            int nRet = connect();
            if (nRet == -1) {
                throw new NetworkStatusException("It doesn't connect to the server.");
            }
        }
        return sendForReplyImpl(callObject, replyObjectProcess);
    }

    private <T> Reply<T> sendForReplyImpl(Call callObject,
                                          ReplyObjectProcess<Reply<T>> replyObjectProcess) throws NetworkStatusException {
        Log.e("send", "sendMessage");
        Gson gson = global_config_object.getInstance().getGsonBuilder().create();
        String strMessage = gson.toJson(callObject);

        synchronized (mHashMapIdToProcess) {
            mHashMapIdToProcess.put(callObject.id, replyObjectProcess);
        }

        synchronized (replyObjectProcess) {
            boolean bRet = mWebsocket.send(strMessage);
            if (bRet == false) {
                throw new NetworkStatusException("Failed to send message to server.");
            }

            try {
                replyObjectProcess.wait();
                Reply<T> replyObject = replyObjectProcess.getReplyObject();
                Log.d("check Quote", callObject.params.toString());

                String strError = replyObjectProcess.getError();
                if (TextUtils.isEmpty(strError) == false) {
                    throw new NetworkStatusException(strError);
                } else if (replyObjectProcess.getException() != null) {
                    sendForReply(callObject, replyObjectProcess);
                    throw new NetworkStatusException(replyObjectProcess.getException());
                } else if (replyObject == null) {
                    throw new NetworkStatusException("Reply object is null.\n" + replyObjectProcess.getResponse());
                } else if (replyObject.error != null) {
                    throw new NetworkStatusException(gson.toJson(replyObject.error));
                }

                return replyObject;
            } catch (InterruptedException e) {
                e.printStackTrace();
                return null;
            }
        }
    }
}
