package com.cybexmobile.Exception;


public class NetworkStatusException extends Exception {

    public NetworkStatusException(String strMessage) {
        super(strMessage);
    }

    public NetworkStatusException(Throwable throwable) {
        super(throwable);
    }

}
