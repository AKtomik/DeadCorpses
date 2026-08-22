package io.github.aktomik.deadcorpses.spawn;

import com.destroystokyo.paper.event.entity.EntityAddToWorldEvent;
import io.papermc.paper.datacomponent.item.ResolvableProfile;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Iterator;

public class CorpseListener implements Listener {

	final private Plugin plugin;
	public CorpseListener(Plugin plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	void OnDeath(PlayerDeathEvent event) {
		Location location = event.getEntity().getLocation();
		Player player = event.getPlayer();


		location.getWorld().spawn(location, Mannequin.class, mannequin -> {
			mannequin.setCollidable(false);
			mannequin.setInvulnerable(true);
			mannequin.setAI(false);
			mannequin.setCanPickupItems(false);
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

			NamespacedKey key = new NamespacedKey(plugin, "corpse");
			mannequin.getPersistentDataContainer().set(key, PersistentDataType.BOOLEAN, true);
			mannequin.setCollidable(false);
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


	@EventHandler
	void onEntityClick(PlayerInteractEntityEvent event) {
		Player player = event.getPlayer();
		Entity target = event.getRightClicked();
		if (target.getType() != EntityType.MANNEQUIN) return;
		if (target.getLocation().distance(player.getLocation()) > 3) return;
		target.remove();
	}

	@EventHandler
	public void onEntityLoad(EntityAddToWorldEvent event) {
		Entity entity = event.getEntity();
		if (!(entity instanceof LivingEntity living)) return;

		NamespacedKey key = new NamespacedKey(plugin, "corpse");
		if (living.getPersistentDataContainer().has(key, PersistentDataType.BOOLEAN)) {
			living.setCollidable(false);
			living.setInvulnerable(true);
		}
	}
}
