package com.yourname.config.Listener;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentParser;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.document.parser.TextDocumentParser;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import jakarta.annotation.Resource;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class TODO {

    private final String url;

    private final EmbeddingStoreIngestor  ingestor;

    public void getDoc(){
        DocumentParser parser = new TextDocumentParser();
        Document document = FileSystemDocumentLoader.loadDocument(url, parser);
        ingestor.ingest(document);
    }
}
