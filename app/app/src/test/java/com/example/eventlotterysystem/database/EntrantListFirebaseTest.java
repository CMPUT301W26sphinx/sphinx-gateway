package com.example.eventlotterysystem.database;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.eventlotterysystem.model.EntrantListEntry;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;

import java.util.Arrays;
import java.util.List;

/**
 * This class is used to test the methods used for connecting EntrantList to the db
 */
// I used Microsoft Copilot to help generate how to do these test cases, and specifically the functions
// "How do I write test cases for Firebase without modifying the db? Android Studio"
// "Can you help generate some test cases for this file: ..."
// Recommended by Prof. Henry Tang:)
public class EntrantListFirebaseTest {
    private static final String EVENT_ID = "event123";
    private static final String ENTRANT_ID = "entrant123";

    private FirebaseFirestore mockDb;
    private CollectionReference mockEntrantListRef;
    private CollectionReference mockEventsRef;
    private DocumentReference mockEventDocRef;
    private DocumentReference mockEntrantDocRef;
    private Query mockQuery;

    private MockedStatic<FirebaseFirestore> firebaseFirestoreStaticMock;

    private EntrantListFirebase entrantListFirebase;

    @Before
    public void setUp() {
        mockDb = mock(FirebaseFirestore.class);
        mockEventsRef = mock(CollectionReference.class);
        mockEntrantListRef = mock(CollectionReference.class);
        mockEventDocRef = mock(DocumentReference.class);
        mockEntrantDocRef = mock(DocumentReference.class);
        mockQuery = mock(Query.class);

        firebaseFirestoreStaticMock = mockStatic(FirebaseFirestore.class);
        firebaseFirestoreStaticMock.when(FirebaseFirestore::getInstance).thenReturn(mockDb);

        when(mockDb.collection("events")).thenReturn(mockEventsRef);
        when(mockEventsRef.document(EVENT_ID)).thenReturn(mockEventDocRef);
        when(mockEventDocRef.collection("EntrantList")).thenReturn(mockEntrantListRef);
        when(mockEntrantListRef.document(ENTRANT_ID)).thenReturn(mockEntrantDocRef);

        entrantListFirebase = new EntrantListFirebase();
    }

    @After //remove the testing additions
    public void tearDown() {
        firebaseFirestoreStaticMock.close();
    }

}

