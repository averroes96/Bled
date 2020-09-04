package com.averroes.hanouti.include;

public interface StoragePermissionMethods {

    static final int STORAGE_REQUEST = 300;

    boolean checkStoragePermission();

    void requestStoragePermission();

}
