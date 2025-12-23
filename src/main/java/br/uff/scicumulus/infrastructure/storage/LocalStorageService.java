
package br.uff.scicumulus.infrastructure.storage;

public class LocalStorageService implements StorageService {
    @Override
    public void upload(String localPath) {
        System.out.println("Uploading " + localPath);
    }

    @Override
    public void download(String remoteId) {
        System.out.println("Downloading " + remoteId);
    }
}
