import {
  CybexDaemon,
  KEY_MODE
} from "./cybex/CybexDaemon";

export const cybexDaemon = new CybexDaemon(
  "wss://hangzhou.51nebula.com/",
  "create-test21",
  "qwer1234qwer1234",
  KEY_MODE.PASSWORD
);

