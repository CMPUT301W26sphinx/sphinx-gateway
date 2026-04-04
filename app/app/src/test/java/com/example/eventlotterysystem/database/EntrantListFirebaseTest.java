package com.example.eventlotterysystem.database;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import com.example.eventlotterysystem.model.EntrantListEntry;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.*;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Unit tests for EntrantListFirebase using Mockito.
 *
 * Now uses constructor injection instead of reflection.
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

        // ✅ Use new constructor (NO reflection)
        firebase = new EntrantListFirebase(mockDb);

        when(mockDb.collection("events")).thenReturn(mockEvents);
        when(mockEvents.document("event1")).thenReturn(mockEventDoc);
        when(mockEventDoc.collection("EntrantList")).thenReturn(mockEntrantList);
        when(mockEntrantList.document("entrant1")).thenReturn(mockEntrantDoc);
    }

    /** Test insert/update */
    @Test
    public void testUpsertEntry() {
        EntrantListEntry entry = mock(EntrantListEntry.class);
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

    /** Test get entry exists */
    @Test
    public void testGetEntry_exists() throws Exception {
        DocumentSnapshot snapshot = mock(DocumentSnapshot.class);
        EntrantListEntry entry = mock(EntrantListEntry.class);

        when(mockEntrantDoc.get()).thenReturn(Tasks.forResult(snapshot));
        when(snapshot.exists()).thenReturn(true);
        when(snapshot.toObject(EntrantListEntry.class)).thenReturn(entry);

        EntrantListEntry result = Tasks.await(firebase.getEntry("event1", "entrant1"));

        assertNotNull(result);
        assertEquals(entry, result);
    }

    /** Test get entry not exists */
    @Test
    public void testGetEntry_notExists() throws Exception {
        DocumentSnapshot snapshot = mock(DocumentSnapshot.class);

        when(mockEntrantDoc.get()).thenReturn(Tasks.forResult(snapshot));
        when(snapshot.exists()).thenReturn(false);

        EntrantListEntry result = Tasks.await(firebase.getEntry("event1", "entrant1"));

        assertNull(result);
    }

    /** Test get status exists */
    @Test
    public void testGetStatus_exists() throws Exception {
        DocumentSnapshot snapshot = mock(DocumentSnapshot.class);
        EntrantListEntry entry = mock(EntrantListEntry.class);

        when(mockEntrantDoc.get()).thenReturn(Tasks.forResult(snapshot));
        when(snapshot.exists()).thenReturn(true);
        when(snapshot.toObject(EntrantListEntry.class)).thenReturn(entry);
        when(entry.getStatus()).thenReturn(2);

        Integer result = Tasks.await(firebase.getEntrantStatus("event1", "entrant1"));

        assertEquals(Integer.valueOf(2), result);
    }

    /** Test get status not exists */
    @Test
    public void testGetStatus_notExists() throws Exception {
        DocumentSnapshot snapshot = mock(DocumentSnapshot.class);

        when(mockEntrantDoc.get()).thenReturn(Tasks.forResult(snapshot));
        when(snapshot.exists()).thenReturn(false);

        Integer result = Tasks.await(firebase.getEntrantStatus("event1", "entrant1"));

        assertEquals(Integer.valueOf(-1), result);
    }

    /** Test update status */
    @Test
    public void testUpdateStatus() {
        when(mockEntrantDoc.update("status", 3)).thenReturn(Tasks.forResult(null));

        Task<Void> result = firebase.updateStatus("event1", "entrant1", 3);

        assertTrue(result.isSuccessful());
        verify(mockEntrantDoc).update("status", 3);
    }

    /** Test waitlist count */
    @Test
    public void testWaitlistCount() throws Exception {
        QuerySnapshot snapshot = mock(QuerySnapshot.class);

        when(mockEntrantList.whereEqualTo("status", EntrantListEntry.STATUS_WAITLIST))
                .thenReturn(mockQuery);
        when(mockQuery.get()).thenReturn(Tasks.forResult(snapshot));
        when(snapshot.size()).thenReturn(4);

        Integer result = Tasks.await(firebase.getWaitlistCount("event1"));

        assertEquals(Integer.valueOf(4), result);
    }

    /** Test get all entrants */
    @Test
    public void testGetEntrantList() throws Exception {
        QuerySnapshot snapshot = mock(QuerySnapshot.class);
        DocumentSnapshot d1 = mock(DocumentSnapshot.class);
        DocumentSnapshot d2 = mock(DocumentSnapshot.class);

        EntrantListEntry e1 = mock(EntrantListEntry.class);
        EntrantListEntry e2 = mock(EntrantListEntry.class);

        when(mockEntrantList.get()).thenReturn(Tasks.forResult(snapshot));
        when(snapshot.getDocuments()).thenReturn(Arrays.asList(d1, d2));
        when(d1.toObject(EntrantListEntry.class)).thenReturn(e1);
        when(d2.toObject(EntrantListEntry.class)).thenReturn(e2);

        List<EntrantListEntry> result = Tasks.await(firebase.getEntrantList("event1"));

        assertEquals(2, result.size());
    }

    /** Test filter by status */
    @Test
    public void testGetByStatus() throws Exception {
        QuerySnapshot snapshot = mock(QuerySnapshot.class);
        DocumentSnapshot doc = mock(DocumentSnapshot.class);
        EntrantListEntry entry = mock(EntrantListEntry.class);

        when(mockEntrantList.whereEqualTo("status", 1)).thenReturn(mockQuery);
        when(mockQuery.get()).thenReturn(Tasks.forResult(snapshot));
        when(snapshot.getDocuments()).thenReturn(Collections.singletonList(doc));
        when(doc.toObject(EntrantListEntry.class)).thenReturn(entry);

        List<EntrantListEntry> result =
                Tasks.await(firebase.getEntrantsByStatus("event1", 1));

        assertEquals(1, result.size());
    }
}