package com.example.eventlotterysystem;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.eventlotterysystem.model.CsvExporter;
import com.example.eventlotterysystem.model.EntrantDisplay;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Unit tests for CsvExporter
 * https://developer.android.com/training/testing/local-tests
 * https://medium.com/@chetanshingare2991/mastering-android-unit-testing-with-mockito-mocking-for-reliability-and-flexibility-93d42078d2ca
 * Covers:
 * - basic writing
 * - empty list
 * - null output stream
 * - null values for name/email
 * - escaping comma in name/email
 * - escaping quotes
 * - escaping newline characters
 * - single entrant test
 */
@RunWith(AndroidJUnit4.class)
public class CsvExporterTest {
    private Context mockContext;
    private ContentResolver mockResolver;
    private Uri mockUri;

    private ByteArrayOutputStream outputStream;

    private CsvExporter exporter;

    @Before
    public void setUp() {
        mockContext = mock(Context.class);
        mockResolver = mock(ContentResolver.class);
        mockUri = mock(Uri.class);

        outputStream = new ByteArrayOutputStream();

        when(mockContext.getApplicationContext()).thenReturn(mockContext);
        when(mockContext.getContentResolver()).thenReturn(mockResolver);

        exporter = new CsvExporter(mockContext);
    }
    /**
     * Test normal CSV writing with multiple entrants.
     */
    @Test
    public void testWriteCsv_basic() throws Exception {
        when(mockResolver.openOutputStream(mockUri)).thenReturn(outputStream);

        EntrantDisplay e1 = new EntrantDisplay("1", "John", "Doe", "john@test.com", 1);
        EntrantDisplay e2 = new EntrantDisplay("2", "Jane", "Doe", "jane@test.com", 2);

        List<EntrantDisplay> list = Arrays.asList(e1, e2);

        exporter.writeCsv(mockUri, list);

        String result = outputStream.toString();

        assertTrue(result.contains("name,email"));
        assertTrue(result.contains("John Doe,john@test.com"));
        assertTrue(result.contains("Jane Doe,jane@test.com"));
    }

    /**
     * Test empty entrant list (only the header should be written)
     */
    @Test
    public void testWriteCsv_emptyList() throws Exception {
        when(mockResolver.openOutputStream(mockUri)).thenReturn(outputStream);

        exporter.writeCsv(mockUri, Collections.emptyList());

        String result = outputStream.toString();

        assertEquals("name,email\n", result);
    }

    /**
     * Test null output stream throws IOException.
     */
    @Test(expected = IOException.class)
    public void testWriteCsv_outputStreamNull() throws Exception {
        when(mockResolver.openOutputStream(mockUri)).thenReturn(null);

        exporter.writeCsv(mockUri, Collections.emptyList());
    }

    /**
     * Test null values for name/email.
     */
    @Test
    public void testWriteCsv_nullValues() throws Exception {
        when(mockResolver.openOutputStream(mockUri)).thenReturn(outputStream);

        EntrantDisplay e = new EntrantDisplay();
        e.setFirstName(null);
        e.setLastName(null);
        e.setEmail(null);

        exporter.writeCsv(mockUri, Collections.singletonList(e));

        String result = outputStream.toString();

        assertTrue(result.contains("Unknown,")); // name becomes "Unknown"
    }

    /**
     * Test escaping comma in name/email.
     */
    @Test
    public void testWriteCsv_escapeComma() throws Exception {
        when(mockResolver.openOutputStream(mockUri)).thenReturn(outputStream);

        EntrantDisplay e = new EntrantDisplay(
                "1",
                "John, Jr",
                "Doe",
                "john,test@example.com",
                1
        );

        exporter.writeCsv(mockUri, Collections.singletonList(e));

        String result = outputStream.toString();

        assertTrue(result.contains("\"John, Jr Doe\""));
        assertTrue(result.contains("\"john,test@example.com\""));
    }

    /**
     * Test escaping quotes.
     */
    @Test
    public void testWriteCsv_escapeQuotes() throws Exception {
        when(mockResolver.openOutputStream(mockUri)).thenReturn(outputStream);

        EntrantDisplay e = new EntrantDisplay(
                "1",
                "John \"Johnny\"",
                "Doe",
                "john\"test@example.com",
                1
        );

        exporter.writeCsv(mockUri, Collections.singletonList(e));

        String result = outputStream.toString();

        assertTrue(result.contains("\"John \"\"Johnny\"\" Doe\""));
        assertTrue(result.contains("\"john\"\"test@example.com\""));
    }

    /**
     * Test escaping newline characters.
     */
    @Test
    public void testWriteCsv_escapeNewline() throws Exception {
        when(mockResolver.openOutputStream(mockUri)).thenReturn(outputStream);

        EntrantDisplay e = new EntrantDisplay(
                "1",
                "John\nDoe",
                "",
                "email\n@test.com",
                1
        );

        exporter.writeCsv(mockUri, Collections.singletonList(e));

        String result = outputStream.toString();

        assertTrue(result.contains("\"John\nDoe\""));
        assertTrue(result.contains("\"email\n@test.com\""));
    }

    /**
     * Test writer handles single entrant correctly.
     */
    @Test
    public void testWriteCsv_singleEntry() throws Exception {
        when(mockResolver.openOutputStream(mockUri)).thenReturn(outputStream);

        EntrantDisplay e = new EntrantDisplay("1", "Jaylin", "Dawn", "jaylin@test.com", 1);

        exporter.writeCsv(mockUri, Collections.singletonList(e));

        String result = outputStream.toString();

        assertTrue(result.contains("Jaylin Dawn,jaylin@test.com"));
    }
}
