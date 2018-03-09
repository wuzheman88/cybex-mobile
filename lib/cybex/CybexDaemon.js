"use strict";
var __awaiter = (this && this.__awaiter) || function (thisArg, _arguments, P, generator) {
    return new (P || (P = Promise))(function (resolve, reject) {
        function fulfilled(value) { try { step(generator.next(value)); } catch (e) { reject(e); } }
        function rejected(value) { try { step(generator["throw"](value)); } catch (e) { reject(e); } }
        function step(result) { result.done ? resolve(result.value) : new P(function (resolve) { resolve(result.value); }).then(fulfilled, rejected); }
        step((generator = generator.apply(thisArg, _arguments || [])).next());
    });
};
Object.defineProperty(exports, "__esModule", { value: true });
/// <reference types="node" />
const cybexjs_1 = require("cybexjs");
const cybexjs_ws_1 = require("cybexjs-ws");
const events = require("events");
const Utils_1 = require("./Utils");
const Constants_1 = require("./Constants");
const lodash_1 = require("lodash");
var KEY_MODE;
(function (KEY_MODE) {
    KEY_MODE[KEY_MODE["PASSWORD"] = 0] = "PASSWORD";
    KEY_MODE[KEY_MODE["WIF"] = 1] = "WIF";
})(KEY_MODE = exports.KEY_MODE || (exports.KEY_MODE = {}));
;
class CybexDaemon extends events.EventEmitter {
    constructor(nodeAddress, daemonUser, daemonPassword, mode = KEY_MODE.PASSWORD) {
        super();
        this.nodeAddress = nodeAddress;
        this.daemonUser = daemonUser;
        this.daemonPassword = daemonPassword;
        this.mode = mode;
      
        // this.addresses = pubKeys["active"].toAdd
        this.history = [];
        this.listenDaemonAccount = this.listenDaemonAccount.bind(this);
    }
    get privKey() {
        return this.privKeys.active;
    }
    get pubKey() {
        return this.pubKeys.active;
    }
    /**
     * 初始化ChainStore，转账操作前需执行
     *
     * @memberof CybexDaemon
     */
    init() {
        return __awaiter(this, void 0, void 0, function* () {
            let { nodeAddress } = this;
            try {
                let instanceRes = yield cybexjs_ws_1.Apis.instance(nodeAddress, true).init_promise;
            }
            catch (e) {
                process.exit(1);
            }
            this.Apis = cybexjs_ws_1.Apis;
            //get_recent_transaction_by_id
            // Apis.instance().ws_rpc.wspm.on("message", e => console.log("Message: ", e));
            console.log("connected to:", nodeAddress);
            // Apis.instance().ws
            yield cybexjs_1.ChainStore.init();
            this.daemonAccountInfo = yield cybexjs_1.FetchChain("getAccount", this.daemonUser);
            let mode = this.mode;
            switch (mode) {
                case KEY_MODE.PASSWORD:
                    let res = cybexjs_1.Login.generateKeys(this.daemonUser, this.daemonPassword);
                    this.privKeys = res.privKeys;
                    this.pubKeys = res.pubKeys;
                    break;
                case KEY_MODE.WIF:
                    let { privKeys, pubKeys } = Utils_1.genKeysFromWif(this.daemonPassword);
                    this.privKeys = privKeys;
                    this.pubKeys = pubKeys;
                    break;
            }
            this.keyMap = {};
            for (let role in this.pubKeys) {
                this.keyMap[this.pubKeys[role]] = this.privKeys[role];
            }
            cybexjs_1.ChainStore.subscribe(this.listenDaemonAccount);
            cybexjs_ws_1.Apis.instance().ws_rpc.ws.on("close", (e) => __awaiter(this, void 0, void 0, function* () {
                console.error("Ws connection has been broken. Reconnect to ws server");
                yield this.init();
            }));
            // console.log("WSS: ",); 
            // instanceRes.setRpcConnectionStatusCallback(status => console.log("WS Status: ", status));  
            this.listenDaemonAccount();
        });
    }
    listenDaemonAccount() {
        return __awaiter(this, void 0, void 0, function* () {
            // console.log("Cybex Tick");
            this.daemonAccountInfo = yield cybexjs_1.FetchChain("getAccount", this.daemonUser);
            let history = this.daemonAccountInfo.get("history");
            if (!history)
                return;
            history = history.toJS();
            // console.log("This: ", this.history, ";History: ", history);
            let newAdded = lodash_1.differenceBy(history, this.history, "id");
            if (newAdded.length) {
                this.history = [...newAdded, ...this.history];
                this.emit(Constants_1.EVENT_ON_NEW_HISTORY, newAdded);
            }
        });
    }
    // Database API:
    lookupAssetSymbols(assetSymbols) {
        return __awaiter(this, void 0, void 0, function* () {
            return yield this.Apis.instance().db_api().exec("lookup_asset_symbols", [assetSymbols]);
        });
    }
    getAccountByName(name) {
        return __awaiter(this, void 0, void 0, function* () {
            return yield this.Apis.instance().db_api().exec("get_account_by_name", [name]);
        });
    }
    getAccountsById(ids) {
        return __awaiter(this, void 0, void 0, function* () {
            return yield this.Apis.instance().db_api().exec("get_accounts", [ids]);
        });
    }
    /**
     * 实现一次Transfer操作
     * @param {TransferObject} transferObj
     * @memberof CybexDaemon
     */
    performTransfer(transferObj) {
        return __awaiter(this, void 0, void 0, function* () {
            if (this.mode === KEY_MODE.PASSWORD && !this.updateAuthForOp(["active"])) {
                throw new Error("Cannot update auths for transfer");
            }
            if (!transferObj.from_account) {
                transferObj.from_account = this.daemonAccountInfo.get("id");
            }
            // 建立一个用于转账操作的Tranaction, 并配置操作/费用/签名
            let tr = new cybexjs_1.TransactionBuilder();
            let transfer_op = tr.get_type_operation("transfer", yield Utils_1.buildTransfer(transferObj, this.keyMap));
            try {
                return yield this.performTransaction(tr, transfer_op);
            }
            catch (e) {
                console.error("Tranfer Error: ", e);
            }
        });
    }
    performTransaction(tr, op, loginInstance = cybexjs_1.Login) {
        return __awaiter(this, void 0, void 0, function* () {
            try {
                yield tr.update_head_block();
                tr.add_operation(op);
                yield tr.set_required_fees();
                yield tr.update_head_block();
                if (this.mode === KEY_MODE.PASSWORD) {
                    loginInstance.signTransaction(tr);
                }
                else {
                    tr.add_signer(this.privKey);
                }
                console.log("Transaction to broadcast: ", tr.serialize());
                tr.broadcast();
                return tr.serialize();
            }
            catch (e) {
                yield this.init();
                console.error("PERFORM ERROR 1: ", e);
                throw new Error(e);
            }
        });
    }
    /**
     * 检测并更新当前Login中存有的auth
     *
     * @public
     * @param {TransferObject} transferObj
     * @param {string[]} [roles=["active", "memo", "owner"]] 更新哪些role，一般操作通常仅需要active
     * @returns {boolean}
     * @memberof CybexDaemon
     */
    updateAuthForOp(roles = ["active", "memo", "owner"], loginInstance = cybexjs_1.Login) {
        if (!this.pubKeys || !this.pubKeys.active) {
            throw new Error("No active auth founded");
        }
        let authToTransfer = Utils_1.getAuthsFromPubkeys(this.pubKeys, roles);
        return loginInstance.checkKeys({
            accountName: this.daemonUser,
            password: this.daemonPassword,
            auths: authToTransfer
        });
    }
}
exports.CybexDaemon = CybexDaemon;
