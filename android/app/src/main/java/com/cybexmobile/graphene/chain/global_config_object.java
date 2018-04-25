package com.cybexmobile.graphene.chain;

import com.cybexmobile.Common.gson_common_deserializer;
import com.cybexmobile.Common.gson_common_serializer;
import com.google.gson.GsonBuilder;

import java.util.Date;

public class global_config_object {
    private static global_config_object mConfigObject = new global_config_object();
    private GsonBuilder mGsonBuilder;
    public static global_config_object getInstance() {
        return mConfigObject;
    }

    private global_config_object() {
        mGsonBuilder = new GsonBuilder();
        mGsonBuilder.registerTypeAdapter(object_id.class, new object_id.object_id_deserializer());
        mGsonBuilder.registerTypeAdapter(object_id.class, new object_id.object_id_serializer());
        mGsonBuilder.registerTypeAdapter(Date.class, new gson_common_deserializer.DateDeserializer());
        mGsonBuilder.registerTypeAdapter(Date.class, new gson_common_serializer.DateSerializer());
    }

    public GsonBuilder getGsonBuilder() {
        return mGsonBuilder;
    }
}
