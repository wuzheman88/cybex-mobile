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
const jayson = require("jayson");
const config_1 = require("./../config");
const db_1 = require("./../db");
const CybexDaemon_1 = require("./CybexDaemon");
const main_1 = require("./main");
function main() {
    return __awaiter(this, void 0, void 0, function* () {
        const db = yield db_1.DB();
        yield main_1.default("wss://hangzhou.51nebula.com/", {
            username: "create-test21",
            seed: "qwer1234qwer1234",
            keyMode: CybexDaemon_1.KEY_MODE.PASSWORD
        });
        const server = jayson.server({
            deposit: (params, cb) => __awaiter(this, void 0, void 0, function* () {
                console.log("A Deposit Request Received: ", params[0]);
                let res;
                try {
                    res = yield main_1.depositHandler(params[0]);
                    cb(null, res);
                }
                catch (e) {
                    cb(e);
                    throw e;
                }
            }),
            ping: (params, cb) => __awaiter(this, void 0, void 0, function* () {
                cb(null, "pong");
            })
        });
        server.http().listen(config_1.PORT_OF_CYBEX, () => {
            console.log("Cybex Daemon Launched");
        });
    });
}
main();
