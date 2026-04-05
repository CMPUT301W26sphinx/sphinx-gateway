package com.example.eventlotterysystem;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.eventlotterysystem.database.EntrantListFirebase;
import com.example.eventlotterysystem.model.EntrantListEntry;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Unit tests for EntrantListFirebase that require the android runtime to work
 *
 * Covers:
 * - getEntry
 * - getEntrantStatus
 * - getWaitlistCount
 * - getEntrantList
 * - getEntrantsByStatus
 */
@RunWith(AndroidJUnit4.class)
public class EntrantListFirebaseTest {

    private FirebaseFirestore mockDb;
    private CollectionReference mockEvents;
    private DocumentReference mockEventDoc;
    private CollectionReference mockEntrantList;
    private DocumentReference mockEntrantDoc;
    private Query mockQuery;

    private EntrantListFirebase firebase;

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
    /**
     * Waits for a Firebase Task to be done
     */
    private <T> T awaitTask(Task<T> task) throws Exception {
        Future<T> future = Executors.newSingleThreadExecutor().submit(new Callable<T>() {
            @Override
            public T call() throws Exception {
                return Tasks.await(task);
            }
        });
        return future.get();
    }

    /** Test getEntry when document exists */
    @Test
    public void testGetEntry_exists() throws Exception {
        DocumentSnapshot snapshot = mock(DocumentSnapshot.class);
        EntrantListEntry entry = mock(EntrantListEntry.class);

        when(mockEntrantDoc.get()).thenReturn(Tasks.forResult(snapshot));
        when(snapshot.exists()).thenReturn(true);
        when(snapshot.toObject(EntrantListEntry.class)).thenReturn(entry);

        EntrantListEntry result = awaitTask(firebase.getEntry("event1", "entrant1"));

        assertNotNull(result);
        assertEquals(entry, result);
    }
    /** Test getEntry when document does not exist */
    @Test
    public void testGetEntry_notExists() throws Exception {
        DocumentSnapshot snapshot = mock(DocumentSnapshot.class);

        when(mockEntrantDoc.get()).thenReturn(Tasks.forResult(snapshot));
        when(snapshot.exists()).thenReturn(false);

        EntrantListEntry result = awaitTask(firebase.getEntry("event1", "entrant1"));

        assertNull(result);
    }

    /** Test getEntrantStatus when entry exists */
    @Test
    public void testGetEntrantStatus_exists() throws Exception {
        DocumentSnapshot snapshot = mock(DocumentSnapshot.class);
        EntrantListEntry entry = mock(EntrantListEntry.class);

        when(mockEntrantDoc.get()).thenReturn(Tasks.forResult(snapshot));
        when(snapshot.exists()).thenReturn(true);
        when(snapshot.toObject(EntrantListEntry.class)).thenReturn(entry);
        when(entry.getStatus()).thenReturn(2);

        Integer result = awaitTask(firebase.getEntrantStatus("event1", "entrant1"));

        assertEquals(Integer.valueOf(2), result);
    }

    /** Test getEntrantStatus when entry does not exist */
    @Test
    public void testGetEntrantStatus_notExists() throws Exception {
        DocumentSnapshot snapshot = mock(DocumentSnapshot.class);


        when(mockEntrantDoc.get()).thenReturn(Tasks.forResult(snapshot));
        when(snapshot.exists()).thenReturn(false);

        Integer result = awaitTask(firebase.getEntrantStatus("event1", "entrant1"));

        assertEquals(Integer.valueOf(-1), result);
    }

    /** Test waitlist count success */
    @Test
    public void testGetWaitlistCount() throws Exception {
        QuerySnapshot snapshot = mock(QuerySnapshot.class);

        when(mockEntrantList.whereEqualTo("status", EntrantListEntry.STATUS_WAITLIST))
                .thenReturn(mockQuery);
        when(mockQuery.get()).thenReturn(Tasks.forResult(snapshot));
        when(snapshot.size()).thenReturn(4);

        Integer result = awaitTask(firebase.getWaitlistCount("event1"));

        assertEquals(Integer.valueOf(4), result);
    }
    /** Edge case: waitlist query failure returns 0 */
    @Test
    public void testGetWaitlistCount_failureReturnsZero() throws Exception {
        when(mockEntrantList.whereEqualTo("status", EntrantListEntry.STATUS_WAITLIST))
                .thenReturn(mockQuery);
        when(mockQuery.get()).thenReturn(Tasks.forException(new RuntimeException("query failed")));

        Integer result = awaitTask(firebase.getWaitlistCount("event1"));

        assertEquals(Integer.valueOf(0), result);
    }
    /** Test getting all entrants */
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

        List<EntrantListEntry> result = awaitTask(firebase.getEntrantList("event1"));

        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains(e1));
        assertTrue(result.contains(e2));
    }

    /** Edge case: null mapped entries are skipped */
    @Test
    public void testGetEntrantList_skipsNullMappedEntries() throws Exception {
        QuerySnapshot snapshot = mock(QuerySnapshot.class);
        DocumentSnapshot d1 = mock(DocumentSnapshot.class);
        DocumentSnapshot d2 = mock(DocumentSnapshot.class);
        EntrantListEntry e1 = mock(EntrantListEntry.class);

        when(mockEntrantList.get()).thenReturn(Tasks.forResult(snapshot));
        when(snapshot.getDocuments()).thenReturn(Arrays.asList(d1, d2));
        when(d1.toObject(EntrantListEntry.class)).thenReturn(e1);
        when(d2.toObject(EntrantListEntry.class)).thenReturn(null);

        List<EntrantListEntry> result = awaitTask(firebase.getEntrantList("event1"));

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(e1, result.get(0));
    }

    /** Edge case: query failure returns empty list */
    @Test
    public void testGetEntrantList_failureReturnsEmptyList() throws Exception {
        when(mockEntrantList.get()).thenReturn(Tasks.forException(new RuntimeException("query failed")));

        List<EntrantListEntry> result = awaitTask(firebase.getEntrantList("event1"));

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    /** Test getting entrants by status */
    @Test
    public void testGetEntrantsByStatus() throws Exception {
        QuerySnapshot snapshot = mock(QuerySnapshot.class);
        DocumentSnapshot doc = mock(DocumentSnapshot.class);
        EntrantListEntry entry = mock(EntrantListEntry.class);

        when(mockEntrantList.whereEqualTo("status", 1)).thenReturn(mockQuery);
        when(mockQuery.get()).thenReturn(Tasks.forResult(snapshot));
        when(snapshot.getDocuments()).thenReturn(Collections.singletonList(doc));
        when(doc.toObject(EntrantListEntry.class)).thenReturn(entry);

        List<EntrantListEntry> result = awaitTask(firebase.getEntrantsByStatus("event1", 1));

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(entry, result.get(0));
    }

    /** Edge case: null mapped entries are skipped in filtered query */
    @Test
    public void testGetEntrantsByStatus_skipsNullMappedEntries() throws Exception {
        QuerySnapshot snapshot = mock(QuerySnapshot.class);
        DocumentSnapshot doc = mock(DocumentSnapshot.class);

        when(mockEntrantList.whereEqualTo("status", 1)).thenReturn(mockQuery);
        when(mockQuery.get()).thenReturn(Tasks.forResult(snapshot));
        when(snapshot.getDocuments()).thenReturn(Collections.singletonList(doc));
        when(doc.toObject(EntrantListEntry.class)).thenReturn(null);

        List<EntrantListEntry> result = awaitTask(firebase.getEntrantsByStatus("event1", 1));

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}
