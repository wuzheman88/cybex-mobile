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
const CybexDaemon_1 = require("./CybexDaemon");
exports.KEY_MODE = CybexDaemon_1.KEY_MODE;
const Constants_1 = require("./Constants");
const config_1 = require("./../config");
const db_1 = require("./../db");
const fetch_1 = require("./../jadepool/fetch");
const cybexjs_1 = require("cybexjs");
const Utils_1 = require("./Utils");
const utils_1 = require("./../utils");
class CybexDaemonUser {
}
exports.CybexDaemonUser = CybexDaemonUser;
function withdraw(op, daemon) {
    return __awaiter(this, void 0, void 0, function* () {
        if (!op || !op.memoContent) {
            throw Error("Withdraw invalid");
        }
        let db = yield db_1.DB();
        let { id, amount } = op;
        let [memo, asset, to] = op.memoContent.match(config_1.WITHDRAW_MEMO_PATTERN);
        console.log("WithdrawToFind:", memo);
        // Check the withdraw has been addad
        if (yield db.findWithdrawRecord(id)) {
            console.log("The withdraw has been performed");
            return;
        }
        // Verify the withdraw asset
        asset = asset.toUpperCase(); // Asseet to be uppercase
        console.log("Cybex Withdraw: ", op);
        let cybexAssetSymbol = config_1.CYBEX_CONFIG.ASSETS[asset];
        let assetInfo = yield db.getAssetInfo(cybexAssetSymbol);
        if (assetInfo.id !== amount.asset_id) {
            db.recordIllegalWithdraw(op);
            throw new Error(`
    A error has occurred during CybexDeamon try to implement a withdraw. 
    Illegal withdraw from Cybex user ${id}.
    The asset to withdraw is not match. Expect Cybex asset ${cybexAssetSymbol} to withdraw ${asset}, but ${amount.asset_id}"
    `);
        }
        // Insert the withdraw record
        let { precision } = assetInfo;
        try {
            let account = (yield daemon.getAccountsById([op.from]))[0];
            yield db.createRecord({ accountName: account.name, address: to, asset, fundType: config_1.FUND_TYPE.WITHDRAW, cybexId: id, amount: amount.amount }); // Todo 新的结构可以不需要再查询accountName
            yield fetch_1.doWithdraw({
                type: asset,
                to,
                value: utils_1.calcValue(amount.amount, precision),
                extraData: id
            });
        }
        catch (e) {
            db.recordIllegalWithdraw(op);
            throw e;
        }
    });
}
function doCybexDeposit(transfer, daemon, db, recordId) {
    return __awaiter(this, void 0, void 0, function* () {
        try {
            yield daemon.performTransfer(transfer);
        }
        catch (e) {
            db.recordErrorTransfer({ errorTransfer: transfer, recordId }, e);
        }
    });
}
function depositClose(op, daemon) {
    return __awaiter(this, void 0, void 0, function* () {
        console.log("Close Deposit: ", op);
        if (!op || !op.memoContent) {
            throw Error("Deposit Close Error");
        }
        let db = yield db_1.DB();
        let { id } = op;
        let [memo, type, to] = op.memoContent.match(config_1.DEPOSIT_MEMO_PATTERN);
        db.finishCybexByAddress(to, id); // Todo check close stated in cache
    });
}
// 
function initCybex(nodeAddress, userParams) {
    return __awaiter(this, void 0, void 0, function* () {
        let daemon = yield new CybexDaemon_1.CybexDaemon(nodeAddress, userParams.username, userParams.seed, userParams.keyMode);
        const db = yield db_1.DB();
        yield daemon.init();
        // update Assets Info
        let assetList = config_1.CYBEX_CONFIG.ASSETS;
        let cAssets = Object.keys(assetList).map(key => assetList[key]);
        let assets = yield daemon.lookupAssetSymbols(cAssets);
        yield db.updateAssets(assets);
        // Handle new deposit/withdraw request
        daemon.addListener(Constants_1.EVENT_ON_NEW_HISTORY, history => {
            // filter the withdraw 
            let his = Utils_1.filterHistoryByOp(history, cybexjs_1.ChainTypes.operations.transfer)
                .filter(entry => entry.op[1].to === daemon.daemonAccountInfo.get("id") ||
                entry.op[1].from === daemon.daemonAccountInfo.get("id")) // Todo no need?
                .map(entry => Utils_1.getTransferOpWithMemo(entry, [daemon.privKey, daemon.privKeys.owner]))
                .forEach((entry) => __awaiter(this, void 0, void 0, function* () {
                try {
                    if (config_1.WITHDRAW_MEMO_PATTERN.test(entry.memoContent)) {
                        console.log("A withdraw requirment: ", entry);
                        yield withdraw(entry, daemon);
                    }
                    else if (config_1.DEPOSIT_MEMO_PATTERN.test(entry.memoContent)) {
                        console.log("A deposit requirment: ", entry);
                        yield depositClose(entry, daemon);
                    }
                }
                catch (e) {
                    yield db.recordError(entry, e);
                }
            }));
        });
        // Handle a deposit
        db.addListener(db_1.EVENT_DEPOSIT_DONE, (transferParams) => __awaiter(this, void 0, void 0, function* () {
            let { cybexAsset, asset, address, accountName, amount, _id, value } = transferParams;
            let to_account = (yield daemon.getAccountByName(accountName)).id;
            console.log("TO_ACCOUNT: ", to_account);
            let transfer = {
                to_account,
                amount,
                asset: cybexAsset,
                memo: `deposit:${asset}:${address}`
            };
            doCybexDeposit(transfer, daemon, db, _id);
        }));
    });
}
exports.initCybex = initCybex;
exports.default = initCybex;
