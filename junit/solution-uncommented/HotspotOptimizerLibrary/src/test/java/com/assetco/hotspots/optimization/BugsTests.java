package com.assetco.hotspots.optimization;

import com.assetco.hotspots.optimization.*;
import com.assetco.search.results.*;
import org.junit.jupiter.api.*;

import java.math.*;
import java.util.*;

import static com.assetco.search.results.AssetVendorRelationshipLevel.*;
import static com.assetco.search.results.HotspotKey.*;
import static org.junit.jupiter.api.Assertions.*;

public class BugsTests {

    private final int maximumShowcaseItems = 4;
    private SearchResults searchResults;
    private AssetVendor partnerVendor;
    private SearchResultHotspotOptimizer optimizer;
    int topicId = 0;

    @BeforeEach
    public void setUp() {
        optimizer = new SearchResultHotspotOptimizer();
        searchResults = new SearchResults();
        partnerVendor = makeVendor(Partner);
    }

    @Test
    public void precedingPartnerWithLongTrailingAssetsDoesNotWin() {
        var missing = givenAssetInResultsWithVendor(partnerVendor);
        givenAssetInResultsWithVendor(makeVendor(Partner));
        var expected = givenAssetsInResultsWithVendor(maximumShowcaseItems, partnerVendor);

        whenOptimize();

        thenHotspotDoesNotHave(Showcase, missing);
        thenHotspotHasExactly(Showcase, expected);
    }

    private AssetVendor makeVendor(AssetVendorRelationshipLevel relationshipLevel) {
        return new AssetVendor("test_vendor", "test_bugs", relationshipLevel, 1);
    }

    private Asset givenAssetInResultsWithVendor(AssetVendor vendor) {
        Asset result = getAsset(vendor);
        searchResults.addFound(result);
        return result;
    }

    private Asset getAsset(AssetVendor vendor) {
        return new Asset("test_vendor", "test_bugs", null, null, getPurchaseInfo(), getPurchaseInfo(), new ArrayList<>(), vendor);
    }

    private AssetPurchaseInfo getPurchaseInfo() {
        return new AssetPurchaseInfo(0, 0, new Money(new BigDecimal("0")), new Money(new BigDecimal("0")));
    }

    private void thenHotspotHasExactly(HotspotKey hotspotKey, List<Asset> expected) {
        Assertions.assertArrayEquals(expected.toArray(), searchResults.getHotspot(hotspotKey).getMembers().toArray());
    }

    private ArrayList<Asset> givenAssetsInResultsWithVendor(int count, AssetVendor vendor) {
        var result = new ArrayList<Asset>();
        for (var i = 0; i < count; ++i) {
            result.add(givenAssetInResultsWithVendor(vendor));
        }
        return result;
    }

    private void whenOptimize() {
        optimizer.optimize(searchResults);
    }

    private void thenHotspotDoesNotHave(HotspotKey key, Asset... forbidden) {
        for (var asset : forbidden) {
            assertFalse(searchResults.getHotspot(key).getMembers().contains(asset));
        }
    }

    private List<Asset> givenAssetsWithTopics(AssetVendor vendor, int count, AssetTopic... topics) {
        var result = new ArrayList<Asset>();
        for (var i = 0; i < count; ++i)
            result.add(givenAssetWithTopics(vendor, topics));

        return result;
    }

    private Asset givenAssetWithTopics(AssetVendor vendor, AssetTopic... topics) {
        var actualTopics = new ArrayList<AssetTopic>();
        for (var topic : topics)
            actualTopics.add(new AssetTopic(topic.getId(), topic.getDisplayName()));

        var result = new Asset(null, null, null, null, getPurchaseInfo(), getPurchaseInfo(), actualTopics, vendor);
        searchResults.addFound(result);
        return result;
    }

    private AssetTopic makeTopic() {
        return new AssetTopic("id-" + (++topicId), "anything");
    }

    private void setHotTopics(AssetTopic... topics) {
        optimizer.setHotTopics(() -> Arrays.asList(topics));
    }

    private void thenHotspotDoesNotHave(HotspotKey key, List<Asset> forbidden) {
        for (var asset : forbidden)
            assertFalse(searchResults.getHotspot(key).getMembers().contains(asset));
    }

    private void thenHotspotHas(HotspotKey hotspotKey, List<Asset> expectedAssets) {
        for (var expectedAsset : expectedAssets) {
            Assertions.assertTrue(searchResults.getHotspot(hotspotKey).getMembers().contains(expectedAsset));
        }
    }
}
