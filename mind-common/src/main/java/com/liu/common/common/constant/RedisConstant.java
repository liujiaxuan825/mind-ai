package com.liu.common.common.constant;


public class RedisConstant {
    public static final String TOKEN_BLACK = "token_black";




    public static final String KNOWLEDGE_ID = "knowledge_id :";
    public static final String DOCUMENT_ID = "document_id :";


    public static final String CACHE_NULL_OBJECT = "";



    public static final String KNOWLEDGE_COUNT_NUM = "knowledge_count_num_userId :";
    public static final String DOCUMENT_COUNT_NUM = "document_count_num_userId :";


    public static final String DOCUMENT_CACHE_DISABLE = "document_cache_disable:docId:";


    public static final long KNOWLEDGE_ID_TTL = 30*60;
    public static final long DOCUMENT_ID_TTL = 30*60;
    public static final long CACHE_NULL_TTL = 2*60;
    public static final long DOCUMENT_CACHE_DISABLE_TTL = 5;
}
