
package br.uff.scicumulus.infrastructure.storage;

public interface StorageService {
    void upload(String localPath);
    void download(String remoteId);
}
