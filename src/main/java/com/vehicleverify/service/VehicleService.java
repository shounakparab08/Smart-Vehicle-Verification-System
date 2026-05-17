package com.vehicleverify.service;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import com.vehicleverify.database.MongoConnection;
import com.vehicleverify.model.Vehicle;
import org.bson.Document;
import java.util.ArrayList;
import java.util.List;

public class VehicleService {
    private final MongoCollection<Document> collection;
    private static final java.util.List<Vehicle> mockVehicles = new java.util.concurrent.CopyOnWriteArrayList<>();

    static {
        mockVehicles.add(new Vehicle("MH12AB1234", "Rohan Sharma", "Valid", "Car (Sedan)", "Honda City", "2024-01-15", false));
        mockVehicles.add(new Vehicle("MH01CD5678", "Priya Patel", "Valid", "Car (Hatchback)", "Hyundai i20", "2024-01-15", false));
        mockVehicles.add(new Vehicle("DL03EF9012", "Amit Verma", "Valid", "SUV", "Toyota Fortuner", "2024-01-15", false));
        mockVehicles.add(new Vehicle("KA05GH3456", "Sneha Kulkarni", "Valid", "SUV", "Mahindra XUV700", "2024-01-15", false));
        mockVehicles.add(new Vehicle("GJ01IJ7890", "Vikram Singh", "Valid", "Motorcycle", "Royal Enfield Classic 350", "2024-01-15", false));
        mockVehicles.add(new Vehicle("MH12KL1122", "Anjali Gupta", "Valid", "Car (Sedan)", "Maruti Suzuki Ciaz", "2024-01-15", false));
        mockVehicles.add(new Vehicle("MH01MN3344", "Rahul Deshmukh", "Valid", "SUV", "Tata Harrier", "2024-01-15", false));
        mockVehicles.add(new Vehicle("DL03OP5566", "Pooja Rao", "Valid", "Scooter", "TVS Jupiter", "2024-01-15", false));
        mockVehicles.add(new Vehicle("KA05QR7788", "Sandeep Mishra", "Valid", "Car (Hatchback)", "Maruti Suzuki Swift", "2024-01-15", false));
        mockVehicles.add(new Vehicle("GJ01ST9900", "Neha Joshi", "Valid", "Motorcycle", "Yamaha FZ", "2024-01-15", false));
        mockVehicles.add(new Vehicle("MH12UV2233", "Aditya Kadam", "Valid", "SUV", "Hyundai Creta", "2024-01-15", false));
        mockVehicles.add(new Vehicle("MH01WX4455", "Meera Nair", "Valid", "Car (Sedan)", "Hyundai Verna", "2024-01-15", false));
        mockVehicles.add(new Vehicle("DL03YZ6677", "Kartik Sawant", "Valid", "Scooter", "Honda Activa", "2024-01-15", false));
        mockVehicles.add(new Vehicle("KA05AB8899", "Shweta Patil", "Valid", "SUV", "Kia Seltos", "2024-01-15", false));
        mockVehicles.add(new Vehicle("GJ01CD1100", "Sameer Khan", "Valid", "Truck", "Tata Signa", "2024-01-15", false));
        mockVehicles.add(new Vehicle("MH12XY9999", "Deepak Kumar", "Valid", "Car (Sedan)", "Maruti Suzuki Dzire", "2024-01-15", false));
    }

    public VehicleService() {
        com.mongodb.client.MongoDatabase db = MongoConnection.getInstance().getDatabase();
        this.collection = (db != null) ? db.getCollection("vehicles") : null;
    }

    public void addVehicle(Vehicle v) {
        if (!MongoConnection.getInstance().isConnected() || collection == null) {
            boolean exists = false;
            for (Vehicle vehicle : mockVehicles) {
                if (vehicle.getVehicleNo().equalsIgnoreCase(v.getVehicleNo())) {
                    exists = true;
                    break;
                }
            }
            if (!exists) {
                mockVehicles.add(v);
            }
            return;
        }
        collection.insertOne(new Document("vehicleNo", v.getVehicleNo()).append("ownerName", v.getOwnerName())
                .append("insuranceStatus", v.getInsuranceStatus()).append("vehicleType", v.getVehicleType())
                .append("vehicleModel", v.getVehicleModel())
                .append("registrationDate", v.getRegistrationDate()).append("blacklistStatus", v.isBlacklistStatus()));
    }

    public Vehicle getVehicle(String vehicleNo) {
        if (!MongoConnection.getInstance().isConnected()) {
            for (Vehicle vehicle : mockVehicles) {
                if (vehicle.getVehicleNo().equalsIgnoreCase(vehicleNo)) {
                    return vehicle;
                }
            }
            return null;
        }
        return docToVehicle(collection.find(Filters.eq("vehicleNo", vehicleNo)).first());
    }

    public void updateVehicle(Vehicle v) {
        if (!MongoConnection.getInstance().isConnected()) {
            for (int i = 0; i < mockVehicles.size(); i++) {
                if (mockVehicles.get(i).getVehicleNo().equalsIgnoreCase(v.getVehicleNo())) {
                    mockVehicles.set(i, v);
                    break;
                }
            }
            return;
        }
        collection.updateOne(Filters.eq("vehicleNo", v.getVehicleNo()),
                Updates.combine(Updates.set("ownerName", v.getOwnerName()), Updates.set("insuranceStatus", v.getInsuranceStatus()),
                        Updates.set("vehicleType", v.getVehicleType()), Updates.set("vehicleModel", v.getVehicleModel()),
                        Updates.set("registrationDate", v.getRegistrationDate()),
                        Updates.set("blacklistStatus", v.isBlacklistStatus())));
    }

    public void blacklistVehicle(String vehicleNo, boolean status) {
        if (!MongoConnection.getInstance().isConnected()) {
            for (Vehicle vehicle : mockVehicles) {
                if (vehicle.getVehicleNo().equalsIgnoreCase(vehicleNo)) {
                    vehicle.setBlacklistStatus(status);
                    break;
                }
            }
            return;
        }
        collection.updateOne(Filters.eq("vehicleNo", vehicleNo), Updates.set("blacklistStatus", status));
    }

    public List<Vehicle> getAllVehicles() {
        if (!MongoConnection.getInstance().isConnected()) {
            return new ArrayList<>(mockVehicles);
        }
        List<Vehicle> list = new ArrayList<>();
        for (Document doc : collection.find()) list.add(docToVehicle(doc));
        return list;
    }

    public List<Vehicle> getBlacklistedVehicles() {
        if (!MongoConnection.getInstance().isConnected()) {
            List<Vehicle> list = new ArrayList<>();
            for (Vehicle vehicle : mockVehicles) {
                if (vehicle.isBlacklistStatus()) {
                    list.add(vehicle);
                }
            }
            return list;
        }
        List<Vehicle> list = new ArrayList<>();
        for (Document doc : collection.find(Filters.eq("blacklistStatus", true))) list.add(docToVehicle(doc));
        return list;
    }

    public long getVehicleCount() { 
        return MongoConnection.getInstance().isConnected() ? collection.countDocuments() : mockVehicles.size(); 
    }
    public long getBlacklistedCount() { 
        if (!MongoConnection.getInstance().isConnected()) {
            long count = 0;
            for (Vehicle vehicle : mockVehicles) {
                if (vehicle.isBlacklistStatus()) {
                    count++;
                }
            }
            return count;
        }
        return collection.countDocuments(Filters.eq("blacklistStatus", true)); 
    }

    public List<Vehicle> getVehiclesByOwner(String ownerName) {
        if (!MongoConnection.getInstance().isConnected()) {
            List<Vehicle> list = new ArrayList<>();
            for (Vehicle vehicle : mockVehicles) {
                if (vehicle.getOwnerName() != null && vehicle.getOwnerName().equalsIgnoreCase(ownerName)) {
                    list.add(vehicle);
                }
            }
            return list;
        }
        List<Vehicle> list = new ArrayList<>();
        for (Document doc : collection.find(Filters.eq("ownerName", ownerName))) list.add(docToVehicle(doc));
        return list;
    }

    private Vehicle docToVehicle(Document doc) {
        if (doc == null) return null;
        return new Vehicle(doc.getString("vehicleNo"), doc.getString("ownerName"), doc.getString("insuranceStatus"),
                doc.getString("vehicleType"), doc.getString("vehicleModel"), doc.getString("registrationDate"), doc.getBoolean("blacklistStatus", false));
    }
}
