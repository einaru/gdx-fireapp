package pl.mk5.gdx.fireapp.android.firestore;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import pl.mk5.gdx.fireapp.distributions.StoreDistribution;
import pl.mk5.gdx.fireapp.promises.FuturePromise;
import pl.mk5.gdx.fireapp.promises.Promise;
import pl.mk5.gdx.fireapp.firestore.GdxFirestoreCollection;

public class Firestore implements StoreDistribution {

    @Override
    public Promise<GdxFirestoreCollection> getCollection(String collectionPath) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference collection = db.collection(collectionPath);
        return FuturePromise.when(new CollectionPromiseConsumer<>(collection.get()));
    }
}
