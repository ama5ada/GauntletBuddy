package org.gauntletbuddy.config;

import net.runelite.client.config.*;
import org.gauntletbuddy.config.types.GearTierType;
import org.gauntletbuddy.config.types.PrayerHighlightModeType;
import org.gauntletbuddy.config.types.SpecificationModeType;
import org.gauntletbuddy.config.types.TrackingModeType;

import java.awt.*;


@ConfigGroup("Gauntlet Buddy")
public interface GauntletBuddyConfig extends Config
{
	/**
	 * Item Tracking Config Section, holds config values related to whether or not to track items
	 */
	@ConfigSection(
			name = "Item Tracking",
			description = "Settings for items to gather with manual input totals",
			position = 1,
			closedByDefault = true
	)
	String itemTracking = "itemTracking";

	@ConfigItem(
			keyName = "trackingMode",
			name = "Tracking Mode",
			description = "Select mode for tracking items, counting up to or down from goal",
			section = itemTracking,
			position = 1
	)
	default TrackingModeType itemTrackingMode() {
		return TrackingModeType.COUNTDOWN;
	}

	@ConfigItem(
			keyName = "specificationMode",
			name = "Specification Mode",
			description = "Select manual specification of resources to gather or calculator version",
			section = itemTracking,
			position = 2
	)
	default SpecificationModeType itemTrackerSpecificationMode() {
		return SpecificationModeType.MANUAL;
	}

	@ConfigItem(
			keyName = "hideCompleted",
			name = "Hide Completed Items",
			description = "Toggle for hiding item counters for items that you don't need more of",
			section = itemTracking,
			position = 3
	)
	default boolean hideCompleted() { return true; }

	/**
	 * Item Tracking manual input section, holds fields to specify item goals manually
	 */
	@ConfigSection(
			name = "Manual Item Goals",
			description = "Section for manually inputting item goals",
			position = 2,
			closedByDefault = true
	)
	String itemsManual = "itemsManual";

	@ConfigItem(
			keyName = "specifiedCrystalOre",
			name = "Crystal Ore",
			description = "Manually specified amount of Crystal Ore to be gathered",
			section = itemsManual,
			position = 1
	)
	default int specifiedCrystalOre() { return 3; }

	@ConfigItem(
			keyName = "specifiedPhrenBark",
			name = "Phren Bark",
			description = "Manually specified amount of Phren Bark to be gathered",
			section = itemsManual,
			position = 2
	)
	default int specifiedPhrenBark() { return 3; }

	@ConfigItem(
			keyName = "specifiedLinumTirinium",
			name = "Linum Tirinium",
			description = "Manually specified amount of Linum Tirinium to be gathered",
			section = itemsManual,
			position = 3
	)
	default int specifiedLinumTirinium() { return 3; }

	@ConfigItem(
			keyName = "specifiedGrymLeaf",
			name = "Grym Leaf",
			description = "Manually specified amount of Grym Leaf to be gathered",
			section = itemsManual,
			position = 4
	)
	default int specifiedGrymLeaf() { return 3; }

	@ConfigItem(
			keyName = "specifiedWeaponFrames",
			name = "Weapon Frames",
			description = "Manually specified amount of Weapon Frames to be gathered",
			section = itemsManual,
			position = 5
	)
	default int specifiedWeaponFrames() { return 2; }

	@ConfigItem(
			keyName = "specifiedCrystalShards",
			name = "Crystal Shards",
			description = "Manually specified amount of Crystal Shards to be gathered",
			section = itemsManual,
			position = 6
	)
	default int specifiedCrystalShards() { return 350; }

	@ConfigItem(
			keyName = "specifiedPaddlefish",
			name = "Paddlefish",
			description = "Manually specified amount of Paddlefish to be gathered",
			section = itemsManual,
			position = 7
	)
	default int specifiedPaddlefish() { return 24; }

	@ConfigItem(
			keyName = "bowString",
			name = "Bow String",
			description = "Manually specify if you're looking for a Bow String",
			section = itemsManual,
			position = 8
	)
	default boolean specifiedBowString() { return false; }

	@ConfigItem(
			keyName = "orb",
			name = "Orb",
			description = "Manually specify if you're looking for an Orb",
			section = itemsManual,
			position = 9
	)
	default boolean specifiedOrb() { return false; }

	@ConfigItem(
			keyName = "spike",
			name = "Spike",
			description = "Manually specify if you're looking for a spike",
			section = itemsManual,
			position = 10
	)
	default boolean specifiedSpike() { return false; }

	/**
	 * Item Calculator Config Section, holds config values for calculating items that need to be tracked
	 */
	@ConfigSection(
			name = "Calculated Item Goals",
			description = "Settings for calculating items to gather based on gear choices",
			position = 3,
			closedByDefault = true
	)
	String itemsCalculator = "itemCalculator";

	@ConfigItem(
			keyName = "calculatorHelm",
			name = "Helm",
			description = "Tier of helm you want to create",
			section = itemsCalculator,
			position = 1
	)
	default GearTierType calculatorHelmetTier() {
		return GearTierType.T1;
	}

	@ConfigItem(
			keyName = "calculatorBody",
			name = "Body",
			description = "Tier of body you want to create",
			section = itemsCalculator,
			position = 2
	)
	default GearTierType calculatorBodyTier() {
		return GearTierType.T1;
	}

	@ConfigItem(
			keyName = "calculatorLegs",
			name = "Legs",
			description = "Tier of legs you want to create",
			section = itemsCalculator,
			position = 3
	)
	default GearTierType calculatorLegsTier() {
		return GearTierType.T1;
	}

	@ConfigItem(
			keyName = "calculatorBow",
			name = "Bow",
			description = "Tier of bow you want to create",
			section = itemsCalculator,
			position = 4
	)
	default GearTierType calculatorBowTier() {
		return GearTierType.T3;
	}

	@ConfigItem(
			keyName = "calculatorStaff",
			name = "Staff",
			description = "Tier of staff you want to create",
			section = itemsCalculator,
			position = 5
	)
	default GearTierType calculatorStaffTier() {
		return GearTierType.T2;
	}

	@ConfigItem(
			keyName = "calculatorHalberd",
			name = "Halberd",
			description = "Tier of halberd you want to create",
			section = itemsCalculator,
			position = 6
	)
	default GearTierType calculatorHalberdTier() {
		return GearTierType.None;
	}

	@ConfigItem(
			keyName = "calculatorPotions",
			name = "Enigol Potions",
			description = "Number of Enigol potions you want to create",
			section = itemsCalculator,
			position = 7
	)
	default int calculatorPotions() { return 3; }

	@ConfigItem(
			keyName = "calculatorTeleportCrystals",
			name = "Teleport Crystals",
			description = "Number of extra Teleport Crystals you want to create",
			section = itemsCalculator,
			position = 8
	)
	default int calculatorTeleportCrystals() { return 1; }

	@ConfigItem(
			keyName = "calculatorPaddlefish",
			name = "Paddlefish",
			description = "Number of Paddlefish you want to gather",
			section = itemsCalculator,
			position = 9
	)
	default int calculatorPaddlefish() { return 24; }

	/**
	 * Resource Highlighting Config Section, holds config values for highlighting different resources in The Gauntlet
	 */
	@ConfigSection(
			name = "Resource Highlights",
			description = "Settings for highlighting resources in The Gauntlet",
			position = 4,
			closedByDefault = true
	)
	String resourceHighlights = "resourceHighlights";

	/**
	 * NPC Highlighting Config Section, holds config values for highlighting different NPCs in The Gauntlet
	 */
	@ConfigSection(
			name = "NPC Highlights",
			description = "Settings for highlighting NPCs in The Gauntlet",
			position = 5,
			closedByDefault = true
	)
	String npcHighlights = "npcHighlights";

	/**
	 * Hunllef Config Section, holds config values for Hunllef fight utilities
	 */
	@ConfigSection(
			name = "Hunllef Utilities",
			description = "Settings for Hunllef fight utilities",
			position = 6,
			closedByDefault = true
	)
	String hunllefUtilities = "hunllefUtilities";

	@ConfigItem(
			keyName = "prayerHighlightMode",
			name = "Prayer Highlight Mode",
			description = "Different options for displaying the prayer highlight outline",
			position = 1,
			section = hunllefUtilities
	)
	default PrayerHighlightModeType hunllefPrayerHighlightMode() {
		return PrayerHighlightModeType.ALWAYS;
	}

	@ConfigItem(
			keyName = "prayerHighlightColor",
			name = "Prayer Highlight Color",
			description = "Change color of prayer highlight box",
			position = 2,
			section = hunllefUtilities
	)
	default Color prayerHighlightColor()
	{
		return new Color(0, 255, 100, 100);
	}

	@ConfigItem(
			keyName = "prayerMismatchHighlightColor",
			name = "Prayer MismatchColor",
			description = "Change the color of your prayer highlight when your current overhead is wrong",
			position = 3,
			section = hunllefUtilities
	)
	default Color prayerMismatchHighlightColor()
	{
		return new Color(255, 200, 0, 100);
	}

	@ConfigItem(
			keyName = "hunllefPrayerSwapAlert",
			name = "Hunllef Prayer Swap Alert",
			description = "Make your screen flash when your next attack will change the Hunllef active prayer",
			position = 4,
			section = hunllefUtilities
	)
	default boolean hunllefPrayerSwapAlert() { return true; }

	@ConfigItem(
			keyName = "hunllefHitTrackerMode",
			name = "Hunllef Hit Tracker",
			description = "Option to count hits off prayer on Hunllef",
			position = 5,
			section = hunllefUtilities
	)
	default TrackingModeType hunllefHitTrackerMode() { return TrackingModeType.COUNTDOWN; }

	/**
	 * Misc Config Section, holds config values for uncategorized utilities
	 */
	@ConfigSection(
			name = "Misc Utilities",
			description = "Settings for miscellaneous Gauntlet utilities",
			position = 7,
			closedByDefault = true
	)
	String miscUtilities = "miscUtilities";

	@ConfigItem(
			keyName = "debugList",
			name = "Debug List",
			description = "Show the list of private variables used by the plugin in the upper left",
			position = 1,
			section = miscUtilities
	)
	default boolean debugList() { return false; }

	@ConfigItem(
			keyName = "totalTimer",
			name = "Total Time",
			description = "Display a timer that counts up from entering The Gauntlet to track total kill time on screen",
			position = 2,
			section = miscUtilities
	)
	default boolean totalTimer() { return true; }

	@ConfigItem(
			keyName = "crystalReminder",
			name = "Crystal Reminder",
			description = "Display a chat reminder when you use the last teleport crystal in your inventory",
			position = 3,
			section = miscUtilities
	)
	default boolean crystalReminder() { return true; }

	@ConfigItem(
			keyName = "highlightStations",
			name = "Highlight Stations",
			description = "Outline starting room crafting stations to easily tell where they are from outside the room",
			position = 4,
			section = miscUtilities
	)
	default boolean highlightStations() { return false; }

	@Alpha
	@ConfigItem(
			keyName = "highlightStationsColor",
			name = "Station Color",
			description = "Color for the outline of starting room stations",
			position = 5,
			section = miscUtilities
	)
	default Color highlightStationsColor()
	{
		return new Color(255, 0, 255, 100);
	}

	@Range( max = 4 )
	@ConfigItem(
			keyName = "highlightStationsWidth",
			name = "Outline Width",
			description = "Width for the outline of starting room stations",
			position = 6,
			section = miscUtilities
	)
	@Units(Units.PIXELS)
	default int stationHighlightWidth()
	{
		return 2;
	}

}
