package com.liu.common.infer;

import java.util.List;

public interface BloomDataProvider<T> {

    List<T> getAllKnowIds();

    List<T> getAllDocumentIds();

}
