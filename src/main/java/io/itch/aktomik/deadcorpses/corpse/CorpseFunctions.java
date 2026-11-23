package io.itch.aktomik.deadcorpses.corpse;

import io.papermc.paper.datacomponent.item.ResolvableProfile;
import org.bukkit.entity.Mannequin;
import org.bukkit.entity.Player;
import org.bukkit.entity.Pose;
import org.bukkit.inventory.EntityEquipment;

public class CorpseFunctions {
	private CorpseFunctions() {}

	public static void applyCorpseProperties(Mannequin corpse) {
		corpse.setCollidable(false);
		corpse.setInvulnerable(true);
		corpse.setAI(false);
		corpse.setCanPickupItems(false);
		corpse.setPose(Pose.SLEEPING);
	}

	public static void morphAndEquipCorpse(Mannequin corpse, Player player) {
		corpse.setProfile(ResolvableProfile.resolvableProfile(player.getPlayerProfile()));
		EntityEquipment playerEq = player.getEquipment();
		EntityEquipment corpseEq = corpse.getEquipment();
		corpseEq.setHelmet(playerEq.getHelmet());
		corpseEq.setChestplate(playerEq.getChestplate());
		corpseEq.setLeggings(playerEq.getLeggings());
		corpseEq.setBoots(playerEq.getBoots());
		corpseEq.setItemInMainHand(playerEq.getItemInMainHand());
		corpseEq.setItemInOffHand(playerEq.getItemInOffHand());
	}
}
