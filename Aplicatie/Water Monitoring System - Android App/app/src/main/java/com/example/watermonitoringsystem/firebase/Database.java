package com.example.watermonitoringsystem.firebase;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * @author Ioan-Alexandru Chirita
 */
public class Database {

    private static final DatabaseReference databaseReference;
    private static final DatabaseReference customerEndpoint;
    private static final DatabaseReference supplierEndpoint;
    private static final DatabaseReference sensorsEndpoint;
    private static final DatabaseReference notificationsEndpoint;
    private static final DatabaseReference waterPumpEndpoint;
    private static final DatabaseReference electrovalvesEndpoint;

    static {
        databaseReference = FirebaseDatabase.getInstance().getReference();
        customerEndpoint = databaseReference.child(FirebaseConstants.CUSTOMERS);
        supplierEndpoint = databaseReference.child(FirebaseConstants.SUPPLIERS);
        sensorsEndpoint = databaseReference.child(FirebaseConstants.SENSORS);
        notificationsEndpoint = databaseReference.child(FirebaseConstants.NOTIFICATIONS);
        waterPumpEndpoint = databaseReference.child(FirebaseConstants.WATER_PUMP);
        electrovalvesEndpoint = databaseReference.child(FirebaseConstants.ELECTROVALVES);
    }

    public Database() {
    }

    public static DatabaseReference getDatabaseConnection() {
        return databaseReference.child(".info/connected");
    }

    public static DatabaseReference getCustomerEndpoint() {
        return customerEndpoint;
    }

    public static DatabaseReference getSupplierEndpoint() {
        return supplierEndpoint;
    }

    public static DatabaseReference getSensorsEndpoint() {
        return sensorsEndpoint;
    }

    public static DatabaseReference getNotificationsEndpoint() {
        return notificationsEndpoint;
    }

    public static DatabaseReference getWaterPumpEndpoint() {
        return waterPumpEndpoint;
    }

    public static DatabaseReference getElectrovalvesEndpoint() {
        return electrovalvesEndpoint;
    }
}
