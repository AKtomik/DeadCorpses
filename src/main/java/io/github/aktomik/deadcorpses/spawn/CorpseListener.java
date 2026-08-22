package io.github.aktomik.deadcorpses.spawn;

import com.destroystokyo.paper.event.entity.EntityAddToWorldEvent;
import io.github.aktomik.deadcorpses.pdc.ItemArrayDataType;
import io.papermc.paper.datacomponent.item.ResolvableProfile;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.util.*;

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
	void onPlayerDeath(PlayerDeathEvent event) {
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

			PersistentDataContainer pdc = corpse.getPersistentDataContainer();
			pdc.set(PDC_CORPSE_KEY, PersistentDataType.BOOLEAN, true);
			pdc.set(PDC_DROP_XP_KEY, PersistentDataType.INTEGER, xpDrop);
			pdc.set(PDC_DROP_ITEMS_KEY, ItemArrayDataType.INSTANCE, itemsDrop.toArray(new ItemStack[] {}));
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
	public void onMannequinDeath(EntityDeathEvent event) {
		Entity entity = event.getEntity();
		if (!(event.getEntity() instanceof Mannequin)) return;
		PersistentDataContainer pdc = entity.getPersistentDataContainer();
		if (!pdc.has(PDC_CORPSE_KEY, PersistentDataType.BOOLEAN)) return;

		int xpDrop = pdc.getOrDefault(PDC_DROP_XP_KEY, PersistentDataType.INTEGER, 0);
		List<ItemStack> itemsDrop = Arrays.stream(pdc.getOrDefault(PDC_DROP_ITEMS_KEY, ItemArrayDataType.INSTANCE, new ItemStack[] {})).toList();

		event.getDrops().clear();
		event.getDrops().addAll(itemsDrop);
		event.setDroppedExp(xpDrop);
	}

	@EventHandler
	void onClickOnEntity(PlayerInteractEntityEvent event) {
		Entity entity = event.getRightClicked();
		if (!(entity instanceof Mannequin mannequin)) return;
		Player player = event.getPlayer();
		if (entity.getLocation().distance(Objects.requireNonNull(player.getLocation())) > 3) return;
		PersistentDataContainer pdc = entity.getPersistentDataContainer();
		if (!pdc.has(PDC_CORPSE_KEY, PersistentDataType.BOOLEAN)) return;

		mannequin.kill();
	}

	@EventHandler
	public void onEntityLoad(EntityAddToWorldEvent event) {
		Entity entity = event.getEntity();
		if (!(entity instanceof Mannequin corpse)) return;

		PersistentDataContainer pdc = entity.getPersistentDataContainer();
		if (pdc.has(PDC_CORPSE_KEY, PersistentDataType.BOOLEAN)) {
			corpse.setCollidable(false);
			corpse.setInvulnerable(true);
		}
	}
}
