package io.github.aktomik.deadcorpses.spawn;

import io.papermc.paper.datacomponent.item.ResolvableProfile;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Location;
import org.bukkit.entity.Mannequin;
import org.bukkit.entity.Player;
import org.bukkit.entity.Pose;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Iterator;

public class CorpseListener implements Listener {

	@EventHandler
	void OnDeath(PlayerDeathEvent event) {
		Location location = event.getEntity().getLocation();
		Player player = event.getPlayer();


		location.getWorld().spawn(location, Mannequin.class, mannequin -> {
			mannequin.setCollidable(false);
			mannequin.setPose(Pose.SLEEPING);

			mannequin.setProfile(ResolvableProfile.resolvableProfile(player.getPlayerProfile()));

			EntityEquipment playerEq = player.getEquipment();
			EntityEquipment mannequinEq = mannequin.getEquipment();
			mannequinEq.setHelmet(playerEq.getHelmet());
			mannequinEq.setChestplate(playerEq.getChestplate());
			mannequinEq.setLeggings(playerEq.getLeggings());
			mannequinEq.setBoots(playerEq.getBoots());
			mannequinEq.setItemInMainHand(playerEq.getItemInMainHand());
			mannequinEq.setItemInOffHand(playerEq.getItemInOffHand());
		});


		int droppedXp = event.getDroppedExp();
		event.setShouldDropExperience(false);
		Iterator<ItemStack> drops = event.getDrops().iterator();
		ArrayList<ItemStack> droppedItems = new ArrayList<>();
		while (drops.hasNext()) {
			ItemStack item = drops.next();
			droppedItems.add(item);
			drops.remove();
		}

		TagResolver resolvers = TagResolver.resolver(
			Placeholder.parsed("x", Integer.toString(location.getBlockX())),
			Placeholder.parsed("y", Integer.toString(location.getBlockY())),
			Placeholder.parsed("z", Integer.toString(location.getBlockZ()))
		);
		Component message = MiniMessage.miniMessage().deserialize("<blue>you died at <aqua><b><x> <y> <z></b></aqua>", resolvers);
		player.sendMessage(message);
	}
}
