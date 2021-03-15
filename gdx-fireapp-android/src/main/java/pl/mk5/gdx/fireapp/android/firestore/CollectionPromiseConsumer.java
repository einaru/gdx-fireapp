package pl.mk5.gdx.fireapp.android.firestore;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import pl.mk5.gdx.fireapp.firestore.GdxFirestoreCollection;
import pl.mk5.gdx.fireapp.functional.Consumer;
import pl.mk5.gdx.fireapp.promises.FuturePromise;

public class CollectionPromiseConsumer<T> implements Consumer<FuturePromise<GdxFirestoreCollection>> {

    private Task<T> task;

    public CollectionPromiseConsumer(Task<T> task) {
        this.task = task;
    }

    @Override
    public void accept(final FuturePromise<GdxFirestoreCollection> promise) {
        if (task != null) {
            task.addOnCompleteListener(new OnCompleteListener<T>() {
                   @Override
                   public void onComplete(@NonNull Task<T> task) {
                       if (task.isSuccessful()) {
                           promise.doComplete();
                       } else {
                           promise.doFail(task.getException() != null ? task.getException().getLocalizedMessage() : "Fetching collection failed!", task.getException());
                       }
                   }
               }
            );
        }
    }
}
