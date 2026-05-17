package com.vehicleverify.util;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.vehicleverify.database.MongoConnection;
import org.bson.Document;
import java.util.Base64;
import java.time.Instant;

public class DatabaseSeeder {

    public static void main(String[] args) {
        System.out.println("Starting Database Seeding...");
        
        MongoDatabase db = MongoConnection.getInstance().getDatabase();
        
        // 1. Clear existing data
        System.out.println("Clearing collections...");
        db.getCollection("users").deleteMany(new Document());
        db.getCollection("vehicles").deleteMany(new Document());
        db.getCollection("reports").deleteMany(new Document());
        db.getCollection("notifications").deleteMany(new Document());
        
        MongoCollection<Document> usersCol = db.getCollection("users");
        MongoCollection<Document> vehiclesCol = db.getCollection("vehicles");
        MongoCollection<Document> reportsCol = db.getCollection("reports");
        
        // 2. Define human-looking data
        String[] names = {
            "Rohan Sharma", "Priya Patel", "Amit Verma", "Sneha Kulkarni", "Vikram Singh",
            "Anjali Gupta", "Rahul Deshmukh", "Pooja Rao", "Sandeep Mishra", "Neha Joshi",
            "Aditya Kadam", "Meera Nair", "Kartik Sawant", "Shweta Patil", "Sameer Khan",
            "Deepak Kumar"
        };
        
        String[] emails = {
            "rohan.sharma88@gmail.com", "priya.patel.vns@gmail.com", "amitverma.official@gmail.com", 
            "sneha.kulkarni.92@gmail.com", "singh.vikram.pro@gmail.com", "gupta.anjali.dev@gmail.com", 
            "rahul.deshmukh.main@gmail.com", "pooja.rao.v@gmail.com", "sandeep.mishra.work@gmail.com", 
            "joshi.neha.27@gmail.com", "aditya.kadam.vvs@gmail.com", "nair.meera.life@gmail.com", 
            "kartik.sawant.85@gmail.com", "shwetapatil.v@gmail.com", "sameerkhan.786@gmail.com",
            "deepak.kumar.vvs@gmail.com"
        };
        
        String[] plates = {
            "MH12AB1234", "MH01CD5678", "DL03EF9012", "KA05GH3456", "GJ01IJ7890",
            "MH12KL1122", "MH01MN3344", "DL03OP5566", "KA05QR7788", "GJ01ST9900",
            "MH12UV2233", "MH01WX4455", "DL03YZ6677", "KA05AB8899", "GJ01CD1100",
            "MH12XY9999"
        };
        
        String[] vehicleTypes = {
            "Car (Sedan)", "Car (Hatchback)", "SUV", "SUV", "Motorcycle", 
            "Car (Sedan)", "SUV", "Scooter", "Car (Hatchback)", "Motorcycle", 
            "SUV", "Car (Sedan)", "Scooter", "SUV", "Truck", "Car (Sedan)"
        };
        String[] vehicleModels = {
            "Honda City", "Hyundai i20", "Toyota Fortuner", "Mahindra XUV700", "Royal Enfield Classic 350",
            "Maruti Suzuki Ciaz", "Tata Harrier", "TVS Jupiter", "Maruti Suzuki Swift", "Yamaha FZ",
            "Hyundai Creta", "Hyundai Verna", "Honda Activa", "Kia Seltos", "Tata Signa",
            "Maruti Suzuki Dzire"
        };
        
        // 3. Seed Users and Vehicles
        for (int i = 0; i < 16; i++) {
            String email = emails[i];
            String name = names[i];
            String plate = plates[i];
            
            // Generate deterministic UID to match JS: "user-" + btoa(email).substring(0, 10)
            String encoded = Base64.getEncoder().encodeToString(email.getBytes());
            String uid = "user-" + encoded.substring(0, 10);
            
            // Insert User
            Document userDoc = new Document("userId", uid)
                .append("name", name)
                .append("email", email)
                .append("role", "customer")
                .append("createdAt", Instant.now().toString())
                .append("isFrozen", false)
                .append("fineAmount", 0.0);
            usersCol.insertOne(userDoc);
            
            // Insert Vehicle
            Document vehicleDoc = new Document("vehicleNo", plate)
                .append("ownerId", uid)
                .append("ownerName", name)
                .append("vehicleType", vehicleTypes[i])
                .append("vehicleModel", vehicleModels[i])
                .append("insuranceStatus", "Valid")
                .append("registrationDate", "2024-01-15")
                .append("blacklistStatus", false);
            vehiclesCol.insertOne(vehicleDoc);
            
            System.out.println("Seeded User & Vehicle: " + name + " (" + plate + ")");
        }
        
        // 4. Seed 7 Incident Reports
        String[] reasons = {"Rash Driving", "Wrong Side Driving", "Overspeeding", "Illegal Parking", "Signal Jumping", "Noisy Exhaust", "Distracted Driving"};
        String[] descs = {
            "Driving very aggressively in a residential area.",
            "Vehicle came from the wrong side of the one-way street.",
            "Exceeded speed limit significantly on the highway.",
            "Parked in a strictly no-parking zone near the hospital.",
            "Drove through the red light at a busy intersection.",
            "Extremely loud aftermarket exhaust causing disturbance.",
            "Driver was using mobile phone while navigating a turn."
        };
        
        for (int i = 0; i < 7; i++) {
            Document reportDoc = new Document("reportId", "rep-" + i + "-" + System.currentTimeMillis() % 10000)
                .append("reporterId", "seeder")
                .append("vehicleNo", plates[i])
                .append("reason", reasons[i])
                .append("description", descs[i])
                .append("status", "Pending")
                .append("createdAt", Instant.now().toString());
            reportsCol.insertOne(reportDoc);
            System.out.println("Seeded Incident Report for: " + plates[i]);
        }
        
        System.out.println("Seeding completed successfully!");
        System.exit(0);
    }
}
