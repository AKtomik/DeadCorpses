package io.github.aktomik.deadcorpses.spawn;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class CorpseListener implements Listener {

	@EventHandler
	void OnDeath(PlayerDeathEvent event) {
		event.getPlayer().sendMessage("you're ded");
	}
}
