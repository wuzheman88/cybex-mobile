package com.cybexmobile.API;


import com.cybexmobile.Exception.NetworkStatusException;
import com.cybexmobile.graphene.chain.account_object;
import com.cybexmobile.graphene.chain.asset;
import com.cybexmobile.graphene.chain.asset_object;
import com.cybexmobile.graphene.chain.bucket_object;
import com.cybexmobile.graphene.chain.full_account_object;
import com.cybexmobile.graphene.chain.limit_order_object;
import com.cybexmobile.graphene.chain.object_id;
import com.cybexmobile.graphene.chain.operation_history_object;
import com.cybexmobile.Market.MarketTicker;
import com.cybexmobile.Market.MarketTrade;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class BitsharesWalletWraper {

    private static BitsharesWalletWraper bitsharesWalletWraper = new BitsharesWalletWraper();
    private wallet_api mWalletApi = new wallet_api();
    private Map<object_id<account_object>, account_object> mMapAccountId2Object = new ConcurrentHashMap<>();
    private Map<object_id<account_object>, List<asset>> mMapAccountId2Asset = new ConcurrentHashMap<>();
    private Map<object_id<account_object>, List<operation_history_object>> mMapAccountId2History = new ConcurrentHashMap<>();
    private Map<object_id<asset_object>, asset_object> mMapAssetId2Object = new ConcurrentHashMap<>();
    private String mstrWalletFilePath;

    private int mnStatus = STATUS_INVALID;

    private static final int STATUS_INVALID = -1;
    private static final int STATUS_INITIALIZED = 0;

    private BitshareData mBitshareData;

    private BitsharesWalletWraper() {
        //mstrWalletFilePath = BitsharesApplication.getInstance().getFilesDir().getPath();
        mstrWalletFilePath += "/wallet.json";
    }

    public static BitsharesWalletWraper getInstance() {
        return bitsharesWalletWraper;
    }

    public void reset() {
        mWalletApi.reset();
        mWalletApi = new wallet_api();
        mMapAccountId2Object.clear();
        ;
        mMapAccountId2Asset.clear();
        ;
        mMapAccountId2History.clear();
        mMapAssetId2Object.clear();
        ;

        File file = new File(mstrWalletFilePath);
        file.delete();

        mnStatus = STATUS_INVALID;
    }

    public account_object get_account() {
        List<account_object> listAccount = mWalletApi.list_my_accounts();
        if (listAccount == null || listAccount.isEmpty()) {
            return null;
        }

        return listAccount.get(0);
    }

//    public boolean is_new() {
//        return mWalletApi.is_new();
//    }
//
//    public  boolean is_locked() {
//        return mWalletApi.is_locked();
//    }
//
//    public int load_wallet_file() {
//        return mWalletApi.load_wallet_file(mstrWalletFilePath);
//    }
//
//    private int save_wallet_file() {
//        return mWalletApi.save_wallet_file(mstrWalletFilePath);
//    }

    public synchronized int build_connect() {
        if (mnStatus == STATUS_INITIALIZED) {
            return 0;
        }

        int nRet = mWalletApi.initialize();
        if (nRet != 0) {
            return nRet;
        }

        mnStatus = STATUS_INITIALIZED;
        return 0;
    }


    public List<account_object> list_my_accounts() {
        return mWalletApi.list_my_accounts();
    }

//    public int import_key(String strAccountNameOrId,
//                          String strPassword,
//                          String strPrivateKey) {
//
//        mWalletApi.set_passwrod(strPassword);
//
//        try {
//            int nRet = mWalletApi.import_key(strAccountNameOrId, strPrivateKey);
//            if (nRet != 0) {
//                return nRet;
//            }
//        } catch (NetworkStatusException e) {
//            e.printStackTrace();
//            return -1;
//        }
//
//        save_wallet_file();
//
//        for (account_object accountObject : list_my_accounts()) {
//            mMapAccountId2Object.put(accountObject.id, accountObject);
//        }
//
//        return 0;
//    }

//    public int import_keys(String strAccountNameOrId,
//                           String strPassword,
//                           String strPrivateKey1,
//                           String strPrivateKey2) {
//
//        mWalletApi.set_passwrod(strPassword);
//
//        try {
//            int nRet = mWalletApi.import_keys(strAccountNameOrId, strPrivateKey1, strPrivateKey2);
//            if (nRet != 0) {
//                return nRet;
//            }
//        } catch (NetworkStatusException e) {
//            e.printStackTrace();
//            return -1;
//        }
//
//        save_wallet_file();
//
//        for (account_object accountObject : list_my_accounts()) {
//            mMapAccountId2Object.put(accountObject.id, accountObject);
//        }
//
//        return 0;
//    }

//    public int import_brain_key(String strAccountNameOrId,
//                                String strPassword,
//                                String strBrainKey) {
//        mWalletApi.set_passwrod(strPassword);
//        try {
//            int nRet = mWalletApi.import_brain_key(strAccountNameOrId, strBrainKey);
//            if (nRet != 0) {
//                return nRet;
//            }
//        } catch (NetworkStatusException e) {
//            e.printStackTrace();
//            return ErrorCode.ERROR_IMPORT_NETWORK_FAIL;
//        }
//
//        save_wallet_file();
//
//        for (account_object accountObject : list_my_accounts()) {
//            mMapAccountId2Object.put(accountObject.id, accountObject);
//        }
//
//        return 0;
//    }

//    public int import_file_bin(String strPassword,
//                               String strFilePath) {
//        File file = new File(strFilePath);
//        if (file.exists() == false) {
//            return ErrorCode.ERROR_FILE_NOT_FOUND;
//        }
//
//        int nSize = (int)file.length();
//
//        final byte[] byteContent = new byte[nSize];
//
//        FileInputStream fileInputStream;
//        try {
//            fileInputStream = new FileInputStream(file);
//            fileInputStream.read(byteContent, 0, byteContent.length);
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//            return ErrorCode.ERROR_FILE_NOT_FOUND;
//        } catch (IOException e) {
//            e.printStackTrace();
//            return ErrorCode.ERROR_FILE_READ_FAIL;
//        }
//
//        WalletBackup walletBackup = FileBin.deserializeWalletBackup(byteContent, strPassword);
//        if (walletBackup == null) {
//            return ErrorCode.ERROR_FILE_BIN_PASSWORD_INVALID;
//        }
//
//        String strBrainKey = walletBackup.getWallet(0).decryptBrainKey(strPassword);
//        //LinkedAccount linkedAccount = walletBackup.getLinkedAccounts()[0];
//
//        int nRet = ErrorCode.ERROR_IMPORT_NOT_MATCH_PRIVATE_KEY;
//        for (LinkedAccount linkedAccount : walletBackup.getLinkedAccounts()) {
//            nRet = import_brain_key(linkedAccount.getName(), strPassword, strBrainKey);
//            if (nRet == 0) {
//                break;
//            }
//        }
//
//        return nRet;
//    }

//    public int import_account_password(String strAccountName,
//                                       String strPassword) {
//        mWalletApi.set_passwrod(strPassword);
//        try {
//            int nRet = mWalletApi.import_account_password(strAccountName, strPassword);
//            if (nRet != 0) {
//                return nRet;
//            }
//        } catch (NetworkStatusException e) {
//            e.printStackTrace();
//            return -1;
//        }
//
//        save_wallet_file();
//
//        for (account_object accountObject : list_my_accounts()) {
//            mMapAccountId2Object.put(accountObject.id, accountObject);
//        }
//
//        return 0;
//
//    }

//    public int unlock(String strPassword) {
//        return mWalletApi.unlock(strPassword);
//    }

//    public int lock() {
//        return mWalletApi.lock();
//    }

    public List<asset> list_balances(boolean bRefresh) throws NetworkStatusException {
        List<asset> listAllAsset = new ArrayList<>();
        for (account_object accountObject : list_my_accounts()) {
            List<asset> listAsset = list_account_balance(accountObject.id, bRefresh);

            listAllAsset.addAll(listAsset);
        }

        return listAllAsset;
    }

    public List<asset> list_account_balance(object_id<account_object> accountObjectId,
                                            boolean bRefresh) throws NetworkStatusException {
        List<asset> listAsset = mMapAccountId2Asset.get(accountObjectId);
        if (bRefresh || listAsset == null) {
            listAsset = mWalletApi.list_account_balance(accountObjectId);
            mMapAccountId2Asset.put(accountObjectId, listAsset);
        }

        return listAsset;
    }

//    public List<operation_history_object> get_history(boolean bRefresh) throws NetworkStatusException {
//        List<operation_history_object> listAllHistoryObject = new ArrayList<>();
//        for (account_object accountObject : list_my_accounts()) {
//            List<operation_history_object> listHistoryObject = get_account_history(
//                    accountObject.id,
//                    100,
//                    bRefresh
//            );
//
//            listAllHistoryObject.addAll(listHistoryObject);
//        }
//
//        return listAllHistoryObject;
//    }

//    public List<operation_history_object> get_account_history(object_id<account_object> accountObjectId,
//                                                              int nLimit,
//                                                              boolean bRefresh) throws NetworkStatusException {
//        List<operation_history_object> listHistoryObject = mMapAccountId2History.get(accountObjectId);
//        if (listHistoryObject == null || bRefresh) {
//            listHistoryObject = mWalletApi.get_account_history(accountObjectId, nLimit);
//            mMapAccountId2History.put(accountObjectId, listHistoryObject);
//        }
//        return listHistoryObject;
//    }

    public List<asset_object> list_assets(String strLowerBound, int nLimit) throws NetworkStatusException {
        return mWalletApi.list_assets(strLowerBound, nLimit);
    }

    public Map<object_id<asset_object>, asset_object> get_assets(List<object_id<asset_object>> listAssetObjectId) throws NetworkStatusException {
        Map<object_id<asset_object>, asset_object> mapId2Object = new HashMap<>();

        List<object_id<asset_object>> listRequestId = new ArrayList<>();
        for (object_id<asset_object> objectId : listAssetObjectId) {
            asset_object assetObject = mMapAssetId2Object.get(objectId);
            if (assetObject != null) {
                mapId2Object.put(objectId, assetObject);
            } else {
                listRequestId.add(objectId);
            }
        }

        if (listRequestId.isEmpty() == false) {
            List<asset_object> listAssetObject = mWalletApi.get_assets(listRequestId);
            for (asset_object assetObject : listAssetObject) {
                mapId2Object.put(assetObject.id, assetObject);
                mMapAssetId2Object.put(assetObject.id, assetObject);
            }
        }

        return mapId2Object;
    }

    public asset_object lookup_asset_symbols(String strAssetSymbol) throws NetworkStatusException {
        return mWalletApi.lookup_asset_symbols(strAssetSymbol);
    }

    public asset_object get_objects(String objectId) throws NetworkStatusException {
        return mWalletApi.get_objects(objectId);
    }

    public Map<object_id<account_object>, account_object> get_accounts(List<object_id<account_object>> listAccountObjectId) throws NetworkStatusException {
        Map<object_id<account_object>, account_object> mapId2Object = new HashMap<>();

        List<object_id<account_object>> listRequestId = new ArrayList<>();
        for (object_id<account_object> objectId : listAccountObjectId) {
            account_object accountObject = mMapAccountId2Object.get(objectId);
            if (accountObject != null) {
                mapId2Object.put(objectId, accountObject);
            } else {
                listRequestId.add(objectId);
            }
        }

        if (listRequestId.isEmpty() == false) {
            List<account_object> listAccountObject = mWalletApi.get_accounts(listRequestId);
            for (account_object accountObject : listAccountObject) {
                mapId2Object.put(accountObject.id, accountObject);
                mMapAccountId2Object.put(accountObject.id, accountObject);
            }
        }

        return mapId2Object;
    }

//    public block_header get_block_header(int nBlockNumber) throws NetworkStatusException {
//        return mWalletApi.get_block_header(nBlockNumber);
//    }

//    public signed_transaction transfer(String strFrom,
//                                       String strTo,
//                                       String strAmount,
//                                       String strAssetSymbol,
//                                       String strMemo) throws NetworkStatusException {
//        signed_transaction signedTransaction = mWalletApi.transfer(
//                strFrom,
//                strTo,
//                strAmount,
//                strAssetSymbol,
//                strMemo
//        );
//        return signedTransaction;
//    }

//    public BitshareData prepare_data_to_display(boolean bRefresh) {
//        try {
//            List<asset> listBalances = BitsharesWalletWraper.getInstance().list_balances(bRefresh);
//
//            List<operation_history_object> operationHistoryObjectList = BitsharesWalletWraper.getInstance().get_history(bRefresh);
//            HashSet<object_id<account_object>> hashSetObjectId = new HashSet<object_id<account_object>>();
//            HashSet<object_id<asset_object>> hashSetAssetObject = new HashSet<object_id<asset_object>>();
//
//            List<Pair<operation_history_object, Date>> listHistoryObjectTime = new ArrayList<Pair<operation_history_object, Date>>();
//            for (operation_history_object historyObject : operationHistoryObjectList) {
//                block_header blockHeader = BitsharesWalletWraper.getInstance().get_block_header(historyObject.block_num);
//                listHistoryObjectTime.add(new Pair<>(historyObject, blockHeader.timestamp));
//                if (historyObject.op.nOperationType <= operations.ID_CREATE_ACCOUNT_OPERATION) {
//                    operations.base_operation operation = (operations.base_operation)historyObject.op.operationContent;
//                    hashSetObjectId.addAll(operation.get_account_id_list());
//                    hashSetAssetObject.addAll(operation.get_asset_id_list());
//                }
//            }
//
//            // 保证默认数据一直存在
//            hashSetAssetObject.add(new object_id<asset_object>(0, asset_object.class));
//
//            //// TODO: 06/09/2017 这里需要优化到一次调用
//
//            for (asset assetBalances : listBalances) {
//                hashSetAssetObject.add(assetBalances.asset_id);
//            }
//
//            List<object_id<account_object>> listAccountObjectId = new ArrayList<object_id<account_object>>();
//            listAccountObjectId.addAll(hashSetObjectId);
//            Map<object_id<account_object>, account_object> mapId2AccountObject =
//                    BitsharesWalletWraper.getInstance().get_accounts(listAccountObjectId);
//
//
//            List<object_id<asset_object>> listAssetObjectId = new ArrayList<object_id<asset_object>>();
//            listAssetObjectId.addAll(hashSetAssetObject);
//
//            // 生成id 2 asset_object映身
//            Map<object_id<asset_object>, asset_object> mapId2AssetObject =
//                    BitsharesWalletWraper.getInstance().get_assets(listAssetObjectId);
//
//            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(BitsharesApplication.getInstance());
//            String strCurrencySetting = prefs.getString("currency_setting", "USD");
//
//            asset_object currencyObject = mWalletApi.list_assets(strCurrencySetting, 1).get(0);
//            mapId2AssetObject.put(currencyObject.id, currencyObject);
//
//            hashSetAssetObject.add(currencyObject.id);
//
//            listAssetObjectId.clear();
//            listAssetObjectId.addAll(hashSetAssetObject);
//
//            Map<object_id<asset_object>, bucket_object> mapAssetId2Bucket = get_market_histories_base(listAssetObjectId);
//
//            mBitshareData = new BitshareData();
//            mBitshareData.assetObjectCurrency = currencyObject;
//            mBitshareData.listBalances = listBalances;
//            mBitshareData.listHistoryObject = listHistoryObjectTime;
//            mBitshareData.mapId2AssetObject = mapId2AssetObject;
//            //mBitshareData.mapId2AccountObject = mapId2AccountObject;
//            mBitshareData.mapAssetId2Bucket = mapAssetId2Bucket;
//
//            return mBitshareData;
//
//        } catch (NetworkStatusException e) {
//            e.printStackTrace();
//        }
//
//        return null;
//    }

    // 获取对于基础货币的所有市场价格
//    public Map<object_id<asset_object>, bucket_object> get_market_histories_base(List<object_id<asset_object>> listAssetObjectId) throws NetworkStatusException {
//        dynamic_global_property_object dynamicGlobalPropertyObject = mWalletApi.get_dynamic_global_properties();
//
//        Date dateObject = dynamicGlobalPropertyObject.time;
//        Calendar calendar = Calendar.getInstance();
//        calendar.setTime(dateObject);
//        calendar.add(Calendar.HOUR, -12);
//
//        Date dateObjectStart = calendar.getTime();
//
//        calendar.setTime(dateObject);
//        calendar.add(Calendar.SECOND, 30);
//
//        Date dateObjectEnd = calendar.getTime();
//
//        Map<object_id<asset_object>, bucket_object> mapId2BucketObject = new HashMap<>();
//
//        object_id<asset_object> assetObjectBase = new object_id<asset_object>(0, asset_object.class);
//        for (object_id<asset_object> objectId : listAssetObjectId) {
//            if (objectId.equals(assetObjectBase)) {
//                continue;
//            }
//            List<bucket_object> listBucketObject = mWalletApi.get_market_history(
//                    objectId,
//                    assetObjectBase,
//                    3600,
//                    dateObjectStart,
//                    dateObjectEnd
//            );
//
//            if (listBucketObject.isEmpty() == false) {
//                bucket_object bucketObject = listBucketObject.get(listBucketObject.size() - 1);
//                mapId2BucketObject.put(objectId, bucketObject);
//            }
//        }
//
//        return mapId2BucketObject;
//    }

    public List<bucket_object> get_market_history(object_id<asset_object> assetObjectId1,
                                                  object_id<asset_object> assetObjectId2,
                                                  int nBucket, Date dateStart,
                                                  Date dateEnd) throws NetworkStatusException {
        return mWalletApi.get_market_history(
                assetObjectId1, assetObjectId2, nBucket, dateStart, dateEnd);
    }

    public String subscribe_to_market(String base, String quote) throws NetworkStatusException {
        return mWalletApi.subscribe_to_market(base, quote);
    }

    public void set_subscribe_market(boolean filter) throws NetworkStatusException {
        mWalletApi.set_subscribe_market(filter);
    }

    public MarketTicker get_ticker(String base, String quote) throws NetworkStatusException {
        return mWalletApi.get_ticker(base, quote);
    }

    public List<MarketTrade> get_trade_history(String base, String quote, Date start, Date end, int limit)
            throws NetworkStatusException {
        return mWalletApi.get_trade_history(base, quote, start, end, limit);
    }

    public List<HashMap<String, Object>> get_fill_order_history(object_id<asset_object> base,
                                                                object_id<asset_object> quote,
                                                                int limit) throws NetworkStatusException {
        return mWalletApi.get_fill_order_history(base, quote, limit);
    }

    public List<limit_order_object> get_limit_orders(object_id<asset_object> base,
                                                     object_id<asset_object> quote,
                                                     int limit) throws NetworkStatusException {
        return mWalletApi.get_limit_orders(base, quote, limit);
    }

//    public signed_transaction sell_asset(String amountToSell, String symbolToSell,
//                                         String minToReceive, String symbolToReceive,
//                                         int timeoutSecs, boolean fillOrKill)
//            throws NetworkStatusException {
//        return mWalletApi.sell_asset(amountToSell, symbolToSell, minToReceive, symbolToReceive,
//                timeoutSecs, fillOrKill);
//    }

//    public asset calculate_sell_fee(asset_object assetToSell, asset_object assetToReceive,
//                                    double rate, double amount,
//                                    global_property_object globalPropertyObject) {
//        return mWalletApi.calculate_sell_fee(assetToSell, assetToReceive, rate, amount,
//                globalPropertyObject);
//    }

//    public asset calculate_buy_fee(asset_object assetToReceive, asset_object assetToSell,
//                                   double rate, double amount,
//                                   global_property_object globalPropertyObject) {
//        return mWalletApi.calculate_buy_fee(assetToReceive, assetToSell, rate, amount,
//                globalPropertyObject);
//    }

//    public signed_transaction sell(String base, String quote, double rate, double amount)
//            throws NetworkStatusException {
//        return mWalletApi.sell(base, quote, rate, amount);
//    }
//
//    public signed_transaction sell(String base, String quote, double rate, double amount,
//                                   int timeoutSecs) throws NetworkStatusException {
//        return mWalletApi.sell(base, quote, rate, amount, timeoutSecs);
//    }
//
//    public signed_transaction buy(String base, String quote, double rate, double amount)
//            throws NetworkStatusException {
//        return mWalletApi.buy(base, quote, rate, amount);
//    }
//
//    public signed_transaction buy(String base, String quote, double rate, double amount,
//                                  int timeoutSecs) throws NetworkStatusException {
//        return mWalletApi.buy(base, quote, rate, amount, timeoutSecs);
//    }

    public BitshareData getBitshareData() {
        return mBitshareData;
    }

    public account_object get_account_object(String strAccount) throws NetworkStatusException {
        return mWalletApi.get_account(strAccount);
    }

//    public asset transfer_calculate_fee(String strAmount,
//                                        String strAssetSymbol,
//                                        String strMemo) throws NetworkStatusException {
//        return mWalletApi.transfer_calculate_fee(strAmount, strAssetSymbol, strMemo);
//    }

//    public String get_plain_text_message(memo_data memoData) {
//        return mWalletApi.decrypt_memo_message(memoData);
//    }

    public List<full_account_object> get_full_accounts(List<String> names, boolean subscribe)
            throws NetworkStatusException {
        return mWalletApi.get_full_accounts(names, subscribe);
    }

//    public signed_transaction cancel_order(object_id<limit_order_object> id)
//            throws NetworkStatusException {
//        return mWalletApi.cancel_order(id);
//    }
//
//    public global_property_object get_global_properties() throws NetworkStatusException {
//        return mWalletApi.get_global_properties();
//    }
}
