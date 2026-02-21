package com.lexsecura.application.port;

import java.io.InputStream;

public interface StoragePort {

    void upload(String key, InputStream inputStream, long contentLength, String contentType);

    InputStream download(String key);

    void delete(String key);
}
