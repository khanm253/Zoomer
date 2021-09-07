package ca.utoronto.utm.mcs;

import com.mongodb.Block;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.DeleteResult;
import org.bson.Document;
import com.mongodb.client.result.UpdateResult;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class TripDAO {

    private MongoCollection mTripCollection;

    public TripDAO(MongoCollection collection) {
        mTripCollection = collection;
    }

    public Trip createTrip(Trip trip) {
        Document tripDoc = TripConverter.toDocument(trip);
        mTripCollection.insertOne(tripDoc);
        ObjectId tripId = (ObjectId) tripDoc.get("_id");
        trip.setId(tripId.toString());
        return trip;
    }

    public UpdateResult updateTrip(Document tripDoc, Document newTripDoc) {
        UpdateResult result =  mTripCollection.updateOne(tripDoc, new Document("$set", newTripDoc));
        return result;
    }

    public Trip getTrip(ObjectId _id) {
        Document queryDoc = (Document) mTripCollection.find(Filters.eq("_id", _id)).first();
        if (queryDoc == null) {
            return null;
        }
        return TripConverter.toTrip(queryDoc);
    }

    public List<Trip> getTripsForPassenger(String uid) {
        List<Trip> tripList = new ArrayList<>();
        mTripCollection.find(Filters.eq("passenger", uid)).forEach(new Block() {
            @Override
            public void apply(Object o) {
                tripList.add(TripConverter.toTrip((Document) o));
            }
        });
        return tripList;
    }

    public List<Trip> getTripsForDriver(String uid) {
        List<Trip> tripList = new ArrayList<>();
        mTripCollection.find(Filters.eq("driver", uid)).forEach(new Block() {
            @Override
            public void apply(Object o) {
                tripList.add(TripConverter.toTrip((Document) o));
            }
        });
        return tripList;
    }

    public boolean isTripDuplicated(String driver, String passenger, long startTime) {
        Document queryDoc = (Document) mTripCollection.find(
                Filters.and(
                        Filters.eq("driver", driver),
                        Filters.eq("passenger", passenger),
                        Filters.eq("startTime", startTime)
                )).first();
        return queryDoc != null;
    }

    public boolean doesPassengerExist(String uid) {
        Document queryDoc = (Document) mTripCollection.find(Filters.eq("passenger", uid)).first();
        return queryDoc != null;
    }

    public boolean doesDriverExist(String uid) {
        Document queryDoc = (Document) mTripCollection.find(Filters.eq("driver", uid)).first();
        return queryDoc != null;
    }

    public void _deleteTestTrips() {
        Pattern pattern = Pattern.compile("^TESTING__.*$");
        DeleteResult deleteResult = mTripCollection.deleteMany(Filters.regex("driver",pattern));
    }

}
