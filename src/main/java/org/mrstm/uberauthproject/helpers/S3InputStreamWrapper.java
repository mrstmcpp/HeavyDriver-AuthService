package org.mrstm.uberauthproject.helpers;


import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.io.InputStream;

@Getter
@RequiredArgsConstructor
public class S3InputStreamWrapper implements AutoCloseable {
    private final InputStream inputStream;
    private final String eTag;

    @Override
    public void close() throws Exception {
        inputStream.close();
    }
}
