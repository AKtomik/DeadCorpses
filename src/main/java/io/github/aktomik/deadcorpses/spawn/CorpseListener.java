package io.github.aktomik.deadcorpses.spawn;

import com.destroystokyo.paper.event.entity.EntityAddToWorldEvent;
import io.github.aktomik.deadcorpses.pdc.ItemArrayDataType;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Mannequin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.util.*;

public class CorpseListener implements Listener {

	private final NamespacedKey corpsePdcKey;
	private final NamespacedKey dropXpPdcKey;
	private final NamespacedKey dropItemsPdcKey;

	// config ideas:
	// - deathSendCoordinates
	// - corpseIsInvulnerable
	// - corpseLargerHitbox
	// - corpseDamageByPlayer
	// - corpseDamageByHot
	// - corpseDamageByOther
	// - pickupByOthers
	// - pickupParticleAmount
	// - itemEnableGive
	// - itemGiveToSlot
	// - xpLossEnableCustom
	// - xpLossCustomPercentage
	// - xpLossCustomMax

	public CorpseListener(Plugin plugin) {
		corpsePdcKey = new NamespacedKey(plugin, "corpse");
		dropXpPdcKey = new NamespacedKey(plugin, "drop_xp");
		dropItemsPdcKey = new NamespacedKey(plugin, "drop_items");
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

		Objects.requireNonNull(location).getWorld().spawn(location, Mannequin.class, corpse -> {
			CorpseFunctions.applyCorpseProperties(corpse);
			CorpseFunctions.morphAndEquipCorpse(corpse, player);

			PersistentDataContainer pdc = corpse.getPersistentDataContainer();
			pdc.set(corpsePdcKey, PersistentDataType.BOOLEAN, true);
			pdc.set(dropXpPdcKey, PersistentDataType.INTEGER, xpDrop);
			pdc.set(dropItemsPdcKey, ItemArrayDataType.INSTANCE, itemsDrop.toArray(new ItemStack[] {}));
		});
	}

	@EventHandler
	public void onCorpseDeath(EntityDeathEvent event) {
		Entity entity = event.getEntity();
		if (!(event.getEntity() instanceof Mannequin mannequin)) return;
		PersistentDataContainer pdc = entity.getPersistentDataContainer();
		if (!pdc.has(corpsePdcKey, PersistentDataType.BOOLEAN)) return;

		int xpDrop = pdc.getOrDefault(dropXpPdcKey, PersistentDataType.INTEGER, 0);
		List<ItemStack> itemsDrop = Arrays.stream(pdc.getOrDefault(dropItemsPdcKey, ItemArrayDataType.INSTANCE, new ItemStack[] {})).toList();

		event.getDrops().clear();
		event.getDrops().addAll(itemsDrop);
		event.setDroppedExp(xpDrop);

		mannequin.remove();// no death animation
	}

	@EventHandler
	void onClickOnCorpse(PlayerInteractEntityEvent event) {
		Entity entity = event.getRightClicked();
		if (!(entity instanceof Mannequin mannequin)) return;
		Player player = event.getPlayer();
		if (entity.getLocation().distance(Objects.requireNonNull(player.getLocation())) > 3) return;
		PersistentDataContainer pdc = entity.getPersistentDataContainer();
		if (!pdc.has(corpsePdcKey, PersistentDataType.BOOLEAN)) return;

		mannequin.kill();
	}

	@EventHandler
	public void onEntityLoad(EntityAddToWorldEvent event) {
		Entity entity = event.getEntity();
		if (!(entity instanceof Mannequin corpse)) return;

		PersistentDataContainer pdc = entity.getPersistentDataContainer();
		if (!pdc.has(corpsePdcKey, PersistentDataType.BOOLEAN)) return;

		CorpseFunctions.applyCorpseProperties(corpse);
	}
}
