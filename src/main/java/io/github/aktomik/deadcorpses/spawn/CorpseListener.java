package io.github.aktomik.deadcorpses.spawn;

import com.destroystokyo.paper.event.entity.EntityAddToWorldEvent;
import io.papermc.paper.datacomponent.item.ResolvableProfile;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Objects;

public class CorpseListener implements Listener {

	private final Plugin plugin;
	private final NamespacedKey PDC_CORPSE_KEY;
	private final NamespacedKey PDC_DROP_XP_KEY;
	private final NamespacedKey PDC_DROP_ITEMS_KEY;

	public CorpseListener(Plugin plugin) {
		this.plugin = plugin;
		PDC_CORPSE_KEY = new NamespacedKey(plugin, "corpse");
		PDC_DROP_XP_KEY = new NamespacedKey(plugin, "drop_xp");
		PDC_DROP_ITEMS_KEY = new NamespacedKey(plugin, "drop_items");
	}

	@EventHandler
	void onDeath(PlayerDeathEvent event) {
		Location location = event.getEntity().getLocation();
		Player player = event.getPlayer();
		if (player.getGameMode() == GameMode.SPECTATOR) return;


		int xpDrop = event.getDroppedExp();
		event.setShouldDropExperience(false);
		Iterator<ItemStack> drops = event.getDrops().iterator();
		ArrayList<ItemStack> itemsDrop = new ArrayList<>();
		while (drops.hasNext()) {
			ItemStack item = drops.next();
			itemsDrop.add(item);
			drops.remove();
		}
		if (xpDrop == 0 && itemsDrop.isEmpty()) return;


		Objects.requireNonNull(location).getWorld().spawn(location, Mannequin.class, corpse -> {
			corpse.setCollidable(false);
			corpse.setInvulnerable(true);
			corpse.setAI(false);
			corpse.setCanPickupItems(false);
			corpse.setPose(Pose.SLEEPING);
			corpse.setProfile(ResolvableProfile.resolvableProfile(player.getPlayerProfile()));

			EntityEquipment playerEq = player.getEquipment();
			EntityEquipment corpseEq = corpse.getEquipment();
			corpseEq.setHelmet(playerEq.getHelmet());
			corpseEq.setChestplate(playerEq.getChestplate());
			corpseEq.setLeggings(playerEq.getLeggings());
			corpseEq.setBoots(playerEq.getBoots());
			corpseEq.setItemInMainHand(playerEq.getItemInMainHand());
			corpseEq.setItemInOffHand(playerEq.getItemInOffHand());

			PersistentDataContainer corpsePdc = corpse.getPersistentDataContainer();
			corpsePdc.set(PDC_CORPSE_KEY, PersistentDataType.BOOLEAN, true);
			corpsePdc.set(PDC_DROP_XP_KEY, PersistentDataType.INTEGER, xpDrop);
		});

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
		Entity entity = event.getRightClicked();
		if (entity.getType() != EntityType.MANNEQUIN || !(entity instanceof LivingEntity)) return;
		if (entity.getLocation().distance(Objects.requireNonNull(player.getLocation())) > 3) return;

		PersistentDataContainer pdc = entity.getPersistentDataContainer();
		if (!pdc.has(PDC_CORPSE_KEY, PersistentDataType.BOOLEAN)) return;

		int xpDrop = pdc.getOrDefault(PDC_DROP_XP_KEY, PersistentDataType.INTEGER, 0);

		Location location = entity.getLocation();
		location.getWorld().spawn(location, ExperienceOrb.class, orb -> {
			orb.setExperience(xpDrop);
		});
		entity.remove();
	}

	@EventHandler
	public void onEntityLoad(EntityAddToWorldEvent event) {
		Entity entity = event.getEntity();
		if (entity.getType() != EntityType.MANNEQUIN || !(entity instanceof LivingEntity living)) return;

		PersistentDataContainer pdc = entity.getPersistentDataContainer();
		if (pdc.has(PDC_CORPSE_KEY, PersistentDataType.BOOLEAN)) {
			living.setCollidable(false);
			living.setInvulnerable(true);
		}
	}
}
