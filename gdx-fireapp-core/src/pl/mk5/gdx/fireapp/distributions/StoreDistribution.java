package pl.mk5.gdx.fireapp.distributions;

import pl.mk5.gdx.fireapp.promises.Promise;
import pl.mk5.gdx.fireapp.firestore.GdxFirestoreCollection;

public interface StoreDistribution {

    Promise<GdxFirestoreCollection> getCollection(String collectionId);
}
