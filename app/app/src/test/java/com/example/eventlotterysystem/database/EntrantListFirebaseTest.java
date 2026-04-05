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
 * covers:
 * - upsertEntry
 * - upsert failure
 * - removeEntrantListEntry
 * - remove failure
 * - update status
 * - update status failure
 * - update status with negative status
 *
 * coverage for other methods in EntrantListFirebaseTest under Android tests due to difficulty with
 * use of continue functions? (error where task not completing etc)
 *
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

    /**
     * Test insert/update failure
     */
    @Test
    public void testUpsertEntry_failure() {
        EntrantListEntry entry = mock(EntrantListEntry.class);
        when(entry.getEntrantId()).thenReturn("entrant1");

        Exception ex = new RuntimeException("write failed");
        when(mockEntrantDoc.set(entry)).thenReturn(Tasks.forException(ex));

        Task<Void> result = firebase.upsertEntry("event1", entry);


        assertFalse(result.isSuccessful());
        assertEquals(ex, result.getException());
    }
    /** Test delete */
    @Test
    public void testRemoveEntry() {
        when(mockEntrantDoc.delete()).thenReturn(Tasks.forResult(null));

        Task<Void> result = firebase.removeEntrantListEntry("event1", "entrant1");

        assertTrue(result.isSuccessful());
        verify(mockEntrantDoc).delete();
    }

    /**
     * Test delete failure
     */
    @Test
    public void testRemoveEntry_failure() {
        Exception ex = new RuntimeException("delete failed");
        when(mockEntrantDoc.delete()).thenReturn(Tasks.forException(ex));

        Task<Void> result = firebase.removeEntrantListEntry("event1", "entrant1");

        assertFalse(result.isSuccessful());
        assertEquals(ex, result.getException());
    }

    /** Test update status */
    @Test
    public void testUpdateStatus() {
        when(mockEntrantDoc.update("status", 3)).thenReturn(Tasks.forResult(null));

        Task<Void> result = firebase.updateStatus("event1", "entrant1", 3);

        assertTrue(result.isSuccessful());
        verify(mockEntrantDoc).update("status", 3);
    }

    /**
     * Test update status failure
     */
    @Test
    public void testUpdateStatus_failure() {
        Exception ex = new RuntimeException("update failed");
        when(mockEntrantDoc.update("status", 3)).thenReturn(Tasks.forException(ex));

        Task<Void> result = firebase.updateStatus("event1", "entrant1", 3);

        assertFalse(result.isSuccessful());
        assertEquals(ex, result.getException());
        verify(mockEntrantDoc).update("status", 3);
    }
    /**
     * Test update status with negative status
     */
    @Test
    public void testUpdateStatus_withNegativeStatus_stillPassesValueToFirestore() {
        when(mockEntrantDoc.update("status", -1)).thenReturn(Tasks.forResult(null));

        Task<Void> result = firebase.updateStatus("event1", "entrant1", -1);

        assertTrue(result.isSuccessful());
        verify(mockEntrantDoc).update("status", -1); //should still be allowed
    }
}