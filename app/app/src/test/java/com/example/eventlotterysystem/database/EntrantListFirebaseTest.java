package com.example.eventlotterysystem.database;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import com.example.eventlotterysystem.model.EntrantListEntry;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.*;

import org.junit.Before;
import org.junit.Test;


/**
 * Unit tests for EntrantListFirebase using Mockito.
 *
 * Generated tests with help from Chatgpt
 * "How do I use mocks in JUnit tests for Firebase Firestore?"
 * and for debugging issues with mock calls
 */
public class EntrantListFirebaseTest {

    private FirebaseFirestore mockDb;
    private CollectionReference mockEvents;
    private DocumentReference mockEventDoc;
    private CollectionReference mockEntrantList;
    private DocumentReference mockEntrantDoc;
    private Query mockQuery;

    private EntrantListFirebase firebase;

    /**
     * Setup mock Firestore structure:
     * events -> event1 -> EntrantList -> entrant1
     */
    @Before
    public void setUp() {

        mockDb = mock(FirebaseFirestore.class);
        mockEvents = mock(CollectionReference.class);
        mockEventDoc = mock(DocumentReference.class);
        mockEntrantList = mock(CollectionReference.class);
        mockEntrantDoc = mock(DocumentReference.class);
        mockQuery = mock(Query.class);

        firebase = new EntrantListFirebase(mockDb);

        when(mockDb.collection("events")).thenReturn(mockEvents);
        when(mockEvents.document("event1")).thenReturn(mockEventDoc);
        when(mockEventDoc.collection("EntrantList")).thenReturn(mockEntrantList);
        when(mockEntrantList.document("entrant1")).thenReturn(mockEntrantDoc);
    }

    /** Test insert/update */
    @Test
    public void testUpsertEntry() {
        EntrantListEntry entry = mock(EntrantListEntry.class); // mock entry to test
        when(entry.getEntrantId()).thenReturn("entrant1");
        when(mockEntrantDoc.set(entry)).thenReturn(Tasks.forResult(null));
        Task<Void> result = firebase.upsertEntry("event1", entry);
        assertTrue(result.isSuccessful());
        verify(mockEntrantDoc).set(entry);
    }

    /** Test delete */
    @Test
    public void testRemoveEntry() {
        when(mockEntrantDoc.delete()).thenReturn(Tasks.forResult(null));

        Task<Void> result = firebase.removeEntrantListEntry("event1", "entrant1");

        assertTrue(result.isSuccessful());
        verify(mockEntrantDoc).delete();
    }

    /** Test update status */
    @Test
    public void testUpdateStatus() {
        when(mockEntrantDoc.update("status", 3)).thenReturn(Tasks.forResult(null));

        Task<Void> result = firebase.updateStatus("event1", "entrant1", 3);

        assertTrue(result.isSuccessful());
        verify(mockEntrantDoc).update("status", 3);
    }


}