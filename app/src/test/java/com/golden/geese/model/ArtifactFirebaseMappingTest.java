package com.golden.geese.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.golden.geese.Artifact;
import com.google.firebase.database.core.utilities.encoding.CustomClassMapper;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeSet;

/**
 * Verifies that the seeded /artifacts records round-trip through the exact mapper the app uses.
 *
 * DataSnapshot.getValue(Artifact.class) — the call in
 * {@link FirebaseArtifactRepository#getAllArtifacts} — delegates straight to
 * CustomClassMapper.convertToCustomClass, and DatabaseReference.setValue(artifact) — the call in
 * {@link FirebaseArtifactRepository#addArtifact} — delegates to convertToPlainJavaTypes. Driving
 * those two entry points directly exercises the app's real serialization behaviour without needing
 * a device or a live database connection.
 */
public class ArtifactFirebaseMappingTest {

    /**
     * A record shaped exactly like the seeded data: the 15 Artifact keys, and nothing else.
     * The Realtime Database hands integral JSON numbers back as Long and JSON arrays as List,
     * so the fixture uses those types rather than int/double[].
     */
    private static Map<String, Object> seededRecord() {
        Map<String, Object> record = new LinkedHashMap<>();
        record.put("lotNum", 1L);
        record.put("name", "Celadon Bowl — 青瓷碗");
        record.put("description", "A celadon bowl with a lobed rim.");
        record.put("category", "Ceramics");
        record.put("material", "Ceramic");
        record.put("dynasty", "Song Dynasty (960-1279 CE)");
        record.put("origin", "");
        record.put("dimensions", Arrays.asList(0L, 0L, 0L));
        record.put("conditionReport", "");
        record.put("location", "");
        record.put("acqMethod", "");
        record.put("provenance", "");
        record.put("accessionNum", 0L);
        record.put("notes", "Period as catalogued: Southern Song Dynasty 南宋");
        record.put("image", "https://uvnrdrmwhkhlfxkgxzmk.supabase.co/storage/v1/object/public/"
                + "artifacts/artifacts/1/1785794919660.jpg");
        return record;
    }

    @Test
    public void seededRecordDeserializesIntoArtifact() {
        Artifact artifact = CustomClassMapper.convertToCustomClass(seededRecord(), Artifact.class);

        assertNotNull(artifact);
        assertEquals(1, artifact.getLotNum());
        assertEquals("Celadon Bowl — 青瓷碗", artifact.getName());
        assertEquals("Ceramics", artifact.getCategory());
        assertEquals("Ceramic", artifact.getMaterial());
        assertEquals("Song Dynasty (960-1279 CE)", artifact.getDynasty());
        assertEquals(0, artifact.getAccessionNum());
        assertEquals("Period as catalogued: Southern Song Dynasty 南宋", artifact.getNotes());
        assertTrue(artifact.getImage().startsWith("https://"));
    }

    /**
     * The seeded dimensions are the integral triple [0,0,0], which the database returns as Longs.
     * They still have to land in the List&lt;Double&gt; field as Doubles.
     */
    @Test
    public void seededDimensionsDeserialize() {
        Artifact artifact = CustomClassMapper.convertToCustomClass(seededRecord(), Artifact.class);

        assertNotNull("dimensions was not populated by the mapper", artifact.getDimensions());
        assertEquals(Arrays.asList(0.0, 0.0, 0.0), artifact.getDimensions());
    }

    /** Non-integral dimensions come back from the database as Double rather than Long. */
    @Test
    public void nonIntegralDimensionsDeserialize() {
        Map<String, Object> record = seededRecord();
        record.put("dimensions", Arrays.asList(12.5, 8.0, 3.25));

        Artifact artifact = CustomClassMapper.convertToCustomClass(record, Artifact.class);

        assertEquals(Arrays.asList(12.5, 8.0, 3.25), artifact.getDimensions());
    }

    /**
     * The seeded records deliberately omit likedBy/savedBy/comments. Deserializing a record without
     * them must still succeed — the app writes them later, on first like/save/comment.
     */
    @Test
    public void recordWithoutInteractionKeysStillDeserializes() {
        Map<String, Object> record = seededRecord();
        assertTrue(!record.containsKey("likedBy") && !record.containsKey("savedBy"));

        Artifact artifact = CustomClassMapper.convertToCustomClass(record, Artifact.class);

        assertNotNull(artifact);
        assertEquals(1, artifact.getLotNum());
    }

    /** The write path used by addArtifact(): setValue(artifact) serializes via this mapper. */
    @Test
    @SuppressWarnings("unchecked")
    public void artifactSerializesForWrite() {
        Artifact artifact = new Artifact(2, "Test", "Desc", "Ceramics", "Ceramic",
                "Song Dynasty (960-1279 CE)");

        Object serialized = CustomClassMapper.convertToPlainJavaTypes(artifact);

        assertTrue(serialized instanceof Map);
        Map<String, Object> fields = (Map<String, Object>) serialized;
        // The mapper narrows whole Doubles to Long, exactly as the seeded [0,0,0] is stored.
        assertEquals(Arrays.asList(0L, 0L, 0L), fields.get("dimensions"));
        for (String key : Arrays.asList("lotNum", "name", "description", "category", "material",
                "dynasty", "origin", "dimensions", "conditionReport", "location", "acqMethod",
                "provenance", "accessionNum", "notes", "image")) {
            assertTrue("addArtifact() would not write " + key, fields.containsKey(key));
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    public void writtenRecordCarriesOnlyTheModelKeys() {
        Artifact artifact = new Artifact(2, "Test", "Desc", "Ceramics", "Ceramic",
                "Song Dynasty (960-1279 CE)");

        Map<String, Object> fields =
                (Map<String, Object>) CustomClassMapper.convertToPlainJavaTypes(artifact);

        assertEquals("app-written records drifted from the expected shape",
                new TreeSet<>(Arrays.asList("lotNum", "name", "description", "category", "material",
                        "dynasty", "origin", "dimensions", "conditionReport", "location",
                        "acqMethod", "provenance", "accessionNum", "notes", "image",
                        "likedBy", "savedBy")),
                new TreeSet<>(fields.keySet()));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void newArtifactWritesEmptyInteractionLists() {
        Artifact artifact = new Artifact(2, "Test", "Desc", "Ceramics", "Ceramic",
                "Song Dynasty (960-1279 CE)");

        Map<String, Object> fields =
                (Map<String, Object>) CustomClassMapper.convertToPlainJavaTypes(artifact);

        assertEquals(Collections.emptyList(), fields.get("likedBy"));
        assertEquals(Collections.emptyList(), fields.get("savedBy"));
    }

    @Test
    public void likedByAndSavedByDeserializeOntoTheArtifact() {
        Map<String, Object> record = seededRecord();
        record.put("likedBy", Arrays.asList("uid-a", "uid-b"));
        record.put("savedBy", Collections.singletonList("uid-b"));

        Artifact artifact = CustomClassMapper.convertToCustomClass(record, Artifact.class);

        assertEquals(2, artifact.getLikeCount());
        assertTrue(artifact.isLikedBy("uid-a"));
        assertTrue(artifact.isSavedBy("uid-b"));
        assertFalse(artifact.isSavedBy("uid-a"));
    }

    @Test
    public void missingInteractionListsBecomeEmptyNotNull() {
        Artifact artifact = CustomClassMapper.convertToCustomClass(seededRecord(), Artifact.class);

        assertNotNull(artifact.getLikedBy());
        assertNotNull(artifact.getSavedBy());
        assertEquals(0, artifact.getLikeCount());
        assertFalse(artifact.isLikedBy("uid-a"));
    }

    @Test
    public void unknownUidIsNeverTreatedAsALike() {
        Artifact artifact = new Artifact();
        artifact.addLike("uid-a");

        assertFalse(artifact.isLikedBy(null));
        assertFalse(artifact.isSavedBy(null));
    }

    @Test
    public void repeatedLikeCountsOnce() {
        Artifact artifact = new Artifact();

        artifact.addLike("uid-a");
        artifact.addLike("uid-a");

        assertEquals(1, artifact.getLikeCount());

        artifact.removeLike("uid-a");

        assertEquals(0, artifact.getLikeCount());
        assertFalse(artifact.isLikedBy("uid-a"));
    }

    /**
     * An artifact written by the app must be readable again by the same mapper, so records the app
     * creates stay compatible with the seeded ones.
     */
    @Test
    public void appWrittenArtifactCanBeReadBack() {
        Artifact original = new Artifact(2, "Test", "Desc", "Ceramics", "Ceramic",
                "Song Dynasty (960-1279 CE)");
        original.setDimensions(Arrays.asList(12.5, 8.0, 3.25));

        Object serialized = CustomClassMapper.convertToPlainJavaTypes(original);
        Artifact restored = CustomClassMapper.convertToCustomClass(serialized, Artifact.class);

        assertEquals(original.getLotNum(), restored.getLotNum());
        assertEquals(original.getName(), restored.getName());
        assertEquals(original.getDimensions(), restored.getDimensions());
    }
}
