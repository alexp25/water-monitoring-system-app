package com.example.watermonitoringsystem.firebase;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

/**
 * @author Ioan-Alexandru Chirita
 */
public class Storage {

    private static final StorageReference storageReference;

    static {
        storageReference = FirebaseStorage.getInstance().getReference();
    }

    public Storage() {
    }

    public static StorageReference getImagesReference() {
        return storageReference;
    }
}
