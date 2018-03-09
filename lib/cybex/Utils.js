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
const cybexjs_1 = require("cybexjs");
exports.getIndexSuffixdArray = (strOrArray) => Array.isArray(strOrArray) ?
    strOrArray.map((item, index) => item) : [strOrArray];
exports.genKeysFromWif = (wifMap) => {
    let privKeys = {};
    let pubKeys = {};
    for (let role in wifMap) {
        privKeys[role] = cybexjs_1.PrivateKey.fromWif(wifMap[role]);
        pubKeys[role] = privKeys[role].toPublicKey().toString();
    }
    return {
        privKeys,
        pubKeys
    };
};
exports.getAuthsFromPubkeys = (pubKeys, rolesToAuth = ["active", "owner", "memo"]) => Object
    .keys(pubKeys)
    .filter(role => rolesToAuth.indexOf(role) != -1)
    .reduce((auths, pubkeyRole) => pubkeyRole in auths ? Object.assign({}, auths, { [pubkeyRole]: [...auths[pubkeyRole], exports.getIndexSuffixdArray(pubKeys[pubkeyRole])] }) : Object.assign({}, auths, { [pubkeyRole]: [exports.getIndexSuffixdArray(pubKeys[pubkeyRole])] }), {});
exports.genMemo = (from_account, to_account, memoContent, keyMaps) => __awaiter(this, void 0, void 0, function* () {
    let [memo_sender, memo_to] = yield Promise.all([
        cybexjs_1.FetchChain("getAccount", from_account),
        cybexjs_1.FetchChain("getAccount", to_account)
    ]);
    // 检查双方公钥存在
    let memo_from_public = memo_sender.getIn(["options", "memo_key"]);
    // The 1s are base58 for all zeros (null)
    if (/111111111111111111111/.test(memo_from_public)) {
        memo_from_public = null;
    }
    let memo_to_public = memo_to.getIn(["options", "memo_key"]);
    if (/111111111111111111111/.test(memo_to_public)) {
        memo_to_public = null;
    }
    if (!memo_from_public || !memo_to_public)
        return undefined;
    let privKey = keyMaps[memo_from_public];
    let nonce = cybexjs_1.TransactionHelper.unique_nonce_uint64();
    return {
        from: memo_from_public,
        to: memo_to_public,
        nonce,
        message: cybexjs_1.Aes.encrypt_with_checksum(privKey, memo_to_public, nonce, new Buffer(memoContent, "utf-8"))
    };
});
exports.buildTransfer = ({ from_account, to_account, amount, asset, memo }, keyMaps) => __awaiter(this, void 0, void 0, function* () {
    let memoObject;
    if (memo) {
        memoObject = yield exports.genMemo(from_account, to_account, memo, keyMaps);
    }
    return {
        fee: {
            amount: 0,
            asset_id: "1.3.0"
        },
        from: from_account,
        to: to_account,
        amount: {
            amount: amount,
            asset_id: asset
        },
        memo: memoObject
    };
});
exports.filterHistoryByOp = (oriHistory, opToRemained) => oriHistory
    .filter(hisEntry => hisEntry.op[0] === opToRemained);
exports.getOpFromHistory = history => (Object.assign({}, history.op[1], { id: history.id }));
exports.getTransferOpWithMemo = (history, privKeys) => {
    let op = exports.getOpFromHistory(history);
    if (op.memo && privKeys && privKeys.length) {
        try {
            op.memoContent = exports.decodeMemo(op.memo, privKeys);
        }
        catch (e) {
            op.memoContent = "***";
        }
    }
    return op;
};
exports.decodeMemo = (memo, privKeys) => {
    let memoContent;
    try {
        memoContent = exports.decodeMemoImpl(memo, privKeys[0]);
    }
    catch (e) {
        memoContent = exports.decodeMemoImpl(memo, privKeys[1]);
    }
    return memoContent;
};
exports.decodeMemoImpl = (memo, privKey) => {
    let memoContent;
    try {
        memoContent = cybexjs_1.Aes.decrypt_with_checksum(privKey, cybexjs_1.PublicKey.fromPublicKeyString(memo.to), memo.nonce, memo.message, true).toString("utf-8");
    }
    catch (e) {
        memoContent = cybexjs_1.Aes.decrypt_with_checksum(privKey, cybexjs_1.PublicKey.fromPublicKeyString(memo.from), memo.nonce, memo.message, true).toString("utf-8");
    }
    return memoContent;
};
