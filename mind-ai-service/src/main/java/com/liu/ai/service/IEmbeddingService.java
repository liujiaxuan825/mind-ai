package com.liu.ai.service;

import com.liu.ai.common.DocumentMqMsgDTO;

public interface IEmbeddingService {

    void saveDocToMilvus(DocumentMqMsgDTO document);
}
