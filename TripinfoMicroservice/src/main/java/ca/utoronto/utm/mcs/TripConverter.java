package ca.utoronto.utm.mcs;

import com.mongodb.DBObject;
import org.bson.Document;
import org.bson.types.ObjectId;

public class TripConverter {


    public static Document toDocument(Trip trip) {
        Document tripDoc = new Document();
        tripDoc.append("distance", trip.getDistance());
        tripDoc.append("totalCost", trip.getTotalCost());
        tripDoc.append("startTime", trip.getStartTime());
        tripDoc.append("endTime", trip.getEndTime());
        tripDoc.append("timeElapsed", trip.getTimeElapsed());
        tripDoc.append("driver", trip.getDriver());
        tripDoc.append("driverPayout", trip.getDriverPayout());
        tripDoc.append("passenger", trip.getPassenger());
        tripDoc.append("discount", trip.getDiscount());

        if (trip.getId() != null) {
            tripDoc.append("_id", new ObjectId(trip.getId()));
        }
        return tripDoc;
    }

    public static Trip toTrip(Document doc) {
        Trip trip = new Trip();
        ObjectId id = (ObjectId) doc.get("_id");
        trip.setId(id.toString());
        trip.setDistance((Double) doc.get("distance"));
        trip.setTotalCost((Double) doc.get("totalCost"));
        trip.setStartTime((long) doc.get("startTime"));
        trip.setEndTime((long) doc.get("endTime"));
        trip.setTimeElapsed((String) doc.get("timeElapsed"));
        trip.setDriver((String) doc.get("driver"));
        trip.setDriverPayout((double) doc.get("driverPayout"));
        trip.setPassenger((String) doc.get("passenger"));
        trip.setDiscount((int) doc.get("discount"));
        return trip;
    }

}
